package com.tesis.parcan.controller;

import com.tesis.parcan.services.PersonaService;
import com.tesis.parcan.services.SolicitudService;
import com.tesis.parcan.services.InstitucionesEclesiasticasService;
import com.tesis.parcan.services.ServicioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private PersonaService personaService;
    
    @Autowired
    private SolicitudService solicitudService;
    
    @Autowired
    private InstitucionesEclesiasticasService institucionService;
    
    @Autowired
    private ServicioService servicioService;

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        Object usuario = session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }
        
        // Agregar estadísticas al modelo
        model.addAttribute("usuarioLogueado", usuario);
        model.addAttribute("totalPersonas", personaService.listarTodas().size());
        model.addAttribute("totalSolicitudes", solicitudService.contarPorEstado("PENDIENTE"));
        model.addAttribute("totalInstituciones", institucionService.listarTodas().size());
        model.addAttribute("totalServicios", servicioService.listarTodos().size());
        
        return "home";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }
}