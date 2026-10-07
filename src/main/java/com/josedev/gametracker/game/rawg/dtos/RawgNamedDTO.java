package com.josedev.gametracker.game.rawg.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Any RAWG object where we only need the name (genre, developer, platform). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RawgNamedDTO(String name) {

}
