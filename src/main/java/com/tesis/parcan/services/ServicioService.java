package com.tesis.parcan.services;

import com.tesis.parcan.model.Servicio;
import com.tesis.parcan.repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ServicioService {

	@Autowired
	private ServicioRepository servicioRepository;

	// Listar todos los servicios
	public List<Servicio> listarTodos() {
		return servicioRepository.findAll();
	}

	// Listar servicios por lista de IDs (usado en solicitudes)
	public List<Servicio> listarPorIds(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return List.of();
		}
		return servicioRepository.findAllById(ids);
	}

	// Buscar por ID
	public Servicio buscarPorId(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("El ID no puede ser nulo");
		}
		return servicioRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
	}

	// Guardar nuevo servicio
	@Transactional
	public Servicio guardar(Servicio servicio) {
		// Validaciones
		if (servicio.getNombreServicio() == null || servicio.getNombreServicio().trim().isEmpty()) {
			throw new IllegalArgumentException("❌ El nombre del servicio es obligatorio");
		}

		// El costo es opcional ahora, solo validar si no es nulo
		if (servicio.getCostoServicio() != null && servicio.getCostoServicio() < 0) {
			throw new IllegalArgumentException("❌ El costo del servicio no puede ser negativo");
		}

		return servicioRepository.save(servicio);
	}

	// Actualizar servicio
	@Transactional
	public Servicio actualizar(Servicio servicio) {
		if (servicio.getIdServicio() == null) {
			throw new IllegalArgumentException("El ID del servicio no puede ser nulo");
		}

		if (!servicioRepository.existsById(servicio.getIdServicio())) {
			throw new RuntimeException("❌ Servicio no encontrado con ID: " + servicio.getIdServicio());
		}

		// Validaciones
		if (servicio.getNombreServicio() == null || servicio.getNombreServicio().trim().isEmpty()) {
			throw new IllegalArgumentException("❌ El nombre del servicio es obligatorio");
		}

		return servicioRepository.save(servicio);
	}

	// Eliminar servicio
	@Transactional
	public void eliminar(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("El ID no puede ser nulo");
		}

		if (!servicioRepository.existsById(id)) {
			throw new RuntimeException("❌ Servicio no encontrado con ID: " + id);
		}
		servicioRepository.deleteById(id);
	}

	// Buscar por nombre o descripción
	public List<Servicio> buscarPorNombre(String nombre) {
		if (nombre == null || nombre.trim().isEmpty()) {
			return listarTodos();
		}
		return servicioRepository.findByNombreServicioContainingIgnoreCase(nombre.trim());
	}

	// Contar total de servicios
	public long contarTotal() {
		return servicioRepository.count();
	}

	// Calcular costo total de servicios seleccionados
	public Integer calcularCostoTotal(List<Long> serviciosIds) {
		if (serviciosIds == null || serviciosIds.isEmpty()) {
			return 0;
		}

		List<Servicio> servicios = servicioRepository.findAllById(serviciosIds);
		return servicios.stream().mapToInt(s -> s.getCostoServicio() != null ? s.getCostoServicio() : 0).sum();
	}

	// Método adicional para obtener servicios con paginación (opcional)
	public List<Servicio> listarPaginado(int pagina, int tamaño) {
		int offset = pagina * tamaño;
		return servicioRepository.findAll().stream().skip(offset).limit(tamaño).toList();
	}
}