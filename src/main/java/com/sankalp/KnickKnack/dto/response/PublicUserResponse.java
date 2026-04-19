package com.sankalp.KnickKnack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicUserResponse {
    private  String name;
    private Integer trustScore;
    private  Integer totalTransactions;
}
