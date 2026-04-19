package com.sankalp.KnickKnack.service;

import com.sankalp.KnickKnack.dto.response.CheckoutResponse;
import com.sankalp.KnickKnack.dto.response.ReservationResponse;
import com.sankalp.KnickKnack.dto.response.ReturnResponse;
import com.sankalp.KnickKnack.exception.ResourceNotFoundException;
import com.sankalp.KnickKnack.exception.UnauthorizedException;
import com.sankalp.KnickKnack.exception.ValidationException;
import com.sankalp.KnickKnack.model.Item;
import com.sankalp.KnickKnack.model.Rating;
import com.sankalp.KnickKnack.model.Reservation;
import com.sankalp.KnickKnack.model.User;
import com.sankalp.KnickKnack.model.enums.ItemAvailabilty;
import com.sankalp.KnickKnack.model.enums.ReservationStatus;
import com.sankalp.KnickKnack.repository.ItemRepository;
import com.sankalp.KnickKnack.repository.RatingRepository;
import com.sankalp.KnickKnack.repository.ReservationRepository;
import com.sankalp.KnickKnack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ReservationService {

        @Autowired
        private ItemRepository itemRepository;

        @Autowired
        private ReservationRepository reservationRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private MongoTemplate mongoTemplate;

        @Autowired
        private RatingRepository ratingRepository;

        @Autowired
        private QRService qrService;

        public ReservationResponse createReservation(String itemId, String borrowerEmail) {
                log.info("Creating reservation for item: {} by user: {}", itemId, borrowerEmail);
                User user = userRepository.findByEmail(borrowerEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                log.debug("Found borrower {}", user.getEmail());
                Item item = itemRepository.findById(itemId)
                                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
                log.debug("Found Item {}", item.getId());
                if (item.getOwnerId().equals(user.getId().toString())) {
                        throw new ValidationException("Cannot Borrow your own item");
                }
                if (!item.getAvailability().equals("AVAILABLE")) {
                        throw new ValidationException("Item is Not Available");
                }
                log.debug("Validation Done for reservation");

                LocalDateTime now = LocalDateTime.now();

                Reservation reservation = new Reservation();
                reservation.setItemId(item.getId());
                reservation.setBorrowerId(user.getId().toString());
                reservation.setOwnerId(item.getOwnerId());
                reservation.setStatus(ReservationStatus.PENDING);
                reservation.setReservedAt(now);
                reservation.setMaxReservationHours(item.getMaxReservationHours());
                reservation.setExpiresAt(now.plusHours(item.getMaxReservationHours()));
                reservation.setCheckoutQrNonce(UUID.randomUUID().toString());
                reservation.setMaxCheckoutDays(item.getMaxCheckoutDays());
                Reservation save = reservationRepository.save(reservation);
                log.debug("Reservation Completed");

                item.setAvailability(ItemAvailabilty.RESERVED);
                item.setReservationId(save.getId());
                itemRepository.save(item);
                log.debug("Successfully Updated Item availability");

                return ReservationResponse.builder()
                                .id(save.getId())
                                .itemId(save.getItemId())
                                .borrowerId(save.getBorrowerId())
                                .ownerId(save.getOwnerId())
                                .status(save.getStatus())
                                .reservedAt(save.getReservedAt())
                                .maxReservationHours(save.getMaxReservationHours())
                                .expiresAt(save.getExpiresAt())
                                .hoursUntilExpiry(calculateHoursUntilExpiry(save.getExpiresAt()))
                                .checkoutQrNonce(save.getCheckoutQrNonce())
                                .checkoutQrCodeBase64(qrService.generateQRCodeBase64(save.getCheckoutQrNonce()))
                                .item(ReservationResponse.ItemSummary.builder()
                                                .id(item.getId())
                                                .title(item.getTitle())
                                                .pickupLocation(item.getPickupLocation())
                                                .build())
                                .build();

        }

        private Long calculateHoursUntilExpiry(LocalDateTime expiryAt) {
                LocalDateTime now = LocalDateTime.now();

                if (now.isAfter(expiryAt)) {
                        return 0L;
                }
                return Duration.between(now, expiryAt).toHours();
        }

        public void cancelReservation(String reservationId, String userEmail) {
                log.info("Canceling reservation: {} by user: {}", reservationId, userEmail);
                Reservation reservation = reservationRepository.findById(reservationId)
                                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (!reservation.getBorrowerId().equals(user.getId().toString())) {
                        throw new UnauthorizedException("Only Borrower can cancel this Reservation");
                }

                if (reservation.getStatus() != ReservationStatus.PENDING) {
                        throw new ValidationException("Can Only Cancel this Reservation");
                }

                reservation.setStatus(ReservationStatus.CANCELLED);
                reservationRepository.save(reservation);

                Item item = itemRepository.findById(reservation.getItemId())
                                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
                item.setAvailability(ItemAvailabilty.AVAILABLE);
                item.setReservationId(null);
                itemRepository.save(item);

        }

        public List<ReservationResponse> getMyBorrows(String userEmail) {
                log.info("Fetching borrows for user: {}", userEmail);
                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                List<Reservation> reservations = reservationRepository
                                .findByBorrowerIdOrderByReservedAtDesc(user.getId().toString());
                return reservations.stream().map(reservation -> {
                        Item item = itemRepository.findById(reservation.getItemId()).orElse(null);
                        return ReservationResponse.builder()
                                        .id(reservation.getId())
                                        .itemId(reservation.getItemId())
                                        .borrowerId(reservation.getBorrowerId())
                                        .ownerId(reservation.getOwnerId())
                                        .status(reservation.getStatus())
                                        .reservedAt(reservation.getReservedAt())
                                        .expiresAt(reservation.getExpiresAt())
                                        .maxReservationHours(reservation.getMaxReservationHours())
                                        .hoursUntilExpiry(calculateHoursUntilExpiry(reservation.getExpiresAt()))
                                        .checkoutQrNonce(reservation.getCheckoutQrNonce())
                                        .checkoutQrCodeBase64(qrService
                                                        .generateQRCodeBase64(reservation.getCheckoutQrNonce()))
                                        .returnQrNonce(reservation.getReturnQrNonce())
                                        .returnQrCodeBase64(reservation.getReturnQrNonce() != null
                                                        ? qrService.generateQRCodeBase64(reservation.getReturnQrNonce())
                                                        : null)
                                        .item(item != null ? ReservationResponse.ItemSummary.builder()
                                                        .id(item.getId())
                                                        .title(item.getTitle())
                                                        .pickupLocation(item.getPickupLocation())
                                                        .build() : null)
                                        .build();
                }).toList();
        }

        public List<ReservationResponse> getMyLends(String userEmail) {
                log.info("Fetching lends for user: {}", userEmail);
                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                List<Reservation> reservations = reservationRepository
                                .findByOwnerIdOrderByReservedAtDesc(user.getId().toString());
                return reservations.stream().map(reservation -> {
                        Item item = itemRepository.findById(reservation.getItemId()).orElse(null);
                        return ReservationResponse.builder()
                                        .id(reservation.getId())
                                        .itemId(reservation.getItemId())
                                        .borrowerId(reservation.getBorrowerId())
                                        .ownerId(reservation.getOwnerId())
                                        .status(reservation.getStatus())
                                        .reservedAt(reservation.getReservedAt())
                                        .expiresAt(reservation.getExpiresAt())
                                        .maxReservationHours(reservation.getMaxReservationHours())
                                        .hoursUntilExpiry(calculateHoursUntilExpiry(reservation.getExpiresAt()))
                                        .checkoutQrNonce(reservation.getCheckoutQrNonce())
                                        .checkoutQrCodeBase64(qrService
                                                        .generateQRCodeBase64(reservation.getCheckoutQrNonce()))
                                        .returnQrNonce(reservation.getReturnQrNonce())
                                        .returnQrCodeBase64(reservation.getReturnQrNonce() != null
                                                        ? qrService.generateQRCodeBase64(reservation.getReturnQrNonce())
                                                        : null)
                                        .item(item != null ? ReservationResponse.ItemSummary.builder()
                                                        .id(item.getId())
                                                        .title(item.getTitle())
                                                        .pickupLocation(item.getPickupLocation())
                                                        .build() : null)
                                        .build();
                }).toList();
        }

        public CheckoutResponse processCheckout(String qrCode, String ownerEmail) {
                log.info("Processing checkout with QR code scanned by: {}", ownerEmail);
                Reservation reservation = reservationRepository.findByCheckoutQrNonce(qrCode)
                                .orElseThrow(() -> new ValidationException("Invalid QR Code"));
                User user = userRepository.findByEmail(ownerEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (!reservation.getOwnerId().equals(user.getId().toString())) {
                        throw new UnauthorizedException("Only the item owner can scan this QR code");
                }

                if (reservation.getStatus() != ReservationStatus.PENDING) {
                        throw new ValidationException(
                                        "Reservation is not PENDING. Current status is " + reservation.getStatus());
                }

                if (reservation.getCheckoutQrScannedAt() != null) {
                        throw new ValidationException("Reservation is already scanned for this QR code");
                }

                if (reservation.getExpiresAt().isBefore(LocalDateTime.now())) {
                        throw new ValidationException("This reservation has expired");
                }

                LocalDateTime now = LocalDateTime.now();
                reservation.setStatus(ReservationStatus.CHECKED_OUT);
                reservation.setCheckedOutAt(now);
                reservation.setExpectedReturnAt(now.plusDays(reservation.getMaxCheckoutDays()));
                reservation.setCheckoutQrScannedAt(now);
                reservation.setReturnQrNonce(UUID.randomUUID().toString());
                Reservation savedReservation = reservationRepository.save(reservation);

                Item item = itemRepository.findById(savedReservation.getItemId())
                                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
                return CheckoutResponse.builder()
                                .reservationId(savedReservation.getId())
                                .itemId(savedReservation.getItemId())
                                .itemTitle(item.getTitle())
                                .status(savedReservation.getStatus())
                                .checkedOutAt(savedReservation.getCheckedOutAt())
                                .expectedReturnAt(savedReservation.getExpectedReturnAt())
                                .maxCheckoutDays(savedReservation.getMaxCheckoutDays())
                                .returnQrCode(savedReservation.getReturnQrNonce())
                                .returnQrCodeBase64(qrService.generateQRCodeBase64(savedReservation.getReturnQrNonce()))
                                .message("Item checked out successfully")
                                .build();
        }

        public ReturnResponse processReturn(String qrCode, String ownerEmail) {
                log.info("Processing return with QR code scanned by: {}", ownerEmail);
                Reservation reservation = reservationRepository.findByReturnQrNonce(qrCode)
                                .orElseThrow(() -> new ValidationException("Invalid QR Code"));

                User owner = userRepository.findByEmail(ownerEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (!reservation.getOwnerId().equals(owner.getId().toString())) {
                        throw new UnauthorizedException("Only the item owner can scan this QR code");
                }

                if (reservation.getStatus() != ReservationStatus.CHECKED_OUT) {
                        throw new ValidationException(
                                        "Item is not checked out. Current status: " + reservation.getStatus());
                }

                if (reservation.getReturnQrScannedAt() != null) {
                        throw new ValidationException("This QR has already been scanned for return");
                }

                LocalDateTime now = LocalDateTime.now();
                boolean isLate = now.isAfter(reservation.getExpectedReturnAt());
                int lateDays = 0;

                if (isLate) {
                        lateDays = (int) ChronoUnit.DAYS.between(reservation.getExpectedReturnAt(), now);
                        if (lateDays == 0)
                                lateDays = 1;
                }

                reservation.setReturnedAt(now);
                reservation.setReturnQrScannedAt(now);
                reservation.setIsLate(isLate);
                reservation.setLateDays(lateDays);
                reservation.setStatus(isLate ? ReservationStatus.RETURNED_LATE : ReservationStatus.RETURNED);
                reservationRepository.save(reservation);

                Item item = itemRepository.findById(reservation.getItemId())
                                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
                item.setAvailability(ItemAvailabilty.AVAILABLE);
                item.setReservationId(null);
                itemRepository.save(item);

                User borrower = userRepository.findById(new org.bson.types.ObjectId(reservation.getBorrowerId()))
                                .orElseThrow(() -> new ResourceNotFoundException("Borrower not found"));

                int trustScoreChange = 0;
                if (!isLate) {
                        trustScoreChange = 5;
                        borrower.setTrustScore(Math.min(100, borrower.getTrustScore() + trustScoreChange));
                        borrower.setOnTimeReturns(borrower.getOnTimeReturns() == null ? 1
                                        : borrower.getOnTimeReturns() + 1);
                } else {
                        int penalty = Math.min(lateDays * 2, 30);
                        trustScoreChange = -penalty;
                        borrower.setTrustScore(Math.max(0, borrower.getTrustScore() - penalty));
                }

                borrower.setTotalTransactions(borrower.getTotalTransactions() == null ? 1
                                : borrower.getTotalTransactions() + 1);
                userRepository.save(borrower);

                try {
                        Rating rating = Rating.builder()
                                        .reservationId(reservation.getId())
                                        .createdAt(LocalDateTime.now())
                                        .build();
                        ratingRepository.save(rating);
                        log.info("Rating document created for reservation {}", reservation.getId());
                } catch (Exception e) {
                        log.warn("Rating document already exists for reservation {}", reservation.getId());
                }
                return ReturnResponse.builder()
                                .reservationId(reservation.getId())
                                .itemId(reservation.getItemId())
                                .itemName(item.getTitle())
                                .status(reservation.getStatus())
                                .checkedOutAt(reservation.getCheckedOutAt())
                                .expectedReturnedAt(reservation.getExpectedReturnAt())
                                .returnedAt(reservation.getReturnedAt())
                                .isLate(reservation.getIsLate())
                                .lateDays(reservation.getLateDays())
                                .trustScoreChange(trustScoreChange)
                                .message(isLate ? "Item returned late. Trust score impacted."
                                                : "Item returned successfully. Trust score increased!")
                                .build();
        }
}
