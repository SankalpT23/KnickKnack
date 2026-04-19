package com.sankalp.KnickKnack.model;

import com.sankalp.KnickKnack.model.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Document(collection = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    @Id
    private String id;

    @Indexed
    private String itemId;

    @Indexed
    private String borrowerId;

    private String ownerId;

    private ReservationStatus status;

    private LocalDateTime reservedAt;

    private LocalDateTime expiresAt;

    private Integer maxReservationHours;

    private Integer maxCheckoutDays;

    @Indexed
    private String checkoutQrNonce;

    private LocalDateTime checkoutQrScannedAt;

    @Indexed
    private String returnQrNonce;

    private LocalDateTime returnQrScannedAt;
    private LocalDateTime checkedOutAt;
    private LocalDateTime expectedReturnAt;

    private  LocalDateTime returnedAt;
    private Boolean isLate = false;
    private Integer lateDays = 0;
}
