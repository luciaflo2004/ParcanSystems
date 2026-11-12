package com.sistemparcan.login;

import org.springframework.stereotype.Service;

import com.sistemparcan.entity.Usuario;
import com.sistemparcan.repository.UsuarioRepository;
import com.sistemparcan.service.PasswordService;

/**
 * Servicio de autenticación: verifica las credenciales de un usuario.
 * Usa la versión normalizada del nombre y verificación BCrypt (con
 * compatibilidad hacia atrás para contraseñas antiguas en texto plano).
 */
@Service
public class LoginService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordService passwordService;

    public LoginService(UsuarioRepository usuarioRepository, PasswordService passwordService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordService = passwordService;
    }

    // Verifica las credenciales y devuelve el resultado del login
    public LoginResponse autenticar(LoginRequest request) {
        String normalizado = normalizar(request.getUsuario());
        Usuario usuario = usuarioRepository.findByUsuarioNormalizado(normalizado).orElse(null);

        if (usuario == null) {
            return new LoginResponse(false, "Usuario o contraseña incorrectos", null, null);
        }
        if (Boolean.TRUE.equals(usuario.getEliminado())) {
            return new LoginResponse(false, "Usuario desactivado", null, null);
        }
        if (!passwordService.verificar(request.getContrasena(), usuario.getContrasena())) {
            return new LoginResponse(false, "Usuario o contraseña incorrectos", null, null);
        }
        return new LoginResponse(true, "Autenticación correcta", usuario.getIdContraseña(), usuario.getUsuario());
    }

    private String normalizar(String valor) {
        if (valor == null) return "";
        return valor.trim().toLowerCase().replaceAll("\\s+", "");
    }
}
