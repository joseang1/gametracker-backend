package com.josedev.gametracker.game;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<GameEntity, Long> {

    Optional<GameEntity> findByRawgId(Long rawgId);

    List<GameEntity> findByRawgIdIn(Collection<Long> rawgIds);

    List<GameEntity> findBySourceAndTitleContainingIgnoreCaseOrderByTitleAsc(GameSource source, String title);

    long countBySource(GameSource source);

}
