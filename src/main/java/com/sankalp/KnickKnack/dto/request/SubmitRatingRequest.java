package com.sankalp.KnickKnack.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SubmitRatingRequest {

    @NotBlank(message = "Reservation ID is required")
    private String reservationId;

    @NotNull(message = "Score is required")
    @Min(value = 1, message = "Score must be between 1 and 5")
    @Max(value = 5, message = "Score must be between 1 and 5")
    private Integer score;

    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String comment;

    @NotBlank(message = "Rating type is required")
    @Pattern(regexp = "BORROWER|OWNER", message = "Rating type must be BORROWER or OWNER")
    private String ratingType;
}
