
package com.propath.modelo;

import java.io.Serializable;

public class Postulacion implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int idEstudiante;
    private int idOferta;
    private String estudiante;
    private String oferta;
    private String empresa;
    private String fechaPostulacion;
    private String estado;
    private String motivoRechazo;
    private int idEmpresa;
    private String carreraEstudiante;
    private String universidadEstudiante;
    private boolean cvDisponible;

    public Postulacion() {
    }

    public Postulacion(int id, String estudiante, String oferta, String empresa, String fechaPostulacion,
            String estado) {
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

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public int getIdOferta() {
        return idOferta;
    }

    public void setIdOferta(int idOferta) {
        this.idOferta = idOferta;
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

    public String getTituloOferta() {
        return oferta;
    }

    public void setTituloOferta(String tituloOferta) {
        this.oferta = tituloOferta;
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

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getCarreraEstudiante() {
        return carreraEstudiante;
    }

    public void setCarreraEstudiante(String carreraEstudiante) {
        this.carreraEstudiante = carreraEstudiante;
    }

    public String getUniversidadEstudiante() {
        return universidadEstudiante;
    }

    public void setUniversidadEstudiante(String universidadEstudiante) {
        this.universidadEstudiante = universidadEstudiante;
    }

    public boolean isCvDisponible() {
        return cvDisponible;
    }

    public void setCvDisponible(boolean cvDisponible) {
        this.cvDisponible = cvDisponible;
    }
}
