package com.josedev.gametracker.gamelog;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.josedev.gametracker.game.GameEntity;
import com.josedev.gametracker.game.GameRepository;
import com.josedev.gametracker.game.exceptions.GameNotFoundException;
import com.josedev.gametracker.gamelog.dtos.GameLogDTORequest;
import com.josedev.gametracker.gamelog.dtos.GameLogDTOResponse;
import com.josedev.gametracker.gamelog.exceptions.GameLogNotFoundException;
import com.josedev.gametracker.gamelog.mappers.GameLogMapper;
import com.josedev.gametracker.globals.dtos.PageDTOResponse;
import com.josedev.gametracker.user.UserEntity;
import com.josedev.gametracker.user.UserRepository;
import com.josedev.gametracker.user.exceptions.UserNotFoundException;

@Service
public class GameLogServiceImpl implements InterfaceGameLogService {

    static final int PAGE_SIZE = 24;

    private final GameLogRepository repository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public GameLogServiceImpl(GameLogRepository repository, UserRepository userRepository,
            GameRepository gameRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTOResponse<GameLogDTOResponse> getByUser(String username, LogStatus status, boolean favoritesOnly,
            int page) {
        findUser(username);
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), PAGE_SIZE);

        Page<GameLogEntity> logs;
        if (favoritesOnly)
            logs = repository.findByUserUsernameAndFavoriteTrueOrderByUpdatedAtDesc(username, pageRequest);
        else if (status != null)
            logs = repository.findByUserUsernameAndStatusOrderByUpdatedAtDesc(username, status, pageRequest);
        else
            logs = repository.findByUserUsernameOrderByUpdatedAtDesc(username, pageRequest);

        return PageDTOResponse.from(logs, GameLogMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GameLogDTOResponse> getMine(String username, Long gameId) {
        UserEntity user = findUser(username);
        return repository.findByUserIdAndGameId(user.getId(), gameId).map(GameLogMapper::toDTO);
    }

    @Override
    @Transactional
    public GameLogDTOResponse save(String username, Long gameId, GameLogDTORequest dto) {
        UserEntity user = findUser(username);
        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found. Id " + gameId + " does not exist."));

        GameLogEntity gameLog = repository.findByUserIdAndGameId(user.getId(), gameId)
                .orElseGet(() -> new GameLogEntity(user, game));
        GameLogMapper.updateEntity(gameLog, dto);

        return GameLogMapper.toDTO(repository.save(gameLog));
    }

    @Override
    @Transactional
    public void deleteMine(String username, Long gameId) {
        UserEntity user = findUser(username);
        GameLogEntity gameLog = repository.findByUserIdAndGameId(user.getId(), gameId)
                .orElseThrow(() -> new GameLogNotFoundException("This game is not in your library"));
        repository.delete(gameLog);
    }

    private UserEntity findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User '" + username + "' not found"));
    }

}
