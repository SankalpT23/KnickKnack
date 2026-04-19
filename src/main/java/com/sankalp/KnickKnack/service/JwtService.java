package com.sankalp.KnickKnack.service;

import com.sankalp.KnickKnack.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET_KEY;
    private static final long ACCESS_TOKEN_EXPIRY = 1000 * 60 * 60; // 1 hour
    private static final long REFRESH_TOKEN_EXPIRY = 1000 * 60 * 60 * 24 * 7; // 7 DAYS

    public String generateAccessToken(User user) {
        log.debug("Generating access token for user: {}", user.getEmail());
        return buildToken(new HashMap<>(), user, ACCESS_TOKEN_EXPIRY);
    }

    public String generateRefreshToken(User user) {
        log.debug("Generating refresh token for user: {}", user.getEmail());
        return buildToken(new HashMap<>(), user, REFRESH_TOKEN_EXPIRY);
    }

    private String buildToken(Map<String, Object> extraClaims, User user, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getEmail())// Who is this token for
                .setIssuedAt(new Date(System.currentTimeMillis()))// When Is it created
                .setExpiration(new Date(System.currentTimeMillis() + expiration))// When will it expire
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)// Sign with Secret Key
                .compact();// Build The String
    }
    /* ================= TOKEN EXTRACTION ================= */

    // Extract email (subject) from JWT
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Generic claim extractor
    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /* ================= TOKEN VALIDATION ================= */

    public boolean validateToken(
            String token,
            UserDetails userDetails) {
        final String username = extractUsername(token);
        boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        if (!isValid) {
            log.debug("Token validation failed for user: {}", userDetails.getUsername());
        }
        return isValid;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
}
