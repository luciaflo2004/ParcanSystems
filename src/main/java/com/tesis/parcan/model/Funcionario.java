package com.tesis.parcan.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "funcionario")
@PrimaryKeyJoinColumn(name = "id_persona")
public class Funcionario extends Persona {
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "rol_id", foreignKey = @ForeignKey(name = "fk_funcionario_rol"))
	private Roles roles;
    
    @ManyToOne
    @JoinColumn(name = "cargo_cod_cargo")
    private Cargo cargo;
    
    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;
    
    @Column(name = "activo")
    private Boolean activo = true;
    
    // Constructores
    public Funcionario() {
        super();
    }
    
    public Funcionario(String ci, String nombre, String apellido, LocalDate fechaNacimiento,
                       String direccion, String telefono, Cargo cargo) {
        super(ci, nombre, apellido, fechaNacimiento, direccion, telefono);
        this.cargo = cargo;
        this.fechaIngreso = LocalDate.now();
        this.activo = true;
    }
    
    // Getters y Setters
    public Cargo getCargo() {
        return cargo;
    }
    
    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }
    
    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }
    
    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

	public void setRoles(Roles roles2) {
		// TODO Auto-generated method stub
		
	}
}