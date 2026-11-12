package com.sistemparcan.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Redirige la raiz del sitio al inicio de la aplicacion
@Controller
public class WebController {

    // Al entrar a "/" se muestra index.html
    @GetMapping("/")
    public String home() {
        return "redirect:/index.html";
    }
}
