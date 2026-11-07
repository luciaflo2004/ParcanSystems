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

import com.sistemparcan.entity.Roles;
import com.sistemparcan.service.RolesService;

@Controller
@RequestMapping("/api/roles")
public class RolesController {

    private final RolesService service;

    public RolesController(RolesService service) {
        this.service = service;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<List<Roles>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Roles> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<Roles> save(@RequestBody Roles entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Roles> update(@PathVariable Integer id, @RequestBody Roles entity) {
        return service.findById(id)
                .map(existing -> {
                    entity.setIdRoles(id);
                    // Se conserva el estado (eliminado) del registro existente
                    if (entity.getEliminado() == null) entity.setEliminado(existing.getEliminado());
                    return ResponseEntity.ok(service.save(entity));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteById(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Eliminado logicamente"));
    }

    @PostMapping("/{id}/restore")
    @ResponseBody
    public ResponseEntity<Map<String, String>> restoreById(@PathVariable Integer id) {
        service.restoreById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Restaurado correctamente"));
    }
}
