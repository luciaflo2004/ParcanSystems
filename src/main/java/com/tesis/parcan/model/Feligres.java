package com.tesis.parcan.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "feligres")
@PrimaryKeyJoinColumn(name = "id_persona")
public class Feligres extends Persona {

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    @Column(name = "activo")
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institucion_eclesiastica_id", 
                foreignKey = @ForeignKey(name = "fk_feligres_institucion"))
    private InstitucionesEclesiasticas institucionesEclesiasticas;

    // Constructores
    public Feligres() {
        super();
    }

    public Feligres(String ci, String nombre, String apellido, LocalDate fechaNacimiento,
                    String direccion, String telefono, InstitucionesEclesiasticas institucion) {
        super(ci, nombre, apellido, fechaNacimiento, direccion, telefono);
        this.institucionesEclesiasticas = institucion;
        this.fechaRegistro = LocalDate.now();
        this.activo = true;
    }

    // Getters y Setters
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public InstitucionesEclesiasticas getInstitucionesEclesiasticas() {
        return institucionesEclesiasticas;
    }

    public void setInstitucionesEclesiasticas(InstitucionesEclesiasticas institucionesEclesiasticas) {
        this.institucionesEclesiasticas = institucionesEclesiasticas;
    }
}