package com.propath.modelo;

import java.io.Serializable;

public class Empresa implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String razonSocial;
    private String ruc;
    private String correo;
    private String telefono;
    private String direccion;
    private String descripcion;
    private String estado;
    private String representanteNombre;
    private String representanteApellidos;
    private int idUsuario;
    private String estadoCuenta;

    public Empresa() {
    }

    public Empresa(int id, String razonSocial, String ruc, String correo, String telefono, String estado) {
        this.id = id;
        this.razonSocial = razonSocial;
        this.ruc = ruc;
        this.correo = correo;
        this.telefono = telefono;
        this.estado = estado;
    }

    public Empresa(int id, String razonSocial, String ruc, String correo, String telefono, String direccion, String descripcion, String estado, String representanteNombre, String representanteApellidos) {
        this.id = id;
        this.razonSocial = razonSocial;
        this.ruc = ruc;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.descripcion = descripcion;
        this.estado = estado;
        this.representanteNombre = representanteNombre;
        this.representanteApellidos = representanteApellidos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono != null ? telefono : "";
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion != null ? direccion : "";
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDescripcion() {
        return descripcion != null ? descripcion : "";
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getRepresentanteNombre() {
        return representanteNombre != null ? representanteNombre : "";
    }

    public void setRepresentanteNombre(String representanteNombre) {
        this.representanteNombre = representanteNombre;
    }

    public String getRepresentanteApellidos() {
        return representanteApellidos != null ? representanteApellidos : "";
    }

    public void setRepresentanteApellidos(String representanteApellidos) {
        this.representanteApellidos = representanteApellidos;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getEstadoCuenta() {
        return estadoCuenta != null && !estadoCuenta.isEmpty() ? estadoCuenta : "ACTIVO";
    }

    public void setEstadoCuenta(String estadoCuenta) {
        this.estadoCuenta = estadoCuenta;
    }

    public String getRepresentanteCompleto() {
        String n = representanteNombre != null ? representanteNombre.trim() : "";
        String a = representanteApellidos != null ? representanteApellidos.trim() : "";
        if (n.isEmpty()) return a;
        if (a.isEmpty()) return n;
        return n + " " + a;
    }
}
