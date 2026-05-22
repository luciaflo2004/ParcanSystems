package com.tesis.parcan.controller;

import com.tesis.parcan.model.InstitucionesEclesiasticas;
import com.tesis.parcan.services.InstitucionesEclesiasticasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/instituciones")
public class InstitucionController {

    @Autowired
    private InstitucionesEclesiasticasService institucionService;

    @GetMapping
    public String listarInstituciones(Model model) {
        model.addAttribute("instituciones", institucionService.listarTodas());
        return "instituciones";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNueva(Model model) {
        model.addAttribute("institucion", new InstitucionesEclesiasticas());
        model.addAttribute("titulo", "Nueva Institución");
        return "institucion-formulario";
    }

    @PostMapping("/guardar")
    public String guardarInstitucion(@ModelAttribute InstitucionesEclesiasticas institucion,
                                     RedirectAttributes redirectAttributes) {
        try {
            institucionService.guardar(institucion);
            redirectAttributes.addFlashAttribute("success", "Institución guardada exitosamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/instituciones/nueva";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar la institución: " + e.getMessage());
            return "redirect:/instituciones/nueva";
        }
        return "redirect:/instituciones";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            InstitucionesEclesiasticas institucion = institucionService.buscarPorId(id);
            model.addAttribute("institucion", institucion);
            model.addAttribute("titulo", "Editar Institución");
            return "institucion-formulario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Institución no encontrada");
            return "redirect:/instituciones";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarInstitucion(@PathVariable Integer id,
                                        @ModelAttribute InstitucionesEclesiasticas institucion,
                                        RedirectAttributes redirectAttributes) {
        try {
            institucion.setIdInstitucionEclesiastica(id);
            institucionService.actualizar(institucion);
            redirectAttributes.addFlashAttribute("success", "Institución actualizada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
            return "redirect:/instituciones/editar/" + id;
        }
        return "redirect:/instituciones";
    }

    @PostMapping("/eliminar")
    public String eliminarInstitucion(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        try {
            institucionService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Institución eliminada exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la institución: " + e.getMessage());
        }
        return "redirect:/instituciones";
    }

    @PostMapping("/buscar")
    public String buscarInstituciones(@RequestParam(required = false) String nombre,
                                      @RequestParam(required = false) String tipo,
                                      @RequestParam(required = false) String ciudad,
                                      @RequestParam(required = false) String diosesis,
                                      Model model) {
        
        if (tipo != null && !tipo.trim().isEmpty()) {
            model.addAttribute("instituciones", institucionService.buscarPorTipo(tipo));
        } else if (ciudad != null && !ciudad.trim().isEmpty()) {
            model.addAttribute("instituciones", institucionService.buscarPorCiudad(ciudad));
        } else if (diosesis != null && !diosesis.trim().isEmpty()) {
            model.addAttribute("instituciones", institucionService.buscarPorDiosesis(diosesis));
        } else if (nombre != null && !nombre.trim().isEmpty()) {
            institucionService.buscarPorNombre(nombre).ifPresentOrElse(
                inst -> model.addAttribute("instituciones", java.util.List.of(inst)),
                () -> model.addAttribute("instituciones", java.util.List.of())
            );
        } else {
            model.addAttribute("instituciones", institucionService.listarTodas());
        }
        
        // Mantener valores en el formulario
        model.addAttribute("nombreBusqueda", nombre);
        model.addAttribute("tipoBusqueda", tipo);
        model.addAttribute("ciudadBusqueda", ciudad);
        model.addAttribute("diosesisBusqueda", diosesis);
        
        return "instituciones";
    }
}