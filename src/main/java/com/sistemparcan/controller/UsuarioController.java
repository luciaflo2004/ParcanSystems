package com.sistemparcan.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sistemparcan.entity.Usuario;
import com.sistemparcan.service.UsuarioService;

// Endpoints para consultar y administrar usuarios
@Controller
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    // Devuelve la lista completa de usuarios
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<Usuario>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // Busca un usuario por su identificador
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Usuario> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Registra un usuario nuevo
    @PostMapping
    @ResponseBody
    public ResponseEntity<Usuario> save(@RequestBody Usuario entity) {
        // Se valida la contraseña nueva (obligatoria y con requisitos de seguridad)
        service.validarContrasena(entity.getContrasena(), entity.getUsuario());
        return ResponseEntity.ok(service.save(entity));
    }

    // Actualiza un usuario; conserva la contraseña si no se envia una nueva
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Usuario> update(@PathVariable Integer id, @RequestBody Usuario entity) {
        return service.findById(id)
                .map(existing -> {
                    entity.setIdContraseña(id);
                    // Se conserva el estado (eliminado) del registro existente
                    if (entity.getEliminado() == null) entity.setEliminado(existing.getEliminado());
                    // Si se envía una contraseña nueva, se valida; si no, se conserva la existente
                    if (entity.getContrasena() != null && !entity.getContrasena().trim().isEmpty()) {
                        service.validarContrasena(entity.getContrasena(), entity.getUsuario());
                    } else {
                        entity.setContrasena(existing.getContrasena());
                    }
                    return ResponseEntity.ok(service.save(entity));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Desactiva el usuario sin borrarlo de la base de datos
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteById(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Eliminado logicamente"));
    }

    // Reactiva un usuario desactivado
    @PostMapping("/{id}/restore")
    @ResponseBody
    public ResponseEntity<Map<String, String>> restoreById(@PathVariable Integer id) {
        service.restoreById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Restaurado correctamente"));
    }
}
