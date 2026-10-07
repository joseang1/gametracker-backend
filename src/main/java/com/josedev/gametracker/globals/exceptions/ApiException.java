package com.josedev.gametracker.globals.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Base class for every business exception of the API.
 * Each feature extends it with its own exception and HTTP status,
 * so the GlobalExceptionHandler can treat all of them the same way.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

}
