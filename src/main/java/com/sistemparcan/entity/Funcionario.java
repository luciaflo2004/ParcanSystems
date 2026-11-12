package com.sistemparcan.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

// Funcionario de la parroquia; comparte la cedula con la persona que lo representa
@Entity
@Table(name = "Funcionario")
public class Funcionario {

    @Id
    @Column(name = "ciPersona")
    private Integer ciPersona;

    // Cargo que desempena el funcionario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Cargo_codCargo")
    private Cargo cargo;

    @Column(name = "eliminado", nullable = false)
    private Boolean eliminado = false;

    // Persona que contiene los datos personales del funcionario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Persona_ciPersona", referencedColumnName = "ciPersona")
    private Persona persona;

    // Servicios asignados a este funcionario
    @OneToMany(mappedBy = "funcionario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("funcionario")
    private Set<Servicio> servicios = new HashSet<>();

    // Solicitudes registradas por este funcionario
    @OneToMany(mappedBy = "funcionario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("funcionario")
    private Set<Solicitud> solicitudes = new HashSet<>();

    // Constructor vacio para que Jackson pueda convertir el JSON recibido
    @JsonCreator
    public Funcionario() {
    }

    public Funcionario(Integer ciPersona, Cargo cargo, Boolean eliminado, Persona persona,
            Set<Servicio> servicios, Set<Solicitud> solicitudes) {
        this.ciPersona = ciPersona;
        this.cargo = cargo;
        this.eliminado = eliminado;
        this.persona = persona;
        this.servicios = servicios;
        this.solicitudes = solicitudes;
    }

    public Integer getCiPersona() {
        return ciPersona;
    }

    public void setCiPersona(Integer ciPersona) {
        this.ciPersona = ciPersona;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    public Boolean getEliminado() {
        return eliminado;
    }

    public void setEliminado(Boolean eliminado) {
        this.eliminado = eliminado;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public Set<Servicio> getServicios() {
        return servicios;
    }

    public void setServicios(Set<Servicio> servicios) {
        this.servicios = servicios;
    }

    public Set<Solicitud> getSolicitudes() {
        return solicitudes;
    }

    public void setSolicitudes(Set<Solicitud> solicitudes) {
        this.solicitudes = solicitudes;
    }

    @PrePersist
    public void prePersist() {
        if (eliminado == null) {
            eliminado = false;
        }
    }
}
