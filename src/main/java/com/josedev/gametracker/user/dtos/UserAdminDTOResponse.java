package com.josedev.gametracker.user.dtos;

import java.time.LocalDateTime;

public record UserAdminDTOResponse(
        Long id,
        String username,
        String email,
        String role,
        boolean banned,
        LocalDateTime createdAt) {

}
