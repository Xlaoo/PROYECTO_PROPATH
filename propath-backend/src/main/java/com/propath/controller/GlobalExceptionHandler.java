package com.propath.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        String mensaje = ex.getMessage();
        if (mensaje != null && mensaje.toLowerCase().contains("no encontrad")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", mensaje));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", mensaje != null ? mensaje : "Solicitud inválida."));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage() != null ? ex.getMessage() : "Conflicto en la solicitud."));
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(org.springframework.web.server.ResponseStatusException ex) {
        String razon = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", razon));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error interno en el servidor: " + ex.getMessage()));
    }
}
