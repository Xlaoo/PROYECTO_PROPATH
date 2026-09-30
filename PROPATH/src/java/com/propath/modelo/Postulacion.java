package com.propath.modelo;

import java.io.Serializable;

/**
 * Modelo para representar una postulación a una oferta de prácticas en ProPath.
 */
public class Postulacion implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String estudiante;
    private String oferta;
    private String empresa;
    private String fechaPostulacion;
    private String estado;

    public Postulacion() {
    }

    public Postulacion(int id, String estudiante, String oferta, String empresa, String fechaPostulacion, String estado) {
        this.id = id;
        this.estudiante = estudiante;
        this.oferta = oferta;
        this.empresa = empresa;
        this.fechaPostulacion = fechaPostulacion;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(String estudiante) {
        this.estudiante = estudiante;
    }

    public String getOferta() {
        return oferta;
    }

    public void setOferta(String oferta) {
        this.oferta = oferta;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getFechaPostulacion() {
        return fechaPostulacion;
    }

    public void setFechaPostulacion(String fechaPostulacion) {
        this.fechaPostulacion = fechaPostulacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
