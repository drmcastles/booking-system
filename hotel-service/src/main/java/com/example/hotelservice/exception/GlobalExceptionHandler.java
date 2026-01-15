package com.example.hotelservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. специфическая обработка отказа в доступе (для тестов и безопасности)
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AuthorizationDeniedException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "ERROR");
        body.put("message", "Access Denied: Insufficient permissions");
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN); // Возвращаем 403!
    }

    // 2. логика для SAGA и прочих Runtime ошибок
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();

        if ("ROOM_ALREADY_BOOKED".equals(ex.getMessage())) {
            body.put("status", "CONFLICT");
            body.put("message", "Извините, эта комната уже занята на выбранные даты.");
            return new ResponseEntity<>(body, HttpStatus.CONFLICT);
        }

        body.put("status", "ERROR");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}