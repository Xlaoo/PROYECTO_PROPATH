package com.propath.modelo;

import java.io.Serializable;

/**
 * Modelo para representar a las empresas empleadoras en ProPath.
 */
public class Empresa implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String razonSocial;
    private String ruc;
    private String correo;
    private String telefono;
    private String estado;

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
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
