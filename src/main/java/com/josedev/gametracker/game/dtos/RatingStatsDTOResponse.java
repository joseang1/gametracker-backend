package com.josedev.gametracker.game.dtos;

import java.util.List;

public record RatingStatsDTOResponse(long reviewCount, Double average, List<Long> distribution) {

}
