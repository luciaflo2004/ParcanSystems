package com.sistemparcan.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

// Servicio realizado o pendiente, relacionado con una solicitud y un funcionario
@Entity
@Table(name = "Servicio")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codServicio")
    private Integer codServicio;

    @Column(name = "DetalleServicio", length = 45, nullable = false)
    private String detalleServicio;

    @Column(name = "CostoServicio")
    private Integer costoServicio;

    // Solicitud a la que pertenece este servicio (una solicitud puede tener varios servicios).
    // Se guarda el id de la solicitud existente mediante una clave foranea.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Solicitud_codSolicitud")
    @JsonIgnoreProperties({"servicios"})
    private Solicitud solicitud;

    // Funcionario responsable de realizar el servicio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Funcionario_ciPersona")
    @JsonIgnoreProperties({"servicios", "solicitudes"})
    private Funcionario funcionario;

    @Column(name = "EstadoServicio", length = 45)
    private String estadoServicio;

    // Estado como booleano: true = Realizado, false = Pendiente
    @Column(name = "realizado", nullable = true)
    private Boolean realizado = false;

    // Fecha en que se realizo el servicio
    @Column(name = "fecha_realizacion", nullable = true)
    private LocalDate fechaRealizacion;

    @Column(name = "eliminado", nullable = false)
    private Boolean eliminado = false;

    public Servicio() {
    }

    public Servicio(Integer codServicio, String detalleServicio, Integer costoServicio, Solicitud solicitud,
            Funcionario funcionario, String estadoServicio, Boolean eliminado) {
        this.codServicio = codServicio;
        this.detalleServicio = detalleServicio;
        this.costoServicio = costoServicio;
        this.solicitud = solicitud;
        this.funcionario = funcionario;
        this.estadoServicio = estadoServicio;
        this.eliminado = eliminado;
    }

    public Integer getCodServicio() {
        return codServicio;
    }

    public void setCodServicio(Integer codServicio) {
        this.codServicio = codServicio;
    }

    public String getDetalleServicio() {
        return detalleServicio;
    }

    public void setDetalleServicio(String detalleServicio) {
        this.detalleServicio = detalleServicio;
    }

    public Integer getCostoServicio() {
        return costoServicio;
    }

    public void setCostoServicio(Integer costoServicio) {
        this.costoServicio = costoServicio;
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(Solicitud solicitud) {
        this.solicitud = solicitud;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public String getEstadoServicio() {
        return estadoServicio;
    }

    public void setEstadoServicio(String estadoServicio) {
        this.estadoServicio = estadoServicio;
    }

    public Boolean getRealizado() {
        return realizado;
    }

    public void setRealizado(Boolean realizado) {
        this.realizado = realizado;
    }

    public LocalDate getFechaRealizacion() {
        return fechaRealizacion;
    }

    public void setFechaRealizacion(LocalDate fechaRealizacion) {
        this.fechaRealizacion = fechaRealizacion;
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
        if (realizado == null) {
            realizado = false;
        }
    }
}
