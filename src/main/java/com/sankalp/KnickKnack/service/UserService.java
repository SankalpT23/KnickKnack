package com.sankalp.KnickKnack.service;

import com.sankalp.KnickKnack.dto.request.UpdateUserRequest;
import com.sankalp.KnickKnack.dto.response.UserRatingStats;
import com.sankalp.KnickKnack.dto.response.UserResponse;
import com.sankalp.KnickKnack.exception.ResourceNotFoundException;
import com.sankalp.KnickKnack.model.User;
import com.sankalp.KnickKnack.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RatingService ratingService;

    public UserResponse getProfile(ObjectId userId) {
        log.info("Fetching user details for ID: {}", userId);
        User newUser = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User Not Found"));

        UserRatingStats stats = ratingService.getUserRatingStats(newUser.getId().toString());

        return UserResponse.builder()
                .id(newUser.getId().toString())
                .name(newUser.getName())
                .phone(newUser.getPhone())
                .campusId(newUser.getCampusId())
                .trustScore(newUser.getTrustScore())
                .totalTransactions(newUser.getTotalTransactions())
                .onTimeReturns(newUser.getOnTimeReturns())
                .isActive(newUser.getIsActive())
                .averageRatingAsBorrower(stats.getAverageBorrowerRating())
                .totalRatingsAsBorrower(stats.getTotalBorrowerRatings())
                .averageRatingAsOwner(stats.getAverageOwnerRating())
                .totalRatingsAsOwner(stats.getTotalOwnerRatings())
                .build();
    }

    public UserResponse updateProfile(ObjectId userId, UpdateUserRequest request) {
        log.info("Updating user details for ID: {}", userId);
        User existingUser = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User Not Found"));

        if (request.getName() != null) {
            existingUser.setName(request.getName());
        }
        if (request.getPhone() != null) {
            existingUser.setPhone(request.getPhone());
        }

        User savedUser = userRepository.save(existingUser);

        return UserResponse.builder()
                .id(String.valueOf(existingUser.getId()))
                .name(savedUser.getName())
                .phone(savedUser.getPhone())
                .campusId(savedUser.getCampusId())
                .trustScore(savedUser.getTrustScore())
                .totalTransactions(savedUser.getTotalTransactions())
                .onTimeReturns(savedUser.getOnTimeReturns())
                .isActive(savedUser.getIsActive())
                .build();

    }
}
