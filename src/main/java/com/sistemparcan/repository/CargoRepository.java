package com.sistemparcan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemparcan.entity.Cargo;

// Acceso a los cargos guardados en la base de datos
@Repository
public interface CargoRepository extends JpaRepository<Cargo, Integer> {

    // Devuelve solamente los cargos activos
    List<Cargo> findByEliminadoFalse();
}
