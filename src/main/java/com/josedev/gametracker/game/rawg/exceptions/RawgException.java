package com.josedev.gametracker.game.rawg.exceptions;

import org.springframework.http.HttpStatus;

import com.josedev.gametracker.globals.exceptions.ApiException;

/** The external catalog (RAWG) failed or is not configured. */
public class RawgException extends ApiException {

    public RawgException(String message) {
        super(HttpStatus.BAD_GATEWAY, message);
    }

}
