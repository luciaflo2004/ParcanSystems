package com.sistemparcan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemparcan.entity.Servicio;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Integer> {
}
