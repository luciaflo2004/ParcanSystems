package com.tesis.parcan.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "instituciones_eclesiasticas")
public class InstitucionesEclesiasticas {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_institucion_eclesiastica")
	private Integer idInstitucionEclesiastica;

	private String nombre;
	private String tipo;
	private String diosesis;
	private String ciudad;

	@OneToMany(mappedBy = "institucionesEclesiasticas", cascade = CascadeType.ALL, orphanRemoval = true,
			fetch = FetchType.LAZY) // LAZY está bien, pero debes cargar los datos antes de la vista
	private List<Feligres> feligreses = new ArrayList<>();

	// ==================== Getters y Setters ====================

	public Integer getIdInstitucionEclesiastica() {
		return idInstitucionEclesiastica;
	}

	public void setIdInstitucionEclesiastica(Integer idInstitucionEclesiastica) {
		this.idInstitucionEclesiastica = idInstitucionEclesiastica;
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

	public String getDiosesis() {
		return diosesis;
	}

	public void setDiosesis(String diosesis) {
		this.diosesis = diosesis;
	}

	public String getCiudad() {
		return ciudad;
	}

	public void setCiudad(String ciudad) {
		this.ciudad = ciudad;
	}

	public List<Feligres> getFeligreses() {
		return feligreses;
	}

	public void setFeligreses(List<Feligres> feligreses) {
		this.feligreses = feligreses;
	}

	// ==================== Métodos Helper (Recomendados) ====================

	public void addFeligres(Feligres feligres) {
		this.feligreses.add(feligres);
		feligres.setInstitucionesEclesiasticas(null);
	}

	public void removeFeligres(Feligres feligres) {
		this.feligreses.remove(feligres);
		feligres.setInstitucionesEclesiasticas(null);
	}
}