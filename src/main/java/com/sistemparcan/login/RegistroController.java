package com.sistemparcan.login;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.sistemparcan.entity.Cargo;
import com.sistemparcan.entity.Funcionario;
import com.sistemparcan.entity.Usuario;
import com.sistemparcan.repository.CargoRepository;
import com.sistemparcan.repository.FuncionarioRepository;
import com.sistemparcan.service.UsuarioService;

/**
 * Registro restringido de cuentas: solo un funcionario activo, con un cargo
 * que aún no tenga cuenta, puede crear su usuario. No asigna roles administrativos.
 */
@RestController
@RequestMapping("/api/registro")
public class RegistroController {

    private final FuncionarioRepository funcionarioRepository;
    private final CargoRepository cargoRepository;
    private final UsuarioService usuarioService;

    public RegistroController(FuncionarioRepository funcionarioRepository,
            CargoRepository cargoRepository, UsuarioService usuarioService) {
        this.funcionarioRepository = funcionarioRepository;
        this.cargoRepository = cargoRepository;
        this.usuarioService = usuarioService;
    }

    // Crea la cuenta de un funcionario activo con el cargo indicado
    @PostMapping
    public ResponseEntity<Map<String, String>> registrar(@RequestBody RegistroRequest request) {
        // 1) El funcionario debe existir y estar activo (junto con su persona)
        Funcionario funcionario = funcionarioRepository.findById(request.getCiFuncionario()).orElse(null);
        if (funcionario == null) {
            return badRequest("El funcionario indicado no existe");
        }
        boolean personaInactiva = funcionario.getPersona() != null
                && Boolean.TRUE.equals(funcionario.getPersona().getEliminado());
        if (Boolean.TRUE.equals(funcionario.getEliminado()) || personaInactiva) {
            return badRequest("El funcionario indicado está inactivo");
        }

        // 2) El cargo debe existir y estar activo
        Cargo cargo = cargoRepository.findById(request.getCodCargo()).orElse(null);
        if (cargo == null || Boolean.TRUE.equals(cargo.getEliminado())) {
            return badRequest("El cargo indicado no es válido");
        }

        // 3) Validar los requisitos de la contraseña
        try {
            usuarioService.validarContrasena(request.getContrasena(), request.getUsuario());
        } catch (ResponseStatusException e) {
            return badRequest(e.getReason());
        }

        // 4) Construir y guardar el usuario (unicidad de nombre, hashing y
        //    unicidad de la combinación funcionario + cargo)
        Usuario usuario = new Usuario();
        usuario.setUsuario(request.getUsuario());
        usuario.setContrasena(request.getContrasena());
        usuario.setFuncionario(funcionario);
        usuario.setCargo(cargo);
        try {
            usuarioService.save(usuario);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("mensaje", e.getReason()));
        }

        return ResponseEntity.ok(Map.of("mensaje", "Cuenta creada correctamente"));
    }

    private ResponseEntity<Map<String, String>> badRequest(String mensaje) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensaje", mensaje));
    }
}
