package com.sistemparcan.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sistemparcan.entity.Roles;
import com.sistemparcan.repository.RolesRepository;

// Reglas de negocio para los roles
@Service
public class RolesService {

    private final RolesRepository repository;

    public RolesService(RolesRepository repository) {
        this.repository = repository;
    }

    // Devuelve todos los roles (activos e inactivos)
    public List<Roles> findAll() {
        return repository.findAll();
    }

    // Busca un rol por su identificador
    public Optional<Roles> findById(Integer id) {
        return repository.findById(id);
    }

    // Guarda un rol nuevo o actualizado
    public Roles save(Roles roles) {
        return repository.save(roles);
    }

    // Desactiva el rol sin borrarlo de la base de datos
    public void deleteById(Integer id) {
        repository.findById(id).ifPresent(r -> {
            r.setEliminado(true);
            repository.save(r);
        });
    }

    // Reactiva un rol desactivado
    public void restoreById(Integer id) {
        repository.findById(id).ifPresent(r -> {
            r.setEliminado(false);
            repository.save(r);
        });
    }
}
