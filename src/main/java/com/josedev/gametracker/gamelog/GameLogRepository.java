package com.josedev.gametracker.gamelog;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameLogRepository extends JpaRepository<GameLogEntity, Long> {

    Optional<GameLogEntity> findByUserIdAndGameId(Long userId, Long gameId);

    // @EntityGraph loads the game in the same query (avoids the N+1 problem)
    @EntityGraph(attributePaths = "game")
    Page<GameLogEntity> findByUserUsernameOrderByUpdatedAtDesc(String username, Pageable pageable);

    @EntityGraph(attributePaths = "game")
    Page<GameLogEntity> findByUserUsernameAndStatusOrderByUpdatedAtDesc(String username, LogStatus status,
            Pageable pageable);

    @EntityGraph(attributePaths = "game")
    Page<GameLogEntity> findByUserUsernameAndFavoriteTrueOrderByUpdatedAtDesc(String username, Pageable pageable);

    // Each row: [LogStatus status, Long count]
    @Query("SELECT l.status, COUNT(l) FROM GameLogEntity l WHERE l.user.id = :userId GROUP BY l.status")
    List<Object[]> countByStatusForUser(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM GameLogEntity l WHERE l.game.id = :gameId")
    void deleteAllByGameId(@Param("gameId") Long gameId);

}
