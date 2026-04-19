package com.sankalp.KnickKnack.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user")
public class User {

    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String email;
    private String name;
    private String phone;
    @Indexed(unique = true)
    private String campusId;

    // Trust & usage metrics
    @Builder.Default
    private Integer trustScore = 50; // 0–100
    private Integer totalTransactions;
    private Integer onTimeReturns;

    // Abuse prevention
    @Builder.Default
    private Integer expiredReservationsCount24h = 0;
    private LocalDateTime lastExpiredReservationAt;
    private LocalDateTime cooldownUntil;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Boolean isActive;

    // Auth
    private String passwordHash;    //Always Hash Password
    private String refreshToken;
}
