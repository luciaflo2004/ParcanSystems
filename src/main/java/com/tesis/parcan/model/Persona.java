package com.tesis.parcan.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "persona")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype")
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona")
    private Long idPersona;

    @Column(name = "ci", unique = true, nullable = false, length = 20)
    @NotBlank(message = "La cédula es obligatoria")
    @Pattern(regexp = "^[0-9]{6,8}$", message = "La cédula debe tener entre 6 y 8 dígitos")
    private String ci;

    @Column(name = "nombre_persona", nullable = false, length = 100)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Column(name = "apellido_persona", nullable = false, length = 100)
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    private String apellido;

    @Column(name = "fecha_nac_persona")
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate fechaNacimiento;

    @Column(name = "direccion_persona", length = 255)
    private String direccion;

    @Column(name = "telefono_persona", length = 20)
    @Pattern(regexp = "^[0-9]{0,20}$", message = "El teléfono solo debe contener números")
    private String telefono;

    @Column(name = "dtype", insertable = false, updatable = false)
    private String dtype;
    
    // Campo para eliminación lógica
    @Column(name = "eliminado")
    private Boolean eliminado = false;
    
    @Column(name = "fecha_eliminacion")
    private LocalDate fechaEliminacion;

    // Constructores
    public Persona() {}

    public Persona(String ci, String nombre, String apellido, LocalDate fechaNacimiento,
                   String direccion, String telefono) {
        this.ci = ci;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.direccion = direccion;
        this.telefono = telefono;
        this.eliminado = false;
    }

    // Getters y Setters
    public Long getIdPersona() { return idPersona; }
    public void setIdPersona(Long idPersona) { this.idPersona = idPersona; }

    public String getCi() { return ci; }
    public void setCi(String ci) { this.ci = ci; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDtype() { return dtype; }
    public void setDtype(String dtype) { this.dtype = dtype; }
    
    public Boolean getEliminado() { return eliminado; }
    public void setEliminado(Boolean eliminado) { this.eliminado = eliminado; }
    
    public LocalDate getFechaEliminacion() { return fechaEliminacion; }
    public void setFechaEliminacion(LocalDate fechaEliminacion) { this.fechaEliminacion = fechaEliminacion; }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}