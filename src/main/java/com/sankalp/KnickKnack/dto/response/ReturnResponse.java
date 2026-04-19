package com.sankalp.KnickKnack.dto.response;

import com.sankalp.KnickKnack.model.enums.ReservationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReturnResponse {
    private String reservationId;
    private String itemId;
    private String itemName;
    private ReservationStatus status;

    private LocalDateTime checkedOutAt;
    private LocalDateTime expectedReturnedAt;
    private LocalDateTime returnedAt;

    private Boolean isLate;
    private Integer lateDays;
    private Integer trustScoreChange;

    private String message;
}
