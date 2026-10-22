package com.propath.modelo;

import java.io.Serializable;

public class Estudiante implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nombre;
    private String apellido;
    private String correo;
    private String universidad;
    private String carrera;
    private int ciclo;
    private String telefono;
    private String cv;
    private String estado;

    public Estudiante() {
    }

    public Estudiante(int id, String nombre, String apellido, String correo, String universidad, String carrera, int ciclo, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.universidad = universidad;
        this.carrera = carrera;
        this.ciclo = ciclo;
        this.estado = estado;
    }

    public Estudiante(int id, String nombre, String apellido, String correo, String universidad, String carrera, int ciclo, String telefono, String cv, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.universidad = universidad;
        this.carrera = carrera;
        this.ciclo = ciclo;
        this.telefono = telefono;
        this.cv = cv;
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

    public String getUniversidad() {
        return universidad;
    }

    public void setUniversidad(String universidad) {
        this.universidad = universidad;
    }

    public String getCarrera() {
        return carrera;
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
    }

    public int getCiclo() {
        return ciclo;
    }

    public void setCiclo(int ciclo) {
        this.ciclo = ciclo;
    }

    public String getTelefono() {
        return telefono != null ? telefono : "";
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCv() {
        return cv != null ? cv : "";
    }

    public void setCv(String cv) {
        this.cv = cv;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNombreCompleto() {
        String n = nombre != null ? nombre.trim() : "";
        String a = apellido != null ? apellido.trim() : "";
        if (n.isEmpty()) return a;
        if (a.isEmpty()) return n;
        return n + " " + a;
    }
}
