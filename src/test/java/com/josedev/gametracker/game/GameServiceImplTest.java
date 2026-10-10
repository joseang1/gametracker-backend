package com.josedev.gametracker.game;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.josedev.gametracker.game.dtos.GameDTORequest;
import com.josedev.gametracker.game.dtos.GameDetailDTOResponse;
import com.josedev.gametracker.game.dtos.GameSummaryDTOResponse;
import com.josedev.gametracker.game.dtos.RatingStatsDTOResponse;
import com.josedev.gametracker.game.exceptions.GameNotFoundException;
import com.josedev.gametracker.game.rawg.InterfaceRawgClient;
import com.josedev.gametracker.game.rawg.dtos.RawgGameDTO;
import com.josedev.gametracker.game.rawg.dtos.RawgNamedDTO;
import com.josedev.gametracker.game.rawg.dtos.RawgPageDTO;
import com.josedev.gametracker.game.rawg.exceptions.RawgException;
import com.josedev.gametracker.gamelog.GameLogRepository;
import com.josedev.gametracker.globals.dtos.PageDTOResponse;
import com.josedev.gametracker.review.ReviewRepository;
import com.josedev.gametracker.support.TestData;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    private GameRepository repository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private GameLogRepository gameLogRepository;

    @Mock
    private InterfaceRawgClient rawgClient;

    @InjectMocks
    private GameServiceImpl service;

    private static RawgGameDTO rawgGame(long id, String name) {
        return new RawgGameDTO(id, name, "2023-08-03", "https://img/" + id + ".jpg", 4.5, null,
                List.of(new RawgNamedDTO("RPG")), List.of(), List.of());
    }

    @Test
    void testSearch_FirstPage_ShouldPutCustomGamesFirstAndLinkStoredRawgGames() {
        GameEntity custom = TestData.customGame(1L, "Hollow Knight Fan Remake");
        GameEntity storedRawg = TestData.rawgGame(2L, 100L, "Baldur's Gate 3");

        when(repository.findBySourceAndTitleContainingIgnoreCaseOrderByTitleAsc(GameSource.CUSTOM, "knight"))
                .thenReturn(List.of(custom));
        when(rawgClient.searchGames("knight", 1, GameServiceImpl.PAGE_SIZE))
                .thenReturn(new RawgPageDTO(2, List.of(rawgGame(100L, "Baldur's Gate 3"), rawgGame(200L, "Elden Ring"))));
        when(repository.findByRawgIdIn(List.of(100L, 200L))).thenReturn(List.of(storedRawg));

        PageDTOResponse<GameSummaryDTOResponse> result = service.search(" knight ", 0);

        assertThat(result.content().stream().map(GameSummaryDTOResponse::title).toList(),
                contains("Hollow Knight Fan Remake", "Baldur's Gate 3", "Elden Ring"));
        assertThat(result.content().get(1).id(), is(equalTo(2L)));
        assertThat(result.content().get(2).id(), is(nullValue()));
        assertThat(result.content().get(2).rating(), is(equalTo(9.0)));
        assertThat(result.totalElements(), is(equalTo(3L)));
    }

    @Test
    void testSearch_SecondPage_ShouldNotRepeatCustomGames() {
        when(repository.findBySourceAndTitleContainingIgnoreCaseOrderByTitleAsc(GameSource.CUSTOM, ""))
                .thenReturn(List.of(TestData.customGame(1L, "Custom")));
        when(rawgClient.searchGames("", 2, GameServiceImpl.PAGE_SIZE))
                .thenReturn(new RawgPageDTO(40, List.of(rawgGame(300L, "Hades"))));

        PageDTOResponse<GameSummaryDTOResponse> result = service.search(null, 1);

        assertThat(result.content().stream().map(GameSummaryDTOResponse::title).toList(), contains("Hades"));
        assertThat(result.page(), is(equalTo(1)));
        assertThat(result.totalPages(), is(equalTo(2)));
    }

    @Test
    void testSearch_RawgDown_ShouldStillReturnCustomGames() {
        when(repository.findBySourceAndTitleContainingIgnoreCaseOrderByTitleAsc(any(), anyString()))
                .thenReturn(List.of(TestData.customGame(1L, "Custom")));
        when(rawgClient.searchGames(anyString(), anyInt(), anyInt())).thenThrow(new RawgException("down"));

        PageDTOResponse<GameSummaryDTOResponse> result = service.search("", 0);

        assertThat(result.content().size(), is(equalTo(1)));
        assertThat(result.content().get(0).source(), is(equalTo("CUSTOM")));
    }

    @Test
    void testGetOrImport_AlreadyStored_ShouldNotCallRawg() {
        when(repository.findByRawgId(100L)).thenReturn(Optional.of(TestData.rawgGame(2L, 100L, "Baldur's Gate 3")));

        GameDetailDTOResponse detail = service.getOrImportByRawgId(100L);

        assertThat(detail.id(), is(equalTo(2L)));
        verify(rawgClient, never()).getGame(any());
    }

    @Test
    void testGetOrImport_NewGame_ShouldFetchFromRawgAndSave() {
        when(repository.findByRawgId(100L)).thenReturn(Optional.empty());
        when(rawgClient.getGame(100L)).thenReturn(Optional.of(rawgGame(100L, "Baldur's Gate 3")));
        when(repository.save(any(GameEntity.class))).thenAnswer(invocation -> {
            GameEntity game = invocation.getArgument(0);
            game.setId(9L);
            return game;
        });

        GameDetailDTOResponse detail = service.getOrImportByRawgId(100L);

        assertThat(detail.id(), is(equalTo(9L)));
        assertThat(detail.rawgId(), is(equalTo(100L)));
        assertThat(detail.title(), is(equalTo("Baldur's Gate 3")));
        assertThat(detail.source(), is(equalTo("RAWG")));
    }

    @Test
    void testGetOrImport_UnknownRawgGame_ShouldThrowNotFound() {
        when(repository.findByRawgId(999L)).thenReturn(Optional.empty());
        when(rawgClient.getGame(999L)).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> service.getOrImportByRawgId(999L));
    }

    @Test
    void testGetById_NotFound_ShouldThrow() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> service.getById(5L));
    }

    @Test
    void testGetStats_ShouldCalculateAverageAndDistribution() {
        when(reviewRepository.countByRatingForGame(1L))
                .thenReturn(List.of(new Object[] { 10, 2L }, new Object[] { 7, 1L }));

        RatingStatsDTOResponse stats = service.getStats(1L);

        assertThat(stats.reviewCount(), is(equalTo(3L)));
        assertThat(stats.average(), is(equalTo(9.0)));
        assertThat(stats.distribution().get(9), is(equalTo(2L)));
        assertThat(stats.distribution().get(6), is(equalTo(1L)));
        assertThat(stats.distribution().get(0), is(equalTo(0L)));
    }

    @Test
    void testCreate_ShouldSaveCustomGame() {
        GameDTORequest dto = new GameDTORequest("  Indie Gem ", "", "A small game", null, List.of("Indie", " "),
                List.of("PC"), "Solo Dev");
        when(repository.save(any(GameEntity.class))).thenAnswer(invocation -> {
            GameEntity game = invocation.getArgument(0);
            game.setId(11L);
            return game;
        });

        GameDetailDTOResponse detail = service.create(dto);

        assertThat(detail.title(), is(equalTo("Indie Gem")));
        assertThat(detail.coverUrl(), is(nullValue()));
        assertThat(detail.genres(), contains("Indie"));
        assertThat(detail.source(), is(equalTo("CUSTOM")));
    }

    @Test
    void testDelete_ShouldRemoveReviewsAndLogsFirst() {
        GameEntity game = TestData.customGame(1L, "To delete");
        when(repository.findById(1L)).thenReturn(Optional.of(game));

        service.delete(1L);

        verify(reviewRepository).deleteAllByGameId(1L);
        verify(gameLogRepository).deleteAllByGameId(1L);
        verify(repository).delete(game);
    }

}
