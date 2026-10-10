package com.josedev.gametracker.user.dtos;

import java.time.LocalDateTime;
import java.util.Map;

public record ProfileDTOResponse(
        Long id,
        String username,
        String role,
        String bio,
        String avatarUrl,
        LocalDateTime joinedAt,
        Map<String, Long> statusCounts,
        long reviewCount,
        Double averageRating) {

}
