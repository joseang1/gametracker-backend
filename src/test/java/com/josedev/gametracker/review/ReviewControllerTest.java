package com.josedev.gametracker.review;

import static com.josedev.gametracker.support.TestJwt.asUser;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.josedev.gametracker.game.dtos.GameSummaryDTOResponse;
import com.josedev.gametracker.globals.dtos.PageDTOResponse;
import com.josedev.gametracker.review.dtos.ReviewDTORequest;
import com.josedev.gametracker.review.dtos.ReviewDTOResponse;
import com.josedev.gametracker.support.WebMvcSecurityTest;
import com.josedev.gametracker.user.dtos.UserDTOResponse;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = ReviewController.class)
@WebMvcSecurityTest
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private InterfaceReviewService service;

    private ReviewDTOResponse review() {
        return new ReviewDTOResponse(1L, 9, "Great", false, LocalDateTime.now(), LocalDateTime.now(),
                new UserDTOResponse(1L, "mario", "USER", null),
                new GameSummaryDTOResponse(10L, null, "Celeste", null, null, List.of(), null, "CUSTOM"));
    }

    @Test
    void testRecent_IsPublic() throws Exception {
        when(service.getRecent(0)).thenReturn(new PageDTOResponse<>(List.of(review()), 0, 10, 1, 1));

        mockMvc.perform(get("/api/v1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].author.username", equalTo("mario")));
    }

    @Test
    void testByGame_IsPublic() throws Exception {
        when(service.getByGame(10L, 1)).thenReturn(new PageDTOResponse<>(List.of(review()), 1, 10, 11, 2));

        mockMvc.perform(get("/api/v1/games/10/reviews").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", equalTo(1)))
                .andExpect(jsonPath("$.content[0].game.title", equalTo("Celeste")));
    }

    @Test
    void testSave_AsGuest_ShouldReturn401() throws Exception {
        mockMvc.perform(put("/api/v1/games/10/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new ReviewDTORequest(9, "Great", false))))
                .andExpect(status().isUnauthorized());

        verify(service, never()).save(anyString(), anyLong(), any());
    }

    @Test
    void testSave_AsUser_ShouldUseUsernameFromToken() throws Exception {
        ReviewDTORequest dto = new ReviewDTORequest(9, "Great", false);
        when(service.save("mario", 10L, dto)).thenReturn(review());

        mockMvc.perform(put("/api/v1/games/10/review").with(asUser("mario"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", equalTo(9)));
    }

    @Test
    void testSave_RatingOutOfRange_ShouldReturn400() throws Exception {
        mockMvc.perform(put("/api/v1/games/10/review").with(asUser("mario"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new ReviewDTORequest(11, null, false))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.rating", equalTo("Rating must be between 1 and 10")));
    }

    @Test
    void testDelete_AsUser_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/games/10/review").with(asUser("mario")))
                .andExpect(status().isNoContent());

        verify(service).deleteMine("mario", 10L);
    }

}
