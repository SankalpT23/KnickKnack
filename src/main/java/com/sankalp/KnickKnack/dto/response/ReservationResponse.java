package com.sankalp.KnickKnack.dto.response;

import com.sankalp.KnickKnack.model.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private String id;
    private String itemId;
    private String borrowerId;
    private String ownerId;
    private ReservationStatus status;
    private LocalDateTime reservedAt;
    private LocalDateTime expiresAt;
    private Integer maxReservationHours;
    private Long hoursUntilExpiry;
    private String checkoutQrNonce;
    private String checkoutQrCodeBase64;
    private String returnQrNonce;
    private String returnQrCodeBase64;

    @Data
    @Builder
    public static class ItemSummary {
        private String id;
        private String title;
        private String pickupLocation;
    }

    private ItemSummary item; // Include item details in response

}
