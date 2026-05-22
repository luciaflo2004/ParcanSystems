package com.tesis.parcan.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tesis.parcan.model.Feligres;
import com.tesis.parcan.model.Servicio;
import com.tesis.parcan.model.Solicitud;
import com.tesis.parcan.services.FeligresService;
import com.tesis.parcan.services.InstitucionesEclesiasticasService;
import com.tesis.parcan.services.ServicioService;
import com.tesis.parcan.services.SolicitudService;

@Controller
@RequestMapping("/solicitudes")
public class SolicitudController {

    @Autowired
    private SolicitudService solicitudService;

    @Autowired
    private FeligresService feligresService;

    @Autowired
    private ServicioService servicioService;

    @Autowired
    private InstitucionesEclesiasticasService institucionesService;

    /**
     * Página principal de solicitudes - Redirige a lista
     */
    @GetMapping
    public String index(Model model) {
        return listarSolicitudes(model);
    }

    /**
     * Lista todas las solicitudes
     */
    @GetMapping("/lista")
    public String listarSolicitudes(Model model) {
        List<Solicitud> solicitudes = solicitudService.listarTodas();
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("tiposEstado", new String[]{"PENDIENTE", "APROBADA", "RECHAZADA", "COMPLETADA"});
        model.addAttribute("totalSolicitudes", solicitudes.size());
        model.addAttribute("totalPendientes", solicitudService.contarPorEstado("PENDIENTE"));
        model.addAttribute("totalAprobadas", solicitudService.contarPorEstado("APROBADA"));
        model.addAttribute("totalRechazadas", solicitudService.contarPorEstado("RECHAZADA"));
        model.addAttribute("totalCompletadas", solicitudService.contarPorEstado("COMPLETADA"));
        return "solicitudes/lista";
    }

    /**
     * Muestra el formulario para nueva solicitud
     */
    @GetMapping("/nueva")
    public String nuevaSolicitud(Model model) {
        model.addAttribute("solicitud", new Solicitud());
        cargarDatosAuxiliares(model);
        model.addAttribute("tiposSolicitud", new String[]{
            "BAUTISMO", "COMUNION", "CONFIRMACION", "MATRIMONIO", 
            "CONFESION", "VISITA_PASTORAL", "EVENTO_ESPECIAL", "OTRO"
        });
        return "solicitudes/nueva";
    }

