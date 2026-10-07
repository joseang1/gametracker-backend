package com.josedev.gametracker.admin;

import static com.josedev.gametracker.support.TestJwt.asAdmin;
import static com.josedev.gametracker.support.TestJwt.asUser;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.josedev.gametracker.admin.dtos.AdminStatsDTOResponse;
import com.josedev.gametracker.review.InterfaceReviewService;
import com.josedev.gametracker.support.WebMvcSecurityTest;
import com.josedev.gametracker.user.dtos.UserAdminDTOResponse;
import com.josedev.gametracker.user.exceptions.UserModerationException;

@WebMvcTest(controllers = AdminController.class)
@WebMvcSecurityTest
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InterfaceAdminService service;

    @MockitoBean
    private InterfaceReviewService reviewService;

    @Test
    void testStats_AsGuest_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stats")).andExpect(status().isUnauthorized());
    }

    @Test
    void testStats_AsUser_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stats").with(asUser("mario"))).andExpect(status().isForbidden());
    }

    @Test
    void testStats_AsAdmin_ShouldReturn200() throws Exception {
        when(service.getStats()).thenReturn(new AdminStatsDTOResponse(10, 1, 30, 4, 55, 80));

        mockMvc.perform(get("/api/v1/admin/stats").with(asAdmin("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", equalTo(10)))
                .andExpect(jsonPath("$.reviews", equalTo(55)));
    }

    @Test
    void testBan_AsAdmin_ShouldReturnBannedUser() throws Exception {
        when(service.setBanned(2L, true))
                .thenReturn(new UserAdminDTOResponse(2L, "troll", "troll@mail.com", "USER", true, LocalDateTime.now()));

        mockMvc.perform(patch("/api/v1/admin/users/2/ban").with(asAdmin("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.banned", equalTo(true)));
    }

    @Test
    void testBan_AnotherAdmin_ShouldReturn403WithMessage() throws Exception {
        when(service.setBanned(1L, true)).thenThrow(new UserModerationException("Admins cannot be banned"));

        mockMvc.perform(patch("/api/v1/admin/users/1/ban").with(asAdmin("admin")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", equalTo("Admins cannot be banned")));
    }

    @Test
    void testDeleteReview_AsAdmin_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/reviews/7").with(asAdmin("admin")))
                .andExpect(status().isNoContent());

        verify(reviewService).deleteById(7L);
    }

}
