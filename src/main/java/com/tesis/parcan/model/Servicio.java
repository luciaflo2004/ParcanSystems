package com.tesis.parcan.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "servicio_eclesial")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Long idServicio;

    @Column(name = "nombre_servicio", length = 100)
    private String nombreServicio;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "costo_servicio")
    private Integer costoServicio;   // Cambiado a Integer (mejor que int primitivo)

    @Column(name = "fecha_servicio")
    private LocalDateTime fechaServicio;

    @Column(name = "ubicacion", length = 200)
    private String ubicacion;

    // Constructores
    public Servicio() {}

    // Getters y Setters
    public Long getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(Long idServicio) {
        this.idServicio = idServicio;
    }

    public String getNombreServicio() {
        return nombreServicio;
    }

    public void setNombreServicio(String nombreServicio) {
        this.nombreServicio = nombreServicio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getCostoServicio() {
        return costoServicio;
    }

    public void setCostoServicio(Integer costoServicio) {
        this.costoServicio = costoServicio;
    }

    public LocalDateTime getFechaServicio() {
        return fechaServicio;
    }

    public void setFechaServicio(LocalDateTime fechaServicio) {
        this.fechaServicio = fechaServicio;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }
}