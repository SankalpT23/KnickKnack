package com.sankalp.KnickKnack.dto.request;

import com.sankalp.KnickKnack.model.enums.ItemAvailabilty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotNull
    private String category;
    @NotNull
    private String condition;

    private List<String> images;
    @NotBlank
    private String pickupLocation;
    private String pickupInstructions;

    private Integer maxReservationHours;
    private Integer maxCheckoutDays;


    private ItemAvailabilty availability;
    private Boolean isDeleted;
    @Indexed(unique = true)
    private String qrCodeHash;
}
