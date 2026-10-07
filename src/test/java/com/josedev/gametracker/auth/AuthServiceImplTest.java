package com.josedev.gametracker.auth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.josedev.gametracker.auth.dtos.AuthDTOResponse;
import com.josedev.gametracker.auth.dtos.LoginDTORequest;
import com.josedev.gametracker.auth.dtos.RegisterDTORequest;
import com.josedev.gametracker.security.TokenService;
import com.josedev.gametracker.support.TestData;
import com.josedev.gametracker.user.Role;
import com.josedev.gametracker.user.UserEntity;
import com.josedev.gametracker.user.UserRepository;
import com.josedev.gametracker.user.exceptions.UserAlreadyExistsException;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthServiceImpl service;

    @Test
    void testRegister_ShouldHashPasswordAndReturnToken() {
        RegisterDTORequest dto = new RegisterDTORequest("Aethel", "Aethel@Mail.com", "supersecret");
        when(userRepository.existsByUsernameIgnoreCase("Aethel")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("Aethel@Mail.com")).thenReturn(false);
        when(passwordEncoder.encode("supersecret")).thenReturn("hashed-password");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(tokenService.generateToken(any(UserEntity.class))).thenReturn("jwt-token");

        AuthDTOResponse response = service.register(dto);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity saved = captor.getValue();

        assertThat(saved.getPassword(), is(equalTo("hashed-password")));
        assertThat(saved.getEmail(), is(equalTo("aethel@mail.com")));
        assertThat(saved.getRole(), is(equalTo(Role.USER)));
        assertThat(response.token(), is(equalTo("jwt-token")));
        assertThat(response.user().username(), is(equalTo("Aethel")));
    }

    @Test
    void testRegister_UsernameTaken_ShouldThrowConflict() {
        RegisterDTORequest dto = new RegisterDTORequest("aethel", "new@mail.com", "supersecret");
        when(userRepository.existsByUsernameIgnoreCase("aethel")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> service.register(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_EmailTaken_ShouldThrowConflict() {
        RegisterDTORequest dto = new RegisterDTORequest("newuser", "used@mail.com", "supersecret");
        when(userRepository.existsByUsernameIgnoreCase("newuser")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("used@mail.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> service.register(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLogin_ValidCredentials_ShouldReturnToken() {
        UserEntity user = TestData.user(3L, "mario");
        when(userRepository.findByUsername("mario")).thenReturn(Optional.of(user));
        when(tokenService.generateToken(user)).thenReturn("jwt-token");

        AuthDTOResponse response = service.login(new LoginDTORequest("mario", "password123"));

        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken("mario", "password123"));
        assertThat(response.token(), is(equalTo("jwt-token")));
        assertThat(response.user().role(), is(equalTo("USER")));
    }

    @Test
    void testLogin_WrongPassword_ShouldPropagateBadCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThrows(BadCredentialsException.class, () -> service.login(new LoginDTORequest("mario", "wrong")));
        verify(tokenService, never()).generateToken(any());
    }

}
