package com.tesis.parcan.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
public class Roles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRol;

    private String nombre;
    private String descripcion;

    @OneToMany(mappedBy = "roles", 
               cascade = CascadeType.ALL, 
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    private List<Funcionario> funcionarios = new ArrayList<>();

    // ==================== Getters y Setters ====================

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<Funcionario> getFuncionarios() {
        return funcionarios;
    }

    public void setFuncionarios(List<Funcionario> funcionarios) {
        this.funcionarios = funcionarios;
    }

    // ==================== Métodos Helper (Muy recomendados) ====================

    public void addFuncionario(Funcionario funcionario) {
        this.funcionarios.add(funcionario);
        funcionario.setRoles(this);
    }

    public void removeFuncionario(Funcionario funcionario) {
        this.funcionarios.remove(funcionario);
        funcionario.setRoles(null);
    }
}