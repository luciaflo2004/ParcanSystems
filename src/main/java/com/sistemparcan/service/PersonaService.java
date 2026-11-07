package com.sistemparcan.service;

import com.sistemparcan.entity.Cargo;
import com.sistemparcan.entity.Funcionario;
import com.sistemparcan.entity.Persona;
import com.sistemparcan.repository.CargoRepository;
import com.sistemparcan.repository.PersonaRepository;
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

    public List<Persona> findAll() {
        return repository.findAllByOrderByFechaRegistroAsc();
    }

    public Optional<Persona> findById(Integer id) {
        return repository.findById(id);
    }

    public Persona save(Persona persona) {
        return repository.save(persona);
    }

    public void deleteById(Integer id) {
        repository.findById(id).ifPresent(p -> {
            p.setEliminado(true);
            repository.save(p);
        });
    }

    public void restoreById(Integer id) {
        repository.findById(id).ifPresent(p -> {
            p.setEliminado(false);
            repository.save(p);
        });
    }

    /**
     * Registra una persona y, si el rol es FUNCIONARIO, crea el funcionario asociado.
     * Los campos rol y cargo son transitorios y llegan desde el formulario.
     */
    @Transactional
    public Persona registrar(Persona persona) {
        validarFecha(persona.getFechaNacPersona());
        persona.setFechaRegistro(LocalDateTime.now());
        persona = repository.save(persona);
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
        validarFecha(persona.getFechaNacPersona());

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
        existente = repository.save(existente);

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
     * Valida que la fecha de nacimiento sea obligatoria y este dentro del rango permitido.
     */
    private void validarFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de nacimiento es obligatoria");
        }
        LocalDate minimo = LocalDate.of(1930, 1, 1);
        if (fecha.isBefore(minimo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de nacimiento no puede ser anterior a 1930");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de nacimiento no puede ser futura");
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

            funcionario.setPersona(persona);
            if (persona.getCargo() != null) {
                Cargo cargo = cargoRepository.findById(persona.getCargo()).orElse(null);
                funcionario.setCargo(cargo);
            }
            funcionarioService.save(funcionario);
        }
    }
}
