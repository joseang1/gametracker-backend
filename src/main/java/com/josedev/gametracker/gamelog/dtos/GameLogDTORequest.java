package com.josedev.gametracker.gamelog.dtos;

import com.josedev.gametracker.gamelog.LogStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GameLogDTORequest(
        @NotNull(message = "Status is required")
        LogStatus status,

        @Size(max = 50, message = "Platform name is too long")
        String platform,

        @Min(value = 0, message = "Hours played cannot be negative")
        @Max(value = 100000, message = "Hours played is too high")
        Integer hoursPlayed,

        Boolean favorite) {

}
