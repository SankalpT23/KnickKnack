package com.sankalp.KnickKnack.service;

import com.sankalp.KnickKnack.dto.request.SubmitRatingRequest;
import com.sankalp.KnickKnack.dto.response.RatingResponse;
import com.sankalp.KnickKnack.dto.response.UserRatingStats;
import com.sankalp.KnickKnack.exception.ResourceNotFoundException;
import com.sankalp.KnickKnack.exception.UnauthorizedException;
import com.sankalp.KnickKnack.exception.ValidationException;
import com.sankalp.KnickKnack.model.Rating;
import com.sankalp.KnickKnack.model.Reservation;
import com.sankalp.KnickKnack.model.User;
import com.sankalp.KnickKnack.model.enums.ReservationStatus;
import com.sankalp.KnickKnack.repository.RatingRepository;
import com.sankalp.KnickKnack.repository.ReservationRepository;
import com.sankalp.KnickKnack.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class RatingService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    public RatingResponse submitRating(SubmitRatingRequest request, String userEmail) {
        log.info("User {} submitting rating for reservation: {}", userEmail, request.getReservationId());
        // STEP 1: Find the reservation
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        // STEP 2: Verify reservation is RETURNED or RETURNED_LATE
        if (reservation.getStatus() != ReservationStatus.RETURNED
                && reservation.getStatus() != ReservationStatus.RETURNED_LATE) {
            throw new ValidationException(
                    "Can only rate completed transactions. Current status: " + reservation.getStatus());
        }

        // STEP 3: Find current user
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // STEP 4: Find or create rating document
        Rating rating = ratingRepository.findByReservationId(request.getReservationId())
                .orElseGet(() -> Rating.builder()
                        .reservationId(reservation.getId())
                        .createdAt(LocalDateTime.now())
                        .build());

        // STEP 5 & 7: Determine which rating to update and update trust score
        if ("BORROWER".equalsIgnoreCase(request.getRatingType())) {
            // Owner is rating the borrower
            if (!reservation.getOwnerId().equals(currentUser.getId().toString())) {
                throw new UnauthorizedException("Only the item owner can rate the borrower");
            }

            if (rating.getBorrowerRating() != null && rating.getBorrowerRating().getRatedBy() != null) {
                throw new ValidationException("You have already rated this borrower for this transaction");
            }

            rating.setBorrowerRating(Rating.BorrowerRating.builder()
                    .score(request.getScore())
                    .comment(request.getComment())
                    .ratedBy(currentUser.getId().toString())
                    .ratedAt(LocalDateTime.now())
                    .build());

            updateTrustScore(reservation.getBorrowerId(), request.getScore());

        } else if ("OWNER".equalsIgnoreCase(request.getRatingType())) {
            // Borrower is rating the owner
            if (!reservation.getBorrowerId().equals(currentUser.getId().toString())) {
                throw new UnauthorizedException("Only the borrower can rate the item owner");
            }

            if (rating.getOwnerRating() != null && rating.getOwnerRating().getRatedBy() != null) {
                throw new ValidationException("You have already rated this owner for this transaction");
            }

            rating.setOwnerRating(Rating.OwnerRating.builder()
                    .score(request.getScore())
                    .comment(request.getComment())
                    .ratedBy(currentUser.getId().toString())
                    .ratedAt(LocalDateTime.now())
                    .build());

            updateTrustScore(reservation.getOwnerId(), request.getScore());

        } else {
            throw new ValidationException("Invalid ratingType. Must be BORROWER or OWNER");
        }

        rating.setUpdatedAt(LocalDateTime.now());

        // STEP 6: Save rating
        Rating savedRating = ratingRepository.save(rating);

        // STEP 8: Return Response
        return mapToRatingResponse(savedRating);
    }

    private void updateTrustScore(String targetUserId, Integer score) {
        User user = userRepository.findById(new ObjectId(targetUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found for trust score update"));

        int trustScoreChange = 0;
        if (score >= 4) {
            trustScoreChange = 2;
        } else if (score <= 2) {
            trustScoreChange = -2;
        }

        if (trustScoreChange != 0) {
            user.setTrustScore(Math.max(0, Math.min(100, user.getTrustScore() + trustScoreChange)));
            userRepository.save(user);
        }
    }

    public RatingResponse getRatingByReservation(String reservationId) {
        log.info("Fetching rating for reservation: {}", reservationId);
        Rating rating = ratingRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found for this reservation"));
        return mapToRatingResponse(rating);
    }

    private RatingResponse mapToRatingResponse(Rating rating) {
        RatingResponse.RatingResponseBuilder builder = RatingResponse.builder()
                .id(rating.getId())
                .reservationId(rating.getReservationId());

        if (rating.getBorrowerRating() != null) {
            builder.borrowerRating(RatingResponse.BorrowerRatingInfo.builder()
                    .score(rating.getBorrowerRating().getScore())
                    .comment(rating.getBorrowerRating().getComment())
                    .ratedBy(rating.getBorrowerRating().getRatedBy())
                    .ratedAt(rating.getBorrowerRating().getRatedAt())
                    .build());
        }

        if (rating.getOwnerRating() != null) {
            builder.ownerRating(RatingResponse.OwnerRatingInfo.builder()
                    .score(rating.getOwnerRating().getScore())
                    .comment(rating.getOwnerRating().getComment())
                    .ratedBy(rating.getOwnerRating().getRatedBy())
                    .ratedAt(rating.getOwnerRating().getRatedAt())
                    .build());
        }

        return builder.build();
    }

    public UserRatingStats getUserRatingStats(String userId) {
        log.info("Calculating user rating stats for user ID: {}", userId);
        List<Reservation> userReservationsAsBorrower = reservationRepository
                .findByBorrowerIdOrderByReservedAtDesc(userId);
        List<String> borrowerReservationIds = userReservationsAsBorrower.stream().map(Reservation::getId).toList();

        List<Rating> ratingsAsBorrower = borrowerReservationIds.isEmpty()
                ? List.of()
                : ratingRepository.findByReservationIdIn(borrowerReservationIds);

        double borrowerSum = 0;
        int borrowerCount = 0;

        for (Rating rating : ratingsAsBorrower) {
            if (rating.getBorrowerRating() != null && rating.getBorrowerRating().getScore() != null) {
                borrowerSum += rating.getBorrowerRating().getScore();
                borrowerCount++;
            }
        }

        List<Reservation> userReservationsAsOwner = reservationRepository.findByOwnerIdOrderByReservedAtDesc(userId);
        List<String> ownerReservationIds = userReservationsAsOwner.stream().map(Reservation::getId).toList();

        List<Rating> ratingsAsOwner = ownerReservationIds.isEmpty()
                ? List.of()
                : ratingRepository.findByReservationIdIn(ownerReservationIds);
        double ownerSum = 0;
        int ownerCount = 0;

        for (Rating rating : ratingsAsOwner) {
            if (rating.getOwnerRating() != null && rating.getOwnerRating().getScore() != null) {
                ownerSum += rating.getOwnerRating().getScore();
                ownerCount++;
            }
        }

        return UserRatingStats.builder()
                .averageBorrowerRating(borrowerCount > 0 ? borrowerSum / borrowerCount : null)
                .totalBorrowerRatings(borrowerCount)
                .averageOwnerRating(ownerCount > 0 ? ownerSum / ownerCount : null)
                .totalOwnerRatings(ownerCount)
                .build();
    }
}
