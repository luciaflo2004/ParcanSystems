package com.sistemparcan.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

// Cuenta de acceso al sistema; puede estar asociada a un funcionario y a un cargo
@Entity
@Table(name = "Usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdContraseña")
    private Integer idContraseña;

    @Column(name = "Usuario", length = 45, nullable = false, unique = true)
    private String usuario;

    // Versión normalizada del nombre de usuario (minúsculas y sin espacios) para garantizar unicidad
    @JsonIgnore
    @Column(name = "usuario_normalizado", length = 45)
    private String usuarioNormalizado;

    // Huella HMAC de la contraseña (para detectar reutilización sin exponer la contraseña)
    @JsonIgnore
    @Column(name = "huella_contrasena")
    private String huellaContrasena;

    @Column(name = "Contraseña", nullable = false)
    // WRITE_ONLY: se lee del request pero no se devuelve en las respuestas (no se expone la contraseña)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String contrasena;

    // Funcionario asociado a la cuenta; puede quedar sin asignar
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Funcionario_ciPersona")
    @JsonIgnoreProperties({"servicios", "solicitudes"})
    private Funcionario funcionario;

    // Cargo asociado al usuario (permite varias cuentas por funcionario con cargos distintos)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Cargo_codCargo")
    private Cargo cargo;

    @Column(name = "eliminado", nullable = false)
    private Boolean eliminado = false;

    // Roles asignados al usuario mediante la tabla Funcionario_Roles
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

    public String getUsuarioNormalizado() {
        return usuarioNormalizado;
    }

    public void setUsuarioNormalizado(String usuarioNormalizado) {
        this.usuarioNormalizado = usuarioNormalizado;
    }

    public String getHuellaContrasena() {
        return huellaContrasena;
    }

    public void setHuellaContrasena(String huellaContrasena) {
        this.huellaContrasena = huellaContrasena;
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

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
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
