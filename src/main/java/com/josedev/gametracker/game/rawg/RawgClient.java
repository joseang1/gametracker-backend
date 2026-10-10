package com.josedev.gametracker.game.rawg;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.josedev.gametracker.game.rawg.dtos.RawgGameDTO;
import com.josedev.gametracker.game.rawg.dtos.RawgPageDTO;
import com.josedev.gametracker.game.rawg.exceptions.RawgException;

@Component
public class RawgClient implements InterfaceRawgClient {

    private final RestClient restClient;
    private final String apiKey;

    public RawgClient(@Qualifier("rawgRestClient") RestClient restClient, @Value("${rawg.api-key}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    @Override
    @Cacheable("rawg-search")
    public RawgPageDTO searchGames(String search, int page, int pageSize) {
        checkApiKey();
        try {
            return restClient.get()
                    .uri(uri -> {
                        uri.path("/games")
                                .queryParam("key", apiKey)
                                .queryParam("page", page)
                                .queryParam("page_size", pageSize);
                        if (search != null && !search.isBlank())
                            uri.queryParam("search", search.trim()).queryParam("search_precise", true);
                        else
                            uri.queryParam("ordering", "-added");
                        return uri.build();
                    })
                    .retrieve()
                    .body(RawgPageDTO.class);
        } catch (HttpClientErrorException.NotFound exception) {
            return RawgPageDTO.empty();
        } catch (RestClientException exception) {
            throw new RawgException("Game catalog is temporarily unavailable");
        }
    }

    @Override
    @Cacheable("rawg-trending")
    public RawgPageDTO getTrendingGames(int pageSize) {
        checkApiKey();
        LocalDate today = LocalDate.now();
        String dates = today.minusYears(1) + "," + today;
        try {
            return restClient.get()
                    .uri(uri -> uri.path("/games")
                            .queryParam("key", apiKey)
                            .queryParam("dates", dates)
                            .queryParam("ordering", "-added")
                            .queryParam("page_size", pageSize)
                            .build())
                    .retrieve()
                    .body(RawgPageDTO.class);
        } catch (RestClientException exception) {
            throw new RawgException("Game catalog is temporarily unavailable");
        }
    }

    @Override
    @Cacheable("rawg-game")
    public Optional<RawgGameDTO> getGame(Long rawgId) {
        checkApiKey();
        try {
            RawgGameDTO game = restClient.get()
                    .uri(uri -> uri.path("/games/{id}").queryParam("key", apiKey).build(rawgId))
                    .retrieve()
                    .body(RawgGameDTO.class);
            return Optional.ofNullable(game);
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND)
                return Optional.empty();
            throw new RawgException("Game catalog is temporarily unavailable");
        } catch (RestClientException exception) {
            throw new RawgException("Game catalog is temporarily unavailable");
        }
    }

    private void checkApiKey() {
        if (apiKey == null || apiKey.isBlank())
            throw new RawgException("RAWG API key is not configured (set RAWG_API_KEY)");
    }

}
