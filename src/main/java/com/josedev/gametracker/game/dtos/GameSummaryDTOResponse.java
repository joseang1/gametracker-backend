package com.josedev.gametracker.game.dtos;

import java.time.LocalDate;
import java.util.List;

public record GameSummaryDTOResponse(
        Long id,
        Long rawgId,
        String title,
        String coverUrl,
        LocalDate releaseDate,
        List<String> genres,
        Double rating,
        String source) {

}