    /**
     * Guarda una nueva solicitud
     */
    @PostMapping("/guardar")
    public String guardarSolicitud(@ModelAttribute Solicitud solicitud,
                                   @RequestParam Long feligresId,
                                   @RequestParam(required = false) Long servicioId,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Validar que el feligrés existe
            Feligres feligres = feligresService.buscarPorId(feligresId);
            if (feligres == null) {
                throw new RuntimeException("Feligrés no encontrado con ID: " + feligresId);
            }
            solicitud.setPersona(feligres);
            
            // Asignar servicio si se seleccionó
            if (servicioId != null && servicioId > 0) {
                Servicio servicio = servicioService.buscarPorId(servicioId);
                solicitud.setServicio(servicio);
            }
            
            // Establecer fecha y estado inicial
            solicitud.setFechaSolicitud(LocalDateTime.now());
            solicitud.setEstado("PENDIENTE");
            
            // Guardar la solicitud
            solicitudService.guardar(solicitud);
            redirectAttributes.addFlashAttribute("success", "✅ Solicitud creada exitosamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error al guardar: " + e.getMessage());
            return "redirect:/solicitudes/nueva";
        }
        return "redirect:/solicitudes/lista";
    }

    /**
     * Muestra formulario de edición
     */
    @GetMapping("/editar/{id}")
    public String editarSolicitud(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Solicitud solicitud = solicitudService.buscarPorId(id);
            model.addAttribute("solicitud", solicitud);
            cargarDatosAuxiliares(model);
            model.addAttribute("tiposSolicitud", new String[]{
                "BAUTISMO", "COMUNION", "CONFIRMACION", "MATRIMONIO", 
                "CONFESION", "VISITA_PASTORAL", "EVENTO_ESPECIAL", "OTRO"
            });
            model.addAttribute("tiposEstado", new String[]{"PENDIENTE", "APROBADA", "RECHAZADA", "COMPLETADA"});
            return "solicitudes/editar";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error: " + e.getMessage());
            return "redirect:/solicitudes/lista";
        }
    }

    /**
     * Actualiza una solicitud existente
     */
    @PostMapping("/actualizar/{id}")
    public String actualizarSolicitud(@PathVariable Long id,
                                      @ModelAttribute Solicitud solicitud,
                                      @RequestParam(required = false) Long feligresId,
                                      @RequestParam(required = false) Long servicioId,
                                      RedirectAttributes redirectAttributes) {
        try {
            solicitud.setIdSolicitud(id);
            
            // Actualizar feligrés si se cambió
            if (feligresId != null && feligresId > 0) {
                Feligres feligres = feligresService.buscarPorId(feligresId);
                solicitud.setPersona(feligres);
            }
            
            // Actualizar servicio si se cambió
            if (servicioId != null && servicioId > 0) {
                Servicio servicio = servicioService.buscarPorId(servicioId);
                solicitud.setServicio(servicio);
            }
            
            solicitudService.actualizar(solicitud);
            redirectAttributes.addFlashAttribute("success", "✅ Solicitud actualizada exitosamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error al actualizar: " + e.getMessage());
        }
        return "redirect:/solicitudes/lista";
    }

    /**
     * Cambia el estado de una solicitud
     */
    @GetMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id, 
                                @RequestParam String estado, 
                                RedirectAttributes redirectAttributes) {
        try {
            // Validar que el estado sea válido
            String[] estadosValidos = {"PENDIENTE", "APROBADA", "RECHAZADA", "COMPLETADA"};
            boolean estadoValido = false;
            for (String e : estadosValidos) {
                if (e.equals(estado)) {
                    estadoValido = true;
                    break;
                }
            }
            
            if (!estadoValido) {
                throw new IllegalArgumentException("Estado no válido: " + estado);
            }
            
            solicitudService.cambiarEstado(id, estado);
            redirectAttributes.addFlashAttribute("success", "✅ Estado cambiado a: " + estado);
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error al cambiar estado: " + e.getMessage());
        }
        return "redirect:/solicitudes/lista";
    }

    /**
     * Elimina una solicitud
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarSolicitud(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            solicitudService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "✅ Solicitud eliminada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error al eliminar: " + e.getMessage());
        }
        return "redirect:/solicitudes/lista";
    }
    
    /**
     * Ver detalles de una solicitud específica
     */
    @GetMapping("/ver/{id}")
    public String verSolicitud(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Solicitud solicitud = solicitudService.buscarPorId(id);
            model.addAttribute("solicitud", solicitud);
            return "solicitudes/ver";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error: " + e.getMessage());
            return "redirect:/solicitudes/lista";
        }
    }
    
    /**
     * Filtra solicitudes por estado
     */
    @GetMapping("/filtrar")
    public String filtrarPorEstado(@RequestParam(required = false) String estado, Model model) {
        List<Solicitud> solicitudes;
        
        if (estado != null && !estado.isEmpty()) {
            solicitudes = solicitudService.listarPorEstado(estado);
            model.addAttribute("estadoFiltro", estado);
        } else {
            solicitudes = solicitudService.listarTodas();
        }
        
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("tiposEstado", new String[]{"PENDIENTE", "APROBADA", "RECHAZADA", "COMPLETADA"});
        model.addAttribute("totalSolicitudes", solicitudes.size());
        model.addAttribute("totalPendientes", solicitudService.contarPorEstado("PENDIENTE"));
        model.addAttribute("totalAprobadas", solicitudService.contarPorEstado("APROBADA"));
        model.addAttribute("totalRechazadas", solicitudService.contarPorEstado("RECHAZADA"));
        model.addAttribute("totalCompletadas", solicitudService.contarPorEstado("COMPLETADA"));
        
        return "solicitudes/lista";
    }
    
    /**
     * Busca solicitudes por feligrés
     */
    @GetMapping("/buscar")
    public String buscarPorFeligres(@RequestParam(required = false) String termino, Model model) {
        List<Solicitud> solicitudes;
        
        if (termino != null && !termino.isEmpty()) {
            solicitudes = solicitudService.buscarPorNombreFeligres(termino);
            model.addAttribute("terminoBusqueda", termino);
        } else {
            solicitudes = solicitudService.listarTodas();
        }
        
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("tiposEstado", new String[]{"PENDIENTE", "APROBADA", "RECHAZADA", "COMPLETADA"});
        model.addAttribute("totalSolicitudes", solicitudes.size());
        model.addAttribute("totalPendientes", solicitudService.contarPorEstado("PENDIENTE"));
        model.addAttribute("totalAprobadas", solicitudService.contarPorEstado("APROBADA"));
        model.addAttribute("totalRechazadas", solicitudService.contarPorEstado("RECHAZADA"));
        model.addAttribute("totalCompletadas", solicitudService.contarPorEstado("COMPLETADA"));
        
        return "solicitudes/lista";
    }

    /**
     * Carga datos auxiliares para formularios
     */
    private void cargarDatosAuxiliares(Model model) {
        model.addAttribute("feligreses", feligresService.listarTodos());
        model.addAttribute("servicios", servicioService.listarTodos());
        model.addAttribute("instituciones", institucionesService.listarTodas());
    }
}