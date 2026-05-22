package com.tesis.parcan.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tesis.parcan.model.Feligres;
import com.tesis.parcan.model.InstitucionesEclesiasticas;
import com.tesis.parcan.repository.FeligresRepository;

@Service
public class FeligresService {

	@Autowired
	private FeligresRepository feligresRepository;

	public List<Feligres> listarTodos() {
		return feligresRepository.findAll();
	}

	// Método que retorna Optional (útil para validaciones)
	public Optional<Feligres> buscarPorIdOptional(Long id) {
		return feligresRepository.findById(id);
	}

	// NUEVO MÉTODO: Retorna Feligres directamente, lanza excepción si no existe
	// Este es el método que usarás en SolicitudController
	public Feligres buscarPorId(Long id) {
		return feligresRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Feligrés no encontrado con ID: " + id));
	}

	public Optional<Feligres> buscarPorCi(String ci) {
		return feligresRepository.findByCi(ci);
	}

	// NUEVO MÉTODO: Buscar por CI y retornar directamente
	public Feligres buscarPorCiDirecto(String ci) {
		return feligresRepository.findByCi(ci)
				.orElseThrow(() -> new RuntimeException("Feligrés no encontrado con CI: " + ci));
	}

	public List<Feligres> buscarPorNombre(String nombre) {
		return feligresRepository.findByNombreContainingIgnoreCase(nombre);
	}

	public List<Feligres> buscarPorApellido(String apellido) {
		return feligresRepository.findByApellidoContainingIgnoreCase(apellido);
	}

	public List<Feligres> buscarPorInstitucion(InstitucionesEclesiasticas institucion) {
		return feligresRepository.findByInstitucionesEclesiasticas(institucion);
	}

	public List<Feligres> buscarPorInstitucionId(Long idInstitucion) {
		return feligresRepository.findByInstitucionesEclesiasticas_IdInstitucionEclesiastica(idInstitucion);
	}

	// NUEVO MÉTODO: Obtener feligreses activos
	public List<Feligres> listarActivos() {
		return feligresRepository.findByActivoTrue();
	}

	// NUEVO MÉTODO: Obtener feligreses por estado
	public List<Feligres> listarPorEstado(boolean activo) {
		return feligresRepository.findByActivo(activo);
	}

	// NUEVO MÉTODO: Contar total de feligreses
	public long contarTotal() {
		return feligresRepository.count();
	}

	// NUEVO MÉTODO: Contar feligreses activos
	public long contarActivos() {
		return feligresRepository.countByActivoTrue();
	}

	@Transactional
	public Feligres guardar(Feligres feligres) {
		// Validaciones básicas
		if (feligres.package com.tesis.parcan.services;

import com.tesis.parcan.model.Feligres;
import com.tesis.parcan.model.InstitucionesEclesiasticas;
import com.tesis.parcan.repository.FeligresRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class FeligresService {

    @Autowired
    private FeligresRepository feligresRepository;

    public List<Feligres> listarTodos() {
        return feligresRepository.findAllNoEliminados();
    }

    public Optional<Feligres> buscarPorIdOptional(Long id) {
        return feligresRepository.findById(id);
    }

    // Retorna Feligres directamente, lanza excepción si no existe
    public Feligres buscarPorId(Long id) {
        return feligresRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feligrés no encontrado con ID: " + id));
    }

    public Optional<Feligres> buscarPorCi(String ci) {
        return feligresRepository.findByCi(ci);
    }

    public Feligres buscarPorCiDirecto(String ci) {
        return feligresRepository.findByCi(ci)
                .orElseThrow(() -> new RuntimeException("Feligrés no encontrado con CI: " + ci));
    }

    public List<Feligres> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return listarTodos();
        }
        return feligresRepository.findByNombreContainingIgnoreCase(nombre.trim());
    }

    public List<Feligres> buscarPorApellido(String apellido) {
        if (apellido == null || apellido.trim().isEmpty()) {
            return listarTodos();
        }
        return feligresRepository.findByApellidoContainingIgnoreCase(apellido.trim());
    }

    public List<Feligres> buscarPorNombreCompleto(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return listarTodos();
        }
        return feligresRepository.findByNombreCompletoContainingIgnoreCase(nombreCompleto.trim());
    }

    public List<Feligres> buscarPorInstitucion(InstitucionesEclesiasticas institucion) {
        return feligresRepository.findByInstitucionesEclesiasticas(institucion);
    }

