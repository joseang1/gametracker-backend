package com.josedev.gametracker.security;

import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.josedev.gametracker.user.UserEntity;
import com.josedev.gametracker.user.UserRepository;

@Component
public class UserStatusJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository repository;

    public UserStatusJwtConverter(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UserEntity user = repository.findByUsername(jwt.getSubject())
                .orElseThrow(() -> new BadCredentialsException("User no longer exists"));

        if (user.isBanned())
            throw new LockedException("This account has been banned");

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return new JwtAuthenticationToken(jwt, authorities, user.getUsername());
    }

}
