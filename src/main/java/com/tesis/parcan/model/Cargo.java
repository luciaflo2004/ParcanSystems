package com.tesis.parcan.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "cargo")
public class Cargo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_cargo")
    private Long codCargo;
    
    @Column(name = "nombre_cargo", length = 100)
    private String nombreCargo;
    
    @Column(name = "descripcion")
    private String descripcion;
    
    @OneToMany(mappedBy = "cargo")
    private List<Funcionario> funcionarios;
    
    // Constructores
    public Cargo() {}
    
    public Cargo(String nombreCargo, String descripcion) {
        this.nombreCargo = nombreCargo;
        this.descripcion = descripcion;
    }
    
    // Getters y Setters
    public Long getCodCargo() {
        return codCargo;
    }
    
    public void setCodCargo(Long codCargo) {
        this.codCargo = codCargo;
    }
    
    public String getNombreCargo() {
        return nombreCargo;
    }
    
    public void setNombreCargo(String nombreCargo) {
        this.nombreCargo = nombreCargo;
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
}