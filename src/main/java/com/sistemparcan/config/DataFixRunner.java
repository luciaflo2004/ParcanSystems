package com.sistemparcan.config;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.sistemparcan.repository.PersonaRepository;

/**
 * Componente que, al iniciar la aplicacion, corrige datos de registros antiguos:
 * - Fecha de nacimiento en null -> se asigna una fecha valida de ejemplo.
 * - Fecha de registro en null -> se asigna la fecha actual para mantener el orden.
 * Es idempotente: solo afecta a los registros con valores nulos.
 */
@Component
public class DataFixRunner implements CommandLineRunner {

    private final PersonaRepository personaRepository;

    public DataFixRunner(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    @Override
    public void run(String... args) {
        LocalDate fechaEjemplo = LocalDate.of(1990, 1, 1);
        LocalDateTime ahora = LocalDateTime.now();
        personaRepository.findAll().forEach(p -> {
            if (p.getFechaNacPersona() == null) {
                p.setFechaNacPersona(fechaEjemplo);
            }
            if (p.getFechaRegistro() == null) {
                p.setFechaRegistro(ahora);
            }
            if (p.getFechaNacPersona() != null && p.getFechaRegistro() != null) {
                personaRepository.save(p);
            }
        });
    }
}
