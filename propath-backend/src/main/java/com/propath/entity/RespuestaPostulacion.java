package com.propath.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "respuestas_postulacion")
public class RespuestaPostulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_respuesta")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_postulacion", nullable = false)
    private Postulacion postulacion;

    @Column(name = "numero_pregunta", nullable = false)
    private Integer numeroPregunta;

    @Column(name = "pregunta", nullable = false)
    private String pregunta;

    @Column(name = "respuesta", nullable = false, columnDefinition = "TEXT")
    private String respuesta;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Postulacion getPostulacion() {
        return postulacion;
    }

    public void setPostulacion(Postulacion postulacion) {
        this.postulacion = postulacion;
    }

    public Integer getNumeroPregunta() {
        return numeroPregunta;
    }

    public void setNumeroPregunta(Integer numeroPregunta) {
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