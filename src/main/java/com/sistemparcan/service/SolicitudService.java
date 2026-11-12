package com.sistemparcan.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.sistemparcan.entity.InstitucionEcclesiastica;
import com.sistemparcan.entity.Persona;
import com.sistemparcan.entity.Servicio;
import com.sistemparcan.entity.Solicitud;
import com.sistemparcan.repository.InstitucionEcclesiasticaRepository;
import com.sistemparcan.repository.PersonaRepository;
import com.sistemparcan.repository.ServicioRepository;
import com.sistemparcan.repository.SolicitudRepository;
import com.sistemparcan.util.ValidacionUtil;

// Reglas de negocio para las solicitudes
@Service
public class SolicitudService {

    private final SolicitudRepository repository;
    private final PersonaRepository personaRepository;
    private final InstitucionEcclesiasticaRepository institucionRepository;
    private final ServicioRepository servicioRepository;

    public SolicitudService(SolicitudRepository repository, PersonaRepository personaRepository,
            InstitucionEcclesiasticaRepository institucionRepository, ServicioRepository servicioRepository) {
        this.repository = repository;
        this.personaRepository = personaRepository;
        this.institucionRepository = institucionRepository;
        this.servicioRepository = servicioRepository;
    }

    // Devuelve todas las solicitudes (activas e inactivas)
    public List<Solicitud> findAll() {
        return repository.findAll();
    }

    // Devuelve solamente las solicitudes activas para los desplegables
    public List<Solicitud> findActivas() {
        return repository.findByEliminadoFalse();
    }

    // Busca una solicitud por su codigo
    public Optional<Solicitud> findById(Integer id) {
        return repository.findById(id);
    }