    public List<Feligres> buscarPorInstitucionId(Long idInstitucion) {
        return feligresRepository.findByInstitucionesEclesiasticas_IdInstitucionEclesiastica(idInstitucion);
    }

    public List<Feligres> listarActivos() {
        return feligresRepository.findByActivoTrue();
    }

    public List<Feligres> listarPorEstado(boolean activo) {
        return feligresRepository.findByActivo(activo);
    }

    public List<Feligres> listarActivosNoEliminados() {
        return feligresRepository.findActivosNoEliminados();
    }

    public long contarTotal() {
        return feligresRepository.countNoEliminados();
    }

    public long contarActivos() {
        return feligresRepository.countByActivoTrue();
    }

    public boolean existePorCi(String ci) {
        return feligresRepository.existsByCi(ci);
    }

    public boolean existePorCiExceptoId(String ci, Long id) {
        return feligresRepository.existsByCiAndIdNot(ci, id);
    }

    @Transactional
    public Feligres guardar(Feligres feligres) {
        // Validaciones básicas
        if (feligres.getCi() == null || feligres.getCi().trim().isEmpty()) {
            throw new IllegalArgumentException("El CI es obligatorio");
        }
        if (feligres.getNombre() == null || feligres.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (feligres.getApellido() == null || feligres.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }
        
        // Verificar si ya existe un feligrés con el mismo CI
        if (existePorCi(feligres.getCi())) {
            throw new IllegalArgumentException("Ya existe un feligrés con el CI: " + feligres.getCi());
        }
        
        // Establecer valores por defecto
        if (feligres.getFechaRegistro() == null) {
            feligres.setFechaRegistro(LocalDate.now());
        }
        if (feligres.getActivo() == null) {
            feligres.setActivo(true);
        }
        feligres.setEliminado(false);
        
        return feligresRepository.save(feligres);
    }

    @Transactional
    public Feligres actualizar(Feligres feligres) {
        if (!feligresRepository.existsById(feligres.getIdPersona())) {
            throw new RuntimeException("Feligres no encontrado con ID: " + feligres.getIdPersona());
        }
        
        // Verificar si ya existe otro feligrés con el mismo CI
        if (existePorCiExceptoId(feligres.getCi(), feligres.getIdPersona())) {
            throw new IllegalArgumentException("Ya existe otro feligrés con el CI: " + feligres.getCi());
        }
        
        return feligresRepository.save(feligres);
    }

    @Transactional
    public void eliminar(Long id) {
        Feligres feligres = buscarPorId(id);
        // Eliminación lógica
        feligres.setEliminado(true);
        feligres.setFechaEliminacion(LocalDate.now());
        feligresRepository.save(feligres);
    }

    @Transactional
    public void eliminarFisico(Long id) {
        if (!feligresRepository.existsById(id)) {
            throw new RuntimeException("Feligres no encontrado con ID: " + id);
        }
        feligresRepository.deleteById(id);
    }

    @Transactional
    public void desactivar(Long id) {
        Feligres feligres = buscarPorId(id);
        feligres.setActivo(false);
        feligresRepository.save(feligres);
    }

    @Transactional
    public void activar(Long id) {
        Feligres feligres = buscarPorId(id);
        feligres.setActivo(true);
        feligresRepository.save(feligres);
    }
}() == null) {
			throw new IllegalArgumentException("Los datos de la persona son obligatorios");
		}
		return feligresRepository.save(feligres);
	}

	@Transactional
	public Feligres actualizar(Feligres feligres) {
		if (!feligresRepository.existsById(feligres.getIdPersona())) {
			throw new RuntimeException("Feligres no encontrado con ID: " + feligres.getIdPersona());
		}
		return feligresRepository.save(feligres);
	}

	@Transactional
	public void eliminar(Long id) {
		if (!feligresRepository.existsById(id)) {
			throw new RuntimeException("Feligres no encontrado con ID: " + id);
		}
		feligresRepository.deleteById(id);
	}

	// NUEVO MÉTODO: Eliminación lógica (soft delete)
	@Transactional
	public void desactivar(Long id) {
		Feligres feligres = buscarPorId(id);
		feligres.setActivo(false);
		feligresRepository.save(feligres);
	}

	// NUEVO MÉTODO: Activar feligrés
	@Transactional
	public void activar(Long id) {
		Feligres feligres = buscarPorId(id);
		feligres.setActivo(true);
		feligresRepository.save(feligres);
	}
}