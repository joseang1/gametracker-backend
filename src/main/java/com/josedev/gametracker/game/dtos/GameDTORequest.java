package com.josedev.gametracker.game.dtos;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GameDTORequest(
        @NotBlank(message = "Title cannot be empty")
        @Size(max = 200, message = "Title cannot be longer than 200 characters")
        String title,

        @Size(max = 500, message = "Cover URL is too long")
        @Pattern(regexp = "^$|^https?://.+", message = "Cover must be a valid http(s) URL")
        String coverUrl,

        @Size(max = 10000, message = "Description is too long")
        String description,

        LocalDate releaseDate,

        List<String> genres,

        List<String> platforms,

        @Size(max = 200, message = "Developer name is too long")
        String developer) {

}
