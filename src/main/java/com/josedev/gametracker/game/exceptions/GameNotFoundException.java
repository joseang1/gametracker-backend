package com.josedev.gametracker.game.exceptions;

import org.springframework.http.HttpStatus;

import com.josedev.gametracker.globals.exceptions.ApiException;

public class GameNotFoundException extends ApiException {

    public GameNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

}
