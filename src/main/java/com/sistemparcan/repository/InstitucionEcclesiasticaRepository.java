package com.sistemparcan.repository;

import com.sistemparcan.entity.InstitucionEcclesiastica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstitucionEcclesiasticaRepository extends JpaRepository<InstitucionEcclesiastica, Integer> {
}
