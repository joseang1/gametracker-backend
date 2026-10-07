package com.josedev.gametracker.user;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.josedev.gametracker.gamelog.GameLogRepository;
import com.josedev.gametracker.gamelog.LogStatus;
import com.josedev.gametracker.review.ReviewRepository;
import com.josedev.gametracker.user.dtos.ProfileDTORequest;
import com.josedev.gametracker.user.dtos.ProfileDTOResponse;
import com.josedev.gametracker.user.exceptions.UserNotFoundException;
import com.josedev.gametracker.user.mappers.UserMapper;

@Service
public class UserServiceImpl implements InterfaceUserService {

    private final UserRepository repository;
    private final GameLogRepository gameLogRepository;
    private final ReviewRepository reviewRepository;

    public UserServiceImpl(UserRepository repository, GameLogRepository gameLogRepository,
            ReviewRepository reviewRepository) {
        this.repository = repository;
        this.gameLogRepository = gameLogRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileDTOResponse getProfile(String username) {
        return toProfile(findUser(username));
    }

    @Override
    @Transactional
    public ProfileDTOResponse updateProfile(String username, ProfileDTORequest dto) {
        UserEntity user = findUser(username);
        user.setBio(blankToNull(dto.bio()));
        user.setAvatarUrl(blankToNull(dto.avatarUrl()));
        return toProfile(repository.save(user));
    }

    private ProfileDTOResponse toProfile(UserEntity user) {
        // Every status appears, even with 0, so the frontend always gets the same keys
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (LogStatus status : LogStatus.values())
            statusCounts.put(status.name(), 0L);
        for (Object[] row : gameLogRepository.countByStatusForUser(user.getId()))
            statusCounts.put(((LogStatus) row[0]).name(), ((Number) row[1]).longValue());

        long reviewCount = reviewRepository.countByUserId(user.getId());
        Double average = reviewRepository.averageRatingForUser(user.getId());
        Double roundedAverage = average == null ? null : Math.round(average * 10) / 10.0;

        return UserMapper.toProfileDTO(user, statusCounts, reviewCount, roundedAverage);
    }

    private UserEntity findUser(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User '" + username + "' not found"));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

}
