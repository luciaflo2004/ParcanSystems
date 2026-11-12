package com.sistemparcan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemparcan.entity.Usuario;

// Acceso a los usuarios y sus consultas de seguridad
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Busca un usuario por su nombre normalizado (minúsculas y sin espacios)
    Optional<Usuario> findByUsuarioNormalizado(String usuarioNormalizado);

    // Busca usuarios que comparten la misma huella de contraseña (para detectar reutilización)
    List<Usuario> findByHuellaContrasena(String huellaContrasena);

    // Busca usuarios con la misma combinación funcionario + cargo
    List<Usuario> findByFuncionarioCiPersonaAndCargoCodCargo(Integer ciPersona, Integer codCargo);
}
