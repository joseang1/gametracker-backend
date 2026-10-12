package com.josedev.gametracker.gamelog;

import java.util.Optional;

import com.josedev.gametracker.gamelog.dtos.GameLogDTORequest;
import com.josedev.gametracker.gamelog.dtos.GameLogDTOResponse;
import com.josedev.gametracker.globals.dtos.PageDTOResponse;

public interface InterfaceGameLogService {

    PageDTOResponse<GameLogDTOResponse> getByUser(String username, LogStatus status, boolean favoritesOnly, int page);

    Optional<GameLogDTOResponse> getMine(String username, Long gameId);

    GameLogDTOResponse save(String username, Long gameId, GameLogDTORequest dto);

    void deleteMine(String username, Long gameId);

}
