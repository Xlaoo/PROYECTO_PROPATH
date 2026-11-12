package com.propath.dto;

import com.propath.modelo.Estudiante;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object que encapsula los indicadores, visualizaciones y segmentadores
 * del Dashboard de Gestión de Estudiantes.
 */
public class DashboardEstudiantesDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // Segmentadores / Filtros aplicados
    private String universidad = "TODOS";
    private String carrera = "TODOS";
    private int ciclo = 0;
    private String estadoCuenta = "TODOS";
    private String cv = "TODOS";
    private String fechaDesde = "";
    private String fechaHasta = "";
    private String busqueda = "";
    private boolean filtrosActivos = false;

    // Opciones dinámicas para los selectores
    private List<String> opcionesUniversidades = new ArrayList<>();
    private List<String> opcionesCarreras = new ArrayList<>();

    // Indicadores clave (KPIs)
    private int totalEstudiantes = 0;
    private int activos = 0;
    private int inactivos = 0; // Baja lógica
    private int bloqueados = 0;
    private int conCv = 0;
    private int sinCv = 0;
    private int totalUniversidades = 0;
    private int totalCarreras = 0;
    private int registrosPeriodo = 0;

    // Visualizaciones (Mapas ordenados para renderizado visual)
    private Map<String, Integer> distribucionUniversidades = new LinkedHashMap<>();
    private Map<String, Integer> distribucionCarreras = new LinkedHashMap<>();
    private Map<Integer, Integer> distribucionCiclos = new LinkedHashMap<>();
    private Map<String, Integer> distribucionEstados = new LinkedHashMap<>();
    private Map<String, Integer> distribucionCv = new LinkedHashMap<>();
    private Map<String, Integer> tendenciaMeses = new LinkedHashMap<>();

    // Lista de perfiles que cumplen exactamente con los segmentadores
    private List<Estudiante> estudiantesFiltrados = new ArrayList<>();

    public DashboardEstudiantesDTO() {
    }

    // Getters y Setters
    public String getUniversidad() { return universidad != null ? universidad : "TODOS"; }
    public void setUniversidad(String universidad) { this.universidad = universidad; }

    public String getCarrera() { return carrera != null ? carrera : "TODOS"; }
    public void setCarrera(String carrera) { this.carrera = carrera; }

    public int getCiclo() { return ciclo; }
    public void setCiclo(int ciclo) { this.ciclo = ciclo; }

    public String getEstadoCuenta() { return estadoCuenta != null ? estadoCuenta : "TODOS"; }
    public void setEstadoCuenta(String estadoCuenta) { this.estadoCuenta = estadoCuenta; }

    public String getCv() { return cv != null ? cv : "TODOS"; }
    public void setCv(String cv) { this.cv = cv; }

    public String getFechaDesde() { return fechaDesde != null ? fechaDesde : ""; }
    public void setFechaDesde(String fechaDesde) { this.fechaDesde = fechaDesde; }

    public String getFechaHasta() { return fechaHasta != null ? fechaHasta : ""; }
    public void setFechaHasta(String fechaHasta) { this.fechaHasta = fechaHasta; }

    public String getBusqueda() { return busqueda != null ? busqueda : ""; }
    public void setBusqueda(String busqueda) { this.busqueda = busqueda; }

    public boolean isFiltrosActivos() { return filtrosActivos; }
    public void setFiltrosActivos(boolean filtrosActivos) { this.filtrosActivos = filtrosActivos; }

    public List<String> getOpcionesUniversidades() { return opcionesUniversidades; }
    public void setOpcionesUniversidades(List<String> opcionesUniversidades) { this.opcionesUniversidades = opcionesUniversidades; }

    public List<String> getOpcionesCarreras() { return opcionesCarreras; }
    public void setOpcionesCarreras(List<String> opcionesCarreras) { this.opcionesCarreras = opcionesCarreras; }

    public int getTotalEstudiantes() { return totalEstudiantes; }
    public void setTotalEstudiantes(int totalEstudiantes) { this.totalEstudiantes = totalEstudiantes; }

    public int getActivos() { return activos; }
    public void setActivos(int activos) { this.activos = activos; }

    public int getInactivos() { return inactivos; }
    public void setInactivos(int inactivos) { this.inactivos = inactivos; }

    public int getBloqueados() { return bloqueados; }
    public void setBloqueados(int bloqueados) { this.bloqueados = bloqueados; }

    public int getConCv() { return conCv; }
    public void setConCv(int conCv) { this.conCv = conCv; }

    public int getSinCv() { return sinCv; }
    public void setSinCv(int sinCv) { this.sinCv = sinCv; }

    public int getTotalUniversidades() { return totalUniversidades; }
    public void setTotalUniversidades(int totalUniversidades) { this.totalUniversidades = totalUniversidades; }

    public int getTotalCarreras() { return totalCarreras; }
    public void setTotalCarreras(int totalCarreras) { this.totalCarreras = totalCarreras; }

    public int getRegistrosPeriodo() { return registrosPeriodo; }
    public void setRegistrosPeriodo(int registrosPeriodo) { this.registrosPeriodo = registrosPeriodo; }

    public Map<String, Integer> getDistribucionUniversidades() { return distribucionUniversidades; }
    public void setDistribucionUniversidades(Map<String, Integer> distribucionUniversidades) { this.distribucionUniversidades = distribucionUniversidades; }

    public Map<String, Integer> getDistribucionCarreras() { return distribucionCarreras; }
    public void setDistribucionCarreras(Map<String, Integer> distribucionCarreras) { this.distribucionCarreras = distribucionCarreras; }

    public Map<Integer, Integer> getDistribucionCiclos() { return distribucionCiclos; }
    public void setDistribucionCiclos(Map<Integer, Integer> distribucionCiclos) { this.distribucionCiclos = distribucionCiclos; }

    public Map<String, Integer> getDistribucionEstados() { return distribucionEstados; }
    public void setDistribucionEstados(Map<String, Integer> distribucionEstados) { this.distribucionEstados = distribucionEstados; }

    public Map<String, Integer> getDistribucionCv() { return distribucionCv; }
    public void setDistribucionCv(Map<String, Integer> distribucionCv) { this.distribucionCv = distribucionCv; }

    public Map<String, Integer> getTendenciaMeses() { return tendenciaMeses; }
    public void setTendenciaMeses(Map<String, Integer> tendenciaMeses) { this.tendenciaMeses = tendenciaMeses; }

    public List<Estudiante> getEstudiantesFiltrados() { return estudiantesFiltrados; }
    public void setEstudiantesFiltrados(List<Estudiante> estudiantesFiltrados) { this.estudiantesFiltrados = estudiantesFiltrados; }

    /**
     * Calcula el porcentaje respecto al total de estudiantes filtrados
     */
    public int getPorcentaje(int valor) {
        if (totalEstudiantes <= 0) return 0;
        return (int) Math.round(((double) valor * 100.0) / totalEstudiantes);
    }

    /**
     * Serializa las métricas y series del Dashboard a JSON para consumo vía fetch asíncrono.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"total\":").append(totalEstudiantes).append(",");
        sb.append("\"activos\":").append(activos).append(",");
        sb.append("\"inactivos\":").append(inactivos).append(",");
        sb.append("\"bloqueados\":").append(bloqueados).append(",");
        sb.append("\"conCv\":").append(conCv).append(",");
        sb.append("\"sinCv\":").append(sinCv).append(",");
        sb.append("\"totalUniversidades\":").append(totalUniversidades).append(",");
        sb.append("\"totalCarreras\":").append(totalCarreras).append(",");

        // distribucionUniversidades
        sb.append("\"distribucionUniversidades\":[");
        int i = 0;
        for (Map.Entry<String, Integer> e : distribucionUniversidades.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("{\"nombre\":\"").append(escapeJson(e.getKey())).append("\",\"cantidad\":").append(e.getValue())
              .append(",\"porcentaje\":").append(getPorcentaje(e.getValue())).append("}");
        }
        sb.append("],");

        // distribucionCarreras
        sb.append("\"distribucionCarreras\":[");
        i = 0;
        for (Map.Entry<String, Integer> e : distribucionCarreras.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("{\"nombre\":\"").append(escapeJson(e.getKey())).append("\",\"cantidad\":").append(e.getValue())
              .append(",\"porcentaje\":").append(getPorcentaje(e.getValue())).append("}");
        }
        sb.append("],");

        // distribucionCiclos
        sb.append("\"distribucionCiclos\":[");
        i = 0;
        for (Map.Entry<Integer, Integer> e : distribucionCiclos.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("{\"ciclo\":").append(e.getKey()).append(",\"cantidad\":").append(e.getValue())
              .append(",\"porcentaje\":").append(getPorcentaje(e.getValue())).append("}");
        }
        sb.append("],");

        // distribucionEstados
        sb.append("\"distribucionEstados\":[");
        i = 0;
        for (Map.Entry<String, Integer> e : distribucionEstados.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("{\"nombre\":\"").append(escapeJson(e.getKey())).append("\",\"cantidad\":").append(e.getValue())
              .append(",\"porcentaje\":").append(getPorcentaje(e.getValue())).append("}");
        }
        sb.append("],");

        // distribucionCv
        sb.append("\"distribucionCv\":[");
        i = 0;
        for (Map.Entry<String, Integer> e : distribucionCv.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("{\"nombre\":\"").append(escapeJson(e.getKey())).append("\",\"cantidad\":").append(e.getValue())
              .append(",\"porcentaje\":").append(getPorcentaje(e.getValue())).append("}");
        }
        sb.append("],");

        sb.append("\"tendenciaMeses\":[");
        i = 0;
        for (Map.Entry<String, Integer> entry : tendenciaMeses.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("{\"mes\":\"").append(escapeJson(entry.getKey()))
              .append("\",\"cantidad\":").append(entry.getValue()).append("}");
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

