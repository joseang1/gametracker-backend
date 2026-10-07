package com.josedev.gametracker.integration;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.josedev.gametracker.TestcontainersConfiguration;
import com.josedev.gametracker.game.rawg.InterfaceRawgClient;

/**
 * Full application + real MySQL (Testcontainers) + real security with real JWTs.
 * Only the external RAWG API is mocked.
 * Requires Docker running.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class UserJourneyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InterfaceRawgClient rawgClient;

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void testFullJourney_RegisterLogReviewAndModeration() throws Exception {
        // 1. A guest registers and receives a token
        String registerBody = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"player1\",\"email\":\"player1@mail.com\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.role", equalTo("USER")))
                .andReturn().getResponse().getContentAsString();
        String playerToken = JsonPath.read(registerBody, "$.token");
        Integer playerId = JsonPath.read(registerBody, "$.user.id");

        // 2. The seeded admin creates a custom game
        String adminToken = login("admin", "admin1234");
        String gameBody = mockMvc.perform(post("/api/v1/games")
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Hollow Knight\",\"genres\":[\"Metroidvania\"],\"platforms\":[\"PC\"]}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer gameId = JsonPath.read(gameBody, "$.id");

        // 3. A normal user cannot create games
        mockMvc.perform(post("/api/v1/games")
                .header("Authorization", bearer(playerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Fake\"}"))
                .andExpect(status().isForbidden());

        // 4. The player logs the game and writes a review
        mockMvc.perform(put("/api/v1/games/" + gameId + "/log")
                .header("Authorization", bearer(playerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\",\"platform\":\"PC\",\"hoursPlayed\":40,\"favorite\":true}"))
                .andExpect(status().isOk());

        String reviewBody = mockMvc.perform(put("/api/v1/games/" + gameId + "/review")
                .header("Authorization", bearer(playerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":9,\"body\":\"Beautiful and hard.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spoiler", equalTo(false)))
                .andReturn().getResponse().getContentAsString();
        Integer reviewId = JsonPath.read(reviewBody, "$.id");

        // 5. Guests see the stats, the review and the profile
        mockMvc.perform(get("/api/v1/games/" + gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats.reviewCount", equalTo(1)))
                .andExpect(jsonPath("$.stats.average", equalTo(9.0)));

        mockMvc.perform(get("/api/v1/users/player1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCounts.COMPLETED", equalTo(1)))
                .andExpect(jsonPath("$.reviewCount", equalTo(1)));

        mockMvc.perform(get("/api/v1/games/" + gameId + "/me").header("Authorization", bearer(playerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.log.status", equalTo("COMPLETED")))
                .andExpect(jsonPath("$.review.rating", equalTo(9)));

        // 6. The admin bans the player: the old token stops working and login is refused
        mockMvc.perform(patch("/api/v1/admin/users/" + playerId + "/ban").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.banned", equalTo(true)));

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(playerToken)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"player1\",\"password\":\"password123\"}"))
                .andExpect(status().isForbidden());

        // 7. The admin removes the review
        mockMvc.perform(delete("/api/v1/admin/reviews/" + reviewId).header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/games/" + gameId + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(0)));
    }

    @Test
    void testTamperedToken_ShouldBeRejected() throws Exception {
        String token = login("demo", "demo1234");
        String tampered = token.substring(0, token.length() - 4) + "abcd";

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(tampered)))
                .andExpect(status().isUnauthorized());
    }

}
