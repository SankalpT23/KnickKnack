package com.sankalp.KnickKnack.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RatingResponse {
    private String id;
    private String reservationId;

    private BorrowerRatingInfo borrowerRating;

    private OwnerRatingInfo ownerRating;

    @Data
    @Builder
    public static class BorrowerRatingInfo {
        private Integer score;
        private String comment;
        private String ratedBy;
        private LocalDateTime ratedAt;
    }

    @Data
    @Builder
    public static class OwnerRatingInfo {
        private Integer score;
        private String comment;
        private String ratedBy;
        private LocalDateTime ratedAt;
    }
}
