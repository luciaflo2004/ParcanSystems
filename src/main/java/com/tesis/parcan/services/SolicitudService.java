package com.tesis.parcan.services;

import com.tesis.parcan.model.Solicitud;
import com.tesis.parcan.model.Persona;
import com.tesis.parcan.model.InstitucionesEclesiasticas;
import com.tesis.parcan.repository.SolicitudRepository;
import com.tesis.parcan.repository.FeligresRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SolicitudService {

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private FeligresRepository feligresRepository;

    public List<Solicitud> listarTodas() {
        return solicitudRepository.findAllByOrderByFechaSolicitudDesc();
    }

    public Solicitud buscarPorId(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));
    }

    @Transactional
    public Solicitud guardar(Solicitud solicitud) {
        if (solicitud.getPersona() == null) {
            throw new IllegalArgumentException("La persona (feligrés) es obligatoria");
        }

        if (solicitud.getFechaSolicitud() == null) {
            solicitud.setFechaSolicitud(LocalDateTime.now());
        }

        if (solicitud.getEstado() == null || solicitud.getEstado().trim().isEmpty()) {
            solicitud.setEstado("PENDIENTE");
        }

        // Validar que la persona existe
        if (!feligresRepository.existsById(solicitud.getPersona().getIdPersona())) {
            throw new IllegalArgumentException("La persona/feligres no existe");
        }

        return solicitudRepository.save(solicitud);
    }

    @Transactional
    public Solicitud actualizar(Solicitud solicitud) {
        Solicitud existente = buscarPorId(solicitud.getIdSolicitud());

        existente.setTipoSolicitud(solicitud.getTipoSolicitud());
        existente.setDescripcion(solicitud.getDescripcion());
        existente.setEstado(solicitud.getEstado());
        existente.setFechaSolicitud(solicitud.getFechaSolicitud());
        existente.setPersona(solicitud.getPersona());

        return solicitudRepository.save(existente);
    }

    @Transactional
    public Solicitud cambiarEstado(Long id, String nuevoEstado) {
        Solicitud solicitud = buscarPorId(id);
        solicitud.setEstado(nuevoEstado);
        return solicitudRepository.save(solicitud);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!solicitudRepository.existsById(id)) {
            throw new RuntimeException("Solicitud no encontrada");
        }
        solicitudRepository.deleteById(id);
    }

    // Métodos de búsqueda
    public List<Solicitud> buscarPorPersona(Persona persona) {
        return solicitudRepository.findByPersona(persona);
    }

    public List<Solicitud> buscarPorEstado(String estado) {
        return solicitudRepository.findByEstado(estado);
    }

    public List<Solicitud> buscarPorInstitucion(InstitucionesEclesiasticas institucion) {
        return solicitudRepository.findByInstitucionEclesiastica(institucion);
    }

    public List<Solicitud> buscarConFiltros(Persona persona, String estado,
                                            LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return solicitudRepository.findWithFilters(persona, estado, fechaInicio, fechaFin);
    }

    public long contarPorEstado(String estado) {
        return solicitudRepository.countByEstado(estado);
    }
}