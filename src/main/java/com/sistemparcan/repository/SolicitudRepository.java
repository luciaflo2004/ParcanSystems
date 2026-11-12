package com.sistemparcan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemparcan.entity.Solicitud;

// Acceso a las solicitudes registradas
@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Integer> {

    // Devuelve solamente las solicitudes activas
    List<Solicitud> findByEliminadoFalse();
}
