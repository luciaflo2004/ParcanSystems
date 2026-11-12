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

import com.sistemparcan.entity.Solicitud;
import com.sistemparcan.service.SolicitudService;

// Endpoints para consultar y administrar solicitudes
@Controller
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private final SolicitudService service;

    public SolicitudController(SolicitudService service) {
        this.service = service;
    }

    // Devuelve la lista completa de solicitudes
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<Solicitud>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // Lista solo las solicitudes activas para los desplegables
    @GetMapping("/activas")
    @ResponseBody
    public ResponseEntity<List<Solicitud>> findActivas() {
        return ResponseEntity.ok(service.findActivas());
    }

    // Busca una solicitud por su codigo
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Solicitud> findById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Registra una solicitud nueva
    @PostMapping
    @ResponseBody
    public ResponseEntity<Solicitud> save(@RequestBody Solicitud entity) {
        return ResponseEntity.ok(service.save(entity));
    }

    // Actualiza los datos de una solicitud
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Solicitud> update(@PathVariable Integer id, @RequestBody Solicitud entity) {
        return service.findById(id)
                .map(existing -> {
                    entity.setCodSolicitud(id);
                    // Se conserva el estado (eliminado) del registro existente
                    if (entity.getEliminado() == null) entity.setEliminado(existing.getEliminado());
                    return ResponseEntity.ok(service.save(entity));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Desactiva la solicitud sin borrarla de la base de datos
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteById(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Eliminado logicamente"));
    }

    // Reactiva una solicitud desactivada
    @PostMapping("/{id}/restore")
    @ResponseBody
    public ResponseEntity<Map<String, String>> restoreById(@PathVariable Integer id) {
        service.restoreById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Restaurado correctamente"));
    }
}
