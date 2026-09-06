package com.sistemparcan.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Roles")
public class Roles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRoles")
    private Integer idRoles;

    @Column(name = "DescripcionRoll", columnDefinition = "TEXT")
    private String descripcionRoll;

    @Column(name = "eliminado", nullable = false)
    private Boolean eliminado = false;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<Usuario> usuarios = new HashSet<>();

    public Roles() {
    }

    public Roles(Integer idRoles, String descripcionRoll, Boolean eliminado, Set<Usuario> usuarios) {
        this.idRoles = idRoles;
        this.descripcionRoll = descripcionRoll;
        this.eliminado = eliminado;
        this.usuarios = usuarios;
    }

    public Integer getIdRoles() {
        return idRoles;
    }

    public void setIdRoles(Integer idRoles) {
        this.idRoles = idRoles;
    }

    public String getDescripcionRoll() {
        return descripcionRoll;
    }

    public void setDescripcionRoll(String descripcionRoll) {
        this.descripcionRoll = descripcionRoll;
    }

    public Boolean getEliminado() {
        return eliminado;
    }

    public void setEliminado(Boolean eliminado) {
        this.eliminado = eliminado;
    }

    public Set<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(Set<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    @PrePersist
    public void prePersist() {
        if (eliminado == null) {
            eliminado = false;
        }
    }
}
