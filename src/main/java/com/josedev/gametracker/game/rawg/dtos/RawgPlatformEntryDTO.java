package com.josedev.gametracker.game.rawg.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawgPlatformEntryDTO(RawgNamedDTO platform) {

}
