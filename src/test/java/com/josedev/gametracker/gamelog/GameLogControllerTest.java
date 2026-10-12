package com.josedev.gametracker.gamelog;

import static com.josedev.gametracker.support.TestJwt.asUser;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.josedev.gametracker.game.dtos.GameSummaryDTOResponse;
import com.josedev.gametracker.gamelog.dtos.GameLogDTORequest;
import com.josedev.gametracker.gamelog.dtos.GameLogDTOResponse;
import com.josedev.gametracker.globals.dtos.PageDTOResponse;
import com.josedev.gametracker.review.InterfaceReviewService;
import com.josedev.gametracker.support.WebMvcSecurityTest;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = GameLogController.class)
@WebMvcSecurityTest
class GameLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private InterfaceGameLogService service;

    @MockitoBean
    private InterfaceReviewService reviewService;

    private GameLogDTOResponse log(String status) {
        return new GameLogDTOResponse(1L, status, "PC", 30, false, LocalDateTime.now(),
                new GameSummaryDTOResponse(10L, null, "Celeste", null, null, List.of(), null, "CUSTOM"));
    }

    @Test
    void testUserLibrary_IsPublicAndFiltersByStatus() throws Exception {
        when(service.getByUser("mario", LogStatus.COMPLETED, false, 0))
                .thenReturn(new PageDTOResponse<>(List.of(log("COMPLETED")), 0, 24, 1, 1));

        mockMvc.perform(get("/api/v1/users/mario/logs").param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status", equalTo("COMPLETED")));
    }

    @Test
    void testUserLibrary_InvalidStatus_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/users/mario/logs").param("status", "FINISHED"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMyEntry_AsGuest_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/games/10/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void testMyEntry_WithoutData_ShouldReturnNulls() throws Exception {
        when(service.getMine("mario", 10L)).thenReturn(Optional.empty());
        when(reviewService.getMine("mario", 10L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/games/10/me").with(asUser("mario")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.log", nullValue()))
                .andExpect(jsonPath("$.review", nullValue()));
    }

    @Test
    void testSave_AsUser_ShouldReturnLog() throws Exception {
        GameLogDTORequest dto = new GameLogDTORequest(LogStatus.PLAYING, "PC", 30, false);
        when(service.save("mario", 10L, dto)).thenReturn(log("PLAYING"));

        mockMvc.perform(put("/api/v1/games/10/log").with(asUser("mario"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("PLAYING")));
    }

    @Test
    void testSave_WithoutStatus_ShouldReturn400() throws Exception {
        mockMvc.perform(put("/api/v1/games/10/log").with(asUser("mario"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"platform\":\"PC\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.status", equalTo("Status is required")));
    }

}
