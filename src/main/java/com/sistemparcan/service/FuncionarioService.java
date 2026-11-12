package com.sistemparcan.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemparcan.entity.Funcionario;
import com.sistemparcan.repository.FuncionarioRepository;
import com.sistemparcan.repository.PersonaRepository;

// Reglas de negocio para los funcionarios
@Service
public class FuncionarioService {

    private final FuncionarioRepository repository;
    private final PersonaRepository personaRepository;

    public FuncionarioService(FuncionarioRepository repository, PersonaRepository personaRepository) {
        this.repository = repository;
        this.personaRepository = personaRepository;
    }

    // Devuelve todos los funcionarios (activos e inactivos)
    public List<Funcionario> findAll() {
        return repository.findAll();
    }

    // Devuelve los funcionarios activos para los desplegables de nuevas operaciones
    public List<Funcionario> findActivos() {
        return repository.findByEliminadoFalseAndPersonaEliminadoFalse();
    }

    // Busca un funcionario por su cedula
    public Optional<Funcionario> findById(Integer id) {
        return repository.findById(id);
    }

    // Guarda los datos del funcionario y su cargo
    public Funcionario save(Funcionario funcionario) {
        return repository.save(funcionario);
    }

    // Desactiva el funcionario sin borrarlo de la base de datos
    public void deleteById(Integer id) {
        repository.findById(id).ifPresent(f -> {
            f.setEliminado(true);
            repository.save(f);
        });
    }

    @Transactional
    public void restoreById(Integer id) {
        repository.findById(id).ifPresent(f -> {
            f.setEliminado(false);
            repository.save(f);
            // Al reactivar el funcionario tambien se reactiva su persona,
            // para que vuelva a aparecer en los desplegables.
            personaRepository.findById(id).ifPresent(p -> {
                p.setEliminado(false);
                personaRepository.save(p);
            });
        });
    }
}
