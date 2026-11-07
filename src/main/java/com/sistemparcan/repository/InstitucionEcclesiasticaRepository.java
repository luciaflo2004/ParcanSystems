package com.sistemparcan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemparcan.entity.InstitucionEcclesiastica;

@Repository
public interface InstitucionEcclesiasticaRepository extends JpaRepository<InstitucionEcclesiastica, Integer> {
}
