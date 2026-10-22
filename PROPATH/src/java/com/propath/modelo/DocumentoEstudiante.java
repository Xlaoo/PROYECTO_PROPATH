package com.propath.modelo;

import java.io.Serializable;

public class DocumentoEstudiante implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int idEstudiante;
    private String tipoDocumento;
    private String nombreArchivo;
    private long tamanioBytes;
    private String contentType;
    private String fechaSubida;

    public DocumentoEstudiante() {
    }

    public DocumentoEstudiante(int id, int idEstudiante, String tipoDocumento, String nombreArchivo, long tamanioBytes, String contentType, String fechaSubida) {
        this.id = id;
        this.idEstudiante = idEstudiante;
        this.tipoDocumento = tipoDocumento;
        this.nombreArchivo = nombreArchivo;
        this.tamanioBytes = tamanioBytes;
        this.contentType = contentType;
        this.fechaSubida = fechaSubida;
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

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public long getTamanioBytes() {
        return tamanioBytes;
    }

    public void setTamanioBytes(long tamanioBytes) {
        this.tamanioBytes = tamanioBytes;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(String fechaSubida) {
        this.fechaSubida = fechaSubida;
    }

    public String getTamanioFormateado() {
        if (tamanioBytes <= 0) return "0 KB";
        if (tamanioBytes < 1024 * 1024) {
            return String.format("%.1f KB", (double) tamanioBytes / 1024.0);
        }
        return String.format("%.2f MB", (double) tamanioBytes / (1024.0 * 1024.0));
    }
}
