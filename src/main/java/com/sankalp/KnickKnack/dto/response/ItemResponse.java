package com.sankalp.KnickKnack.dto.response;

import com.sankalp.KnickKnack.model.enums.ItemAvailabilty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemResponse {
    private String id;
    private String title;
    private String description;
    private String category;
    private String condition;
    private List<String> imageUrls;
    private ItemAvailabilty availability;

    private OwnerSummary owner;

    @Data
    @Builder
    public static  class OwnerSummary{
        private String id;
        private String name;
        private Integer trustScore;
        private Integer totalTransactions;
    }
}
