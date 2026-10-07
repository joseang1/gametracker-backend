package com.josedev.gametracker.user.dtos;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Public profile of a user.
 * statusCounts: number of logged games per status (COMPLETED, PLAYING, ...).
 * averageRating: 1-10 scale (half stars), null when the user has no reviews.
 */
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
