package com.tesis.parcan.controller;

import com.tesis.parcan.model.Servicio;
import com.tesis.parcan.services.ServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/servicios")
public class ServiciosController {

	@Autowired
	private ServicioService servicioService;

	/**
	 * Muestra el formulario para crear un NUEVO servicio
	 */
	@GetMapping("/nuevo")
	public String mostrarFormularioNuevoServicio(Model model) {
		model.addAttribute("servicio", new Servicio());
		return "servicios/nuevo";
	}

	/**
	 * Lista todos los servicios
	 */
	@GetMapping("/lista")
	public String listarServicios(Model model) {
		List<Servicio> servicios = servicioService.listarTodos();
		model.addAttribute("servicios", servicios);

		// Estadísticas
		model.addAttribute("totalServicios", servicioService.contarTotal());

		return "servicios/lista";
	}

	/**
	 * Guarda un nuevo servicio
	 */
	@PostMapping("/guardar")
	public String guardarServicio(@ModelAttribute Servicio servicio, RedirectAttributes redirectAttributes) {
		try {
			// Validar que el costo no sea nulo, si es nulo poner 0
			if (servicio.getCostoServicio() == null) {
				servicio.setCostoServicio(0);
			}

			servicioService.guardar(servicio);
			redirectAttributes.addFlashAttribute("success", "✅ Servicio guardado exitosamente");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/servicios/nuevo";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "❌ Error al guardar: " + e.getMessage());
			return "redirect:/servicios/nuevo";
		}
		return "redirect:/servicios/lista";
	}

	/**
	 * Muestra el formulario de edición
	 */
	@GetMapping("/editar/{id}")
	public String editarServicio(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
		try {
			Servicio servicio = servicioService.buscarPorId(id);
			model.addAttribute("servicio", servicio);
			return "servicios/editar";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "❌ Error: " + e.getMessage());
			return "redirect:/servicios/lista";
		}
	}

	/**
	 * Actualiza un servicio existente
	 */
	@PostMapping("/actualizar/{id}")
	public String actualizarServicio(@PathVariable Long id, @ModelAttribute Servicio servicio,
			RedirectAttributes redirectAttributes) {
		try {
			servicio.setIdServicio(id);

			// Validar que el costo no sea nulo, si es nulo poner 0
			if (servicio.getCostoServicio() == null) {
				servicio.setCostoServicio(0);
			}

			servicioService.actualizar(servicio);
			redirectAttributes.addFlashAttribute("success", "✅ Servicio actualizado exitosamente");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/servicios/editar/" + id;
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "❌ Error al actualizar: " + e.getMessage());
			return "redirect:/servicios/editar/" + id;
		}
		return "redirect:/servicios/lista";
	}

	/**
	 * Elimina un servicio
	 */
	@GetMapping("/eliminar/{id}")
	public String eliminarServicio(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			servicioService.eliminar(id);
			redirectAttributes.addFlashAttribute("success", "✅ Servicio eliminado exitosamente");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "❌ Error al eliminar: " + e.getMessage());
		}
		return "redirect:/servicios/lista";
	}

	/**
	 * Busca servicios por nombre (para autocompletar o búsqueda)
	 */
	@GetMapping("/buscar")
	public String buscarServicios(@RequestParam(required = false) String nombre, Model model) {
		List<Servicio> servicios = servicioService.buscarPorNombre(nombre);
		model.addAttribute("servicios", servicios);
		model.addAttribute("terminoBusqueda", nombre);
		model.addAttribute("totalServicios", servicios.size());
		return "servicios/lista";
	}
}