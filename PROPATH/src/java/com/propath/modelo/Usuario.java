package com.propath.modelo;

import java.io.Serializable;

public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nombre;
    private String apellido;
    private String correo;
    private String rol;
    private String estado;
    private Integer idEstudiante;
    private Integer idEmpresa;
    private String razonSocial;
    private String fechaRegistro;

    public Usuario() {
    }

    public Usuario(int id, String nombre, String apellido, String correo, String rol, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.rol = rol;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido != null ? apellido : "";
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getApellidos() {
        return getApellido();
    }

    public void setApellidos(String apellidos) {
        this.apellido = apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(Integer idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public Integer getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Integer idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public boolean esAdminPrincipal() {
        return this.id == 60 || ("ADMIN".equalsIgnoreCase(this.rol) && "admin@propath.com".equalsIgnoreCase(this.correo));
    }

    public String getNombreCompleto() {
        String n = nombre != null ? nombre.trim() : "";
        String a = apellido != null ? apellido.trim() : "";
        if (n.isEmpty()) return a;
        if (a.isEmpty()) return n;
        return n + " " + a;
    }

    public String getFechaRegistro() {
        return fechaRegistro != null && !fechaRegistro.isEmpty() ? fechaRegistro : "Registrado";
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
