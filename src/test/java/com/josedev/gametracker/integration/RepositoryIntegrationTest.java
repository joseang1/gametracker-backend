package com.josedev.gametracker.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.josedev.gametracker.game.GameEntity;
import com.josedev.gametracker.game.GameRepository;
import com.josedev.gametracker.game.GameSource;
import com.josedev.gametracker.gamelog.GameLogEntity;
import com.josedev.gametracker.gamelog.GameLogRepository;
import com.josedev.gametracker.gamelog.LogStatus;
import com.josedev.gametracker.review.ReviewEntity;
import com.josedev.gametracker.review.ReviewRepository;
import com.josedev.gametracker.user.Role;
import com.josedev.gametracker.user.UserEntity;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private GameLogRepository gameLogRepository;

    @Autowired
    private GameRepository gameRepository;

    private UserEntity mario;
    private UserEntity luigi;
    private GameEntity celeste;
    private GameEntity hades;

    @BeforeEach
    void setUp() {
        mario = entityManager.persist(new UserEntity("mario", "mario@mail.com", "hash", Role.USER));
        luigi = entityManager.persist(new UserEntity("luigi", "luigi@mail.com", "hash", Role.USER));
        celeste = entityManager.persist(GameEntity.builder().title("Celeste").source(GameSource.CUSTOM).build());
        hades = entityManager.persist(GameEntity.builder().title("Hades").rawgId(274755L).source(GameSource.RAWG).build());

        entityManager.persist(review(mario, celeste, 10));
        entityManager.persist(review(luigi, celeste, 7));
        entityManager.persist(review(mario, hades, 8));

        entityManager.persist(log(mario, celeste, LogStatus.COMPLETED, true));
        entityManager.persist(log(mario, hades, LogStatus.PLAYING, false));
        entityManager.flush();
    }

    private static ReviewEntity review(UserEntity user, GameEntity game, int rating) {
        ReviewEntity review = new ReviewEntity(user, game);
        review.setRating(rating);
        return review;
    }

    private static GameLogEntity log(UserEntity user, GameEntity game, LogStatus status, boolean favorite) {
        GameLogEntity log = new GameLogEntity(user, game);
        log.setStatus(status);
        log.setFavorite(favorite);
        return log;
    }

    @Test
    @DisplayName("Rating distribution of a game groups reviews by rating")
    void testCountByRatingForGame() {
        Map<Integer, Long> distribution = reviewRepository.countByRatingForGame(celeste.getId()).stream()
                .collect(Collectors.toMap(row -> ((Number) row[0]).intValue(), row -> ((Number) row[1]).longValue()));

        assertThat(distribution, is(equalTo(Map.of(10, 1L, 7, 1L))));
    }

    @Test
    @DisplayName("Average rating per game for a list of games")
    void testAverageRatingForGames() {
        Map<Long, Double> averages = reviewRepository.averageRatingForGames(List.of(celeste.getId(), hades.getId()))
                .stream()
                .collect(Collectors.toMap(row -> ((Number) row[0]).longValue(), row -> ((Number) row[1]).doubleValue()));

        assertThat(averages.get(celeste.getId()), is(closeTo(8.5, 0.001)));
        assertThat(averages.get(hades.getId()), is(closeTo(8.0, 0.001)));
    }

    @Test
    @DisplayName("Reviews of a user are paginated and load the game")
    void testFindByUserUsername() {
        Page<ReviewEntity> page = reviewRepository.findByUserUsernameOrderByCreatedAtDesc("mario", PageRequest.of(0, 10));

        assertThat(page.getTotalElements(), is(equalTo(2L)));
        assertThat(page.getContent().get(0).getGame().getTitle(), is(notNullValue()));
        assertThat(reviewRepository.averageRatingForUser(mario.getId()), is(closeTo(9.0, 0.001)));
    }

    @Test
    @DisplayName("A user cannot review the same game twice")
    void testUniqueReviewPerUserAndGame() {
        assertThrows(DataIntegrityViolationException.class, () -> {
            reviewRepository.saveAndFlush(review(mario, celeste, 3));
        });
    }

    @Test
    @DisplayName("Library counts per status and favorites filter")
    void testGameLogQueries() {
        Map<LogStatus, Long> counts = gameLogRepository.countByStatusForUser(mario.getId()).stream()
                .collect(Collectors.toMap(row -> (LogStatus) row[0], row -> ((Number) row[1]).longValue()));

        assertThat(counts, is(equalTo(Map.of(LogStatus.COMPLETED, 1L, LogStatus.PLAYING, 1L))));
        assertThat(gameLogRepository.findByUserUsernameAndFavoriteTrueOrderByUpdatedAtDesc("mario", PageRequest.of(0, 10))
                .getContent().get(0).getGame().getTitle(), is(equalTo("Celeste")));
    }

    @Test
    @DisplayName("Deleting a game's reviews and logs by game id")
    void testDeleteAllByGameId() {
        reviewRepository.deleteAllByGameId(celeste.getId());
        gameLogRepository.deleteAllByGameId(celeste.getId());

        assertThat(reviewRepository.count(), is(equalTo(1L)));
        assertThat(gameLogRepository.count(), is(equalTo(1L)));
    }

    @Test
    @DisplayName("Custom game search by title ignores case")
    void testCustomGameSearch() {
        List<GameEntity> found = gameRepository.findBySourceAndTitleContainingIgnoreCaseOrderByTitleAsc(
                GameSource.CUSTOM, "CEL");

        assertThat(found.size(), is(equalTo(1)));
        assertThat(gameRepository.findByRawgId(274755L).isPresent(), is(true));
    }

}
