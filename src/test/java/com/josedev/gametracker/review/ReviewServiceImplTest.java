package com.josedev.gametracker.review;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.josedev.gametracker.game.GameEntity;
import com.josedev.gametracker.game.GameRepository;
import com.josedev.gametracker.game.exceptions.GameNotFoundException;
import com.josedev.gametracker.review.dtos.ReviewDTORequest;
import com.josedev.gametracker.review.dtos.ReviewDTOResponse;
import com.josedev.gametracker.review.exceptions.ReviewNotFoundException;
import com.josedev.gametracker.support.TestData;
import com.josedev.gametracker.user.UserEntity;
import com.josedev.gametracker.user.UserRepository;
import com.josedev.gametracker.user.exceptions.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private ReviewServiceImpl service;

    private UserEntity mario;
    private GameEntity game;

    @BeforeEach
    void setUp() {
        mario = TestData.user(1L, "mario");
        game = TestData.customGame(10L, "Celeste");
    }

    @Test
    void testSave_NewReview_ShouldCreateIt() {
        when(userRepository.findByUsername("mario")).thenReturn(Optional.of(mario));
        when(gameRepository.findById(10L)).thenReturn(Optional.of(game));
        when(repository.findByUserIdAndGameId(1L, 10L)).thenReturn(Optional.empty());
        when(repository.save(any(ReviewEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewDTOResponse review = service.save("mario", 10L, new ReviewDTORequest(9, "  Loved it  ", false));

        assertThat(review.rating(), is(equalTo(9)));
        assertThat(review.body(), is(equalTo("Loved it")));
        assertThat(review.author().username(), is(equalTo("mario")));
        assertThat(review.game().title(), is(equalTo("Celeste")));
    }

    @Test
    void testSave_ExistingReview_ShouldUpdateTheSameEntity() {
        ReviewEntity existing = new ReviewEntity(mario, game);
        existing.setId(50L);
        existing.setRating(4);
        existing.setBody("Meh");

        when(userRepository.findByUsername("mario")).thenReturn(Optional.of(mario));
        when(gameRepository.findById(10L)).thenReturn(Optional.of(game));
        when(repository.findByUserIdAndGameId(1L, 10L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        ReviewDTOResponse review = service.save("mario", 10L, new ReviewDTORequest(8, "  ", true));

        assertThat(review.id(), is(equalTo(50L)));
        assertThat(review.rating(), is(equalTo(8)));
        assertThat(review.body(), is(nullValue()));
        assertThat(review.spoiler(), is(true));
    }

    @Test
    void testSave_UnknownGame_ShouldThrow() {
        when(userRepository.findByUsername("mario")).thenReturn(Optional.of(mario));
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> service.save("mario", 99L, new ReviewDTORequest(5, null, false)));
        verify(repository, never()).save(any());
    }

    @Test
    void testSave_UnknownUser_ShouldThrow() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.save("ghost", 10L, new ReviewDTORequest(5, null, false)));
    }

    @Test
    void testDeleteMine_WithoutReview_ShouldThrow() {
        when(userRepository.findByUsername("mario")).thenReturn(Optional.of(mario));
        when(repository.findByUserIdAndGameId(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> service.deleteMine("mario", 10L));
    }

    @Test
    void testDeleteById_ShouldDeleteAnyReview() {
        ReviewEntity review = new ReviewEntity(mario, game);
        when(repository.findById(50L)).thenReturn(Optional.of(review));

        service.deleteById(50L);

        verify(repository).delete(review);
    }

    @Test
    void testGetByGame_UnknownGame_ShouldThrow() {
        when(gameRepository.existsById(99L)).thenReturn(false);

        assertThrows(GameNotFoundException.class, () -> service.getByGame(99L, 0));
    }

}
