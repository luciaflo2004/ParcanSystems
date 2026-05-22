package com.tesis.parcan.services;

import com.tesis.parcan.dto.PersonaDTO;
import com.tesis.parcan.model.Persona;
import com.tesis.parcan.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonaService {
    
    @Autowired
    private PersonaRepository personaRepository;
    
    @Transactional
    public PersonaDTO guardarPersona(PersonaDTO personaDTO) {
        if (personaRepository.existsByCiAndEliminadoFalse(personaDTO.getCi())) {
            throw new RuntimeException("Ya existe una persona activa con la cédula: " + personaDTO.getCi());
        }
        
        Persona persona = new Persona();
        persona.setCi(personaDTO.getCi());
        persona.setNombre(personaDTO.getNombre());
        persona.setApellido(personaDTO.getApellido());
        persona.setFechaNacimiento(personaDTO.getFechaNacimiento());
        persona.setDireccion(personaDTO.getDireccion());
        persona.setTelefono(personaDTO.getTelefono());
        persona.setEliminado(false);
        
        Persona saved = personaRepository.save(persona);
        return convertToDTO(saved);
    }
    
    @Transactional
    public PersonaDTO actualizarPersona(Long id, PersonaDTO personaDTO) {
        Persona persona = personaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Persona no encontrada con ID: " + id));
        
        if (persona.getEliminado()) {
            throw new RuntimeException("No se puede editar una persona eliminada. Primero debe restaurarla.");
        }
        
        if (!persona.getCi().equals(personaDTO.getCi()) && 
            personaRepository.existsByCiAndEliminadoFalse(personaDTO.getCi())) {
            throw new RuntimeException("Ya existe otra persona activa con la cédula: " + personaDTO.getCi());
        }
        
        persona.setCi(personaDTO.getCi());
        persona.setNombre(personaDTO.getNombre());
        persona.setApellido(personaDTO.getApellido());
        persona.setFechaNacimiento(personaDTO.getFechaNacimiento());
        persona.setDireccion(personaDTO.getDireccion());
        persona.setTelefono(personaDTO.getTelefono());
        
        Persona updated = personaRepository.save(persona);
        return convertToDTO(updated);
    }
    
    public List<PersonaDTO> listarTodas() {
        return personaRepository.findAllByEliminadoFalseOrderByIdPersonaAsc().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<PersonaDTO> listarEliminadas() {
        return personaRepository.findAllByEliminadoTrueOrderByIdPersonaAsc().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public PersonaDTO buscarPorId(Long id) {
        Persona persona = personaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Persona no encontrada con ID: " + id));
        return convertToDTO(persona);
    }
    
    public List<PersonaDTO> buscarPorNombre(String nombre) {
        return personaRepository.buscarPorNombre(nombre).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<PersonaDTO> buscarPorApellido(String apellido) {
        return personaRepository.buscarPorApellido(apellido).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<PersonaDTO> buscarPorNombreYApellido(String nombre, String apellido) {
        return personaRepository.buscarPorNombreYApellido(nombre, apellido).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<PersonaDTO> buscarPorCi(String ci) {
        return personaRepository.buscarPorCi(ci).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<PersonaDTO> buscarPorTelefono(String telefono) {
        return personaRepository.buscarPorTelefono(telefono).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public void eliminarPersona(Long id) {
        Persona persona = personaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Persona no encontrada con ID: " + id));
        
        if (persona.getEliminado()) {
            throw new RuntimeException("La persona ya está eliminada");
        }
        
        personaRepository.softDelete(id);
    }
    
    @Transactional
    public void restaurarPersona(Long id) {
        Persona persona = personaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Persona no encontrada con ID: " + id));
        
        if (!persona.getEliminado()) {
            throw new RuntimeException("La persona no está eliminada");
        }
        
        personaRepository.restore(id);
    }
    
    private PersonaDTO convertToDTO(Persona persona) {
        PersonaDTO dto = new PersonaDTO();
        dto.setIdPersona(persona.getIdPersona());
        dto.setCi(persona.getCi());
        dto.setNombre(persona.getNombre());
        dto.setApellido(persona.getApellido());
        dto.setFechaNacimiento(persona.getFechaNacimiento());
        dto.setDireccion(persona.getDireccion());
        dto.setTelefono(persona.getTelefono());
        dto.setEliminado(persona.getEliminado());
        dto.setFechaEliminacion(persona.getFechaEliminacion());
        return dto;
    }
}