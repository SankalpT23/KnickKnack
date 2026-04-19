package com.sankalp.KnickKnack.controller;

import com.sankalp.KnickKnack.dto.request.CheckoutRequest;
import com.sankalp.KnickKnack.dto.request.ReservationRequest;
import com.sankalp.KnickKnack.dto.request.ReturnRequest;
import com.sankalp.KnickKnack.dto.response.CheckoutResponse;
import com.sankalp.KnickKnack.dto.response.ReservationResponse;
import com.sankalp.KnickKnack.dto.response.ReturnResponse;
import com.sankalp.KnickKnack.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/reservations")
public class ReservationController {
    @Autowired
    private ReservationService reservationService;

    @PostMapping("/reserve-item")
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String borrowerEmail = userDetails.getUsername();
        log.info("User {} is requesting to reserve item ID: {}", borrowerEmail, request.getItemId());
        ReservationResponse response = reservationService.createReservation(request.getItemId().toString(),
                borrowerEmail);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}/cancel-reservation")
    public ResponseEntity<String> cancelReservation(@PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        reservationService.cancelReservation(id, userEmail);
        return new ResponseEntity<>("Reservation cancelled", HttpStatus.OK);
    }

    @GetMapping("/my-borrows")
    public ResponseEntity<List<ReservationResponse>> getMyBorrows(@AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        List<ReservationResponse> list = reservationService.getMyBorrows(userEmail);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/my-lends")
    public ResponseEntity<List<ReservationResponse>> getMyLends(@AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        List<ReservationResponse> list = reservationService.getMyLends(userEmail);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> processCheckout(@Valid @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("Processing checkout via QR code scanned by User {}", username);
        CheckoutResponse response = reservationService.processCheckout(request.getQrCode(), username);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/return")
    public ResponseEntity<ReturnResponse> processReturn(@Valid @RequestBody ReturnRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("Processing item return via QR code scanned by User {}", username);
        ReturnResponse response = reservationService.processReturn(request.getQrCode(), username);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
