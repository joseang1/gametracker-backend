package com.josedev.gametracker.game.rawg.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawgPageDTO(long count, List<RawgGameDTO> results) {

    public static RawgPageDTO empty() {
        return new RawgPageDTO(0, List.of());
    }

}
