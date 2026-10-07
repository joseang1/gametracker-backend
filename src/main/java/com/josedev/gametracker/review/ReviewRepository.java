package com.josedev.gametracker.review;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    Optional<ReviewEntity> findByUserIdAndGameId(Long userId, Long gameId);

    @EntityGraph(attributePaths = { "user", "game" })
    Page<ReviewEntity> findByGameIdOrderByCreatedAtDesc(Long gameId, Pageable pageable);

    @EntityGraph(attributePaths = { "user", "game" })
    Page<ReviewEntity> findByUserUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    @EntityGraph(attributePaths = { "user", "game" })
    Page<ReviewEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Each row: [Integer rating, Long count]
    @Query("SELECT r.rating, COUNT(r) FROM ReviewEntity r WHERE r.game.id = :gameId GROUP BY r.rating")
    List<Object[]> countByRatingForGame(@Param("gameId") Long gameId);

    // Each row: [Long gameId, Double average]
    @Query("SELECT r.game.id, AVG(r.rating) FROM ReviewEntity r WHERE r.game.id IN :gameIds GROUP BY r.game.id")
    List<Object[]> averageRatingForGames(@Param("gameIds") Collection<Long> gameIds);

    long countByUserId(Long userId);

    @Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.user.id = :userId")
    Double averageRatingForUser(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM ReviewEntity r WHERE r.game.id = :gameId")
    void deleteAllByGameId(@Param("gameId") Long gameId);

}
