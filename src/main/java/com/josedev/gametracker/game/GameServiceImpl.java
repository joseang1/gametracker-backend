package com.josedev.gametracker.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.josedev.gametracker.game.dtos.GameDTORequest;
import com.josedev.gametracker.game.dtos.GameDetailDTOResponse;
import com.josedev.gametracker.game.dtos.GameSummaryDTOResponse;
import com.josedev.gametracker.game.dtos.RatingStatsDTOResponse;
import com.josedev.gametracker.game.exceptions.GameNotFoundException;
import com.josedev.gametracker.game.mappers.GameMapper;
import com.josedev.gametracker.game.rawg.InterfaceRawgClient;
import com.josedev.gametracker.game.rawg.dtos.RawgGameDTO;
import com.josedev.gametracker.game.rawg.dtos.RawgPageDTO;
import com.josedev.gametracker.game.rawg.exceptions.RawgException;
import com.josedev.gametracker.gamelog.GameLogRepository;
import com.josedev.gametracker.globals.dtos.PageDTOResponse;
import com.josedev.gametracker.review.ReviewRepository;

@Service
public class GameServiceImpl implements InterfaceGameService {

    private static final Logger log = LoggerFactory.getLogger(GameServiceImpl.class);

    static final int PAGE_SIZE = 20;
    static final int TRENDING_SIZE = 12;

    private final GameRepository repository;
    private final ReviewRepository reviewRepository;
    private final GameLogRepository gameLogRepository;
    private final InterfaceRawgClient rawgClient;

    public GameServiceImpl(GameRepository repository, ReviewRepository reviewRepository,
            GameLogRepository gameLogRepository, InterfaceRawgClient rawgClient) {
        this.repository = repository;
        this.reviewRepository = reviewRepository;
        this.gameLogRepository = gameLogRepository;
        this.rawgClient = rawgClient;
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTOResponse<GameSummaryDTOResponse> search(String search, int page) {
        String term = search == null ? "" : search.trim();

        List<GameEntity> customGames = repository
                .findBySourceAndTitleContainingIgnoreCaseOrderByTitleAsc(GameSource.CUSTOM, term);

        RawgPageDTO rawgPage;
        try {
            rawgPage = rawgClient.searchGames(term, page + 1, PAGE_SIZE);
        } catch (RawgException exception) {
            log.warn("RAWG search failed: {}", exception.getMessage());
            rawgPage = RawgPageDTO.empty();
        }

        List<GameSummaryDTOResponse> content = new ArrayList<>();
        if (page == 0)
            content.addAll(toSummaries(customGames));
        content.addAll(rawgToSummaries(rawgPage.results()));

        long total = rawgPage.count() + customGames.size();
        int totalPages = (int) Math.max(1, Math.ceil((double) rawgPage.count() / PAGE_SIZE));
        return new PageDTOResponse<>(content, page, PAGE_SIZE, total, totalPages);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSummaryDTOResponse> getTrending() {
        try {
            return rawgToSummaries(rawgClient.getTrendingGames(TRENDING_SIZE).results());
        } catch (RawgException exception) {
            log.warn("RAWG trending failed: {}", exception.getMessage());
            return toSummaries(repository.findAll().stream().limit(TRENDING_SIZE).toList());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public GameDetailDTOResponse getById(Long id) {
        return GameMapper.toDetailDTO(findGame(id), getStats(id));
    }

    @Override
    @Transactional
    public GameDetailDTOResponse getOrImportByRawgId(Long rawgId) {
        GameEntity game = repository.findByRawgId(rawgId).orElseGet(() -> {
            RawgGameDTO rawg = rawgClient.getGame(rawgId)
                    .orElseThrow(() -> new GameNotFoundException("Game not found. RAWG id " + rawgId + " does not exist."));
            return repository.save(GameMapper.fromRawg(rawg));
        });
        return GameMapper.toDetailDTO(game, getStats(game.getId()));
    }

    @Override
    @Transactional
    public GameDetailDTOResponse create(GameDTORequest dto) {
        GameEntity saved = repository.save(GameMapper.toEntity(dto));
        return GameMapper.toDetailDTO(saved, getStats(saved.getId()));
    }

    @Override
    @Transactional
    public GameDetailDTOResponse update(Long id, GameDTORequest dto) {
        GameEntity game = findGame(id);
        GameMapper.updateEntity(game, dto);
        return GameMapper.toDetailDTO(repository.save(game), getStats(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        GameEntity game = findGame(id);
        reviewRepository.deleteAllByGameId(id);
        gameLogRepository.deleteAllByGameId(id);
        repository.delete(game);
    }

    RatingStatsDTOResponse getStats(Long gameId) {
        List<Long> distribution = new ArrayList<>(Collections.nCopies(10, 0L));
        long count = 0;
        long sum = 0;

        if (gameId != null) {
            for (Object[] row : reviewRepository.countByRatingForGame(gameId)) {
                int rating = ((Number) row[0]).intValue();
                long ratingCount = ((Number) row[1]).longValue();
                if (rating >= 1 && rating <= 10) {
                    distribution.set(rating - 1, ratingCount);
                    count += ratingCount;
                    sum += rating * ratingCount;
                }
            }
        }

        Double average = count == 0 ? null : Math.round((double) sum / count * 10) / 10.0;
        return new RatingStatsDTOResponse(count, average, distribution);
    }

    private GameEntity findGame(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new GameNotFoundException("Game not found. Id " + id + " does not exist."));
    }

    private List<GameSummaryDTOResponse> toSummaries(List<GameEntity> games) {
        Map<Long, Double> averages = averageRatings(games.stream().map(GameEntity::getId).toList());
        return games.stream().map(game -> GameMapper.toSummaryDTO(game, averages.get(game.getId()))).toList();
    }

    private List<GameSummaryDTOResponse> rawgToSummaries(List<RawgGameDTO> results) {
        if (results == null || results.isEmpty())
            return List.of();

        Map<Long, GameEntity> stored = repository.findByRawgIdIn(results.stream().map(RawgGameDTO::id).toList())
                .stream()
                .collect(Collectors.toMap(GameEntity::getRawgId, Function.identity()));
        Map<Long, Double> averages = averageRatings(stored.values().stream().map(GameEntity::getId).toList());

        return results.stream().map(rawg -> {
            GameEntity local = stored.get(rawg.id());
            GameSummaryDTOResponse summary = GameMapper.toSummaryDTO(rawg, local == null ? null : local.getId());
            Double communityRating = local == null ? null : averages.get(local.getId());
            if (communityRating == null)
                return summary;
            return new GameSummaryDTOResponse(summary.id(), summary.rawgId(), summary.title(), summary.coverUrl(),
                    summary.releaseDate(), summary.genres(), communityRating, summary.source());
        }).toList();
    }

    private Map<Long, Double> averageRatings(Collection<Long> gameIds) {
        Map<Long, Double> averages = new HashMap<>();
        if (gameIds.isEmpty())
            return averages;
        for (Object[] row : reviewRepository.averageRatingForGames(gameIds))
            averages.put(((Number) row[0]).longValue(), Math.round(((Number) row[1]).doubleValue() * 10) / 10.0);
        return averages;
    }

}
