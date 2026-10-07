package com.josedev.gametracker.user.mappers;

import java.util.Map;

import com.josedev.gametracker.user.UserEntity;
import com.josedev.gametracker.user.dtos.ProfileDTOResponse;
import com.josedev.gametracker.user.dtos.UserAdminDTOResponse;
import com.josedev.gametracker.user.dtos.UserDTOResponse;

public class UserMapper {

    private UserMapper() {
    }

    public static UserDTOResponse toDTO(UserEntity entity) {
        return new UserDTOResponse(entity.getId(), entity.getUsername(), entity.getRole().name(),
                entity.getAvatarUrl());
    }

    public static UserAdminDTOResponse toAdminDTO(UserEntity entity) {
        return new UserAdminDTOResponse(entity.getId(), entity.getUsername(), entity.getEmail(),
                entity.getRole().name(), entity.isBanned(), entity.getCreatedAt());
    }

    public static ProfileDTOResponse toProfileDTO(UserEntity entity, Map<String, Long> statusCounts,
            long reviewCount, Double averageRating) {
        return new ProfileDTOResponse(entity.getId(), entity.getUsername(), entity.getRole().name(),
                entity.getBio(), entity.getAvatarUrl(), entity.getCreatedAt(), statusCounts, reviewCount,
                averageRating);
    }

}
