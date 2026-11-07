package com.sistemparcan.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.sistemparcan.entity.Solicitud;
import com.sistemparcan.repository.InstitucionEcclesiasticaRepository;
import com.sistemparcan.repository.PersonaRepository;
import com.sistemparcan.repository.SolicitudRepository;

@Service
public class SolicitudService {

    private final SolicitudRepository repository;
    private final PersonaRepository personaRepository;
    private final InstitucionEcclesiasticaRepository institucionRepository;

    public SolicitudService(SolicitudRepository repository, PersonaRepository personaRepository,
            InstitucionEcclesiasticaRepository institucionRepository) {
        this.repository = repository;
        this.personaRepository = personaRepository;
        this.institucionRepository = institucionRepository;
    }

    public List<Solicitud> findAll() {
        return repository.findAll();
    }

    public Optional<Solicitud> findById(Integer id) {
        return repository.findById(id);
    }

    /**
     * Guarda una solicitud validando que tenga un unico solicitante
     * (feligres o institucion) y resolviendo la referencia al registro existente.
     */
    public Solicitud save(Solicitud solicitud) {
        validarSolicitante(solicitud);

        // Se carga el solicitante real desde su repositorio para garantizar
        // que la solicitud quede asociada a un registro existente de su modulo.
        if (solicitud.getFeligres() != null && solicitud.getFeligres().getCiPersona() != null) {
            solicitud.setFeligres(
                    personaRepository.findById(solicitud.getFeligres().getCiPersona()).orElse(null));
        }
        if (solicitud.getInstitucion() != null
                && solicitud.getInstitucion().getIdInstitucionEclesiastica() != null) {
            solicitud.setInstitucion(institucionRepository
                    .findById(solicitud.getInstitucion().getIdInstitucionEclesiastica()).orElse(null));
        }

        // Tras resolver, se vuelve a validar por si el id no existe en su modulo.
        validarSolicitante(solicitud);
        return repository.save(solicitud);
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

    public void deleteById(Integer id) {
        repository.findById(id).ifPresent(s -> {
            s.setEliminado(true);
            repository.save(s);
        });
    }

    public void restoreById(Integer id) {
        repository.findById(id).ifPresent(s -> {
            s.setEliminado(false);
            repository.save(s);
        });
    }
}
