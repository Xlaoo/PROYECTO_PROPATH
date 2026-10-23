package com.propath.modelo;

import java.io.Serializable;

public class OfertaPractica implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int idEmpresa;
    private String titulo;
    private String empresa;
    private String carrera;
    private String modalidad;
    private int vacantes;
    private String fechaPublicacion;
    private String estado;
private String fechaLimite;
private String ubicacion;
private String descripcion;
private String requisitos;
private String remuneracion;
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

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
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
    public String getFechaLimite() {
    return fechaLimite;
}

public void setFechaLimite(String fechaLimite) {
    this.fechaLimite = fechaLimite;
}

public String getUbicacion() {
    return ubicacion;
}

public void setUbicacion(String ubicacion) {
    this.ubicacion = ubicacion;
}

public String getDescripcion() {
    return descripcion;
}

public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
}
public String getRequisitos() {
    return requisitos;
}

public void setRequisitos(String requisitos) {
    this.requisitos = requisitos;
}
public String getRemuneracion() {
    return remuneracion;
}

public void setRemuneracion(String remuneracion) {
    this.remuneracion = remuneracion;
}
    
}
