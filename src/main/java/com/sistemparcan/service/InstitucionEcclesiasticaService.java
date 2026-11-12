package com.sistemparcan.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sistemparcan.entity.InstitucionEcclesiastica;
import com.sistemparcan.repository.InstitucionEcclesiasticaRepository;

// Reglas de negocio para las instituciones eclesiasticas
@Service
public class InstitucionEcclesiasticaService {

    private final InstitucionEcclesiasticaRepository repository;

    public InstitucionEcclesiasticaService(InstitucionEcclesiasticaRepository repository) {
        this.repository = repository;
    }

    // Devuelve todas las instituciones (activas e inactivas)
    public List<InstitucionEcclesiastica> findAll() {
        return repository.findAll();
    }

    // Devuelve solamente las instituciones activas para los desplegables
    public List<InstitucionEcclesiastica> findActivas() {
        return repository.findByEliminadoFalse();
    }

    // Busca una institucion por su identificador
    public Optional<InstitucionEcclesiastica> findById(Integer id) {
        return repository.findById(id);
    }

    // Guarda una institucion nueva o actualizada
    public InstitucionEcclesiastica save(InstitucionEcclesiastica institucion) {
        return repository.save(institucion);
    }

    // Desactiva la institucion sin borrarla de la base de datos
    public void deleteById(Integer id) {
        repository.findById(id).ifPresent(i -> {
            i.setEliminado(true);
            repository.save(i);
        });
    }

    // Reactiva una institucion desactivada
    public void restoreById(Integer id) {
        repository.findById(id).ifPresent(i -> {
            i.setEliminado(false);
            repository.save(i);
        });
    }
}
