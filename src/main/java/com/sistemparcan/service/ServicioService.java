package com.sistemparcan.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.sistemparcan.entity.Funcionario;
import com.sistemparcan.entity.Servicio;
import com.sistemparcan.entity.Solicitud;
import com.sistemparcan.repository.FuncionarioRepository;
import com.sistemparcan.repository.ServicioRepository;
import com.sistemparcan.repository.SolicitudRepository;
import com.sistemparcan.util.ValidacionUtil;

// Reglas de negocio para los servicios
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

    // Devuelve todos los servicios (activos e inactivos)
    public List<Servicio> findAll() {
        return repository.findAll();
    }

    // Busca un servicio por su codigo
    public Optional<Servicio> findById(Integer id) {
        return repository.findById(id);
    }

    /**
     * Guarda un servicio validando que tenga una solicitud existente y activa,
     * y resolviendo las referencias (solicitud y funcionario) a sus registros reales.
     */
    @Transactional
    public Servicio save(Servicio servicio) {
        validarDetalle(servicio);
        validarCosto(servicio);
        // Al editar se busca el registro actual para conservar sus relaciones historicas
        Servicio actual = servicio.getCodServicio() != null
                ? repository.findById(servicio.getCodServicio()).orElse(null)
                : null;
        validarYResolverSolicitud(servicio, actual);
        // La fecha de realización depende de la fecha de la solicitud asociada
        validarFechaRealizacion(servicio);
        validarYResolverFuncionario(servicio, actual);
        return repository.save(servicio);
    }

    /**
     * Valida que el costo, si se informa, sea un importe no negativo.
     */
    private void validarCosto(Servicio servicio) {
        if (servicio.getCostoServicio() != null && servicio.getCostoServicio() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El costo debe ser un número válido y no negativo");
        }
    }

    /**
     * Valida el detalle del servicio: máximo 50 caracteres, sin más de dos
     * espacios ni dos caracteres idénticos consecutivos.
     */
    private void validarDetalle(Servicio servicio) {
        String detalle = servicio.getDetalleServicio();
        if (detalle == null || detalle.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El detalle del servicio es obligatorio");
        }
        ValidacionUtil.validarLongitud(detalle, 50, "El detalle no puede superar los 50 caracteres");
        ValidacionUtil.validarEspaciosConsecutivos(detalle);
        ValidacionUtil.validarCaracteresConsecutivos(detalle, 2,
                "No se permiten más de dos caracteres iguales consecutivos");
    }

    /**
     * Valida que la solicitud seleccionada exista y no este eliminada logicamente,
     * y la reemplaza por el registro cargado desde la base de datos.
     */
    private void validarYResolverSolicitud(Servicio servicio, Servicio actual) {
        Solicitud solicitud = servicio.getSolicitud();
        if (solicitud == null || solicitud.getCodSolicitud() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede validar la fecha de realización. Seleccione primero una solicitud válida.");
        }
        Solicitud existente = solicitudRepository.findById(solicitud.getCodSolicitud()).orElse(null);
        if (existente == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La solicitud seleccionada no existe");
        }
        // Se permite conservar una solicitud inactiva solo si ya estaba asociada al servicio
        if (Boolean.TRUE.equals(existente.getEliminado())) {
            boolean mismaSolicitud = actual != null && actual.getSolicitud() != null
                    && existente.getCodSolicitud().equals(actual.getSolicitud().getCodSolicitud());
            if (!mismaSolicitud) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La solicitud seleccionada está eliminada");
            }
        }
        servicio.setSolicitud(existente);
    }

    /**
     * Valida que el funcionario seleccionado exista y no este eliminado logicamente,
     * y lo reemplaza por el registro cargado desde la base de datos.
     */
    private void validarYResolverFuncionario(Servicio servicio, Servicio actual) {
        Funcionario funcionario = servicio.getFuncionario();
        if (funcionario == null || funcionario.getCiPersona() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe seleccionar un funcionario");
        }
        Funcionario existente = funcionarioRepository.findById(funcionario.getCiPersona()).orElse(null);
        if (existente == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El funcionario seleccionado no existe");
        }
        // Tambien se controla que la persona del funcionario no este desactivada
        boolean personaInactiva = existente.getPersona() != null
                && Boolean.TRUE.equals(existente.getPersona().getEliminado());
        if (Boolean.TRUE.equals(existente.getEliminado()) || personaInactiva) {
            // Se permite mantener el funcionario inactivo solo si ya estaba en el servicio
            boolean mismoFuncionario = actual != null && actual.getFuncionario() != null
                    && existente.getCiPersona().equals(actual.getFuncionario().getCiPersona());
            if (!mismoFuncionario) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El funcionario seleccionado está inactivo");
            }
        }
        servicio.setFuncionario(existente);
    }

    /**
     * Valida que la fecha de realización no sea anterior a la fecha de la
     * solicitud asociada ni posterior a la fecha actual.
     */
    private void validarFechaRealizacion(Servicio servicio) {
        LocalDate fechaRealizacion = servicio.getFechaRealizacion();
        if (fechaRealizacion == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Fecha inválida. Debe ingresar una fecha de realización.");
        }

        Solicitud solicitud = servicio.getSolicitud();
        if (solicitud == null || solicitud.getFechaSolicitud() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede validar la fecha de realización. Seleccione primero una solicitud válida.");
        }

        // Se usa la zona horaria local del equipo de desarrollo
        LocalDate hoy = LocalDate.now(ZoneId.systemDefault());
        if (fechaRealizacion.isBefore(solicitud.getFechaSolicitud())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Fecha inválida. Ingrese otra fecha. La fecha de realización no puede ser anterior a la fecha de solicitud.");
        }
        if (fechaRealizacion.isAfter(hoy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Fecha inválida. Ingrese otra fecha. La fecha de realización no puede ser posterior a la fecha actual.");
        }
    }

    // Desactiva el servicio sin borrarlo de la base de datos
    public void deleteById(Integer id) {
        repository.findById(id).ifPresent(s -> {
            s.setEliminado(true);
            repository.save(s);
        });
    }

    // Reactiva un servicio desactivado
    public void restoreById(Integer id) {
        repository.findById(id).ifPresent(s -> {
            s.setEliminado(false);
            repository.save(s);
        });
    }
}
