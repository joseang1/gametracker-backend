package com.josedev.gametracker.gamelog.dtos;

import com.josedev.gametracker.review.dtos.ReviewDTOResponse;

/** What the logged user has saved for one game. Both fields are null when there is nothing yet. */
public record MyGameEntryDTOResponse(GameLogDTOResponse log, ReviewDTOResponse review) {

}
