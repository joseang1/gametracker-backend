package com.josedev.gametracker.game.dtos;

import java.time.LocalDate;
import java.util.List;

public record GameDetailDTOResponse(
        Long id,
        Long rawgId,
        String title,
        String coverUrl,
        String description,
        LocalDate releaseDate,
        List<String> genres,
        List<String> platforms,
        String developer,
        String source,
        RatingStatsDTOResponse stats) {

}
