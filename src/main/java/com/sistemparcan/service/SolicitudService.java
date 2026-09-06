package com.sistemparcan.service;

import com.sistemparcan.entity.Solicitud;
import com.sistemparcan.repository.SolicitudRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SolicitudService {

    private final SolicitudRepository repository;

    public SolicitudService(SolicitudRepository repository) {
        this.repository = repository;
    }

    public List<Solicitud> findAll() {
        return repository.findAll();
    }

    public Optional<Solicitud> findById(Integer id) {
        return repository.findById(id);
    }

    public Solicitud save(Solicitud solicitud) {
        return repository.save(solicitud);
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
