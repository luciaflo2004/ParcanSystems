package com.tesis.parcan.services;

import com.tesis.parcan.model.InstitucionesEclesiasticas;
import com.tesis.parcan.repository.InstitucionesEclesiasticasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class InstitucionesEclesiasticasService {

    @Autowired
    private InstitucionesEclesiasticasRepository institucionesRepository;

    // Listar todas las instituciones ordenadas ascendente por ID
    @Transactional(readOnly = true)
    public List<InstitucionesEclesiasticas> listarTodas() {
        List<InstitucionesEclesiasticas> instituciones = institucionesRepository.findAllByOrderByIdInstitucionEclesiasticaAsc();
        // Forzar la carga de la colección feligreses
        instituciones.forEach(inst -> inst.getFeligreses().size());
        return instituciones;
    }

    // Buscar por ID con carga eager
    @Transactional(readOnly = true)
    public InstitucionesEclesiasticas buscarPorId(Integer id) {
        InstitucionesEclesiasticas institucion = institucionesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Institución no encontrada con ID: " + id));
        institucion.getFeligreses().size();
        return institucion;
    }

    // Buscar por nombre
    @Transactional(readOnly = true)
    public Optional<InstitucionesEclesiasticas> buscarPorNombre(String nombre) {
        Optional<InstitucionesEclesiasticas> institucion = institucionesRepository.findByNombre(nombre);
        institucion.ifPresent(inst -> inst.getFeligreses().size());
        return institucion;
    }

    // Guardar nueva institución
    @Transactional
    public InstitucionesEclesiasticas guardar(InstitucionesEclesiasticas institucion) {
        if (institucion.getNombre() == null || institucion.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la institución es obligatorio");
        }
        
        if (institucion.getTipo() == null || institucion.getTipo().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de institución es obligatorio");
        }
        
        Optional<InstitucionesEclesiasticas> existente = institucionesRepository.findByNombre(institucion.getNombre());
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe una institución con el nombre: " + institucion.getNombre());
        }
        
        return institucionesRepository.save(institucion);
    }

    // Actualizar institución
    @Transactional
    public InstitucionesEclesiasticas actualizar(InstitucionesEclesiasticas institucion) {
        if (!institucionesRepository.existsById(institucion.getIdInstitucionEclesiastica())) {
            throw new RuntimeException("Institución no encontrada");
        }
        return institucionesRepository.save(institucion);
    }

    // Eliminar institución
    @Transactional
    public void eliminar(Integer id) {
        if (!institucionesRepository.existsById(id)) {
            throw new RuntimeException("Institución no encontrada");
        }
        
        InstitucionesEclesiasticas institucion = buscarPorId(id);
        if (institucion.getFeligreses() != null && !institucion.getFeligreses().isEmpty()) {
            throw new RuntimeException("No se puede eliminar la institución porque tiene " + 
                                      institucion.getFeligreses().size() + " feligreses asociados");
        }
        
        institucionesRepository.deleteById(id);
    }

    // Buscar por tipo (ordenado ascendente)
    @Transactional(readOnly = true)
    public List<InstitucionesEclesiasticas> buscarPorTipo(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            return listarTodas();
        }
        List<InstitucionesEclesiasticas> instituciones = institucionesRepository.findByTipoContainingIgnoreCase(tipo);
        instituciones.forEach(inst -> inst.getFeligreses().size());
        return instituciones;
    }

    // Buscar por ciudad (ordenado ascendente)
    @Transactional(readOnly = true)
    public List<InstitucionesEclesiasticas> buscarPorCiudad(String ciudad) {
        if (ciudad == null || ciudad.trim().isEmpty()) {
            return listarTodas();
        }
        List<InstitucionesEclesiasticas> instituciones = institucionesRepository.findByCiudadContainingIgnoreCase(ciudad);
        instituciones.forEach(inst -> inst.getFeligreses().size());
        return instituciones;
    }

    // Buscar por diócesis (ordenado ascendente)
    @Transactional(readOnly = true)
    public List<InstitucionesEclesiasticas> buscarPorDiosesis(String diosesis) {
        if (diosesis == null || diosesis.trim().isEmpty()) {
            return listarTodas();
        }
        List<InstitucionesEclesiasticas> instituciones = institucionesRepository.findByDiosesisContainingIgnoreCase(diosesis);
        instituciones.forEach(inst -> inst.getFeligreses().size());
        return instituciones;
    }

    // Contar total de instituciones por tipo
    public long contarPorTipo(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            return 0;
        }
        return institucionesRepository.countByTipo(tipo);
    }
}