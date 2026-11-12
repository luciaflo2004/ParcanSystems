package com.sistemparcan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemparcan.entity.Persona;

// Acceso a las personas registradas
@Repository
public interface PersonaRepository extends JpaRepository<Persona, Integer> {
    // Devuelve las personas ordenadas por fecha de registro (orden de inscripcion)
    List<Persona> findAllByOrderByFechaRegistroAsc();

    // Devuelve solamente las personas activas
    List<Persona> findByEliminadoFalseOrderByFechaRegistroAsc();
}
