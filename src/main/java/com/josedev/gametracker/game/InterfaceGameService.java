package com.josedev.gametracker.game;

import java.util.List;

import com.josedev.gametracker.game.dtos.GameDTORequest;
import com.josedev.gametracker.game.dtos.GameDetailDTOResponse;
import com.josedev.gametracker.game.dtos.GameSummaryDTOResponse;
import com.josedev.gametracker.globals.dtos.PageDTOResponse;

public interface InterfaceGameService {

    PageDTOResponse<GameSummaryDTOResponse> search(String search, int page);

    List<GameSummaryDTOResponse> getTrending();

    GameDetailDTOResponse getById(Long id);

    GameDetailDTOResponse getOrImportByRawgId(Long rawgId);

    GameDetailDTOResponse create(GameDTORequest dto);

    GameDetailDTOResponse update(Long id, GameDTORequest dto);

    void delete(Long id);

}
