package com.josedev.gametracker.globals.dtos;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorDTOResponse(
        int status,
        String message,
        Map<String, String> errors,
        LocalDateTime timestamp) {

    public static ErrorDTOResponse of(int status, String message) {
        return new ErrorDTOResponse(status, message, Map.of(), LocalDateTime.now());
    }

    public static ErrorDTOResponse of(int status, String message, Map<String, String> errors) {
        return new ErrorDTOResponse(status, message, errors, LocalDateTime.now());
    }

}
