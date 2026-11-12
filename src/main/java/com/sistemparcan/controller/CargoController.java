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

import com.sistemparcan.entity.Cargo;
import com.sistemparcan.service.CargoService;

// Endpoints para consultar y administrar los cargos
@Controller
@RequestMapping("/api/cargos")
public class CargoController {

    private final CargoService service;

    public CargoController(CargoService service) {
        this.service = service;
    }

    // Devuelve la lista completa de cargos
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<Cargo>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // Lista solo los cargos activos para los desplegables
    @GetMapping("/activos")
    @ResponseBody
    public ResponseEntity<List<Cargo>> findActivos() {
        return ResponseEntity.ok(service.findActivos());
    }

    // Busca un cargo por su codigo
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Cargo> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Registra un cargo nuevo
    @PostMapping
    @ResponseBody
    public ResponseEntity<Cargo> save(@RequestBody Cargo entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    // Actualiza un cargo existente
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Cargo> update(@PathVariable Integer id, @RequestBody Cargo entity) {
        return service.findById(id)
                .map(existing -> {
                    entity.setCodCargo(id);
                    // Se conserva el estado (eliminado) del registro existente
                    if (entity.getEliminado() == null) entity.setEliminado(existing.getEliminado());
                    return ResponseEntity.ok(service.save(entity));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Desactiva el cargo sin borrarlo de la base de datos
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteById(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Eliminado logicamente"));
    }

    // Reactiva un cargo desactivado
    @PostMapping("/{id}/restore")
    @ResponseBody
    public ResponseEntity<Map<String, String>> restoreById(@PathVariable Integer id) {
        service.restoreById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Restaurado correctamente"));
    }
}
