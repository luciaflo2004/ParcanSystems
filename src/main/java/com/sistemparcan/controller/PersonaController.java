package com.sistemparcan.controller;

import com.sistemparcan.entity.Persona;
import com.sistemparcan.service.PersonaService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Endpoints para consultar y administrar personas y funcionarios
@Controller
@RequestMapping("/api/personas")
public class PersonaController {

    private final PersonaService service;

    public PersonaController(PersonaService service) {
        this.service = service;
    }

    // Devuelve la lista completa de personas
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<Persona>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // Lista solo las personas activas para los desplegables
    @GetMapping("/activas")
    @ResponseBody
    public ResponseEntity<List<Persona>> findActivas() {
        return ResponseEntity.ok(service.findActivas());
    }

    // Busca una persona por su cedula
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Persona> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Guarda una persona sin pasar por el registro unificado
    @PostMapping
    @ResponseBody
    public ResponseEntity<Persona> save(@RequestBody Persona entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    /**
     * Registro unificado: crea una Persona y, si el rol es FUNCIONARIO,
     * tambien crea el Funcionario asociado.
     */
    @PostMapping("/registro")
    @ResponseBody
    public ResponseEntity<Persona> registrar(@RequestBody Persona entity) {
        return ResponseEntity.ok(service.registrar(entity));
    }

    /**
     * Actualizacion unificada: actualiza una Persona existente y, si el rol es FUNCIONARIO,
     * tambien actualiza el Funcionario asociado. Permite cambiar la cedula.
     */
    @PutMapping("/registro/{id}")
    @ResponseBody
    public ResponseEntity<Persona> actualizar(@PathVariable Integer id, @RequestBody Persona entity) {
        return ResponseEntity.ok(service.actualizar(id, entity));
    }

    // Actualiza una persona sin pasar por el registro unificado
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Persona> update(@PathVariable Integer id, @RequestBody Persona entity) {
        return service.findById(id)
                .map(existing -> {
                    entity.setCiPersona(id);
                    // Se conserva el estado (eliminado) del registro existente
                    if (entity.getEliminado() == null) entity.setEliminado(existing.getEliminado());
                    return ResponseEntity.ok(service.save(entity));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Desactiva la persona sin borrarla de la base de datos
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteById(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Eliminado logicamente"));
    }

    // Reactiva la persona y su funcionario si existe
    @PostMapping("/{id}/restore")
    @ResponseBody
    public ResponseEntity<Map<String, String>> restoreById(@PathVariable Integer id) {
        service.restoreById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Restaurado correctamente"));
    }
}
