package com.josedev.gametracker.review;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.josedev.gametracker.game.GameEntity;
import com.josedev.gametracker.game.GameRepository;
import com.josedev.gametracker.game.exceptions.GameNotFoundException;
import com.josedev.gametracker.globals.dtos.PageDTOResponse;
import com.josedev.gametracker.review.dtos.ReviewDTORequest;
import com.josedev.gametracker.review.dtos.ReviewDTOResponse;
import com.josedev.gametracker.review.exceptions.ReviewNotFoundException;
import com.josedev.gametracker.review.mappers.ReviewMapper;
import com.josedev.gametracker.user.UserEntity;
import com.josedev.gametracker.user.UserRepository;
import com.josedev.gametracker.user.exceptions.UserNotFoundException;

@Service
public class ReviewServiceImpl implements InterfaceReviewService {

    static final int PAGE_SIZE = 10;

    private final ReviewRepository repository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public ReviewServiceImpl(ReviewRepository repository, UserRepository userRepository,
            GameRepository gameRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTOResponse<ReviewDTOResponse> getByGame(Long gameId, int page) {
        if (!gameRepository.existsById(gameId))
            throw new GameNotFoundException("Game not found. Id " + gameId + " does not exist.");
        return PageDTOResponse.from(repository.findByGameIdOrderByCreatedAtDesc(gameId, pageOf(page)),
                ReviewMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTOResponse<ReviewDTOResponse> getByUser(String username, int page) {
        findUser(username);
        return PageDTOResponse.from(repository.findByUserUsernameOrderByCreatedAtDesc(username, pageOf(page)),
                ReviewMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTOResponse<ReviewDTOResponse> getRecent(int page) {
        return PageDTOResponse.from(repository.findAllByOrderByCreatedAtDesc(pageOf(page)), ReviewMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewDTOResponse> getMine(String username, Long gameId) {
        UserEntity user = findUser(username);
        return repository.findByUserIdAndGameId(user.getId(), gameId).map(ReviewMapper::toDTO);
    }

    @Override
    @Transactional
    public ReviewDTOResponse save(String username, Long gameId, ReviewDTORequest dto) {
        UserEntity user = findUser(username);
        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found. Id " + gameId + " does not exist."));

        ReviewEntity review = repository.findByUserIdAndGameId(user.getId(), gameId)
                .orElseGet(() -> new ReviewEntity(user, game));
        ReviewMapper.updateEntity(review, dto);

        return ReviewMapper.toDTO(repository.save(review));
    }

    @Override
    @Transactional
    public void deleteMine(String username, Long gameId) {
        UserEntity user = findUser(username);
        ReviewEntity review = repository.findByUserIdAndGameId(user.getId(), gameId)
                .orElseThrow(() -> new ReviewNotFoundException("You have not reviewed this game"));
        repository.delete(review);
    }

    @Override
    @Transactional
    public void deleteById(Long reviewId) {
        ReviewEntity review = repository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found. Id " + reviewId + " does not exist."));
        repository.delete(review);
    }

    private UserEntity findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User '" + username + "' not found"));
    }

    private static PageRequest pageOf(int page) {
        return PageRequest.of(Math.max(page, 0), PAGE_SIZE);
    }

}
