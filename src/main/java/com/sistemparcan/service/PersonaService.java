package com.sistemparcan.service;

import com.sistemparcan.entity.Cargo;
import com.sistemparcan.entity.Funcionario;
import com.sistemparcan.entity.Persona;
import com.sistemparcan.repository.CargoRepository;
import com.sistemparcan.repository.PersonaRepository;
import com.sistemparcan.util.ValidacionUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de Persona. Ademas de las operaciones basicas, centraliza el registro
 * y actualizacion unificada (persona + funcionario) y el cambio de cedula.
 */
@Service
public class PersonaService {

    private final PersonaRepository repository;
    private final FuncionarioService funcionarioService;
    private final CargoRepository cargoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public PersonaService(PersonaRepository repository, FuncionarioService funcionarioService,
                          CargoRepository cargoRepository) {
        this.repository = repository;
        this.funcionarioService = funcionarioService;
        this.cargoRepository = cargoRepository;
    }

    // Devuelve todas las personas ordenadas por fecha de registro
    public List<Persona> findAll() {
        return repository.findAllByOrderByFechaRegistroAsc();
    }

    // Devuelve solamente las personas activas para los desplegables
    public List<Persona> findActivas() {
        return repository.findByEliminadoFalseOrderByFechaRegistroAsc();
    }

    // Busca una persona por su cedula
    public Optional<Persona> findById(Integer id) {
        return repository.findById(id);
    }

    // Guarda una persona validando sus fechas y datos
    public Persona save(Persona persona) {
        // Los endpoints genericos tambien validan fechas y datos, aunque el navegador
        // omita sus comprobaciones.
        boolean yaEsFuncionario = persona.getCiPersona() != null
                && funcionarioService.findById(persona.getCiPersona()).isPresent();
        validarFecha(persona, yaEsFuncionario);
        validarDatosPersona(persona);
        return repository.save(persona);
    }

    // Desactiva la persona sin borrarla de la base de datos
    public void deleteById(Integer id) {
        repository.findById(id).ifPresent(p -> {
            p.setEliminado(true);
            repository.save(p);
        });
    }

    // Reactiva la persona y su funcionario si existe
    @Transactional
    public void restoreById(Integer id) {
        repository.findById(id).ifPresent(p -> {
            p.setEliminado(false);
            repository.save(p);
            // Si la persona tambien es funcionario, se reactiva su registro
            // para que vuelva a estar disponible en los desplegables.
            funcionarioService.findById(id).ifPresent(f -> {
                f.setEliminado(false);
                funcionarioService.save(f);
            });
        });
    }

    /**
     * Registra una persona y, si el rol es FUNCIONARIO, crea el funcionario asociado.
     * Los campos rol y cargo son transitorios y llegan desde el formulario.
     */
    @Transactional
    public Persona registrar(Persona persona) {
        validarFecha(persona, false);
        validarDatosPersona(persona);
        // No se permite registrar dos veces la misma cedula
        if (persona.getCiPersona() != null && repository.findById(persona.getCiPersona()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La cédula " + persona.getCiPersona() + " ya está registrada");
        }
        persona.setFechaRegistro(LocalDateTime.now());
        // repository.save sobre una entidad con id asignado usa merge, y merge no copia
        // los campos @Transient: se conservan para poder crear el funcionario asociado.
        String rol = persona.getRol();
        Integer cargo = persona.getCargo();
        persona = repository.save(persona);
        persona.setRol(rol);
        persona.setCargo(cargo);
        guardarFuncionarioSiCorresponde(persona);
        return persona;
    }

    /**
     * Actualiza una persona existente. Si la cedula cambio, se actualiza la clave
     * primaria y todas las tablas que la referencian.
     *
     * @param oldCi   cedula original (identificador en la URL)
     * @param persona datos nuevos (incluye la nueva cedula si fue modificada)
     */
    @Transactional
    public Persona actualizar(Integer oldCi, Persona persona) {
        // La condición de funcionario se toma del registro real en la base y del rol enviado,
        // de modo que la validación de edad no dependa únicamente de la pantalla utilizada.
        boolean yaEsFuncionario = funcionarioService.findById(oldCi).isPresent();
        validarFecha(persona, yaEsFuncionario);
        validarDatosPersona(persona);

        Persona existente = repository.findById(oldCi)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe la persona con CI " + oldCi));

        Integer newCi = persona.getCiPersona() != null ? persona.getCiPersona() : oldCi;

        // Si la cedula cambio, se actualiza la clave y sus referencias
        if (!newCi.equals(oldCi)) {
            cambiarCi(oldCi, newCi);
            entityManager.flush();
            entityManager.clear();
            existente = repository.findById(newCi)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "No se pudo actualizar la cedula"));
        }

