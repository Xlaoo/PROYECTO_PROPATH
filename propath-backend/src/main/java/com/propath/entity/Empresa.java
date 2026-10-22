package com.propath.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "empresas")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Empresa implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Estado {
        PENDIENTE, APROBADA, RECHAZADA, SUSPENDIDA;

        @JsonCreator
        public static Estado fromString(String value) {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            String v = value.trim().toUpperCase();
            if ("APROBADA".equals(v) || "APROBADO".equals(v)) {
                return APROBADA;
            }
            if ("PENDIENTE".equals(v)) {
                return PENDIENTE;
            }
            if ("RECHAZADA".equals(v) || "RECHAZADO".equals(v)) {
                return RECHAZADA;
            }
            if ("SUSPENDIDA".equals(v) || "SUSPENDIDO".equals(v) || "BLOQUEADA".equals(v) || "BLOQUEADO".equals(v)) {
                return SUSPENDIDA;
            }
            for (Estado e : Estado.values()) {
                if (e.name().equalsIgnoreCase(v)) {
                    return e;
                }
            }
            return null;
        }

        @JsonValue
        public String toValue() {
            return this.name();
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private Integer id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "ruc", nullable = false, unique = true, length = 11)
    private String ruc;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "descripcion", columnDefinition = "text")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado = Estado.PENDIENTE;

    public Empresa() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
}
