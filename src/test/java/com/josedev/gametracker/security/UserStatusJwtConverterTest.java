package com.josedev.gametracker.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import com.josedev.gametracker.support.TestData;
import com.josedev.gametracker.user.UserEntity;
import com.josedev.gametracker.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserStatusJwtConverterTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserStatusJwtConverter converter;

    private Jwt jwtFor(String username) {
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(username)
                .claim("role", "ADMIN") // ignored: role is read from the database
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
    }

    @Test
    void testConvert_ShouldUseRoleFromDatabase() {
        when(repository.findByUsername("mario")).thenReturn(Optional.of(TestData.user(1L, "mario")));

        AbstractAuthenticationToken authentication = converter.convert(jwtFor("mario"));

        assertThat(authentication.getName(), is(equalTo("mario")));
        assertThat(authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList(),
                contains("ROLE_USER"));
    }

    @Test
    void testConvert_BannedUser_ShouldBeRejected() {
        UserEntity banned = TestData.user(1L, "mario");
        banned.setBanned(true);
        when(repository.findByUsername("mario")).thenReturn(Optional.of(banned));

        assertThrows(LockedException.class, () -> converter.convert(jwtFor("mario")));
    }

    @Test
    void testConvert_DeletedUser_ShouldBeRejected() {
        when(repository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> converter.convert(jwtFor("ghost")));
    }

}
