package com.josedev.gametracker.review;

import java.util.Optional;

import com.josedev.gametracker.globals.dtos.PageDTOResponse;
import com.josedev.gametracker.review.dtos.ReviewDTORequest;
import com.josedev.gametracker.review.dtos.ReviewDTOResponse;

public interface InterfaceReviewService {

    PageDTOResponse<ReviewDTOResponse> getByGame(Long gameId, int page);

    PageDTOResponse<ReviewDTOResponse> getByUser(String username, int page);

    PageDTOResponse<ReviewDTOResponse> getRecent(int page);

    Optional<ReviewDTOResponse> getMine(String username, Long gameId);

    /** Creates the review of the user for that game, or updates it if it already exists. */
    ReviewDTOResponse save(String username, Long gameId, ReviewDTORequest dto);

    void deleteMine(String username, Long gameId);

    /** Moderation: an admin removes any review. */
    void deleteById(Long reviewId);

}
