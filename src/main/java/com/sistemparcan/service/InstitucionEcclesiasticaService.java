package com.sistemparcan.service;

import com.sistemparcan.entity.InstitucionEcclesiastica;
import com.sistemparcan.repository.InstitucionEcclesiasticaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InstitucionEcclesiasticaService {

    private final InstitucionEcclesiasticaRepository repository;

    public InstitucionEcclesiasticaService(InstitucionEcclesiasticaRepository repository) {
        this.repository = repository;
    }

    public List<InstitucionEcclesiastica> findAll() {
        return repository.findAll();
    }

    public Optional<InstitucionEcclesiastica> findById(Integer id) {
        return repository.findById(id);
    }

    public InstitucionEcclesiastica save(InstitucionEcclesiastica institucion) {
        return repository.save(institucion);
    }

    public void deleteById(Integer id) {
        repository.findById(id).ifPresent(i -> {
            i.setEliminado(true);
            repository.save(i);
        });
    }

    public void restoreById(Integer id) {
        repository.findById(id).ifPresent(i -> {
            i.setEliminado(false);
            repository.save(i);
        });
    }
}
