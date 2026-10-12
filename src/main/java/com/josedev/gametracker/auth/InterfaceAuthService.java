package com.josedev.gametracker.auth;

import com.josedev.gametracker.auth.dtos.AuthDTOResponse;
import com.josedev.gametracker.auth.dtos.LoginDTORequest;
import com.josedev.gametracker.auth.dtos.RegisterDTORequest;
import com.josedev.gametracker.user.dtos.UserDTOResponse;

public interface InterfaceAuthService {

    AuthDTOResponse register(RegisterDTORequest dto);

    AuthDTOResponse login(LoginDTORequest dto);

    UserDTOResponse getCurrentUser(String username);

}
