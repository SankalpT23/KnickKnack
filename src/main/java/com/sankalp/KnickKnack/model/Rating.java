package com.sankalp.KnickKnack.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "ratings")
@Data
@Builder
public class Rating {

    @Id
    private String id;

    @Indexed(unique = true)
    private String reservationId;

    private BorrowerRating borrowerRating;

    private OwnerRating ownerRating;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class BorrowerRating {
        private Integer score;
        private String comment;
        private String ratedBy;
        private LocalDateTime ratedAt;
    }

    @Data
    @Builder
    public static class OwnerRating {
        private Integer score;
        private String comment;
        private String ratedBy;
        private LocalDateTime ratedAt;
    }
}