        existente.setNombrePersona(persona.getNombrePersona());
        existente.setApellidoPersona(persona.getApellidoPersona());
        existente.setFechaNacPersona(persona.getFechaNacPersona());
        existente.setDireccionPersona(persona.getDireccionPersona());
        existente.setTelefonoPersona(persona.getTelefonoPersona());
        // Se propagan los campos transitorios (rol y cargo) para crear o actualizar el funcionario asociado
        existente.setRol(persona.getRol());
        existente.setCargo(persona.getCargo());
        String rol = existente.getRol();
        Integer cargo = existente.getCargo();
        existente = repository.save(existente);
        existente.setRol(rol);
        existente.setCargo(cargo);
        guardarFuncionarioSiCorresponde(existente);
        return existente;
    }

    /**
     * Cambia la cedula de una persona y actualiza todas las tablas que la referencian.
     * Se hace dentro de una transaccion para mantener la consistencia.
     */
    private void cambiarCi(Integer oldCi, Integer newCi) {
        // Evita duplicar la cedula si ya existe otra persona con ese numero
        if (repository.findById(newCi).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La cedula " + newCi + " ya esta registrada");
        }

        // 1) Se inserta una copia de la persona con la nueva cedula (conservando la fecha de registro)
        entityManager.createNativeQuery(
                "INSERT INTO persona (ci_persona, nombre_persona, apellido_persona, fecha_nac_persona, direccion_persona, telefono_persona, eliminado, fecha_registro) " +
                "SELECT :newCi, nombre_persona, apellido_persona, fecha_nac_persona, direccion_persona, telefono_persona, eliminado, fecha_registro " +
                "FROM persona WHERE ci_persona = :oldCi")
                .setParameter("newCi", newCi).setParameter("oldCi", oldCi).executeUpdate();

        // 2) Se inserta una copia del funcionario (si existe) con la nueva cedula
        entityManager.createNativeQuery(
                "INSERT INTO funcionario (ci_persona, cargo_cod_cargo, eliminado, persona_ci_persona) " +
                "SELECT :newCi, cargo_cod_cargo, eliminado, :newCi " +
                "FROM funcionario WHERE ci_persona = :oldCi")
                .setParameter("newCi", newCi).setParameter("oldCi", oldCi).executeUpdate();

        // 3) Se actualizan las referencias de la cedula en las demas tablas
        entityManager.createNativeQuery(
                "UPDATE solicitud SET feligres_ci_persona = :newCi WHERE feligres_ci_persona = :oldCi")
                .setParameter("newCi", newCi).setParameter("oldCi", oldCi).executeUpdate();
        entityManager.createNativeQuery(
                "UPDATE solicitud SET funcionario_ci_persona = :newCi WHERE funcionario_ci_persona = :oldCi")
                .setParameter("newCi", newCi).setParameter("oldCi", oldCi).executeUpdate();
        entityManager.createNativeQuery(
                "UPDATE servicio SET funcionario_ci_persona = :newCi WHERE funcionario_ci_persona = :oldCi")
                .setParameter("newCi", newCi).setParameter("oldCi", oldCi).executeUpdate();
        entityManager.createNativeQuery(
                "UPDATE usuario SET funcionario_ci_persona = :newCi WHERE funcionario_ci_persona = :oldCi")
                .setParameter("newCi", newCi).setParameter("oldCi", oldCi).executeUpdate();

        // 4) Se elimina el registro antiguo (funcionario y persona)
        entityManager.createNativeQuery("DELETE FROM funcionario WHERE ci_persona = :oldCi")
                .setParameter("oldCi", oldCi).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM persona WHERE ci_persona = :oldCi")
                .setParameter("oldCi", oldCi).executeUpdate();
    }

    /**
     * Valida la fecha de nacimiento según la condición del registro:
     * - Para todos: no puede ser posterior a la fecha actual.
     * - Para funcionarios: además debe tener al menos 18 años cumplidos.
     */
    private void validarFecha(Persona persona, boolean yaEsFuncionario) {
        LocalDate fecha = persona.getFechaNacPersona();
        if (fecha == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ingrese una fecha de nacimiento válida");
        }
        LocalDate hoy = LocalDate.now();
        // No se permite una fecha de nacimiento posterior al día actual
        if (fecha.isAfter(hoy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de nacimiento no es válida. No puede ser posterior a la fecha actual. Verifique e intente nuevamente");
        }
        LocalDate minimo = LocalDate.of(1930, 1, 1);
        if (fecha.isBefore(minimo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de nacimiento no puede ser anterior a 1930");
        }
        // Si es funcionario (por el rol enviado o porque ya lo era en la base),
        // debe tener al menos 18 años cumplidos
        if (yaEsFuncionario || "FUNCIONARIO".equalsIgnoreCase(persona.getRol())) {
            LocalDate maximo = hoy.minusYears(18);
            if (fecha.isAfter(maximo)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El funcionario debe tener al menos 18 años cumplidos. Verifique la fecha de nacimiento");
            }
        }
    }

    /**
     * Valida los campos de texto de la persona (nombre, apellido, teléfono, dirección y cédula).
     */
    private void validarDatosPersona(Persona persona) {
        if (persona.getNombrePersona() == null || persona.getNombrePersona().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingrese un nombre válido");
        }
        ValidacionUtil.validarSoloLetras(persona.getNombrePersona(), "El nombre solo puede contener letras");
        ValidacionUtil.validarLongitud(persona.getNombrePersona(), 30, "El nombre no puede superar los 30 caracteres");
        ValidacionUtil.validarEspaciosConsecutivos(persona.getNombrePersona());
        ValidacionUtil.validarCaracteresConsecutivos(persona.getNombrePersona(), 2,
                "No se permiten más de dos letras iguales consecutivas. Ingrese un nombre válido");

        if (persona.getApellidoPersona() == null || persona.getApellidoPersona().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingrese un apellido válido");
        }
        ValidacionUtil.validarSoloLetras(persona.getApellidoPersona(), "El apellido contiene caracteres no permitidos");
        ValidacionUtil.validarLongitud(persona.getApellidoPersona(), 30, "El apellido no puede superar los 30 caracteres");
        ValidacionUtil.validarEspaciosConsecutivos(persona.getApellidoPersona());
        ValidacionUtil.validarCaracteresConsecutivos(persona.getApellidoPersona(), 2,
                "No se permiten más de dos letras iguales consecutivas. Ingrese un apellido válido");

        ValidacionUtil.validarSoloNumeros(persona.getTelefonoPersona(), "El teléfono debe contener únicamente números");
        ValidacionUtil.validarLongitud(persona.getTelefonoPersona(), 10, "El teléfono no puede superar los 10 dígitos");
        ValidacionUtil.validarNumerosConsecutivos(persona.getTelefonoPersona(), 4,
                "No se permiten más de cuatro números iguales consecutivos");

        ValidacionUtil.validarLongitud(persona.getDireccionPersona(), 50, "La dirección no puede superar los 50 caracteres");
        ValidacionUtil.validarEspaciosConsecutivos(persona.getDireccionPersona());
        ValidacionUtil.validarCaracteresConsecutivos(persona.getDireccionPersona(), 2,
                "No se permiten más de dos caracteres iguales consecutivos");

        if (persona.getCiPersona() != null) {
            String ci = String.valueOf(persona.getCiPersona());
            if (ci.length() < 6) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La cédula debe tener mínimo 6 números");
            }
            if (ci.length() > 8) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La cédula debe contener como máximo 8 números");
            }
            ValidacionUtil.validarNumerosConsecutivos(ci, 4,
                    "No se permiten más de cuatro números iguales consecutivos");
        }
    }

    /**
     * Crea o actualiza el funcionario cuando el rol es FUNCIONARIO.
     */
    private void guardarFuncionarioSiCorresponde(Persona persona) {
        if ("FUNCIONARIO".equalsIgnoreCase(persona.getRol())) {
            Funcionario funcionario = funcionarioService.findById(persona.getCiPersona())
                    .orElseGet(() -> {
                        Funcionario f = new Funcionario();
                        f.setCiPersona(persona.getCiPersona());
                        return f;
                    });

            // Se guarda el cargo anterior para permitir conservarlo si esta inactivo
            Cargo cargoAnterior = funcionario.getCargo();
            funcionario.setPersona(persona);
            if (persona.getCargo() != null) {
                Cargo cargo = cargoRepository.findById(persona.getCargo())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "El cargo seleccionado no existe"));
                boolean esMismoCargo = cargoAnterior != null
                        && cargoAnterior.getCodCargo().equals(cargo.getCodCargo());
                // No se permite asignar un cargo inactivo nuevo, pero se conserva el historico
                if (Boolean.TRUE.equals(cargo.getEliminado()) && !esMismoCargo) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "El cargo seleccionado está inactivo");
                }
                funcionario.setCargo(cargo);
            }
            funcionarioService.save(funcionario);
        }
    }
}
