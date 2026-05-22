package com.tesis.parcan.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud")
public class Solicitud {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long idSolicitud;
    
    @ManyToOne
    @JoinColumn(name = "persona_id", referencedColumnName = "id_persona")
    private Persona persona;
    
    @Column(name = "tipo_solicitud", length = 50)
    private String tipoSolicitud;
    
    @Column(name = "descripcion")
    private String descripcion;
    
    @Column(name = "estado", length = 30)
    private String estado;
    
    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;
    
    // Constructores
    public Solicitud() {}
    
    // Getters y Setters
    public Long getIdSolicitud() {
        return idSolicitud;
    }
    
    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
    }
    
    public Persona getPersona() {
        return persona;
    }
    
    public void setPersona(Persona persona) {
        this.persona = persona;
    }
    
    public String getTipoSolicitud() {
        return tipoSolicitud;
    }
    
    public void setTipoSolicitud(String tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }
    
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

	public void setServicio(Servicio servicio) {
		// TODO Auto-generated method stub
		
	}
}