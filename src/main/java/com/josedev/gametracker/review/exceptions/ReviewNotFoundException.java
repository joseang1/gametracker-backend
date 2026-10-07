package com.josedev.gametracker.review.exceptions;

import org.springframework.http.HttpStatus;

import com.josedev.gametracker.globals.exceptions.ApiException;

public class ReviewNotFoundException extends ApiException {

    public ReviewNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

}
