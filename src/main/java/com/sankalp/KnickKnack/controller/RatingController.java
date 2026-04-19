package com.sankalp.KnickKnack.controller;

import com.sankalp.KnickKnack.dto.request.SubmitRatingRequest;
import com.sankalp.KnickKnack.dto.response.RatingResponse;
import com.sankalp.KnickKnack.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/ratings")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @PostMapping
    public ResponseEntity<RatingResponse> submitRating(
            @Valid @RequestBody SubmitRatingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        log.info("User {} submitting rating for reservation ID: {}", email, request.getReservationId());
        RatingResponse response = ratingService.submitRating(request, email);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<RatingResponse> getRatingByReservation(
            @PathVariable String reservationId) {
        RatingResponse response = ratingService.getRatingByReservation(reservationId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
