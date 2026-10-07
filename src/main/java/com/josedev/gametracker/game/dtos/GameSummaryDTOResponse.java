package com.josedev.gametracker.game.dtos;

import java.time.LocalDate;
import java.util.List;

/**
 * Game card data used in lists.
 * id: our database id, null when a RAWG game has not been imported yet (use rawgId then).
 * rating: 1-10 scale (half stars), may be null.
 */
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
