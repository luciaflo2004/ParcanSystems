package com.sistemparcan.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Solicitud")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codSolicitud")
    private Integer codSolicitud;

    @Column(name = "Detalle", length = 45, nullable = false)
    private String detalle;

    @Column(name = "FechaSolicitud")
    private LocalDate fechaSolicitud;

    @Column(name = "EstadoSolicitud", length = 45)
    private String estadoSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Feligres_ciPersona")
    private Persona feligres;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Funcionario_ciPersona")
    private Funcionario funcionario;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
        name = "Detalle_Solicitud",
        joinColumns = @JoinColumn(name = "Solicitud_codSolicitud"),
        inverseJoinColumns = @JoinColumn(name = "Servicio_codServicio")
    )
    private Set<Servicio> servicios = new HashSet<>();

    @Column(name = "eliminado", nullable = false)
    private Boolean eliminado = false;

    public Solicitud() {
    }

    public Solicitud(Integer codSolicitud, String detalle, LocalDate fechaSolicitud, String estadoSolicitud,
            Persona feligres, Funcionario funcionario, Set<Servicio> servicios, Boolean eliminado) {
        this.codSolicitud = codSolicitud;
        this.detalle = detalle;
        this.fechaSolicitud = fechaSolicitud;
        this.estadoSolicitud = estadoSolicitud;
        this.feligres = feligres;
        this.funcionario = funcionario;
        this.servicios = servicios;
        this.eliminado = eliminado;
    }

    public Integer getCodSolicitud() {
        return codSolicitud;
    }

    public void setCodSolicitud(Integer codSolicitud) {
        this.codSolicitud = codSolicitud;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getEstadoSolicitud() {
        return estadoSolicitud;
    }

    public void setEstadoSolicitud(String estadoSolicitud) {
        this.estadoSolicitud = estadoSolicitud;
    }

    public Persona getFeligres() {
        return feligres;
    }

    public void setFeligres(Persona feligres) {
        this.feligres = feligres;
    }

    public Set<Servicio> getServicios() {
        return servicios;
    }

    public void setServicios(Set<Servicio> servicios) {
        this.servicios = servicios;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
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
