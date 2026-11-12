package com.sistemparcan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemparcan.entity.InstitucionEcclesiastica;

// Acceso a las instituciones eclesiasticas
@Repository
public interface InstitucionEcclesiasticaRepository extends JpaRepository<InstitucionEcclesiastica, Integer> {

    // Devuelve solamente las instituciones activas
    List<InstitucionEcclesiastica> findByEliminadoFalse();
}
