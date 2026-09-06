package com.sistemparcan.service;

import com.sistemparcan.entity.Roles;
import com.sistemparcan.repository.RolesRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RolesService {

    private final RolesRepository repository;

    public RolesService(RolesRepository repository) {
        this.repository = repository;
    }

    public List<Roles> findAll() {
        return repository.findAll();
    }

    public Optional<Roles> findById(Integer id) {
        return repository.findById(id);
    }

    public Roles save(Roles roles) {
        return repository.save(roles);
    }

    public void deleteById(Integer id) {
        repository.findById(id).ifPresent(r -> {
            r.setEliminado(true);
            repository.save(r);
        });
    }

    public void restoreById(Integer id) {
        repository.findById(id).ifPresent(r -> {
            r.setEliminado(false);
            repository.save(r);
        });
    }
}
