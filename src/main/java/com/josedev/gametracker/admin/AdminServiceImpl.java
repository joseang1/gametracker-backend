package com.josedev.gametracker.admin;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.josedev.gametracker.admin.dtos.AdminStatsDTOResponse;
import com.josedev.gametracker.game.GameRepository;
import com.josedev.gametracker.game.GameSource;
import com.josedev.gametracker.gamelog.GameLogRepository;
import com.josedev.gametracker.globals.dtos.PageDTOResponse;
import com.josedev.gametracker.review.ReviewRepository;
import com.josedev.gametracker.user.UserEntity;
import com.josedev.gametracker.user.UserRepository;
import com.josedev.gametracker.user.dtos.UserAdminDTOResponse;
import com.josedev.gametracker.user.exceptions.UserModerationException;
import com.josedev.gametracker.user.exceptions.UserNotFoundException;
import com.josedev.gametracker.user.mappers.UserMapper;

@Service
public class AdminServiceImpl implements InterfaceAdminService {

    static final int PAGE_SIZE = 20;

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final ReviewRepository reviewRepository;
    private final GameLogRepository gameLogRepository;

    public AdminServiceImpl(UserRepository userRepository, GameRepository gameRepository,
            ReviewRepository reviewRepository, GameLogRepository gameLogRepository) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.reviewRepository = reviewRepository;
        this.gameLogRepository = gameLogRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStatsDTOResponse getStats() {
        return new AdminStatsDTOResponse(
                userRepository.count(),
                userRepository.countByBannedTrue(),
                gameRepository.count(),
                gameRepository.countBySource(GameSource.CUSTOM),
                reviewRepository.count(),
                gameLogRepository.count());
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTOResponse<UserAdminDTOResponse> getUsers(String search, int page) {
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("createdAt").descending());
        String term = search == null ? "" : search.trim();
        return PageDTOResponse.from(userRepository.findByUsernameContainingIgnoreCase(term, pageRequest),
                UserMapper::toAdminDTO);
    }

    @Override
    @Transactional
    public UserAdminDTOResponse setBanned(Long userId, boolean banned) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found. Id " + userId + " does not exist."));

        if (user.isAdmin())
            throw new UserModerationException("Admins cannot be banned");

        user.setBanned(banned);
        return UserMapper.toAdminDTO(userRepository.save(user));
    }

}
