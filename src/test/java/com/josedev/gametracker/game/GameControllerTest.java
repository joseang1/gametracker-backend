package com.josedev.gametracker.game;

import static com.josedev.gametracker.support.TestJwt.asAdmin;
import static com.josedev.gametracker.support.TestJwt.asUser;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.josedev.gametracker.game.dtos.GameDTORequest;
import com.josedev.gametracker.game.dtos.GameDetailDTOResponse;
import com.josedev.gametracker.game.dtos.GameSummaryDTOResponse;
import com.josedev.gametracker.game.dtos.RatingStatsDTOResponse;
import com.josedev.gametracker.game.exceptions.GameNotFoundException;
import com.josedev.gametracker.globals.dtos.PageDTOResponse;
import com.josedev.gametracker.support.WebMvcSecurityTest;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = GameController.class)
@WebMvcSecurityTest
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private InterfaceGameService service;

    private final GameDTORequest request = new GameDTORequest("Indie Gem", null, "Desc",
            LocalDate.of(2024, 1, 1), List.of("Indie"), List.of("PC"), "Solo Dev");

    private GameDetailDTOResponse detail(Long id) {
        return new GameDetailDTOResponse(id, null, "Indie Gem", null, "Desc", LocalDate.of(2024, 1, 1),
                List.of("Indie"), List.of("PC"), "Solo Dev", "CUSTOM",
                new RatingStatsDTOResponse(0, null, List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L)));
    }

    @Test
    void testSearch_IsPublic() throws Exception {
        GameSummaryDTOResponse game = new GameSummaryDTOResponse(null, 3498L, "GTA V", null, null, List.of(), 9.0,
                "RAWG");
        when(service.search("gta", 0)).thenReturn(new PageDTOResponse<>(List.of(game), 0, 20, 1, 1));

        mockMvc.perform(get("/api/v1/games").param("search", "gta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", equalTo("GTA V")))
                .andExpect(jsonPath("$.content[0].rawgId", equalTo(3498)));
    }

    @Test
    void testGetById_IsPublic() throws Exception {
        when(service.getById(1L)).thenReturn(detail(1L));

        mockMvc.perform(get("/api/v1/games/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", equalTo("Indie Gem")))
                .andExpect(jsonPath("$.releaseDate", equalTo("2024-01-01")))
                .andExpect(jsonPath("$.stats.distribution", hasSize(10)));
    }

    @Test
    void testGetById_NotFound_ShouldReturn404() throws Exception {
        when(service.getById(99L)).thenThrow(new GameNotFoundException("Game not found. Id 99 does not exist."));

        mockMvc.perform(get("/api/v1/games/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", equalTo(404)));
    }

    @Test
    void testCreate_AsGuest_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(service, never()).create(any());
    }

    @Test
    void testCreate_AsUser_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/games").with(asUser("mario"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(service, never()).create(any());
    }

    @Test
    void testCreate_AsAdmin_ShouldReturn201() throws Exception {
        when(service.create(request)).thenReturn(detail(5L));

        mockMvc.perform(post("/api/v1/games").with(asAdmin("admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/games/5"))
                .andExpect(jsonPath("$.id", equalTo(5)));
    }

    @Test
    void testCreate_BlankTitle_ShouldReturn400() throws Exception {
        GameDTORequest invalid = new GameDTORequest(" ", null, null, null, null, null, null);

        mockMvc.perform(post("/api/v1/games").with(asAdmin("admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title", equalTo("Title cannot be empty")));
    }

    @Test
    void testUpdate_AsAdmin_ShouldReturn200() throws Exception {
        when(service.update(1L, request)).thenReturn(detail(1L));

        mockMvc.perform(put("/api/v1/games/1").with(asAdmin("admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testDelete_AsUser_ShouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/v1/games/1").with(asUser("mario")))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDelete_AsAdmin_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/games/1").with(asAdmin("admin")))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

}
