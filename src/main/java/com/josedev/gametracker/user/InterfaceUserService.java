package com.josedev.gametracker.user;

import com.josedev.gametracker.user.dtos.ProfileDTORequest;
import com.josedev.gametracker.user.dtos.ProfileDTOResponse;

public interface InterfaceUserService {

    ProfileDTOResponse getProfile(String username);

    ProfileDTOResponse updateProfile(String username, ProfileDTORequest dto);

}
