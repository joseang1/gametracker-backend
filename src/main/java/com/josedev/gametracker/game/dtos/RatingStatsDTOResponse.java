package com.josedev.gametracker.game.dtos;

import java.util.List;

/**
 * Community rating stats of a game.
 * average: 1-10 scale (half stars), null without reviews.
 * distribution: 10 positions, index 0 = rating 1 (half star) ... index 9 = rating 10 (five stars).
 */
public record RatingStatsDTOResponse(long reviewCount, Double average, List<Long> distribution) {

}
