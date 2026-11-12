package com.sistemparcan.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.sistemparcan.entity.Usuario;
import com.sistemparcan.repository.UsuarioRepository;
import com.sistemparcan.service.PasswordService;

/**
 * Componente que, al iniciar la aplicación, rellena los campos de seguridad
 * de los usuarios existentes:
 * - usuarioNormalizado: versión en minúsculas y sin espacios del nombre.
 * - huellaContrasena: huella HMAC de la contraseña (solo si está en texto plano).
 * Es idempotente: no modifica contraseñas (no las hashea automáticamente).
 */
@Component
public class UsuarioSecurityMigration implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordService passwordService;

    public UsuarioSecurityMigration(UsuarioRepository usuarioRepository, PasswordService passwordService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordService = passwordService;
    }

    @Override
    public void run(String... args) {
        usuarioRepository.findAll().forEach(this::migrar);
    }

    // Completa los campos de seguridad que falten en un usuario
    private void migrar(Usuario usuario) {
        boolean changed = false;

        if ((usuario.getUsuarioNormalizado() == null || usuario.getUsuarioNormalizado().isEmpty())
                && usuario.getUsuario() != null) {
            usuario.setUsuarioNormalizado(normalizar(usuario.getUsuario()));
            changed = true;
        }

        if (usuario.getHuellaContrasena() == null
                && usuario.getContrasena() != null
                && !passwordService.esHash(usuario.getContrasena())) {
            usuario.setHuellaContrasena(passwordService.huella(usuario.getContrasena()));
            changed = true;
        }

        if (changed) {
            usuarioRepository.save(usuario);
        }
    }

    private String normalizar(String valor) {
        if (valor == null) return "";
        return valor.trim().toLowerCase().replaceAll("\\s+", "");
    }
}
