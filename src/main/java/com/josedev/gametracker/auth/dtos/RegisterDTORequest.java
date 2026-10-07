package com.josedev.gametracker.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterDTORequest(
        @NotBlank(message = "Username cannot be empty")
        @Size(min = 3, max = 30, message = "Username must have between 3 and 30 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and _")
        String username,

        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Email is not valid")
        @Size(max = 120, message = "Email is too long")
        String email,

        // BCrypt only uses the first 72 bytes
        @NotBlank(message = "Password cannot be empty")
        @Size(min = 8, max = 72, message = "Password must have between 8 and 72 characters")
        String password) {

}
