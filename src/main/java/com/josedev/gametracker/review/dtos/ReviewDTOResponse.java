package com.josedev.gametracker.review.dtos;

import java.time.LocalDateTime;

import com.josedev.gametracker.game.dtos.GameSummaryDTOResponse;
import com.josedev.gametracker.user.dtos.UserDTOResponse;

public record ReviewDTOResponse(
        Long id,
        int rating,
        String body,
        boolean spoiler,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UserDTOResponse author,
        GameSummaryDTOResponse game) {

}
