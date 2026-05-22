// FeligresController.java (básico)
package com.tesis.parcan.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/feligreses")
public class FeligresController {

	@GetMapping
	public String listarFeligreses() {
		return "redirect:/personas";
	}
}
