package com.josedev.gametracker.gamelog.dtos;

import com.josedev.gametracker.review.dtos.ReviewDTOResponse;

public record MyGameEntryDTOResponse(GameLogDTOResponse log, ReviewDTOResponse review) {

}