    /**
     * Guarda una solicitud validando que tenga un unico solicitante
     * (feligres o institucion) y resolviendo la referencia al registro existente.
     */
    @Transactional
    public Solicitud save(Solicitud solicitud) {
        validarDetalle(solicitud);
        // Se valida la fecha de solicitud dentro del rango permitido
        validarFechaSolicitud(solicitud.getFechaSolicitud());
        validarSolicitante(solicitud);

        // Al editar se busca la solicitud actual para conservar datos historicos
        Solicitud actual = solicitud.getCodSolicitud() != null
                ? repository.findById(solicitud.getCodSolicitud()).orElse(null)
                : null;

        // Si la solicitud tiene servicios, su fecha no puede superar la realización
        if (actual != null) {
            validarFechaContraServicios(actual.getCodSolicitud(), solicitud.getFechaSolicitud());
        }

        // Se carga el solicitante real desde su repositorio para garantizar
        // que la solicitud quede asociada a un registro existente de su modulo.
        if (solicitud.getFeligres() != null && solicitud.getFeligres().getCiPersona() != null) {
            Persona feligres = personaRepository.findById(solicitud.getFeligres().getCiPersona())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "El feligrés seleccionado no existe"));
            // No se permite asociar una persona inactiva a una nueva solicitud.
            // Si ya estaba asociada, se conserva para no perder el historial.
            if (Boolean.TRUE.equals(feligres.getEliminado())) {
                boolean mismoFeligres = actual != null && actual.getFeligres() != null
                        && feligres.getCiPersona().equals(actual.getFeligres().getCiPersona());
                if (!mismoFeligres) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "El feligrés seleccionado está inactivo");
                }
            }
            solicitud.setFeligres(feligres);
        }
        if (solicitud.getInstitucion() != null
                && solicitud.getInstitucion().getIdInstitucionEclesiastica() != null) {
            InstitucionEcclesiastica institucion = institucionRepository
                    .findById(solicitud.getInstitucion().getIdInstitucionEclesiastica())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "La institución seleccionada no existe"));
            // Igual que el feligrés: solo se acepta una institución inactiva si ya estaba
            if (Boolean.TRUE.equals(institucion.getEliminado())) {
                boolean mismaInstitucion = actual != null && actual.getInstitucion() != null
                        && institucion.getIdInstitucionEclesiastica()
                                .equals(actual.getInstitucion().getIdInstitucionEclesiastica());
                if (!mismaInstitucion) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "La institución seleccionada está inactiva");
                }
            }
            solicitud.setInstitucion(institucion);
        }

        // Se limpia la relación que no corresponde para conservar un único solicitante.
        if (solicitud.getFeligres() != null) {
            solicitud.setInstitucion(null);
        } else {
            solicitud.setFeligres(null);
        }

        validarSolicitante(solicitud);
        return repository.save(solicitud);
    }

    /**
     * Valida el detalle de la solicitud: máximo 50 caracteres, sin más de dos
     * espacios ni dos caracteres idénticos consecutivos.
     */
    private void validarDetalle(Solicitud solicitud) {
        String detalle = solicitud.getDetalle();
        if (detalle == null || detalle.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El detalle es obligatorio");
        }
        ValidacionUtil.validarLongitud(detalle, 50, "El detalle no puede superar los 50 caracteres");
        ValidacionUtil.validarEspaciosConsecutivos(detalle);
        ValidacionUtil.validarCaracteresConsecutivos(detalle, 2,
                "No se permiten más de dos caracteres iguales consecutivos");
    }

    /**
     * Valida que la solicitud tenga exactamente un solicitante:
     * ni ambos a la vez ni ninguno.
     */
    private void validarSolicitante(Solicitud solicitud) {
        boolean tieneFeligres = solicitud.getFeligres() != null
                && solicitud.getFeligres().getCiPersona() != null;
        boolean tieneInstitucion = solicitud.getInstitucion() != null
                && solicitud.getInstitucion().getIdInstitucionEclesiastica() != null;

        if (tieneFeligres && tieneInstitucion) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La solicitud no puede tener feligrés e institución a la vez");
        }
        if (!tieneFeligres && !tieneInstitucion) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe seleccionar un solicitante válido");
        }
    }

    /**
     * Valida que la fecha de solicitud esté entre los últimos seis meses
     * calendario y la fecha actual, incluyendo ambos extremos.
     */
    private void validarFechaSolicitud(LocalDate fechaSolicitud) {
        // Se usa la zona horaria local del equipo de desarrollo
        LocalDate hoy = LocalDate.now(ZoneId.systemDefault());
        if (fechaSolicitud == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Fecha inválida. Debe ingresar una fecha de solicitud.");
        }
        LocalDate fechaMinima = hoy.minusMonths(6);
        if (fechaSolicitud.isBefore(fechaMinima)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Fecha inválida. Ingrese otra fecha. La fecha de solicitud no puede ser anterior a los últimos seis meses.");
        }
        if (fechaSolicitud.isAfter(hoy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Fecha inválida. Ingrese otra fecha. La fecha de solicitud no puede ser posterior a la fecha actual.");
        }
    }

    /**
     * Impide que una solicitud quede con fecha posterior a algún servicio asociado.
     * Se consultan los servicios reales mediante el repositorio.
     */
    private void validarFechaContraServicios(Integer codSolicitud, LocalDate fechaSolicitud) {
        for (Servicio servicio : servicioRepository.findBySolicitudCodSolicitud(codSolicitud)) {
            if (servicio.getFechaRealizacion() != null
                    && fechaSolicitud.isAfter(servicio.getFechaRealizacion())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Fecha inválida. Ingrese otra fecha. La fecha de solicitud no puede ser posterior a la fecha de realización de un servicio asociado.");
            }
        }
    }

    // Desactiva la solicitud sin borrarla de la base de datos
    public void deleteById(Integer id) {
        repository.findById(id).ifPresent(s -> {
            s.setEliminado(true);
            repository.save(s);
        });
    }

    // Reactiva una solicitud desactivada
    public void restoreById(Integer id) {
        repository.findById(id).ifPresent(s -> {
            s.setEliminado(false);
            repository.save(s);
        });
    }
}
