package com.josedev.gametracker.gamelog;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.josedev.gametracker.game.GameEntity;
import com.josedev.gametracker.game.GameRepository;
import com.josedev.gametracker.gamelog.dtos.GameLogDTORequest;
import com.josedev.gametracker.gamelog.dtos.GameLogDTOResponse;
import com.josedev.gametracker.gamelog.exceptions.GameLogNotFoundException;
import com.josedev.gametracker.globals.dtos.PageDTOResponse;
import com.josedev.gametracker.support.TestData;
import com.josedev.gametracker.user.UserEntity;
import com.josedev.gametracker.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class GameLogServiceImplTest {

    @Mock
    private GameLogRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameLogServiceImpl service;

    private UserEntity mario;
    private GameEntity game;

    @BeforeEach
    void setUp() {
        mario = TestData.user(1L, "mario");
        game = TestData.customGame(10L, "Celeste");
    }

    @Test
    void testSave_NewLog_ShouldCreateIt() {
        when(userRepository.findByUsername("mario")).thenReturn(Optional.of(mario));
        when(gameRepository.findById(10L)).thenReturn(Optional.of(game));
        when(repository.findByUserIdAndGameId(1L, 10L)).thenReturn(Optional.empty());
        when(repository.save(any(GameLogEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameLogDTOResponse log = service.save("mario", 10L,
                new GameLogDTORequest(LogStatus.COMPLETED, " Switch ", 42, true));

        assertThat(log.status(), is(equalTo("COMPLETED")));
        assertThat(log.platform(), is(equalTo("Switch")));
        assertThat(log.hoursPlayed(), is(equalTo(42)));
        assertThat(log.favorite(), is(true));
        assertThat(log.game().id(), is(equalTo(10L)));
    }

    @Test
    void testSave_ExistingLog_ShouldChangeStatus() {
        GameLogEntity existing = new GameLogEntity(mario, game);
        existing.setStatus(LogStatus.BACKLOG);
        when(userRepository.findByUsername("mario")).thenReturn(Optional.of(mario));
        when(gameRepository.findById(10L)).thenReturn(Optional.of(game));
        when(repository.findByUserIdAndGameId(1L, 10L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        GameLogDTOResponse log = service.save("mario", 10L, new GameLogDTORequest(LogStatus.PLAYING, null, null, false));

        assertThat(log.status(), is(equalTo("PLAYING")));
    }

    @Test
    void testGetByUser_WithStatus_ShouldUseStatusQuery() {
        GameLogEntity log = new GameLogEntity(mario, game);
        log.setStatus(LogStatus.DROPPED);
        when(userRepository.findByUsername("mario")).thenReturn(Optional.of(mario));
        when(repository.findByUserUsernameAndStatusOrderByUpdatedAtDesc(eq("mario"), eq(LogStatus.DROPPED),
                any(Pageable.class))).thenReturn(new PageImpl<>(List.of(log)));

        PageDTOResponse<GameLogDTOResponse> page = service.getByUser("mario", LogStatus.DROPPED, false, 0);

        assertThat(page.content().size(), is(equalTo(1)));
        assertThat(page.content().get(0).status(), is(equalTo("DROPPED")));
        verify(repository, never()).findByUserUsernameOrderByUpdatedAtDesc(any(), any());
    }

    @Test
    void testGetByUser_Favorites_ShouldUseFavoritesQuery() {
        when(userRepository.findByUsername("mario")).thenReturn(Optional.of(mario));
        when(repository.findByUserUsernameAndFavoriteTrueOrderByUpdatedAtDesc(eq("mario"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        PageDTOResponse<GameLogDTOResponse> page = service.getByUser("mario", null, true, 0);

        assertThat(page.content().size(), is(equalTo(0)));
    }

    @Test
    void testDeleteMine_NotInLibrary_ShouldThrow() {
        when(userRepository.findByUsername("mario")).thenReturn(Optional.of(mario));
        when(repository.findByUserIdAndGameId(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(GameLogNotFoundException.class, () -> service.deleteMine("mario", 10L));
    }

}
