package com.josedev.gametracker.game.rawg.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawgGameDTO(
        Long id,
        String name,
        String released,
        @JsonProperty("background_image") String backgroundImage,
        Double rating,
        @JsonProperty("description_raw") String descriptionRaw,
        List<RawgNamedDTO> genres,
        List<RawgPlatformEntryDTO> platforms,
        List<RawgNamedDTO> developers) {

}
