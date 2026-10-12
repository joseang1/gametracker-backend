package com.josedev.gametracker.user.dtos;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileDTORequest(
        @Size(max = 300, message = "Bio cannot be longer than 300 characters")
        String bio,

        @Size(max = 500, message = "Avatar URL is too long")
        @Pattern(regexp = "^$|^https?://.+", message = "Avatar must be a valid http(s) URL")
        String avatarUrl) {

}
