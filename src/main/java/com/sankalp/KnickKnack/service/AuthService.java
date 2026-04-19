package com.sankalp.KnickKnack.service;

import com.sankalp.KnickKnack.dto.request.LoginRequest;
import com.sankalp.KnickKnack.dto.request.RegisterRequest;
import com.sankalp.KnickKnack.dto.response.AuthResponse;
import com.sankalp.KnickKnack.exception.UnauthorizedException;
import com.sankalp.KnickKnack.model.User;
import com.sankalp.KnickKnack.exception.ValidationException;
import com.sankalp.KnickKnack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
// Manages "Entry" (Login/Register)
public class AuthService {
    // Controller --> Service --> Repository(Interface)

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email Already In Use");
        }

        if (userRepository.findByCampusId(request.getCampusId()).isPresent()) {
            throw new ValidationException("Campus ID Already In Use");
        }

        String hashedPassword = passwordEncoder.encode(request.getPasswordHash());

        User newuser = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .phone(request.getPhone())
                .campusId(request.getCampusId())
                .passwordHash(hashedPassword)
                .isActive(true)
                .build();

        userRepository.save(newuser);

        String accessToken = jwtService.generateAccessToken(newuser);
        String refreshToken = jwtService.generateRefreshToken(newuser);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(AuthResponse.UserInfo.builder()
                        .id(newuser.getId().toString())
                        .name(newuser.getName())
                        .email(newuser.getEmail())
                        .trustScore(newuser.getTrustScore())
                        .build())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for email: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        String rawPassword = request.getPasswordHash();
        String encodedPassword = user.getPasswordHash();
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        if (!matches) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(AuthResponse.UserInfo.builder()
                        .id(String.valueOf(user.getId()))
                        .name(user.getName())
                        .email(user.getEmail())
                        .trustScore(user.getTrustScore())
                        .build())
                .build();
    }
}
