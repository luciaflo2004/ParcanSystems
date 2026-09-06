package com.sistemparcan.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Instituciones_Eclesiasticas")
public class InstitucionEcclesiastica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdInstitucionEclesiastica")
    private Integer idInstitucionEclesiastica;

    @Column(name = "Diosesis", length = 45)
    private String diocese;

    @Column(name = "Nombre", length = 45, nullable = false)
    private String nombre;

    @Column(name = "Tipo", length = 45)
    private String tipo;

    @Column(name = "Ciudad", length = 45)
    private String ciudad;

    @Column(name = "eliminado", nullable = false)
    private Boolean eliminado = false;

    public InstitucionEcclesiastica() {
    }

    public InstitucionEcclesiastica(Integer idInstitucionEclesiastica, String diocese, String nombre,
            String tipo, String ciudad, Boolean eliminado) {
        this.idInstitucionEclesiastica = idInstitucionEclesiastica;
        this.diocese = diocese;
        this.nombre = nombre;
        this.tipo = tipo;
        this.ciudad = ciudad;
        this.eliminado = eliminado;
    }

    public Integer getIdInstitucionEclesiastica() {
        return idInstitucionEclesiastica;
    }

    public void setIdInstitucionEclesiastica(Integer idInstitucionEclesiastica) {
        this.idInstitucionEclesiastica = idInstitucionEclesiastica;
    }

    public String getDiocese() {
        return diocese;
    }

    public void setDiocese(String diocese) {
        this.diocese = diocese;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
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
