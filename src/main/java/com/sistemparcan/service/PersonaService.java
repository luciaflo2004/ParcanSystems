package com.sistemparcan.service;

import com.sistemparcan.entity.Persona;
import com.sistemparcan.repository.PersonaRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PersonaService {

    private final PersonaRepository repository;

    public PersonaService(PersonaRepository repository) {
        this.repository = repository;
    }

    public List<Persona> findAll() {
        return repository.findAll();
    }

    public Optional<Persona> findById(Integer id) {
        return repository.findById(id);
    }

    public Persona save(Persona persona) {
        return repository.save(persona);
    }

    public void deleteById(Integer id) {
        repository.findById(id).ifPresent(p -> {
            p.setEliminado(true);
            repository.save(p);
        });
    }

    public void restoreById(Integer id) {
        repository.findById(id).ifPresent(p -> {
            p.setEliminado(false);
            repository.save(p);
        });
    }
}
