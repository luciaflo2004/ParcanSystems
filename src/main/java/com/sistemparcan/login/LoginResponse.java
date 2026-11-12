package com.sistemparcan.login;

/**
 * Respuesta del inicio de sesión. No incluye la contraseña ni su hash.
 */
public class LoginResponse {

    private boolean exito;
    private String mensaje;
    private Integer idContraseña;
    private String usuario;

    public LoginResponse() {
    }

    public LoginResponse(boolean exito, String mensaje, Integer idContraseña, String usuario) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.idContraseña = idContraseña;
        this.usuario = usuario;
    }

    public boolean isExito() {
        return exito;
    }

    public void setExito(boolean exito) {
        this.exito = exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Integer getIdContraseña() {
        return idContraseña;
    }

    public void setIdContraseña(Integer idContraseña) {
        this.idContraseña = idContraseña;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}
