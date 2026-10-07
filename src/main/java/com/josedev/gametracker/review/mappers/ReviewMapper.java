package com.josedev.gametracker.review.mappers;

import com.josedev.gametracker.game.mappers.GameMapper;
import com.josedev.gametracker.review.ReviewEntity;
import com.josedev.gametracker.review.dtos.ReviewDTORequest;
import com.josedev.gametracker.review.dtos.ReviewDTOResponse;
import com.josedev.gametracker.user.mappers.UserMapper;

public class ReviewMapper {

    private ReviewMapper() {
    }

    public static void updateEntity(ReviewEntity entity, ReviewDTORequest dto) {
        entity.setRating(dto.rating());
        entity.setBody(dto.body() == null || dto.body().isBlank() ? null : dto.body().trim());
        entity.setSpoiler(Boolean.TRUE.equals(dto.spoiler()));
    }

    public static ReviewDTOResponse toDTO(ReviewEntity entity) {
        return new ReviewDTOResponse(
                entity.getId(),
                entity.getRating(),
                entity.getBody(),
                entity.isSpoiler(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                UserMapper.toDTO(entity.getUser()),
                GameMapper.toSummaryDTO(entity.getGame(), null));
    }

}
