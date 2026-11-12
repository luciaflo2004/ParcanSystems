package com.sistemparcan.login;

/**
 * Petición de registro de cuenta: cédula del funcionario, cargo autorizado,
 * nombre de usuario y contraseña.
 */
public class RegistroRequest {

    private Integer ciFuncionario;
    private Integer codCargo;
    private String usuario;
    private String contrasena;

    public RegistroRequest() {
    }

    public Integer getCiFuncionario() {
        return ciFuncionario;
    }

    public void setCiFuncionario(Integer ciFuncionario) {
        this.ciFuncionario = ciFuncionario;
    }

    public Integer getCodCargo() {
        return codCargo;
    }

    public void setCodCargo(Integer codCargo) {
        this.codCargo = codCargo;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
