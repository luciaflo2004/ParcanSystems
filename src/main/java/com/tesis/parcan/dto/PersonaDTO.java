package com.tesis.parcan.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class PersonaDTO {
    
    private Long idPersona;
    
    @NotBlank(message = "La cédula es obligatoria")
    @Pattern(regexp = "^[0-9]{6,8}$", message = "La cédula debe tener entre 6 y 8 dígitos")
    private String ci;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    private String apellido;
    
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate fechaNacimiento;
    
    private String direccion;
    
    @Pattern(regexp = "^[0-9]{0,20}$", message = "El teléfono solo debe contener números")
    private String telefono;
    
    private String tipo;
    
    // Nuevos campos para eliminación lógica
    private Boolean eliminado = false;
    private LocalDate fechaEliminacion;
    
    public PersonaDTO() {}
    
    public PersonaDTO(String ci, String nombre, String apellido, LocalDate fechaNacimiento, String direccion, String telefono) {
        this.ci = ci;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.direccion = direccion;
        this.telefono = telefono;
        this.eliminado = false;
    }
    
    public PersonaDTO(Long idPersona, String ci, String nombre, String apellido, LocalDate fechaNacimiento, String direccion, String telefono) {
        this.idPersona = idPersona;
        this.ci = ci;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.direccion = direccion;
        this.telefono = telefono;
        this.eliminado = false;
    }
    
    // Getters
    public Long getIdPersona() { return idPersona; }
    public String getCi() { return ci; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public String getTipo() { return tipo; }
    public Boolean getEliminado() { return eliminado; }
    public LocalDate getFechaEliminacion() { return fechaEliminacion; }
    
    // Setters
    public void setIdPersona(Long idPersona) { this.idPersona = idPersona; }
    public void setCi(String ci) { this.ci = ci; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setEliminado(Boolean eliminado) { this.eliminado = eliminado; }
    public void setFechaEliminacion(LocalDate fechaEliminacion) { this.fechaEliminacion = fechaEliminacion; }
    
    public String getNombreCompleto() { return nombre + " " + apellido; }
}