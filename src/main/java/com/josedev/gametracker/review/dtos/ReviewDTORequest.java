package com.josedev.gametracker.review.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** rating in half stars: 1 = half a star ... 10 = five stars. body is optional (rating only). */
public record ReviewDTORequest(
        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be between 1 and 10")
        @Max(value = 10, message = "Rating must be between 1 and 10")
        Integer rating,

        @Size(max = 5000, message = "Review cannot be longer than 5000 characters")
        String body,

        boolean spoiler) {

}
