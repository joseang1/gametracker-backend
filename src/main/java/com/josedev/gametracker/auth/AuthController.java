package com.josedev.gametracker.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.josedev.gametracker.auth.dtos.AuthDTOResponse;
import com.josedev.gametracker.auth.dtos.LoginDTORequest;
import com.josedev.gametracker.auth.dtos.RegisterDTORequest;
import com.josedev.gametracker.user.dtos.UserDTOResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "${api-endpoint}/auth")
public class AuthController {

    private final InterfaceAuthService service;

    public AuthController(InterfaceAuthService service) {
        this.service = service;
    }

    @PostMapping("register")
    public ResponseEntity<AuthDTOResponse> register(@Valid @RequestBody RegisterDTORequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(dto));
    }

    @PostMapping("login")
    public AuthDTOResponse login(@Valid @RequestBody LoginDTORequest dto) {
        return service.login(dto);
    }

    @GetMapping("me")
    public UserDTOResponse me(@AuthenticationPrincipal Jwt jwt) {
        return service.getCurrentUser(jwt.getSubject());
    }

}
