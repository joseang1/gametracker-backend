package com.josedev.gametracker.admin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.josedev.gametracker.admin.dtos.AdminStatsDTOResponse;
import com.josedev.gametracker.game.GameRepository;
import com.josedev.gametracker.game.GameSource;
import com.josedev.gametracker.gamelog.GameLogRepository;
import com.josedev.gametracker.review.ReviewRepository;
import com.josedev.gametracker.support.TestData;
import com.josedev.gametracker.user.UserEntity;
import com.josedev.gametracker.user.UserRepository;
import com.josedev.gametracker.user.dtos.UserAdminDTOResponse;
import com.josedev.gametracker.user.exceptions.UserModerationException;
import com.josedev.gametracker.user.exceptions.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private GameLogRepository gameLogRepository;

    @InjectMocks
    private AdminServiceImpl service;

    @Test
    void testBan_User_ShouldMarkAsBanned() {
        UserEntity user = TestData.user(2L, "troll");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserAdminDTOResponse result = service.setBanned(2L, true);

        assertThat(result.banned(), is(true));
        assertThat(user.isBanned(), is(true));
    }

    @Test
    void testUnban_ShouldRestoreAccess() {
        UserEntity user = TestData.user(2L, "troll");
        user.setBanned(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        assertThat(service.setBanned(2L, false).banned(), is(false));
    }

    @Test
    void testBan_Admin_ShouldBeForbidden() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(TestData.admin(1L, "admin")));

        assertThrows(UserModerationException.class, () -> service.setBanned(1L, true));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testBan_UnknownUser_ShouldThrow() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.setBanned(99L, true));
    }

    @Test
    void testGetStats() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByBannedTrue()).thenReturn(1L);
        when(gameRepository.count()).thenReturn(30L);
        when(gameRepository.countBySource(GameSource.CUSTOM)).thenReturn(4L);
        when(reviewRepository.count()).thenReturn(55L);
        when(gameLogRepository.count()).thenReturn(80L);

        AdminStatsDTOResponse stats = service.getStats();

        assertThat(stats, is(equalTo(new AdminStatsDTOResponse(10, 1, 30, 4, 55, 80))));
    }

}
