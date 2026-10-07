package com.josedev.gametracker.support;

import java.time.LocalDate;

import com.josedev.gametracker.game.GameEntity;
import com.josedev.gametracker.game.GameSource;
import com.josedev.gametracker.user.Role;
import com.josedev.gametracker.user.UserEntity;

/** Builders for entities reused across unit tests. */
public final class TestData {

    private TestData() {
    }

    public static UserEntity user(Long id, String username) {
        UserEntity user = new UserEntity(username, username + "@mail.com", "hashed", Role.USER);
        user.setId(id);
        return user;
    }

    public static UserEntity admin(Long id, String username) {
        UserEntity user = new UserEntity(username, username + "@mail.com", "hashed", Role.ADMIN);
        user.setId(id);
        return user;
    }

    public static GameEntity customGame(Long id, String title) {
        return GameEntity.builder()
                .id(id)
                .title(title)
                .genres("Indie, Platformer")
                .releaseDate(LocalDate.of(2024, 5, 10))
                .source(GameSource.CUSTOM)
                .build();
    }

    public static GameEntity rawgGame(Long id, Long rawgId, String title) {
        return GameEntity.builder()
                .id(id)
                .rawgId(rawgId)
                .title(title)
                .genres("RPG")
                .source(GameSource.RAWG)
                .build();
    }

}
