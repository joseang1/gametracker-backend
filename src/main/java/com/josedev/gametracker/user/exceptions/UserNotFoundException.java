package com.josedev.gametracker.user.exceptions;

import org.springframework.http.HttpStatus;

import com.josedev.gametracker.globals.exceptions.ApiException;

public class UserNotFoundException extends ApiException {

    public UserNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

}
