package com.josedev.gametracker.auth.dtos;

import jakarta.validation.constraints.NotBlank;

public record LoginDTORequest(
        @NotBlank(message = "Username cannot be empty")
        String username,

        @NotBlank(message = "Password cannot be empty")
        String password) {

}
