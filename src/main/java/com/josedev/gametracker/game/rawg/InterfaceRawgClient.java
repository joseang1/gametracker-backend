package com.josedev.gametracker.game.rawg;

import java.util.Optional;

import com.josedev.gametracker.game.rawg.dtos.RawgGameDTO;
import com.josedev.gametracker.game.rawg.dtos.RawgPageDTO;

public interface InterfaceRawgClient {

    RawgPageDTO searchGames(String search, int page, int pageSize);

    RawgPageDTO getTrendingGames(int pageSize);

    Optional<RawgGameDTO> getGame(Long rawgId);

}
