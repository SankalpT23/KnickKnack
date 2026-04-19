package com.sankalp.KnickKnack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String name;
    private String phone;
    private String campusId;

    private Integer trustScore;
    private Integer totalTransactions;
    private Integer onTimeReturns;

    private Boolean isActive;

    private Double averageRatingAsBorrower;
    private Integer totalRatingsAsBorrower;

    private Double averageRatingAsOwner;
    private Integer totalRatingsAsOwner;

}
