package com.sistemparcan.config;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

// Devuelve al frontend el mensaje de las validaciones de negocio
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Conserva el estado HTTP y el motivo del error para mostrarlo junto al campo
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> manejarResponseStatus(ResponseStatusException ex) {
        String mensaje = ex.getReason() != null
                ? ex.getReason()
                : "No se pudo completar la operación";
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message", mensaje));
    }

    // Devuelve un mensaje claro cuando el JSON trae una fecha con formato incorrecto
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> manejarJsonInvalido(HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        String ruta = request.getRequestURI();
        String mensaje;
        if (ruta.contains("/api/solicitudes")) {
            mensaje = "Fecha inválida. Ingrese una fecha de solicitud válida.";
        } else if (ruta.contains("/api/servicios")) {
            mensaje = "Fecha inválida. Ingrese una fecha de realización válida.";
        } else {
            mensaje = "Los datos enviados no son válidos.";
        }
        return ResponseEntity.badRequest().body(Map.of("message", mensaje));
    }
}
