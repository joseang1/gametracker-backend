package com.josedev.gametracker.game.rawg.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** RAWG wraps each platform: "platforms": [ { "platform": { "name": "PC" } } ] */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RawgPlatformEntryDTO(RawgNamedDTO platform) {

}
