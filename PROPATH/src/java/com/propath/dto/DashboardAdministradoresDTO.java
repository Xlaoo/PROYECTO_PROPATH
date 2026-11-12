package com.propath.dto;

import com.propath.modelo.Administrador;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object que encapsula los indicadores, visualizaciones y segmentadores
 * del Dashboard de Gestión de Administradores.
 */
public class DashboardAdministradoresDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // Segmentadores / Filtros aplicados
    private String estado = "TODOS";
    private String cargo = "TODOS";
    private boolean filtrosActivos = false;

    // Opciones dinámicas para los selectores
    private List<String> opcionesCargos = new ArrayList<>();

    // Indicadores clave (KPIs)
    private int totalAdministradores = 0;
    private int activos = 0;
    private int inactivos = 0; // Baja lógica
    private int bloqueados = 0;

    // Visualizaciones
    private Map<String, Integer> distribucionEstados = new LinkedHashMap<>();
    private Map<String, Integer> distribucionCargos = new LinkedHashMap<>();

    // Lista de administradores que cumplen con los filtros
    private List<Administrador> administradoresFiltrados = new ArrayList<>();

    public DashboardAdministradoresDTO() {
    }

    public String getEstado() {
        return estado != null ? estado : "TODOS";
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCargo() {
        return cargo != null ? cargo : "TODOS";
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public boolean isFiltrosActivos() {
        return filtrosActivos;
    }

    public void setFiltrosActivos(boolean filtrosActivos) {
        this.filtrosActivos = filtrosActivos;
    }

    public List<String> getOpcionesCargos() {
        return opcionesCargos;
    }

    public void setOpcionesCargos(List<String> opcionesCargos) {
        this.opcionesCargos = opcionesCargos;
    }

    public int getTotalAdministradores() {
        return totalAdministradores;
    }

    public void setTotalAdministradores(int totalAdministradores) {
        this.totalAdministradores = totalAdministradores;
    }

    public int getActivos() {
        return activos;
    }

    public void setActivos(int activos) {
        this.activos = activos;
    }

    public int getInactivos() {
        return inactivos;
    }

    public void setInactivos(int inactivos) {
        this.inactivos = inactivos;
    }

    public int getBloqueados() {
        return bloqueados;
    }

    public void setBloqueados(int bloqueados) {
        this.bloqueados = bloqueados;
    }

    public Map<String, Integer> getDistribucionEstados() {
        return distribucionEstados;
    }

    public void setDistribucionEstados(Map<String, Integer> distribucionEstados) {
        this.distribucionEstados = distribucionEstados;
    }

    public Map<String, Integer> getDistribucionCargos() {
        return distribucionCargos;
    }

    public void setDistribucionCargos(Map<String, Integer> distribucionCargos) {
        this.distribucionCargos = distribucionCargos;
    }

    public List<Administrador> getAdministradoresFiltrados() {
        return administradoresFiltrados;
    }

    public void setAdministradoresFiltrados(List<Administrador> administradoresFiltrados) {
        this.administradoresFiltrados = administradoresFiltrados;
    }

    public int getPorcentaje(int valor) {
        if (totalAdministradores <= 0) return 0;
        return (int) Math.round((valor * 100.0) / totalAdministradores);
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"total\":").append(totalAdministradores).append(",");
        sb.append("\"activos\":").append(activos).append(",");
        sb.append("\"inactivos\":").append(inactivos).append(",");
        sb.append("\"bloqueados\":").append(bloqueados).append(",");
        sb.append("\"estado\":\"").append(escapeJson(estado)).append("\",");
        sb.append("\"cargo\":\"").append(escapeJson(cargo)).append("\",");
        sb.append("\"filtrosActivos\":").append(filtrosActivos).append(",");

        // distribucionEstados
        sb.append("\"distribucionEstados\":[");
        int i = 0;
        for (Map.Entry<String, Integer> e : distribucionEstados.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("{\"estado\":\"").append(escapeJson(e.getKey())).append("\",\"cantidad\":").append(e.getValue())
              .append(",\"porcentaje\":").append(getPorcentaje(e.getValue())).append("}");
        }
        sb.append("],");

        // distribucionCargos
        sb.append("\"distribucionCargos\":[");
        i = 0;
        for (Map.Entry<String, Integer> e : distribucionCargos.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("{\"cargo\":\"").append(escapeJson(e.getKey())).append("\",\"cantidad\":").append(e.getValue())
              .append(",\"porcentaje\":").append(getPorcentaje(e.getValue())).append("}");
        }
        sb.append("],");

        // opcionesCargos
        sb.append("\"opcionesCargos\":[");
        i = 0;
        for (String c : opcionesCargos) {
            if (i++ > 0) sb.append(",");
            sb.append("\"").append(escapeJson(c)).append("\"");
        }
        sb.append("],");

        // administradoresFiltrados
        sb.append("\"administradores\":[");
        i = 0;
        for (Administrador a : administradoresFiltrados) {
            if (i++ > 0) sb.append(",");
            sb.append("{");
            sb.append("\"id\":").append(a.getId()).append(",");
            sb.append("\"idUsuario\":").append(a.getIdUsuario()).append(",");
            sb.append("\"nombre\":\"").append(escapeJson(a.getNombre())).append("\",");
            sb.append("\"apellidos\":\"").append(escapeJson(a.getApellidos())).append("\",");
            sb.append("\"nombreCompleto\":\"").append(escapeJson(a.getNombreCompleto())).append("\",");
            sb.append("\"correo\":\"").append(escapeJson(a.getCorreo())).append("\",");
            sb.append("\"cargo\":\"").append(escapeJson(a.getCargo())).append("\",");
            sb.append("\"estado\":\"").append(escapeJson(a.getEstado())).append("\",");
            sb.append("\"fechaRegistro\":\"").append(escapeJson(a.getFechaRegistro())).append("\"");
            sb.append("}");
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

