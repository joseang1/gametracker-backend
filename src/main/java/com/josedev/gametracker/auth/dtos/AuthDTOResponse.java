package com.josedev.gametracker.auth.dtos;

import com.josedev.gametracker.user.dtos.UserDTOResponse;

public record AuthDTOResponse(String token, UserDTOResponse user) {

}
