package com.propath.dto;

import com.propath.modelo.Empresa;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object que encapsula los indicadores, visualizaciones y segmentadores
 * del Dashboard de Gestión de Empresas.
 */
public class DashboardEmpresasDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // Segmentadores / Filtros aplicados
    private String estadoPerfil = "TODOS";
    private String estadoCuenta = "TODOS";
    private String conOfertas = "TODOS";
    private String conOfertasActivas = "TODOS";
    private String fechaDesde = "";
    private String fechaHasta = "";
    private String busqueda = "";
    private boolean filtrosActivos = false;

    // Indicadores clave (KPIs)
    private int totalEmpresas = 0;
    private int pendientes = 0;
    private int aprobadas = 0;
    private int rechazadas = 0;
    private int suspendidas = 0;
    private int cuentasActivas = 0;
    private int cuentasInactivas = 0;
    private int cuentasBloqueadas = 0;
    private int empresasConOfertas = 0;
    private int empresasSinOfertas = 0;
    private int empresasConOfertasActivas = 0;
    private int registrosPeriodo = 0;

    // Visualizaciones
    private Map<String, Integer> distribucionEstadoPerfil = new LinkedHashMap<>();
    private Map<String, Integer> distribucionEstadoCuenta = new LinkedHashMap<>();
    private Map<String, Integer> distribucionOfertas = new LinkedHashMap<>();
    private Map<String, Integer> rankingEmpresasOfertas = new LinkedHashMap<>(); // Nombre empresa -> cant ofertas
    private Map<String, Integer> tendenciaMeses = new LinkedHashMap<>();

    // Lista de perfiles filtrados
    private List<Empresa> empresasFiltradas = new ArrayList<>();

    public DashboardEmpresasDTO() {
    }

    // Getters y Setters
    public String getEstadoPerfil() { return estadoPerfil != null ? estadoPerfil : "TODOS"; }
    public void setEstadoPerfil(String estadoPerfil) { this.estadoPerfil = estadoPerfil; }

    public String getEstadoCuenta() { return estadoCuenta != null ? estadoCuenta : "TODOS"; }
    public void setEstadoCuenta(String estadoCuenta) { this.estadoCuenta = estadoCuenta; }

    public String getConOfertas() { return conOfertas != null ? conOfertas : "TODOS"; }
    public void setConOfertas(String conOfertas) { this.conOfertas = conOfertas; }

    public String getConOfertasActivas() { return conOfertasActivas != null ? conOfertasActivas : "TODOS"; }
    public void setConOfertasActivas(String conOfertasActivas) { this.conOfertasActivas = conOfertasActivas; }

    public String getFechaDesde() { return fechaDesde != null ? fechaDesde : ""; }
    public void setFechaDesde(String fechaDesde) { this.fechaDesde = fechaDesde; }

    public String getFechaHasta() { return fechaHasta != null ? fechaHasta : ""; }
    public void setFechaHasta(String fechaHasta) { this.fechaHasta = fechaHasta; }

    public String getBusqueda() { return busqueda != null ? busqueda : ""; }
    public void setBusqueda(String busqueda) { this.busqueda = busqueda; }

    public boolean isFiltrosActivos() { return filtrosActivos; }
    public void setFiltrosActivos(boolean filtrosActivos) { this.filtrosActivos = filtrosActivos; }

    public int getTotalEmpresas() { return totalEmpresas; }
    public void setTotalEmpresas(int totalEmpresas) { this.totalEmpresas = totalEmpresas; }

    public int getPendientes() { return pendientes; }
    public void setPendientes(int pendientes) { this.pendientes = pendientes; }

    public int getAprobadas() { return aprobadas; }
    public void setAprobadas(int aprobadas) { this.aprobadas = aprobadas; }

    public int getRechazadas() { return rechazadas; }
    public void setRechazadas(int rechazadas) { this.rechazadas = rechazadas; }

    public int getSuspendidas() { return suspendidas; }
    public void setSuspendidas(int suspendidas) { this.suspendidas = suspendidas; }

    public int getCuentasActivas() { return cuentasActivas; }
    public void setCuentasActivas(int cuentasActivas) { this.cuentasActivas = cuentasActivas; }

    public int getCuentasInactivas() { return cuentasInactivas; }
    public void setCuentasInactivas(int cuentasInactivas) { this.cuentasInactivas = cuentasInactivas; }

    public int getCuentasBloqueadas() { return cuentasBloqueadas; }
    public void setCuentasBloqueadas(int cuentasBloqueadas) { this.cuentasBloqueadas = cuentasBloqueadas; }

    public int getEmpresasConOfertas() { return empresasConOfertas; }
    public void setEmpresasConOfertas(int empresasConOfertas) { this.empresasConOfertas = empresasConOfertas; }

    public int getEmpresasSinOfertas() { return empresasSinOfertas; }
    public void setEmpresasSinOfertas(int empresasSinOfertas) { this.empresasSinOfertas = empresasSinOfertas; }

    public int getEmpresasConOfertasActivas() { return empresasConOfertasActivas; }
    public void setEmpresasConOfertasActivas(int empresasConOfertasActivas) { this.empresasConOfertasActivas = empresasConOfertasActivas; }

    public int getRegistrosPeriodo() { return registrosPeriodo; }
    public void setRegistrosPeriodo(int registrosPeriodo) { this.registrosPeriodo = registrosPeriodo; }

    public Map<String, Integer> getDistribucionEstadoPerfil() { return distribucionEstadoPerfil; }
    public void setDistribucionEstadoPerfil(Map<String, Integer> distribucionEstadoPerfil) { this.distribucionEstadoPerfil = distribucionEstadoPerfil; }

    public Map<String, Integer> getDistribucionEstadoCuenta() { return distribucionEstadoCuenta; }
    public void setDistribucionEstadoCuenta(Map<String, Integer> distribucionEstadoCuenta) { this.distribucionEstadoCuenta = distribucionEstadoCuenta; }

    public Map<String, Integer> getDistribucionOfertas() { return distribucionOfertas; }
    public void setDistribucionOfertas(Map<String, Integer> distribucionOfertas) { this.distribucionOfertas = distribucionOfertas; }

    public Map<String, Integer> getRankingEmpresasOfertas() { return rankingEmpresasOfertas; }
    public void setRankingEmpresasOfertas(Map<String, Integer> rankingEmpresasOfertas) { this.rankingEmpresasOfertas = rankingEmpresasOfertas; }

    public Map<String, Integer> getTendenciaMeses() { return tendenciaMeses; }
    public void setTendenciaMeses(Map<String, Integer> tendenciaMeses) { this.tendenciaMeses = tendenciaMeses; }

    public List<Empresa> getEmpresasFiltradas() { return empresasFiltradas; }
    public void setEmpresasFiltradas(List<Empresa> empresasFiltradas) { this.empresasFiltradas = empresasFiltradas; }

    public int getPorcentaje(int valor) {
        if (totalEmpresas <= 0) return 0;
        return (int) Math.round(((double) valor * 100.0) / totalEmpresas);
    }

    /**
     * Serializa las métricas y series del Dashboard a JSON para consumo vía fetch asíncrono.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"total\":").append(totalEmpresas).append(",");
        sb.append("\"pendientes\":").append(pendientes).append(",");
        sb.append("\"aprobadas\":").append(aprobadas).append(",");
        sb.append("\"rechazadas\":").append(rechazadas).append(",");
        sb.append("\"suspendidas\":").append(suspendidas).append(",");
        sb.append("\"cuentasActivas\":").append(cuentasActivas).append(",");
        sb.append("\"cuentasInactivas\":").append(cuentasInactivas).append(",");
        sb.append("\"cuentasBloqueadas\":").append(cuentasBloqueadas).append(",");
        sb.append("\"conOfertas\":").append(empresasConOfertas).append(",");
        sb.append("\"sinOfertas\":").append(empresasSinOfertas).append(",");
        sb.append("\"conOfertasActivas\":").append(empresasConOfertasActivas).append(",");

        // distribucionEstadoPerfil
        sb.append("\"distribucionEstadoPerfil\":[");
        int i = 0;
        for (Map.Entry<String, Integer> e : distribucionEstadoPerfil.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("{\"nombre\":\"").append(escapeJson(e.getKey())).append("\",\"cantidad\":").append(e.getValue())
              .append(",\"porcentaje\":").append(getPorcentaje(e.getValue())).append("}");
        }
        sb.append("],");

        // distribucionEstadoCuenta
        sb.append("\"distribucionEstadoCuenta\":[");
        i = 0;
        for (Map.Entry<String, Integer> e : distribucionEstadoCuenta.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("{\"nombre\":\"").append(escapeJson(e.getKey())).append("\",\"cantidad\":").append(e.getValue())
              .append(",\"porcentaje\":").append(getPorcentaje(e.getValue())).append("}");
        }
        sb.append("],");

        // distribucionOfertas
        sb.append("\"distribucionOfertas\":[");
        i = 0;
        for (Map.Entry<String, Integer> e : distribucionOfertas.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("{\"nombre\":\"").append(escapeJson(e.getKey())).append("\",\"cantidad\":").append(e.getValue())
              .append(",\"porcentaje\":").append(getPorcentaje(e.getValue())).append("}");
        }
        sb.append("],");

        // rankingEmpresasOfertas
        sb.append("\"rankingEmpresasOfertas\":[");
        i = 0;
        for (Map.Entry<String, Integer> e : rankingEmpresasOfertas.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("{\"empresa\":\"").append(escapeJson(e.getKey())).append("\",\"ofertas\":").append(e.getValue()).append("}");
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

