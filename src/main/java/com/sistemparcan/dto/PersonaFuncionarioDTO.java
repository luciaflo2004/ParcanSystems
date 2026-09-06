package com.sistemparcan.dto;

import java.time.LocalDate;

/**
 * DTO para el registro unificado de una Persona y, opcionalmente, un Funcionario.
 * El campo "rol" indica si la persona es solo PERSONA o tambien FUNCIONARIO.
 * El campo "cargo" (codCargo) solo se usa cuando rol = FUNCIONARIO.
 */
public class PersonaFuncionarioDTO {

    private Integer ciPersona;
    private String nombrePersona;
    private String apellidoPersona;
    private LocalDate fechaNacPersona;
    private String direccionPersona;
    private String telefonoPersona;
    private String rol;        // "PERSONA" o "FUNCIONARIO"
    private Integer cargo;     // codCargo, opcional

    public PersonaFuncionarioDTO() {
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
}
