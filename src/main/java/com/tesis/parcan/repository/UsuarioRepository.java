package com.tesis.parcan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tesis.parcan.model.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    // Método personalizado para buscar por el campo 'usuario' de tu tabla
    Optional<Usuario> findByUsuario(String usuario);
}
