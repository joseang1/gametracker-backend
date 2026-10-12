package com.josedev.gametracker.game.mappers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.josedev.gametracker.game.GameEntity;
import com.josedev.gametracker.game.GameSource;
import com.josedev.gametracker.game.rawg.dtos.RawgGameDTO;
import com.josedev.gametracker.game.rawg.dtos.RawgNamedDTO;
import com.josedev.gametracker.game.rawg.dtos.RawgPlatformEntryDTO;

class GameMapperTest {

    @Test
    void testFromRawg_ShouldMapEveryField() {
        RawgGameDTO rawg = new RawgGameDTO(3498L, "Grand Theft Auto V", "2013-09-17", "https://img/gta.jpg", 4.47,
                "Open world game",
                List.of(new RawgNamedDTO("Action"), new RawgNamedDTO("Adventure")),
                List.of(new RawgPlatformEntryDTO(new RawgNamedDTO("PC")),
                        new RawgPlatformEntryDTO(new RawgNamedDTO("PlayStation 5"))),
                List.of(new RawgNamedDTO("Rockstar North")));

        GameEntity entity = GameMapper.fromRawg(rawg);

        assertThat(entity.getRawgId(), is(equalTo(3498L)));
        assertThat(entity.getTitle(), is(equalTo("Grand Theft Auto V")));
        assertThat(entity.getReleaseDate(), is(equalTo(LocalDate.of(2013, 9, 17))));
        assertThat(entity.getGenres(), is(equalTo("Action, Adventure")));
        assertThat(entity.getPlatforms(), is(equalTo("PC, PlayStation 5")));
        assertThat(entity.getDeveloper(), is(equalTo("Rockstar North")));
        assertThat(entity.getSource(), is(equalTo(GameSource.RAWG)));
    }

    @Test
    void testFromRawg_MissingOptionalData_ShouldNotFail() {
        RawgGameDTO rawg = new RawgGameDTO(1L, "Unreleased", null, null, null, null, null, null, null);

        GameEntity entity = GameMapper.fromRawg(rawg);

        assertThat(entity.getReleaseDate(), is(nullValue()));
        assertThat(entity.getGenres(), is(nullValue()));
        assertThat(GameMapper.toSummaryDTO(rawg, null).rating(), is(nullValue()));
    }

    @ParameterizedTest(name = "\"{0}\" is split into {1}")
    @MethodSource("splitCases")
    void testSplit(String input, List<String> expected) {
        assertThat(GameMapper.split(input), is(equalTo(expected)));
    }

    private static Stream<Arguments> splitCases() {
        return Stream.of(
                Arguments.of(null, List.of()),
                Arguments.of("", List.of()),
                Arguments.of("RPG", List.of("RPG")),
                Arguments.of("Action, RPG", List.of("Action", "RPG")),
                Arguments.of(" Action ,, RPG ", List.of("Action", "RPG")));
    }

    @Test
    void testJoin_ShouldIgnoreBlankValues() {
        assertThat(GameMapper.join(List.of("Indie", " ", "Puzzle")), is(equalTo("Indie, Puzzle")));
        assertThat(GameMapper.join(List.of(" ")), is(nullValue()));
        assertThat(GameMapper.join(null), is(nullValue()));
    }

    @Test
    void testParseDate_InvalidDate_ShouldReturnNull() {
        assertThat(GameMapper.parseDate("2024-13-45"), is(nullValue()));
        assertThat(GameMapper.split(null), is(empty()));
        assertThat(GameMapper.split("A,B"), contains("A", "B"));
    }

}
