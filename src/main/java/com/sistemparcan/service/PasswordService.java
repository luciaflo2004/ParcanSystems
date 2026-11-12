package com.sistemparcan.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio de contraseñas: hashing seguro (BCrypt) y huella HMAC para
 * detectar reutilización de contraseñas sin exponerlas ni compararlas en claro.
 */
@Service
public class PasswordService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Clave secreta para la huella HMAC. Se toma de la variable de entorno;
    // si no está definida se usa una clave de desarrollo (solo para pruebas locales).
    private final String claveHuella;

    public PasswordService() {
        String clave = System.getenv("PASSWORD_HMAC_KEY");
        this.claveHuella = (clave != null && !clave.isBlank()) ? clave : "clave-desarrollo-sistemparcan";
    }

    /** Genera el hash BCrypt de una contraseña en texto plano. */
    public String hash(String contrasena) {
        return encoder.encode(contrasena);
    }

    /** Indica si un valor ya es un hash BCrypt. */
    public boolean esHash(String valor) {
        if (valor == null || valor.isEmpty()) return false;
        return valor.startsWith("$2a$") || valor.startsWith("$2b$") || valor.startsWith("$2y$");
    }

    /**
     * Verifica una contraseña contra el valor guardado. Mantiene compatibilidad
     * con contraseñas antiguas almacenadas en texto plano (no rehasheadas todavía).
     */
    public boolean verificar(String contrasena, String guardado) {
        if (guardado == null || guardado.isEmpty()) return false;
        if (esHash(guardado)) {
            try {
                return encoder.matches(contrasena, guardado);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        // Contraseña antigua en texto plano (compatibilidad hacia atrás)
        return guardado.equals(contrasena);
    }

    /**
     * Calcula la huella HMAC-SHA256 de una contraseña en texto plano.
     * Sirve para detectar reutilización entre usuarios sin almacenar la contraseña.
     */
    public String huella(String contrasena) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(claveHuella.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            byte[] hmac = mac.doFinal(contrasena.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmac);
        } catch (Exception e) {
            return null;
        }
    }
}
