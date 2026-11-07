package com.sistemparcan.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.sistemparcan.entity.Servicio;
import com.sistemparcan.entity.Solicitud;
import com.sistemparcan.repository.FuncionarioRepository;
import com.sistemparcan.repository.ServicioRepository;
import com.sistemparcan.repository.SolicitudRepository;

@Service
public class ServicioService {

    private final ServicioRepository repository;
    private final SolicitudRepository solicitudRepository;
    private final FuncionarioRepository funcionarioRepository;

    public ServicioService(ServicioRepository repository, SolicitudRepository solicitudRepository,
            FuncionarioRepository funcionarioRepository) {
        this.repository = repository;
        this.solicitudRepository = solicitudRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    public List<Servicio> findAll() {
        return repository.findAll();
    }

    public Optional<Servicio> findById(Integer id) {
        return repository.findById(id);
    }

    /**
     * Guarda un servicio validando que tenga una solicitud existente y activa,
     * y resolviendo las referencias (solicitud y funcionario) a sus registros reales.
     */
    public Servicio save(Servicio servicio) {
        validarYResolverSolicitud(servicio);
        resolverFuncionario(servicio);
        return repository.save(servicio);
    }

    /**
     * Valida que la solicitud seleccionada exista y no este eliminada logicamente,
     * y la reemplaza por el registro cargado desde la base de datos.
     */
    private void validarYResolverSolicitud(Servicio servicio) {
        Solicitud solicitud = servicio.getSolicitud();
        if (solicitud == null || solicitud.getCodSolicitud() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe seleccionar una solicitud");
        }
        Solicitud existente = solicitudRepository.findById(solicitud.getCodSolicitud()).orElse(null);
        if (existente == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La solicitud seleccionada no existe");
        }
        if (Boolean.TRUE.equals(existente.getEliminado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La solicitud seleccionada está eliminada");
        }
        servicio.setSolicitud(existente);
    }

    /**
     * Resuelve el funcionario responsable cargando el registro real, para que
     * la referencia quede asociada correctamente (sin alterar sus datos).
     */
    private void resolverFuncionario(Servicio servicio) {
        if (servicio.getFuncionario() != null && servicio.getFuncionario().getCiPersona() != null) {
            servicio.setFuncionario(
                    funcionarioRepository.findById(servicio.getFuncionario().getCiPersona()).orElse(null));
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
