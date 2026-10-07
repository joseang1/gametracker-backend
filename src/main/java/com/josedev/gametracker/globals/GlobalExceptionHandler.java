package com.josedev.gametracker.globals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.josedev.gametracker.globals.dtos.ErrorDTOResponse;
import com.josedev.gametracker.globals.exceptions.ApiException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Business exceptions of every feature (not found, conflict, ...)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorDTOResponse> handleApiException(ApiException exception) {
        HttpStatus status = exception.getStatus();
        return ResponseEntity.status(status).body(ErrorDTOResponse.of(status.value(), exception.getMessage()));
    }

    // Bean validation (@Valid) - returns one message per invalid field
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTOResponse> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(ErrorDTOResponse.of(400, "Validation failed", errors));
    }

    @ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
    public ResponseEntity<ErrorDTOResponse> handleBadRequest(Exception exception) {
        return ResponseEntity.badRequest().body(ErrorDTOResponse.of(400, "Malformed request"));
    }

    // Login errors
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDTOResponse> handleBadCredentials(BadCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorDTOResponse.of(401, "Invalid username or password"));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorDTOResponse> handleLocked(LockedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorDTOResponse.of(403, "This account has been banned"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDTOResponse> handleNoResource(NoResourceFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorDTOResponse.of(404, "Resource not found"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTOResponse> handleGenericException(Exception exception) {
        log.error("Unexpected error", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDTOResponse.of(500, "Internal server error"));
    }

}
