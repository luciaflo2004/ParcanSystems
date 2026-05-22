package com.tesis.parcan.controller;

import com.tesis.parcan.dto.PersonaDTO;
import com.tesis.parcan.services.PersonaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/personas")
public class PersonasController {
    
    @Autowired
    private PersonaService personaService;
    
    @GetMapping
    public String listarPersonas(Model model) {
        List<PersonaDTO> personas = personaService.listarTodas();
        model.addAttribute("personas", personas);
        model.addAttribute("mostrarRestaurar", false);  // IMPORTANTE: agregar esta línea
        return "personas/lista";
    }
    
    @GetMapping("/eliminadas")
    public String listarEliminadas(Model model) {
        List<PersonaDTO> personasEliminadas = personaService.listarEliminadas();
        model.addAttribute("personas", personasEliminadas);
        model.addAttribute("mostrarRestaurar", true);  // IMPORTANTE: agregar esta línea
        return "personas/lista";
    }
    
    @GetMapping("/nueva")
    public String mostrarFormularioNueva(Model model) {
        model.addAttribute("personaDTO", new PersonaDTO());
        return "personas/formulario";
    }
    
    @PostMapping("/guardar")
    public String guardarPersona(@Valid @ModelAttribute("personaDTO") PersonaDTO personaDTO,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "personas/formulario";
        }
        
        try {
            personaService.guardarPersona(personaDTO);
            redirectAttributes.addFlashAttribute("success", "Persona guardada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
            return "redirect:/personas/nueva";
        }
        
        return "redirect:/personas";
    }
    
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            PersonaDTO personaDTO = personaService.buscarPorId(id);
            if (personaDTO.getEliminado() != null && personaDTO.getEliminado()) {
                redirectAttributes.addFlashAttribute("error", "No se puede editar una persona eliminada. Primero debe restaurarla.");
                return "redirect:/personas";
            }
            model.addAttribute("personaDTO", personaDTO);
            return "personas/formulario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Persona no encontrada");
            return "redirect:/personas";
        }
    }
    
    @PostMapping("/actualizar/{id}")
    public String actualizarPersona(@PathVariable Long id,
                                    @Valid @ModelAttribute("personaDTO") PersonaDTO personaDTO,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "personas/formulario";
        }
        
        try {
            personaService.actualizarPersona(id, personaDTO);
            redirectAttributes.addFlashAttribute("success", "Persona actualizada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
            return "redirect:/personas/editar/" + id;
        }
        
        return "redirect:/personas";
    }
    
    @PostMapping("/eliminar")
    public String eliminarPersona(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            personaService.eliminarPersona(id);
            redirectAttributes.addFlashAttribute("success", "Persona eliminada exitosamente (puede restaurarse)");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
        }
        
        return "redirect:/personas";
    }
    
    @PostMapping("/restaurar")
    public String restaurarPersona(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            personaService.restaurarPersona(id);
            redirectAttributes.addFlashAttribute("success", "Persona restaurada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al restaurar: " + e.getMessage());
        }
        
        return "redirect:/personas";
    }
    
    @PostMapping("/buscar")
    public String buscarPersonas(@RequestParam(required = false) String nombre,
                                 @RequestParam(required = false) String apellido,
                                 @RequestParam(required = false) String ci,
                                 @RequestParam(required = false) String telefono,
                                 Model model) {
        List<PersonaDTO> personas;
        
        if (ci != null && !ci.trim().isEmpty()) {
            personas = personaService.buscarPorCi(ci);
        }
        else if (telefono != null && !telefono.trim().isEmpty()) {
            personas = personaService.buscarPorTelefono(telefono);
        }
        else if (nombre != null && !nombre.trim().isEmpty() && apellido != null && !apellido.trim().isEmpty()) {
            personas = personaService.buscarPorNombreYApellido(nombre, apellido);
        }
        else if (nombre != null && !nombre.trim().isEmpty()) {
            personas = personaService.buscarPorNombre(nombre);
        }
        else if (apellido != null && !apellido.trim().isEmpty()) {
            personas = personaService.buscarPorApellido(apellido);
        }
        else {
            personas = personaService.listarTodas();
        }
        
        model.addAttribute("personas", personas);
        model.addAttribute("mostrarRestaurar", false);  // IMPORTANTE: agregar esta línea
        model.addAttribute("nombreBusqueda", nombre);
        model.addAttribute("apellidoBusqueda", apellido);
        model.addAttribute("ciBusqueda", ci);
        model.addAttribute("telefonoBusqueda", telefono);
        
        return "personas/lista";
    }
}