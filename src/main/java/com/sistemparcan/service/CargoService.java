package com.sistemparcan.service;

import com.sistemparcan.entity.Cargo;
import com.sistemparcan.repository.CargoRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CargoService {

    private final CargoRepository repository;

    public CargoService(CargoRepository repository) {
        this.repository = repository;
    }

    public List<Cargo> findAll() {
        return repository.findAll();
    }

    public Optional<Cargo> findById(Integer id) {
        return repository.findById(id);
    }

    public Cargo save(Cargo cargo) {
        return repository.save(cargo);
    }

    public void deleteById(Integer id) {
        repository.findById(id).ifPresent(c -> {
            c.setEliminado(true);
            repository.save(c);
        });
    }

    public void restoreById(Integer id) {
        repository.findById(id).ifPresent(c -> {
            c.setEliminado(false);
            repository.save(c);
        });
    }
}
