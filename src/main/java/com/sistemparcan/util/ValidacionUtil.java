package com.sistemparcan.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Utilidades de validación reutilizables para campos de texto y números.
 * Lanzan ResponseStatusException con mensajes claros cuando un valor es inválido.
 */
public final class ValidacionUtil {

    private ValidacionUtil() {
    }

    /** Valida que el texto no supere la longitud máxima indicada (los espacios cuentan). */
    public static void validarLongitud(String valor, int maximo, String mensaje) {
        if (valor != null && valor.length() > maximo) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
        }
    }

    /** Valida que no haya más de dos espacios consecutivos. */
    public static void validarEspaciosConsecutivos(String valor) {
        if (valor != null && valor.matches(".* {3,}.*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se permiten más de dos espacios consecutivos");
        }
    }

    /**
     * Valida que no haya más de "maximo" caracteres idénticos consecutivos.
     * Ignora diferencias entre mayúsculas y minúsculas.
     */
    public static void validarCaracteresConsecutivos(String valor, int maximo, String mensaje) {
        if (valor == null || valor.length() <= maximo) return;
        String s = valor.toLowerCase();
        int count = 1;
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) == s.charAt(i - 1)) {
                count++;
                if (count > maximo) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
                }
            } else {
                count = 1;
            }
        }
    }

    /** Valida que el valor contenga únicamente letras (con tildes y ñ) y espacios. */
    public static void validarSoloLetras(String valor, String mensaje) {
        if (valor != null && !valor.isEmpty() && !valor.matches("[A-Za-zÁÉÍÓÚáéíóúÑñÜü ]+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
        }
    }

    /** Valida que el valor contenga únicamente dígitos. */
    public static void validarSoloNumeros(String valor, String mensaje) {
        if (valor != null && !valor.isEmpty() && !valor.matches("[0-9]+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
        }
    }

    /** Valida que no haya más de "maximo" números idénticos consecutivos. */
    public static void validarNumerosConsecutivos(String valor, int maximo, String mensaje) {
        if (valor == null || valor.length() <= maximo) return;
        int count = 1;
        for (int i = 1; i < valor.length(); i++) {
            if (valor.charAt(i) == valor.charAt(i - 1)) {
                count++;
                if (count > maximo) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
                }
            } else {
                count = 1;
            }
        }
    }
}
