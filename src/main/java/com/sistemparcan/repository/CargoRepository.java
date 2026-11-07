package com.sistemparcan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemparcan.entity.Cargo;

@Repository
public interface CargoRepository extends JpaRepository<Cargo, Integer> {
}
