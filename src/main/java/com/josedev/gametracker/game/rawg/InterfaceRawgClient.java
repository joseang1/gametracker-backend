package com.josedev.gametracker.game.rawg;

import java.util.Optional;

import com.josedev.gametracker.game.rawg.dtos.RawgGameDTO;
import com.josedev.gametracker.game.rawg.dtos.RawgPageDTO;

public interface InterfaceRawgClient {

    /** page is 1-based, like RAWG. A blank search returns the most popular games. */
    RawgPageDTO searchGames(String search, int page, int pageSize);

    /** Most added games released during the last year. */
    RawgPageDTO getTrendingGames(int pageSize);

    Optional<RawgGameDTO> getGame(Long rawgId);

}
