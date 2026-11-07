package com.sistemparcan.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sistemparcan.entity.Funcionario;
import com.sistemparcan.repository.FuncionarioRepository;

@Service
public class FuncionarioService {

    private final FuncionarioRepository repository;

    public FuncionarioService(FuncionarioRepository repository) {
        this.repository = repository;
    }

    public List<Funcionario> findAll() {
        return repository.findAll();
    }

    public Optional<Funcionario> findById(Integer id) {
        return repository.findById(id);
    }

    public Funcionario save(Funcionario funcionario) {
        return repository.save(funcionario);
    }

    public void deleteById(Integer id) {
        repository.findById(id).ifPresent(f -> {
            f.setEliminado(true);
            repository.save(f);
        });
    }

    public void restoreById(Integer id) {
        repository.findById(id).ifPresent(f -> {
            f.setEliminado(false);
            repository.save(f);
        });
    }
}
