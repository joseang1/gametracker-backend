package com.josedev.gametracker.gamelog.mappers;

import com.josedev.gametracker.game.mappers.GameMapper;
import com.josedev.gametracker.gamelog.GameLogEntity;
import com.josedev.gametracker.gamelog.dtos.GameLogDTORequest;
import com.josedev.gametracker.gamelog.dtos.GameLogDTOResponse;

public class GameLogMapper {

    private GameLogMapper() {
    }

    public static void updateEntity(GameLogEntity entity, GameLogDTORequest dto) {
        entity.setStatus(dto.status());
        entity.setPlatform(dto.platform() == null || dto.platform().isBlank() ? null : dto.platform().trim());
        entity.setHoursPlayed(dto.hoursPlayed());
        entity.setFavorite(dto.favorite());
    }

    public static GameLogDTOResponse toDTO(GameLogEntity entity) {
        return new GameLogDTOResponse(
                entity.getId(),
                entity.getStatus().name(),
                entity.getPlatform(),
                entity.getHoursPlayed(),
                entity.isFavorite(),
                entity.getUpdatedAt(),
                GameMapper.toSummaryDTO(entity.getGame(), null));
    }

}
