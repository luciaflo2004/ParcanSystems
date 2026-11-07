package com.sistemparcan.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "Persona")
public class Persona {

    @Id
    @Column(name = "ciPersona")
    private Integer ciPersona;

    @Column(name = "NombrePersona", length = 50, nullable = false)
    private String nombrePersona;

    @Column(name = "ApellidoPersona", length = 50, nullable = false)
    private String apellidoPersona;

    @Column(name = "FechaNacPersona")
    private LocalDate fechaNacPersona;

    @Column(name = "DireccionPersona", length = 45)
    private String direccionPersona;

    @Column(name = "TelefonoPersona", length = 45)
    private String telefonoPersona;

    @Column(name = "eliminado", nullable = false)
    private Boolean eliminado = false;

    // Fecha de registro: se usa para mantener el orden de inscripcion en la lista
    @Column(name = "fecha_registro", nullable = true)
    private LocalDateTime fechaRegistro;

    // Campos transitorios (no se guardan): se usan en el registro unificado (rol y cargo)
    // WRITE_ONLY: se leen del request pero no se devuelven en las respuestas
    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String rol;
    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer cargo;

    public Persona() {
    }

    public Persona(Integer ciPersona, String nombrePersona, String apellidoPersona, LocalDate fechaNacPersona,
            String direccionPersona, String telefonoPersona, Boolean eliminado) {
        this.ciPersona = ciPersona;
        this.nombrePersona = nombrePersona;
        this.apellidoPersona = apellidoPersona;
        this.fechaNacPersona = fechaNacPersona;
        this.direccionPersona = direccionPersona;
        this.telefonoPersona = telefonoPersona;
        this.eliminado = eliminado;
    }

    public Integer getCiPersona() {
        return ciPersona;
    }

    public void setCiPersona(Integer ciPersona) {
        this.ciPersona = ciPersona;
    }

    public String getNombrePersona() {
        return nombrePersona;
    }

    public void setNombrePersona(String nombrePersona) {
        this.nombrePersona = nombrePersona;
    }

    public String getApellidoPersona() {
        return apellidoPersona;
    }

    public void setApellidoPersona(String apellidoPersona) {
        this.apellidoPersona = apellidoPersona;
    }

    public LocalDate getFechaNacPersona() {
        return fechaNacPersona;
    }

    public void setFechaNacPersona(LocalDate fechaNacPersona) {
        this.fechaNacPersona = fechaNacPersona;
    }

    public String getDireccionPersona() {
        return direccionPersona;
    }

    public void setDireccionPersona(String direccionPersona) {
        this.direccionPersona = direccionPersona;
    }

    public String getTelefonoPersona() {
        return telefonoPersona;
    }

    public void setTelefonoPersona(String telefonoPersona) {
        this.telefonoPersona = telefonoPersona;
    }

    public Boolean getEliminado() {
        return eliminado;
    }

    public void setEliminado(Boolean eliminado) {
        this.eliminado = eliminado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Integer getCargo() {
        return cargo;
    }

    public void setCargo(Integer cargo) {
        this.cargo = cargo;
    }

    @PrePersist
    public void prePersist() {
        if (eliminado == null) {
            eliminado = false;
        }
    }
}
