package com.sistemparcan.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

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

    // Estado como booleano: true = Aprobada, false = Pendiente
    @Column(name = "aprobada", nullable = true)
    private Boolean aprobada = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Feligres_ciPersona")
    private Persona feligres;

    // Una solicitud puede pertenecer a una institucion eclesiastica en lugar de un feligres.
    // Solo se usa una de las dos relaciones (feligres o institucion), nunca ambas a la vez.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Institucion_idInstitucionEclesiastica")
    private InstitucionEcclesiastica institucion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Funcionario_ciPersona")
    @JsonIgnoreProperties({"servicios", "solicitudes"})
    private Funcionario funcionario;

    // Una solicitud puede generar varios servicios (relacion uno a muchos).
    // El lado dueno de la relacion es Servicio.solicitud (clave foranea en la tabla Servicio).
    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("solicitud")
    private Set<Servicio> servicios = new HashSet<>();

    @Column(name = "eliminado", nullable = false)
    private Boolean eliminado = false;

    @JsonCreator
    public Solicitud() {
    }

    public Solicitud(Integer codSolicitud, String detalle, LocalDate fechaSolicitud, String estadoSolicitud,
            Persona feligres, InstitucionEcclesiastica institucion, Funcionario funcionario, Set<Servicio> servicios,
            Boolean eliminado) {
        this.codSolicitud = codSolicitud;
        this.detalle = detalle;
        this.fechaSolicitud = fechaSolicitud;
        this.estadoSolicitud = estadoSolicitud;
        this.feligres = feligres;
        this.institucion = institucion;
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

    public Boolean getAprobada() {
        return aprobada;
    }

    public void setAprobada(Boolean aprobada) {
        this.aprobada = aprobada;
    }

    public Persona getFeligres() {
        return feligres;
    }

    public void setFeligres(Persona feligres) {
        this.feligres = feligres;
    }

    public InstitucionEcclesiastica getInstitucion() {
        return institucion;
    }

    public void setInstitucion(InstitucionEcclesiastica institucion) {
        this.institucion = institucion;
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
        if (aprobada == null) {
            aprobada = false;
        }
    }
}
