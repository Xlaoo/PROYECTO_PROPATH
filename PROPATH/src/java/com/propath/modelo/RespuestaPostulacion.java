package com.propath.modelo;

import java.io.Serializable;

public class RespuestaPostulacion implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int idPostulacion;
    private int numeroPregunta;
    private String pregunta;
    private String respuesta;

    public RespuestaPostulacion() {
    }

    public RespuestaPostulacion(int idPostulacion, int numeroPregunta, String pregunta, String respuesta) {
        this.idPostulacion = idPostulacion;
        this.numeroPregunta = numeroPregunta;
        this.pregunta = pregunta;
        this.respuesta = respuesta;
    }

    public RespuestaPostulacion(int id, int idPostulacion, int numeroPregunta, String pregunta, String respuesta) {
        this.id = id;
        this.idPostulacion = idPostulacion;
        this.numeroPregunta = numeroPregunta;
        this.pregunta = pregunta;
        this.respuesta = respuesta;
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

    public int getNumeroPregunta() {
        return numeroPregunta;
    }

    public void setNumeroPregunta(int numeroPregunta) {
        this.numeroPregunta = numeroPregunta;
    }

    public String getPregunta() {
        return pregunta;
    }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }
}
