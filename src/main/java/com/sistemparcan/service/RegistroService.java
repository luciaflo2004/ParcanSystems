package com.sistemparcan.service;

import com.sistemparcan.dto.PersonaFuncionarioDTO;
import com.sistemparcan.entity.Cargo;
import com.sistemparcan.entity.Funcionario;
import com.sistemparcan.entity.Persona;
import com.sistemparcan.repository.CargoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

/**
 * Servicio que centraliza el registro y actualizacion de Persona y Funcionario.
 * Crea/actualiza la persona y, si el rol es FUNCIONARIO, el funcionario asociado.
 */
@Service
public class RegistroService {

    private final PersonaService personaService;
    private final FuncionarioService funcionarioService;
    private final CargoRepository cargoRepository;

    public RegistroService(PersonaService personaService, FuncionarioService funcionarioService,
                           CargoRepository cargoRepository) {
        this.personaService = personaService;
        this.funcionarioService = funcionarioService;
        this.cargoRepository = cargoRepository;
    }

    /**
     * Registra una persona y, opcionalmente, la convierte en funcionario.
     */
    @Transactional
    public Persona registrar(PersonaFuncionarioDTO dto) {
        validarFecha(dto.getFechaNacPersona());

        // Se crea la persona con los datos personales
        Persona persona = new Persona();
        persona.setCiPersona(dto.getCiPersona());
        persona.setNombrePersona(dto.getNombrePersona());
        persona.setApellidoPersona(dto.getApellidoPersona());
        persona.setFechaNacPersona(dto.getFechaNacPersona());
        persona.setDireccionPersona(dto.getDireccionPersona());
        persona.setTelefonoPersona(dto.getTelefonoPersona());
        persona = personaService.save(persona);

        // Si el rol es FUNCIONARIO, se crea el funcionario vinculado a la persona
        guardarFuncionarioSiCorresponde(dto, persona);

        return persona;
    }

    /**
     * Actualiza los datos de una persona existente y, si corresponde, su funcionario.
     */
    @Transactional
    public Persona actualizar(PersonaFuncionarioDTO dto) {
        validarFecha(dto.getFechaNacPersona());

        // Busca la persona existente por su CI
        Persona persona = personaService.findById(dto.getCiPersona())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe la persona con CI " + dto.getCiPersona()));

        persona.setNombrePersona(dto.getNombrePersona());
        persona.setApellidoPersona(dto.getApellidoPersona());
        persona.setFechaNacPersona(dto.getFechaNacPersona());
        persona.setDireccionPersona(dto.getDireccionPersona());
        persona.setTelefonoPersona(dto.getTelefonoPersona());
        persona = personaService.save(persona);

        // Si el rol es FUNCIONARIO, se crea/actualiza el funcionario vinculado
        guardarFuncionarioSiCorresponde(dto, persona);

        return persona;
    }

    /**
     * Valida que la fecha de nacimiento sea obligatoria y este dentro del rango permitido.
     * No se permite anterior a 1930 ni futura.
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
    private void guardarFuncionarioSiCorresponde(PersonaFuncionarioDTO dto, Persona persona) {
        if ("FUNCIONARIO".equalsIgnoreCase(dto.getRol())) {
            // Se busca el funcionario existente o se crea uno nuevo
            Funcionario funcionario = funcionarioService.findById(dto.getCiPersona())
                    .orElseGet(() -> {
                        Funcionario f = new Funcionario();
                        f.setCiPersona(dto.getCiPersona());
                        return f;
                    });

            funcionario.setPersona(persona);

            // Se asigna el cargo si viene indicado
            if (dto.getCargo() != null) {
                Cargo cargo = cargoRepository.findById(dto.getCargo()).orElse(null);
                funcionario.setCargo(cargo);
            }

            funcionarioService.save(funcionario);
        }
    }
}
