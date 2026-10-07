package com.josedev.gametracker.support;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** Shortcuts to send requests as a logged user or admin in MockMvc tests. */
public final class TestJwt {

    private TestJwt() {
    }

    public static RequestPostProcessor asUser(String username) {
        return jwt().jwt(token -> token.subject(username)).authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    public static RequestPostProcessor asAdmin(String username) {
        return jwt().jwt(token -> token.subject(username)).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

}
