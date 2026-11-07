package com.sistemparcan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemparcan.entity.Roles;

@Repository
public interface RolesRepository extends JpaRepository<Roles, Integer> {
}
