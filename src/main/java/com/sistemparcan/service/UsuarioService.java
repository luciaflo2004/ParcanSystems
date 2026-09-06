package com.sistemparcan.service;

import com.sistemparcan.entity.Usuario;
import com.sistemparcan.repository.UsuarioRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public List<Usuario> findAll() {
        return repository.findAll();
    }

    public Optional<Usuario> findById(Integer id) {
        return repository.findById(id);
    }

    public Usuario save(Usuario usuario) {
        return repository.save(usuario);
    }

    public void deleteById(Integer id) {
        repository.findById(id).ifPresent(u -> {
            u.setEliminado(true);
            repository.save(u);
        });
    }

    public void restoreById(Integer id) {
        repository.findById(id).ifPresent(u -> {
            u.setEliminado(false);
            repository.save(u);
        });
    }
}
