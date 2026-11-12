package com.sistemparcan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemparcan.entity.Funcionario;

// Acceso a los funcionarios y sus relaciones
@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Integer> {

    // Devuelve solo los funcionarios activos y con su persona activa
    List<Funcionario> findByEliminadoFalseAndPersonaEliminadoFalse();
}
