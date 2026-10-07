package com.josedev.gametracker.auth;

import static com.josedev.gametracker.support.TestJwt.asUser;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.josedev.gametracker.auth.dtos.AuthDTOResponse;
import com.josedev.gametracker.auth.dtos.LoginDTORequest;
import com.josedev.gametracker.auth.dtos.RegisterDTORequest;
import com.josedev.gametracker.support.WebMvcSecurityTest;
import com.josedev.gametracker.user.dtos.UserDTOResponse;
import com.josedev.gametracker.user.exceptions.UserAlreadyExistsException;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = AuthController.class)
@WebMvcSecurityTest
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private InterfaceAuthService service;

    private final UserDTOResponse mario = new UserDTOResponse(1L, "mario", "USER", null);

    @Test
    void testRegister_ShouldReturn201WithToken() throws Exception {
        RegisterDTORequest dto = new RegisterDTORequest("mario", "mario@mail.com", "password123");
        when(service.register(dto)).thenReturn(new AuthDTOResponse("jwt-token", mario));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", equalTo("jwt-token")))
                .andExpect(jsonPath("$.user.username", equalTo("mario")));
    }

    @Test
    void testRegister_InvalidData_ShouldReturn400WithFieldErrors() throws Exception {
        RegisterDTORequest dto = new RegisterDTORequest("a!", "not-an-email", "short");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.email", equalTo("Email is not valid")))
                .andExpect(jsonPath("$.errors.password").exists());

        verify(service, never()).register(any());
    }

    @Test
    void testRegister_DuplicatedUser_ShouldReturn409() throws Exception {
        RegisterDTORequest dto = new RegisterDTORequest("mario", "mario@mail.com", "password123");
        when(service.register(dto)).thenThrow(new UserAlreadyExistsException("Username 'mario' is already taken"));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", equalTo("Username 'mario' is already taken")));
    }

    @Test
    void testLogin_WrongPassword_ShouldReturn401() throws Exception {
        LoginDTORequest dto = new LoginDTORequest("mario", "wrong");
        when(service.login(dto)).thenThrow(new BadCredentialsException("bad"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", equalTo("Invalid username or password")));
    }

    @Test
    void testLogin_BannedUser_ShouldReturn403() throws Exception {
        LoginDTORequest dto = new LoginDTORequest("troll", "password123");
        when(service.login(dto)).thenThrow(new LockedException("locked"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", equalTo("This account has been banned")));
    }

    @Test
    void testMe_WithoutToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void testMe_WithToken_ShouldReturnCurrentUser() throws Exception {
        when(service.getCurrentUser("mario")).thenReturn(mario);

        mockMvc.perform(get("/api/v1/auth/me").with(asUser("mario")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", equalTo("mario")))
                .andExpect(jsonPath("$.role", equalTo("USER")));
    }

}
