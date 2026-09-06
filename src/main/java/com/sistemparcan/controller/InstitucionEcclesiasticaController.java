package com.sistemparcan.controller;

import com.sistemparcan.entity.InstitucionEcclesiastica;
import com.sistemparcan.service.InstitucionEcclesiasticaService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/instituciones")
public class InstitucionEcclesiasticaController {

    private final InstitucionEcclesiasticaService service;

    public InstitucionEcclesiasticaController(InstitucionEcclesiasticaService service) {
        this.service = service;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<List<InstitucionEcclesiastica>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<InstitucionEcclesiastica> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<InstitucionEcclesiastica> save(@RequestBody InstitucionEcclesiastica entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<InstitucionEcclesiastica> update(@PathVariable Integer id, @RequestBody InstitucionEcclesiastica entity) {
        return service.findById(id)
                .map(existing -> {
                    entity.setIdInstitucionEclesiastica(id);
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
