package com.sistemparcan.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Cargo")
public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codCargo")
    private Integer codCargo;

    @Column(name = "Descripcion", length = 45, nullable = false)
    private String descripcion;

    @Column(name = "eliminado", nullable = false)
    private Boolean eliminado = false;

    public Cargo() {
    }

    public Cargo(Integer codCargo, String descripcion, Boolean eliminado) {
        this.codCargo = codCargo;
        this.descripcion = descripcion;
        this.eliminado = eliminado;
    }

    public Integer getCodCargo() {
        return codCargo;
    }

    public void setCodCargo(Integer codCargo) {
        this.codCargo = codCargo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getEliminado() {
        return eliminado;
    }

    public void setEliminado(Boolean eliminado) {
        this.eliminado = eliminado;
    }

    @PrePersist
    public void prePersist() {
        if (eliminado == null) {
            eliminado = false;
        }
    }
}
