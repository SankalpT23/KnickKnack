package com.sankalp.KnickKnack.controller;

import com.sankalp.KnickKnack.dto.request.UpdateUserRequest;
import com.sankalp.KnickKnack.dto.response.PublicUserResponse;
import com.sankalp.KnickKnack.dto.response.UserResponse;
import com.sankalp.KnickKnack.exception.ResourceNotFoundException;
import com.sankalp.KnickKnack.model.User;
import com.sankalp.KnickKnack.repository.UserRepository;
import com.sankalp.KnickKnack.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/users")
// Allows User to View and Manage Campus Profiles
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private UserRepository repository;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        log.info("Fetching profile for user: {}", email);
        User user1 = repository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserResponse profile = service.getProfile(user1.getId());

        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(@AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserRequest request) {
        String email = userDetails.getUsername();
        log.info("Updating profile for user: {}", email);
        User user = repository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserResponse updatedProfile = service.updateProfile(user.getId(), request);
        return new ResponseEntity<>(updatedProfile, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicUserResponse> publicView(@PathVariable String id) {
        log.info("Fetching public profile for user ID: {}", id);
        User user = repository.findById(new ObjectId(id))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        PublicUserResponse build = PublicUserResponse.builder()
                .name(user.getName())
                .trustScore(user.getTrustScore())
                .totalTransactions(user.getTotalTransactions())
                .build();

        return new ResponseEntity<>(build, HttpStatus.OK);

    }
}
