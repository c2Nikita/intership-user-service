package com.innowise.task.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TIMESTAMP = "timestamp";
    private static final String STATUS = "status";
    private static final String ERROR = "error";
    private static final String MESSAGE = "message";
    private static final String MESSAGE_FOR_UNEXPECTED_ERROR = "An unexpected error occurred";

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP, LocalDateTime.now());
        body.put(STATUS, status.value());
        body.put(ERROR, status.getReasonPhrase());
        body.put(MESSAGE, message);

        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFound(NotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({ValidationException.class, BusinessRuleException.class})
    public ResponseEntity<Object> handleBadRequest(RuntimeException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneral(Exception exception) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, MESSAGE_FOR_UNEXPECTED_ERROR);
    }
}
