package com.sistemparcan.config;

import com.sistemparcan.entity.Persona;
import com.sistemparcan.repository.PersonaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Componente que, al iniciar la aplicacion, corrige las fechas de nacimiento
 * que quedaron en null (registros antiguos) asignando una fecha de ejemplo valida.
 * Es idempotente: solo afecta a los registros con fecha nula.
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
        personaRepository.findAll().forEach(p -> {
            if (p.getFechaNacPersona() == null) {
                p.setFechaNacPersona(fechaEjemplo);
                personaRepository.save(p);
            }
        });
    }
}
