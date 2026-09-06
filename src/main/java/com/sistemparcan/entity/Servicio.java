package com.sistemparcan.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Funcionario_ciPersona")
    private Funcionario funcionario;

    @Column(name = "EstadoServicio", length = 45)
    private String estadoServicio;

    @Column(name = "eliminado", nullable = false)
    private Boolean eliminado = false;

    @ManyToMany(mappedBy = "servicios", fetch = FetchType.LAZY)
    private Set<Solicitud> solicitudes = new HashSet<>();

    public Servicio() {
    }

    public Servicio(Integer codServicio, String detalleServicio, Integer costoServicio, Funcionario funcionario,
            String estadoServicio, Boolean eliminado, Set<Solicitud> solicitudes) {
        this.codServicio = codServicio;
        this.detalleServicio = detalleServicio;
        this.costoServicio = costoServicio;
        this.funcionario = funcionario;
        this.estadoServicio = estadoServicio;
        this.eliminado = eliminado;
        this.solicitudes = solicitudes;
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

    public Boolean getEliminado() {
        return eliminado;
    }

    public void setEliminado(Boolean eliminado) {
        this.eliminado = eliminado;
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
