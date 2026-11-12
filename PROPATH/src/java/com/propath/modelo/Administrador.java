package com.propath.modelo;

import java.io.Serializable;

public class Administrador implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int idUsuario;
    private String cargo;
    private Usuario usuario;

    public Administrador() {
    }

    public Administrador(int id, int idUsuario, String cargo) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.cargo = cargo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNombre() {
        return usuario != null && usuario.getNombre() != null ? usuario.getNombre() : "";
    }

    public String getApellidos() {
        return usuario != null && usuario.getApellidos() != null ? usuario.getApellidos() : "";
    }

    public String getNombreCompleto() {
        return usuario != null ? usuario.getNombreCompleto() : "";
    }

    public String getCorreo() {
        return usuario != null && usuario.getCorreo() != null ? usuario.getCorreo() : "";
    }

    public String getEstado() {
        return usuario != null && usuario.getEstado() != null ? usuario.getEstado() : "ACTIVO";
    }

    public String getFechaRegistro() {
        return usuario != null ? usuario.getFechaRegistro() : "";
    }
}
