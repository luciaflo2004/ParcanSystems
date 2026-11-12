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

import com.sistemparcan.entity.Funcionario;
import com.sistemparcan.service.FuncionarioService;

// Endpoints para consultar y administrar funcionarios
@Controller
@RequestMapping("/api/funcionarios")
public class FuncionarioController {

    private final FuncionarioService service;

    public FuncionarioController(FuncionarioService service) {
        this.service = service;
    }

    // Devuelve la lista completa de funcionarios
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<Funcionario>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // Lista solo los funcionarios activos para los desplegables
    @GetMapping("/activos")
    @ResponseBody
    public ResponseEntity<List<Funcionario>> findActivos() {
        return ResponseEntity.ok(service.findActivos());
    }

    // Busca un funcionario por su cedula
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Funcionario> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Registra un funcionario nuevo
    @PostMapping
    @ResponseBody
    public ResponseEntity<Funcionario> save(@RequestBody Funcionario entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    // Actualiza los datos de un funcionario
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Funcionario> update(@PathVariable Integer id, @RequestBody Funcionario entity) {
        return service.findById(id)
                .map(existing -> {
                    entity.setCiPersona(id);
                    // Se conserva el estado (eliminado) del registro existente
                    if (entity.getEliminado() == null) entity.setEliminado(existing.getEliminado());
                    return ResponseEntity.ok(service.save(entity));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Desactiva el funcionario sin eliminarlo de la base de datos
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteById(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Eliminado logicamente"));
    }

    // Reactiva el funcionario y su persona para que vuelva a estar disponible
    @PostMapping("/{id}/restore")
    @ResponseBody
    public ResponseEntity<Map<String, String>> restoreById(@PathVariable Integer id) {
        service.restoreById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Restaurado correctamente"));
    }
}
