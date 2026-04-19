package com.sankalp.KnickKnack.dto.response;

import com.sankalp.KnickKnack.model.enums.ReservationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CheckoutResponse {
    private String reservationId;
    private String itemId;
    private String itemTitle;
    private ReservationStatus status;

    private LocalDateTime checkedOutAt;
    private LocalDateTime expectedReturnAt;
    private Integer maxCheckoutDays;

    private String returnQrCode;
    private String returnQrCodeBase64;
    private String message;
}
