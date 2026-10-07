package com.josedev.gametracker.admin.dtos;

public record AdminStatsDTOResponse(
        long users,
        long bannedUsers,
        long games,
        long customGames,
        long reviews,
        long logs) {

}
