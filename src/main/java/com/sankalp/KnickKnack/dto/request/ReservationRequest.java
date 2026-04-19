package com.sankalp.KnickKnack.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReservationRequest {
    @NotBlank(message = "Item ID is required to create a reservation")
    private String itemId;
}
