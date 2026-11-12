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

import com.sistemparcan.entity.InstitucionEcclesiastica;
import com.sistemparcan.service.InstitucionEcclesiasticaService;

// Endpoints para consultar y administrar instituciones eclesiasticas
@Controller
@RequestMapping("/api/instituciones")
public class InstitucionEcclesiasticaController {

    private final InstitucionEcclesiasticaService service;

    public InstitucionEcclesiasticaController(InstitucionEcclesiasticaService service) {
        this.service = service;
    }

    // Devuelve la lista completa de instituciones
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<InstitucionEcclesiastica>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // Lista solo las instituciones activas para los desplegables
    @GetMapping("/activas")
    @ResponseBody
    public ResponseEntity<List<InstitucionEcclesiastica>> findActivas() {
        return ResponseEntity.ok(service.findActivas());
    }

    // Busca una institucion por su identificador
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<InstitucionEcclesiastica> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Registra una institucion nueva
    @PostMapping
    @ResponseBody
    public ResponseEntity<InstitucionEcclesiastica> save(@RequestBody InstitucionEcclesiastica entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    // Actualiza los datos de una institucion
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<InstitucionEcclesiastica> update(@PathVariable Integer id, @RequestBody InstitucionEcclesiastica entity) {
        return service.findById(id)
                .map(existing -> {
                    entity.setIdInstitucionEclesiastica(id);
                    // Se conserva el estado (eliminado) del registro existente
                    if (entity.getEliminado() == null) entity.setEliminado(existing.getEliminado());
                    return ResponseEntity.ok(service.save(entity));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Desactiva la institucion sin borrarla de la base de datos
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteById(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Eliminado logicamente"));
    }

    // Reactiva una institucion desactivada
    @PostMapping("/{id}/restore")
    @ResponseBody
    public ResponseEntity<Map<String, String>> restoreById(@PathVariable Integer id) {
        service.restoreById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Restaurado correctamente"));
    }
}
