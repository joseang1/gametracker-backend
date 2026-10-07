package com.josedev.gametracker.admin;

import com.josedev.gametracker.admin.dtos.AdminStatsDTOResponse;
import com.josedev.gametracker.globals.dtos.PageDTOResponse;
import com.josedev.gametracker.user.dtos.UserAdminDTOResponse;

public interface InterfaceAdminService {

    AdminStatsDTOResponse getStats();

    PageDTOResponse<UserAdminDTOResponse> getUsers(String search, int page);

    UserAdminDTOResponse setBanned(Long userId, boolean banned);

}
