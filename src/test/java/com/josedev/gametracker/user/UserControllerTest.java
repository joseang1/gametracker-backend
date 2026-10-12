package com.josedev.gametracker.user;

import static com.josedev.gametracker.support.TestJwt.asUser;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.josedev.gametracker.support.WebMvcSecurityTest;
import com.josedev.gametracker.user.dtos.ProfileDTORequest;
import com.josedev.gametracker.user.dtos.ProfileDTOResponse;
import com.josedev.gametracker.user.exceptions.UserNotFoundException;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = UserController.class)
@WebMvcSecurityTest
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private InterfaceUserService service;

    private ProfileDTOResponse profile(String bio) {
        return new ProfileDTOResponse(1L, "mario", "USER", bio, null, LocalDateTime.now(),
                Map.of("COMPLETED", 3L), 2, 8.5);
    }

    @Test
    void testGetProfile_IsPublic() throws Exception {
        when(service.getProfile("mario")).thenReturn(profile(null));

        mockMvc.perform(get("/api/v1/users/mario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCounts.COMPLETED", equalTo(3)))
                .andExpect(jsonPath("$.averageRating", equalTo(8.5)));
    }

    @Test
    void testGetProfile_UnknownUser_ShouldReturn404() throws Exception {
        when(service.getProfile("ghost")).thenThrow(new UserNotFoundException("User 'ghost' not found"));

        mockMvc.perform(get("/api/v1/users/ghost")).andExpect(status().isNotFound());
    }

    @Test
    void testUpdateMe_AsGuest_ShouldReturn401() throws Exception {
        mockMvc.perform(put("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new ProfileDTORequest("Hi", null))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateMe_AsUser_ShouldUpdateOwnProfile() throws Exception {
        ProfileDTORequest dto = new ProfileDTORequest("Souls-like fan", null);
        when(service.updateProfile("mario", dto)).thenReturn(profile("Souls-like fan"));

        mockMvc.perform(put("/api/v1/users/me").with(asUser("mario"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio", equalTo("Souls-like fan")));
    }

    @Test
    void testUpdateMe_InvalidAvatar_ShouldReturn400() throws Exception {
        mockMvc.perform(put("/api/v1/users/me").with(asUser("mario"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new ProfileDTORequest(null, "javascript:alert(1)"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.avatarUrl").exists());
    }

}
