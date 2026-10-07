package com.josedev.gametracker.game.mappers;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.josedev.gametracker.game.GameEntity;
import com.josedev.gametracker.game.GameSource;
import com.josedev.gametracker.game.dtos.GameDTORequest;
import com.josedev.gametracker.game.dtos.GameDetailDTOResponse;
import com.josedev.gametracker.game.dtos.GameSummaryDTOResponse;
import com.josedev.gametracker.game.dtos.RatingStatsDTOResponse;
import com.josedev.gametracker.game.rawg.dtos.RawgGameDTO;
import com.josedev.gametracker.game.rawg.dtos.RawgNamedDTO;
import com.josedev.gametracker.game.rawg.dtos.RawgPlatformEntryDTO;

public class GameMapper {

    private static final String SEPARATOR = ", ";

    private GameMapper() {
    }

    public static GameEntity toEntity(GameDTORequest dto) {
        GameEntity entity = GameEntity.builder().source(GameSource.CUSTOM).build();
        updateEntity(entity, dto);
        return entity;
    }

    public static void updateEntity(GameEntity entity, GameDTORequest dto) {
        entity.setTitle(dto.title().trim());
        entity.setCoverUrl(blankToNull(dto.coverUrl()));
        entity.setDescription(blankToNull(dto.description()));
        entity.setReleaseDate(dto.releaseDate());
        entity.setGenres(join(dto.genres()));
        entity.setPlatforms(join(dto.platforms()));
        entity.setDeveloper(blankToNull(dto.developer()));
    }

    public static GameEntity fromRawg(RawgGameDTO rawg) {
        return GameEntity.builder()
                .rawgId(rawg.id())
                .title(rawg.name())
                .coverUrl(rawg.backgroundImage())
                .description(rawg.descriptionRaw())
                .releaseDate(parseDate(rawg.released()))
                .genres(join(names(rawg.genres())))
                .platforms(join(platformNames(rawg.platforms())))
                .developer(join(names(rawg.developers())))
                .source(GameSource.RAWG)
                .build();
    }

    public static GameSummaryDTOResponse toSummaryDTO(GameEntity entity, Double rating) {
        return new GameSummaryDTOResponse(entity.getId(), entity.getRawgId(), entity.getTitle(),
                entity.getCoverUrl(), entity.getReleaseDate(), split(entity.getGenres()), rating,
                entity.getSource().name());
    }

    /** localId is null when the game is not stored in our database yet. RAWG rating (0-5) is scaled to 1-10. */
    public static GameSummaryDTOResponse toSummaryDTO(RawgGameDTO rawg, Long localId) {
        Double rating = rawg.rating() == null || rawg.rating() == 0 ? null : Math.round(rawg.rating() * 20) / 10.0;
        return new GameSummaryDTOResponse(localId, rawg.id(), rawg.name(), rawg.backgroundImage(),
                parseDate(rawg.released()), names(rawg.genres()), rating, GameSource.RAWG.name());
    }

    public static GameDetailDTOResponse toDetailDTO(GameEntity entity, RatingStatsDTOResponse stats) {
        return new GameDetailDTOResponse(entity.getId(), entity.getRawgId(), entity.getTitle(), entity.getCoverUrl(),
                entity.getDescription(), entity.getReleaseDate(), split(entity.getGenres()),
                split(entity.getPlatforms()), entity.getDeveloper(), entity.getSource().name(), stats);
    }

    static List<String> split(String value) {
        if (value == null || value.isBlank())
            return List.of();
        return Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    static String join(List<String> values) {
        if (values == null)
            return null;
        List<String> clean = values.stream().filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty())
                .toList();
        return clean.isEmpty() ? null : String.join(SEPARATOR, clean);
    }

    static LocalDate parseDate(String date) {
        if (date == null || date.isBlank())
            return null;
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private static List<String> names(List<RawgNamedDTO> items) {
        if (items == null)
            return List.of();
        return items.stream().filter(Objects::nonNull).map(RawgNamedDTO::name).filter(Objects::nonNull).toList();
    }

    private static List<String> platformNames(List<RawgPlatformEntryDTO> items) {
        if (items == null)
            return List.of();
        return names(items.stream().filter(Objects::nonNull).map(RawgPlatformEntryDTO::platform).toList());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

}
