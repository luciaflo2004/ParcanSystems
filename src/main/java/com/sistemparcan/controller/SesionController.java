package com.sistemparcan.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sistemparcan.service.UsuarioService;

// Endpoints con datos basicos de la sesion autenticada
@RestController
@RequestMapping("/api/sesion")
public class SesionController {

    private final UsuarioService usuarioService;

    public SesionController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Devuelve el primer nombre y las iniciales del funcionario autenticado.
     *
     * @param authentication datos de la sesion que entrega Spring Security
     * @return nombre e iniciales; ambos vacios si la cuenta no tiene funcionario
     */
    @GetMapping("/saludo")
    public ResponseEntity<Map<String, String>> saludo(Authentication authentication) {
        Map<String, String> datos = authentication == null
                ? Map.of("nombre", "", "iniciales", "")
                : usuarioService.obtenerSaludoFuncionario(authentication.getName());
        // Si no hay funcionario asociado se devuelven valores vacios
        return ResponseEntity.ok(datos);
    }
}
