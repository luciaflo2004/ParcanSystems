package com.tesis.parcan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.tesis.parcan.model.Usuario;
import com.tesis.parcan.services.UsuarioService;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @PostMapping("/procesar-login")
    public String procesarLogin(@RequestParam String username,
                                @RequestParam String password,
                                HttpSession session) {
        
        System.out.println(">>> LoginController recibido - Usuario: " + username);
        
        Usuario usuario = usuarioService.validarUsuario(username, password);
        
        if (usuario != null) {
            System.out.println(">>> Login EXITOSO - Redirigiendo a /home");
            session.setAttribute("usuarioLogueado", usuario);
            return "redirect:/home";
        } else {
            System.out.println(">>> Login FALLIDO - Redirigiendo con error");
            return "redirect:/login?error=true";
        }
    }
}