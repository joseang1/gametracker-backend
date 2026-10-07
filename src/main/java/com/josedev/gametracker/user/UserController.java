package com.josedev.gametracker.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.josedev.gametracker.user.dtos.ProfileDTORequest;
import com.josedev.gametracker.user.dtos.ProfileDTOResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "${api-endpoint}/users")
public class UserController {

    private final InterfaceUserService service;

    public UserController(InterfaceUserService service) {
        this.service = service;
    }

    @GetMapping("{username}")
    public ProfileDTOResponse getProfile(@PathVariable String username) {
        return service.getProfile(username);
    }

    @PutMapping("me")
    public ProfileDTOResponse updateMyProfile(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ProfileDTORequest dto) {
        return service.updateProfile(jwt.getSubject(), dto);
    }

}
