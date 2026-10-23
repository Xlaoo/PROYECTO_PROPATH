package com.propath.modelo;

import java.io.Serializable;

public class Entrevista implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int idPostulacion;
    private String fecha;
    private String hora;
    private String modalidad;
    private String enlaceLugar;
    private String estado;
    private String observaciones;
    private String fechaCreacion;
    private String estudiante;
    private String oferta;
    private String empresa;
    private Integer idEmpresa;
    private Integer idEstudiante;

    public Entrevista() {
    }

    public Entrevista(int id, int idPostulacion, String fecha, String hora, String modalidad, String enlaceLugar, String estado, String observaciones) {
        this.id = id;
        this.idPostulacion = idPostulacion;
        this.fecha = fecha;
        this.hora = hora;
        this.modalidad = modalidad;
        this.enlaceLugar = enlaceLugar;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdPostulacion() {
        return idPostulacion;
    }

    public void setIdPostulacion(int idPostulacion) {
        this.idPostulacion = idPostulacion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getEnlaceLugar() {
        return enlaceLugar;
    }

    public void setEnlaceLugar(String enlaceLugar) {
        this.enlaceLugar = enlaceLugar;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
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

    public Integer getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Integer idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public Integer getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(Integer idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public String getFechaEntrevista() {
        return fecha;
    }

    public void setFechaEntrevista(String fecha) {
        this.fecha = fecha;
    }

    public String getHoraEntrevista() {
        return hora;
    }

    public void setHoraEntrevista(String hora) {
        this.hora = hora;
    }

    public String getLugarEnlace() {
        return enlaceLugar;
    }

    public void setLugarEnlace(String lugarEnlace) {
        this.enlaceLugar = lugarEnlace;
    }
}
