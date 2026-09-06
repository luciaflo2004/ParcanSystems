package com.sistemparcan.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdContraseña")
    private Integer idContraseña;

    @Column(name = "Usuario", length = 45, nullable = false, unique = true)
    private String usuario;

    @Column(name = "Contraseña", nullable = false)
    private String contrasena;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Funcionario_ciPersona")
    private Funcionario funcionario;

    @Column(name = "eliminado", nullable = false)
    private Boolean eliminado = false;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
        name = "Funcionario_Roles",
        joinColumns = @JoinColumn(name = "Usuario_IdContraseña"),
        inverseJoinColumns = @JoinColumn(name = "Roles_idRoles")
    )
    private Set<Roles> roles = new HashSet<>();

    public Usuario() {
    }

    public Usuario(Integer idContraseña, String usuario, String contrasena, Funcionario funcionario,
            Boolean eliminado, Set<Roles> roles) {
        this.idContraseña = idContraseña;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.funcionario = funcionario;
        this.eliminado = eliminado;
        this.roles = roles;
    }

    public Integer getIdContraseña() {
        return idContraseña;
    }

    public void setIdContraseña(Integer idContraseña) {
        this.idContraseña = idContraseña;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
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

    public Set<Roles> getRoles() {
        return roles;
    }

    public void setRoles(Set<Roles> roles) {
        this.roles = roles;
    }

    @PrePersist
    public void prePersist() {
        if (eliminado == null) {
            eliminado = false;
        }
    }
}
