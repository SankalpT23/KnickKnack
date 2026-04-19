package com.sankalp.KnickKnack.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckoutRequest {
    @NotBlank(message = "QR code is Required")
    private String qrCode;
}
