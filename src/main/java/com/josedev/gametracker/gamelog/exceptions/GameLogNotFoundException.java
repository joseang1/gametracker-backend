package com.josedev.gametracker.gamelog.exceptions;

import org.springframework.http.HttpStatus;

import com.josedev.gametracker.globals.exceptions.ApiException;

public class GameLogNotFoundException extends ApiException {

    public GameLogNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

}
