package com.josedev.gametracker.gamelog.dtos;

import java.time.LocalDateTime;

import com.josedev.gametracker.game.dtos.GameSummaryDTOResponse;

public record GameLogDTOResponse(
        Long id,
        String status,
        String platform,
        Integer hoursPlayed,
        boolean favorite,
        LocalDateTime updatedAt,
        GameSummaryDTOResponse game) {

}
