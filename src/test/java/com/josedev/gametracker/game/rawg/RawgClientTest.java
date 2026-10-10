package com.josedev.gametracker.game.rawg;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.josedev.gametracker.game.rawg.dtos.RawgGameDTO;
import com.josedev.gametracker.game.rawg.dtos.RawgPageDTO;
import com.josedev.gametracker.game.rawg.exceptions.RawgException;

class RawgClientTest {

    private static final String BASE_URL = "https://api.rawg.io/api";

    private MockRestServiceServer server;
    private RawgClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new RawgClient(builder.build(), "test-key");
    }

    @Test
    void testSearchGames_ShouldSendKeyAndSearchAndMapResults() {
        String json = """
                {
                  "count": 1,
                  "next": null,
                  "results": [{
                    "id": 22511,
                    "slug": "the-legend-of-zelda-breath-of-the-wild",
                    "name": "The Legend of Zelda: Breath of the Wild",
                    "released": "2017-03-03",
                    "background_image": "https://media.rawg.io/zelda.jpg",
                    "rating": 4.48,
                    "genres": [{ "id": 4, "name": "Action" }],
                    "platforms": [{ "platform": { "id": 7, "name": "Nintendo Switch" } }]
                  }]
                }
                """;

        server.expect(requestTo(Matchers.startsWith(BASE_URL + "/games")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("key", "test-key"))
                .andExpect(queryParam("search", "zelda"))
                .andExpect(queryParam("page", "1"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        RawgPageDTO page = client.searchGames("zelda", 1, 20);

        server.verify();
        assertThat(page.count(), is(equalTo(1L)));
        RawgGameDTO game = page.results().get(0);
        assertThat(game.id(), is(equalTo(22511L)));
        assertThat(game.backgroundImage(), is(equalTo("https://media.rawg.io/zelda.jpg")));
        assertThat(game.genres().stream().map(genre -> genre.name()).toList(), contains("Action"));
        assertThat(game.platforms().get(0).platform().name(), is(equalTo("Nintendo Switch")));
    }

    @Test
    void testSearchGames_WithoutSearch_ShouldOrderByPopularity() {
        server.expect(requestTo(Matchers.startsWith(BASE_URL + "/games")))
                .andExpect(queryParam("ordering", "-added"))
                .andRespond(withSuccess("{\"count\":0,\"results\":[]}", MediaType.APPLICATION_JSON));

        RawgPageDTO page = client.searchGames(" ", 1, 20);

        server.verify();
        assertThat(page.results().size(), is(equalTo(0)));
    }

    @Test
    void testSearchGames_PageAfterTheLast_ShouldReturnEmptyPage() {
        server.expect(requestTo(Matchers.startsWith(BASE_URL + "/games"))).andRespond(withResourceNotFound());

        RawgPageDTO page = client.searchGames("zelda", 999, 20);

        assertThat(page.count(), is(equalTo(0L)));
    }

    @Test
    void testGetGame_ShouldMapDescriptionAndDevelopers() {
        String json = """
                {
                  "id": 3328,
                  "name": "The Witcher 3: Wild Hunt",
                  "released": "2015-05-18",
                  "description_raw": "Monster hunter story.",
                  "developers": [{ "name": "CD PROJEKT RED" }]
                }
                """;
        server.expect(requestTo(Matchers.startsWith(BASE_URL + "/games/3328")))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        Optional<RawgGameDTO> game = client.getGame(3328L);

        assertThat(game.isPresent(), is(true));
        assertThat(game.get().descriptionRaw(), is(equalTo("Monster hunter story.")));
        assertThat(game.get().developers().get(0).name(), is(equalTo("CD PROJEKT RED")));
    }

    @Test
    void testGetGame_NotFound_ShouldReturnEmpty() {
        server.expect(requestTo(Matchers.startsWith(BASE_URL + "/games/1"))).andRespond(withResourceNotFound());

        assertThat(client.getGame(1L).isPresent(), is(false));
    }

    @Test
    void testGetGame_ServerError_ShouldThrowRawgException() {
        server.expect(requestTo(Matchers.startsWith(BASE_URL + "/games/1"))).andRespond(withServerError());

        assertThrows(RawgException.class, () -> client.getGame(1L));
    }

    @Test
    void testWithoutApiKey_ShouldThrowRawgException() {
        RawgClient withoutKey = new RawgClient(RestClient.create(BASE_URL), "");

        RawgException exception = assertThrows(RawgException.class, () -> withoutKey.searchGames("zelda", 1, 20));
        assertThat(exception.getMessage(), Matchers.containsString("RAWG_API_KEY"));
    }

}
