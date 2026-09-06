package com.sistemparcan.service;

import com.sistemparcan.entity.Servicio;
import com.sistemparcan.repository.ServicioRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioService {

    private final ServicioRepository repository;

    public ServicioService(ServicioRepository repository) {
        this.repository = repository;
    }

    public List<Servicio> findAll() {
        return repository.findAll();
    }

    public Optional<Servicio> findById(Integer id) {
        return repository.findById(id);
    }

    public Servicio save(Servicio servicio) {
        return repository.save(servicio);
    }

    public void deleteById(Integer id) {
        repository.findById(id).ifPresent(s -> {
            s.setEliminado(true);
            repository.save(s);
        });
    }

    public void restoreById(Integer id) {
        repository.findById(id).ifPresent(s -> {
            s.setEliminado(false);
            repository.save(s);
        });
    }
}
