package com.josedev.gametracker.user.exceptions;

import org.springframework.http.HttpStatus;

import com.josedev.gametracker.globals.exceptions.ApiException;

/** Thrown when an admin tries a forbidden moderation action, like banning another admin. */
public class UserModerationException extends ApiException {

    public UserModerationException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }

}
