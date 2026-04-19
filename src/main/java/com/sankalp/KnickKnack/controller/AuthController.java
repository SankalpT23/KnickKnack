package com.sankalp.KnickKnack.controller;

import com.sankalp.KnickKnack.dto.request.LoginRequest;
import com.sankalp.KnickKnack.dto.request.RegisterRequest;
import com.sankalp.KnickKnack.dto.response.AuthResponse;
import com.sankalp.KnickKnack.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
// Manages Registration and login
public class AuthController {

    @Autowired
    private AuthService service;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        log.info("Attempting to register new user with email: {}", request.getEmail());
        AuthResponse register = service.register(request);
        log.info("Successfully registered user with email: {}", request.getEmail());
        return new ResponseEntity<>(register, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        log.info("User {} is attempting to login", request.getEmail());
        AuthResponse login = service.login(request);
        log.info("User {} logged in successfully", request.getEmail());
        return new ResponseEntity<>(login, HttpStatus.OK);
    }
}
