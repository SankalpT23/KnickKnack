package com.sankalp.KnickKnack.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRatingStats {
    private Double averageBorrowerRating;
    private Integer totalBorrowerRatings;

    private Double averageOwnerRating;
    private Integer totalOwnerRatings;
}
