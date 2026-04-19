package com.sankalp.KnickKnack.model;

import com.sankalp.KnickKnack.model.enums.ItemAvailabilty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "items")
public class Item {
    @Id
    private String id;

    // Relationships
    private String ownerId;        // ref: users
    private String reservationId;  // active reservation reference

    // Core details
    private String title;
    private String description;
    private String category;
    private String condition;

    private List<String> images;

    // Pickup logistics
    private String pickupLocation;
    private String pickupInstructions; // optional

    // Availability state
    private ItemAvailabilty availability;

    // Security
    @Indexed(unique = true)
    private String qrCodeHash;     // SHA-256 hash //Should be Generated in Service

    // Constraints
    private Integer maxReservationHours;
    private Integer maxCheckoutDays;

    // Meta
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Boolean isDeleted;
}
