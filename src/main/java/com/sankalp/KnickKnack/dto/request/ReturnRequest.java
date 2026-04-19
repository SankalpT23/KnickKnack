package com.sankalp.KnickKnack.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReturnRequest {
    @NotBlank(message = "QR code is required")
    private String qrCode;
}
