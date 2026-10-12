package com.josedev.gametracker.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.time.Duration;
import java.util.Base64;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import com.josedev.gametracker.config.JwtConfig;
import com.josedev.gametracker.support.TestData;
import com.josedev.gametracker.user.UserEntity;

class TokenServiceTest {

    private TokenService tokenService;
    private JwtDecoder decoder;

    @BeforeEach
    void setUp() {
        JwtConfig config = new JwtConfig();
        String secret = Base64.getEncoder().encodeToString("a-test-secret-with-at-least-32-bytes!!".getBytes());
        SecretKey key = config.jwtSecretKey(secret);

        tokenService = new TokenService(config.jwtEncoder(key), 24);
        decoder = config.jwtDecoder(key);
    }

    @Test
    void testGenerateToken_ShouldContainUsernameRoleAndExpiration() {
        UserEntity user = TestData.admin(7L, "aethel");

        String token = tokenService.generateToken(user);
        Jwt jwt = decoder.decode(token);

        assertThat(jwt.getSubject(), is(equalTo("aethel")));
        assertThat(jwt.getClaimAsString("role"), is(equalTo("ADMIN")));
        assertThat(((Number) jwt.getClaim("uid")).longValue(), is(equalTo(7L)));
        assertThat(jwt.getExpiresAt(), is(notNullValue()));
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()).toHours(), is(equalTo(24L)));
        assertThat(token.split("\\.").length, is(greaterThan(2)));
    }

}
