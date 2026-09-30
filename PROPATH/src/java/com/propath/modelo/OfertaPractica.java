package com.propath.modelo;

import java.io.Serializable;

/**
 * Modelo para representar ofertas de prácticas preprofesionales en ProPath.
 */
public class OfertaPractica implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String titulo;
    private String empresa;
    private String carrera;
    private String modalidad;
    private int vacantes;
    private String fechaPublicacion;
    private String estado;

    public OfertaPractica() {
    }

    public OfertaPractica(int id, String titulo, String empresa, String carrera, String modalidad, int vacantes, String fechaPublicacion, String estado) {
        this.id = id;
        this.titulo = titulo;
        this.empresa = empresa;
        this.carrera = carrera;
        this.modalidad = modalidad;
        this.vacantes = vacantes;
        this.fechaPublicacion = fechaPublicacion;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getCarrera() {
        return carrera;
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
    }

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public int getVacantes() {
        return vacantes;
    }

    public void setVacantes(int vacantes) {
        this.vacantes = vacantes;
    }

    public String getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(String fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
