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

import com.sistemparcan.entity.Servicio;
import com.sistemparcan.service.ServicioService;

// Endpoints para consultar y administrar servicios
@Controller
@RequestMapping("/api/servicios")
public class ServicioController {

    private final ServicioService service;

    public ServicioController(ServicioService service) {
        this.service = service;
    }

    // Devuelve la lista completa de servicios
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<Servicio>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // Busca un servicio por su codigo
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Servicio> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Registra un servicio nuevo
    @PostMapping
    @ResponseBody
    public ResponseEntity<Servicio> save(@RequestBody Servicio entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    // Actualiza los datos de un servicio
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Servicio> update(@PathVariable Integer id, @RequestBody Servicio entity) {
        return service.findById(id)
                .map(existing -> {
                    entity.setCodServicio(id);
                    // Se conserva el estado (eliminado) del registro existente
                    if (entity.getEliminado() == null) entity.setEliminado(existing.getEliminado());
                    return ResponseEntity.ok(service.save(entity));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Desactiva el servicio sin borrarlo de la base de datos
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteById(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Eliminado logicamente"));
    }

    // Reactiva un servicio desactivado
    @PostMapping("/{id}/restore")
    @ResponseBody
    public ResponseEntity<Map<String, String>> restoreById(@PathVariable Integer id) {
        service.restoreById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Restaurado correctamente"));
    }
}
