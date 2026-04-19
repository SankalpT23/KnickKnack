package com.sankalp.KnickKnack.dto.request;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemRequest {
    private String title;
    private String description;
    private String condition;
    private String pickupLocation;
    private String pickupInstructions;
    private List<String> images;
}
