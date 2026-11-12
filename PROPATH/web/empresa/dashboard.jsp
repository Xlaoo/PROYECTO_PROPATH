<%!
    private String escaparHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private long getLongStat(java.util.Map<String, Object> m, String key) {
        if (m == null || !m.containsKey(key) || m.get(key) == null) return 0L;
        Object val = m.get(key);
        if (val instanceof Number) return ((Number) val).longValue();
        try { return Long.parseLong(val.toString()); } catch (Exception e) { return 0L; }
    }

    private double getDoubleStat(java.util.Map<String, Object> m, String key) {
        if (m == null || !m.containsKey(key) || m.get(key) == null) return 0.0;
        Object val = m.get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }

    private String badgeEstadoOferta(String est) {
        if ("ACTIVA".equalsIgnoreCase(est)) return "bg-success-subtle text-success border-success-subtle";
        if ("PENDIENTE".equalsIgnoreCase(est)) return "bg-warning-subtle text-warning border-warning-subtle";
        if ("RECHAZADA".equalsIgnoreCase(est)) return "bg-danger-subtle text-danger border-danger-subtle";
        return "bg-secondary-subtle text-secondary border-secondary-subtle";
    }

    private String badgeEstadoPostulacion(String est) {
        if ("SELECCIONADO".equalsIgnoreCase(est)) return "badge-seleccionado";
        if ("EN_REVISION".equalsIgnoreCase(est)) return "badge-en-revision";
        if ("NO_SELECCIONADO".equalsIgnoreCase(est)) return "badge-bloqueado";
        return "badge-postulado";
    }

    private String labelEstadoPostulacion(String est) {
        if ("EN_REVISION".equalsIgnoreCase(est)) return "EN REVISIÓN";
        if ("NO_SELECCIONADO".equalsIgnoreCase(est)) return "NO SELECCIONADO";
        if (est == null || est.trim().isEmpty()) return "POSTULADO";
        return est.toUpperCase();
    }

    private String badgeEstadoEntrevista(String est) {
        if ("REALIZADA".equalsIgnoreCase(est)) return "badge-seleccionado";
        if ("CANCELADA".equalsIgnoreCase(est)) return "badge-bloqueado";
        return "badge-activo";
    }
%>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@page import="com.propath.modelo.Usuario" %>
<%@page import="com.propath.modelo.Empresa" %>
<%@page import="com.propath.modelo.OfertaPractica" %>
<%@page import="com.propath.modelo.Postulacion" %>
<%@page import="com.propath.modelo.Entrevista" %>
<%@page import="com.propath.servicio.BackendCliente" %>
<%@page import="java.util.List" %>
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.Map" %>
<%@page import="java.util.HashMap" %>
<%@page import="java.time.LocalDate" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }

    Empresa miEmpresa = null;
    if (usuario.getIdEmpresa() != null && usuario.getIdEmpresa() > 0) {
        miEmpresa = BackendCliente.obtenerEmpresaPorId(usuario.getIdEmpresa());
    }
    if (miEmpresa == null) {
        List<Empresa> empresas = BackendCliente.listarEmpresas();
        for (Empresa e : empresas) {
            if (e.getCorreo() != null && e.getCorreo().equalsIgnoreCase(usuario.getCorreo())) {
                miEmpresa = e;
                usuario.setIdEmpresa(e.getId());
                break;
            }
        }
    }

    int idEmpresa = miEmpresa != null ? miEmpresa.getId()
            : (usuario.getIdEmpresa() != null ? usuario.getIdEmpresa() : 0);
    String razon = miEmpresa != null && miEmpresa.getRazonSocial() != null ? miEmpresa.getRazonSocial()
            : (usuario.getRazonSocial() != null ? usuario.getRazonSocial() : usuario.getNombre());

    String estadoEmpresa = (miEmpresa != null && miEmpresa.getEstado() != null)
            ? miEmpresa.getEstado().trim() : "PENDIENTE";
    String estadoCuenta = (miEmpresa != null && miEmpresa.getEstadoCuenta() != null)
            ? miEmpresa.getEstadoCuenta().trim()
            : (usuario.getEstado() != null ? usuario.getEstado().trim() : "ACTIVO");
    boolean esActivo = "ACTIVO".equalsIgnoreCase(estadoCuenta);
    boolean esAprobada = "APROBADA".equalsIgnoreCase(estadoEmpresa);
    boolean puedeOperar = esActivo && esAprobada;

    List<OfertaPractica> misOfertas = idEmpresa > 0
            ? BackendCliente.listarOfertasPorEmpresa(idEmpresa) : new ArrayList<>();
    List<Postulacion> misPostulaciones = idEmpresa > 0
            ? BackendCliente.listarPostulacionesPorEmpresa(idEmpresa) : new ArrayList<>();
    List<Entrevista> misEntrevistas = idEmpresa > 0
            ? BackendCliente.listarEntrevistasPorEmpresa(idEmpresa) : new ArrayList<>();
    Map<String, Object> statsEmpresa = idEmpresa > 0
            ? BackendCliente.obtenerEstadisticasEmpresa(idEmpresa) : new HashMap<>();

    long totalOfertasStat = getLongStat(statsEmpresa, "totalOfertas");
    long ofertasActivasStat = getLongStat(statsEmpresa, "ofertasActivas");
    long ofertasCerradasStat = getLongStat(statsEmpresa, "ofertasCerradas");
    long totalVacantesActivas = getLongStat(statsEmpresa, "totalVacantesActivas");

    long totalPostStat = getLongStat(statsEmpresa, "totalPostulaciones");
    long postuladosStat = getLongStat(statsEmpresa, "postulados");
    long enRevisionStat = getLongStat(statsEmpresa, "enRevision");
    long seleccionadosStat = getLongStat(statsEmpresa, "seleccionados");
    long noSeleccionadosStat = getLongStat(statsEmpresa, "noSeleccionados");
    double tasaSeleccionStat = getDoubleStat(statsEmpresa, "tasaSeleccion");
    double tasaRevisionStat = getDoubleStat(statsEmpresa, "tasaRevision");

    long totalEntStat = getLongStat(statsEmpresa, "totalEntrevistas");
    long entProgramadasStat = getLongStat(statsEmpresa, "entrevistasProgramadas");
    long entRealizadasStat = getLongStat(statsEmpresa, "entrevistasRealizadas");
    long entCanceladasStat = getLongStat(statsEmpresa, "entrevistasCanceladas");
    double tasaEntRealizadaStat = getDoubleStat(statsEmpresa, "tasaEntrevistaRealizada");

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> resumenOfertas = (List<Map<String, Object>>) statsEmpresa.get("resumenOfertas");
    if (resumenOfertas == null) resumenOfertas = new ArrayList<>();

    String tabParam = request.getParameter("tab");
    String activeTab = "resumen";
    if ("ofertas".equalsIgnoreCase(tabParam) || "postulantes".equalsIgnoreCase(tabParam)
            || "entrevistas".equalsIgnoreCase(tabParam) || "estadisticas".equalsIgnoreCase(tabParam)) {
        activeTab = tabParam.toLowerCase();
    }

    String exito = request.getParameter("exito");
    String error = request.getParameter("error");
    String hoyIso = LocalDate.now().toString();
%>
<!DOCTYPE html>
<html lang="es" data-bs-theme="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel de Empresa | ProPath</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<div class="admin-layout">
    <!-- Sidebar Empresarial -->
    <aside class="admin-sidebar">
        <div class="sidebar-brand">
            <div class="sidebar-logo-container">
                <a href="${pageContext.request.contextPath}/empresa/dashboard.jsp" class="d-block text-decoration-none">
                    <img src="${pageContext.request.contextPath}/img/logo.png" alt="ProPath" class="sidebar-logo">
                </a>
            </div>
        </div>

        <div class="sidebar-nav-container">
            <div class="sidebar-heading">Gestión Empresarial</div>
            <nav class="nav flex-column" id="empresaSidebarNav">
                <button type="button" class="nav-link-admin btn-nav-section <%= "resumen".equals(activeTab) ? "active" : "" %>"
                        data-section="resumen" onclick="cambiarSeccionEmpresa('resumen')">
                    <i class="bi bi-grid-1x2-fill"></i>
                    <span>Inicio</span>
                </button>
                <button type="button" class="nav-link-admin btn-nav-section <%= "ofertas".equals(activeTab) ? "active" : "" %>"
                        data-section="ofertas" onclick="cambiarSeccionEmpresa('ofertas')">
                    <i class="bi bi-briefcase-fill"></i>
                    <span>Mis Ofertas</span>
                    <span class="nav-badge-count"><%= misOfertas.size() %></span>
                </button>
                <button type="button" class="nav-link-admin btn-nav-section <%= "postulantes".equals(activeTab) ? "active" : "" %>"
                        data-section="postulantes" onclick="cambiarSeccionEmpresa('postulantes')">
                    <i class="bi bi-people-fill"></i>
                    <span>Postulantes</span>
                    <span class="nav-badge-count"><%= misPostulaciones.size() %></span>
                </button>
                <button type="button" class="nav-link-admin btn-nav-section <%= "entrevistas".equals(activeTab) ? "active" : "" %>"
                        data-section="entrevistas" onclick="cambiarSeccionEmpresa('entrevistas')">
                    <i class="bi bi-calendar-check-fill"></i>
                    <span>Entrevistas</span>
                    <span class="nav-badge-count"><%= entProgramadasStat %></span>
                </button>
                <button type="button" class="nav-link-admin btn-nav-section <%= "estadisticas".equals(activeTab) ? "active" : "" %>"
                        data-section="estadisticas" onclick="cambiarSeccionEmpresa('estadisticas')">
                    <i class="bi bi-bar-chart-line-fill"></i>
                    <span>Reportes y Estadísticas</span>
                </button>
            </nav>

            <div class="sidebar-heading mt-3">Mi Cuenta</div>
            <button type="button" class="nav-link-admin btn-nav-section" data-bs-toggle="modal" data-bs-target="#modalEditarPerfilEmpresa">
                <i class="bi bi-building-gear"></i>
                <span>Perfil de Empresa</span>
            </button>
        </div>

        <div class="sidebar-footer">
            <a class="nav-link-admin nav-link-logout" href="${pageContext.request.contextPath}/logout">
                <i class="bi bi-box-arrow-right"></i>
                <span>Cerrar sesión</span>
            </a>
        </div>
    </aside>

    <!-- Contenido Principal -->
    <main class="admin-main">
        <header class="admin-topbar">
            <div class="topbar-left">
                <button class="sidebar-toggle-btn" id="sidebarToggle" type="button" aria-label="Abrir menú">
                    <i class="bi bi-list"></i>
                </button>
                <div class="topbar-welcome">
                    <h1 class="welcome-title h5 mb-0"><%= escaparHtml(razon) %></h1>
                    <span class="welcome-date"><i class="bi bi-building me-1"></i>Portal de Empresa &bull; RUC <%= miEmpresa != null && miEmpresa.getRuc() != null ? escaparHtml(miEmpresa.getRuc()) : "—" %></span>
                </div>
            </div>

            <div class="topbar-right">
                <span class="badge <%= esAprobada ? "bg-success-subtle text-success border border-success-subtle" : "bg-warning-subtle text-warning border border-warning-subtle" %> rounded-pill px-3 py-2 fw-semibold d-none d-sm-inline-flex align-items-center">
                    <i class="bi <%= esAprobada ? "bi-patch-check-fill" : "bi-hourglass-split" %> me-1"></i>
                    <%= escaparHtml(estadoEmpresa) %>
                </span>
                <button type="button" class="btn btn-sm btn-outline-primary rounded-pill px-3 fw-semibold"
                        data-bs-toggle="modal" data-bs-target="#modalEditarPerfilEmpresa">
                    <i class="bi bi-pencil-square me-1"></i>Editar Empresa
                </button>
            </div>
        </header>

        <div class="admin-content">
            <% if (exito != null && !exito.isEmpty()) {
                String msgExito = "Operación completada exitosamente.";
                if ("cerrada_por_postulaciones".equalsIgnoreCase(exito)) {
                    msgExito = "La oferta tiene postulaciones registradas, por lo que fue cerrada (CERRADA) para preservar el historial de postulantes.";
                } else if ("publicada".equalsIgnoreCase(exito)) {
                    msgExito = "Oferta de práctica publicada exitosamente con estado ACTIVA.";
                } else if ("actualizada".equalsIgnoreCase(exito)) {
                    msgExito = "Oferta de práctica actualizada exitosamente.";
                } else if ("eliminada".equalsIgnoreCase(exito)) {
                    msgExito = "Oferta de práctica eliminada exitosamente.";
                } else if ("oferta_cerrada".equalsIgnoreCase(exito)) {
                    msgExito = "La convocatoria ha sido cerrada correctamente. Ya no recibirá nuevas postulaciones.";
                } else if ("oferta_reactivada".equalsIgnoreCase(exito)) {
                    msgExito = "La convocatoria ha sido reactivada exitosamente.";
                } else if ("perfil_actualizado".equalsIgnoreCase(exito)) {
                    msgExito = "Perfil corporativo actualizado exitosamente.";
                } else if ("estado_actualizado".equalsIgnoreCase(exito)) {
                    msgExito = "Estado de la postulación actualizado exitosamente.";
                } else if ("entrevista_guardada".equalsIgnoreCase(exito)) {
                    msgExito = "Entrevista programada/actualizada exitosamente.";
                } else if ("entrevista_eliminada".equalsIgnoreCase(exito)) {
                    msgExito = "Entrevista eliminada del registro.";
                } else if ("entrevista_estado_actualizado".equalsIgnoreCase(exito)) {
                    msgExito = "Estado de la entrevista actualizado exitosamente.";
                }
            %>
            <div class="alert alert-success alert-dismissible fade show rounded-3 mb-4 shadow-sm" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i><%= escaparHtml(msgExito) %>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
            </div>
            <% } %>

            <% if (error != null && !error.isEmpty()) { %>
            <div class="alert alert-danger alert-dismissible fade show rounded-3 mb-4 shadow-sm" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i><%= escaparHtml(error) %>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
            </div>
            <% } %>

            <% if (!puedeOperar) { %>
            <div class="alert <%= !esActivo || "RECHAZADA".equalsIgnoreCase(estadoEmpresa) ? "alert-danger" : "alert-warning" %> rounded-3 mb-4 shadow-sm" role="alert">
                <div class="d-flex align-items-center gap-3">
                    <i class="bi <%= !esActivo ? "bi-shield-x" : ("RECHAZADA".equalsIgnoreCase(estadoEmpresa) ? "bi-x-octagon-fill" : "bi-hourglass-split") %> fs-3"></i>
                    <div>
                        <h2 class="h6 fw-bold mb-1">
                            <%= !esActivo ? "Cuenta Inactiva" : ("RECHAZADA".equalsIgnoreCase(estadoEmpresa) ? "Empresa No Aprobada" : ("SUSPENDIDA".equalsIgnoreCase(estadoEmpresa) ? "Empresa Suspendida" : "Aprobación Institucional Pendiente")) %>
                        </h2>
                        <p class="mb-0 small">
                            <%= !esActivo ? "Tu cuenta corporativa se encuentra inactiva. No puedes realizar operaciones hasta ser reactivada."
                                    : ("RECHAZADA".equalsIgnoreCase(estadoEmpresa) ? "Tu empresa no ha sido aprobada. Las funciones de publicación y gestión están deshabilitadas."
                                    : ("SUSPENDIDA".equalsIgnoreCase(estadoEmpresa) ? "Tu empresa se encuentra suspendida por el administrador."
                                    : "Tu empresa está pendiente de aprobación por el administrador de PROPATH. Puedes editar tu perfil corporativo, pero aún no puedes publicar ofertas ni evaluar postulantes.")) %>
                        </p>
                    </div>
                </div>
            </div>
            <% } %>

            <!-- ============================================================== -->
            <!-- SECCIÓN 1: RESUMEN / INICIO                                    -->
            <!-- ============================================================== -->
            <section id="sec-resumen" class="dashboard-section <%= "resumen".equals(activeTab) ? "active" : "" %>">
                <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
                    <div>
                        <h2 class="h4 fw-bold text-dark mb-1">Resumen de Reclutamiento</h2>
                        <p class="text-muted small mb-0">Estado general de tus convocatorias, postulantes y entrevistas en tiempo real.</p>
                    </div>
                    <div class="d-flex gap-2 flex-wrap">
                        <button type="button" class="btn btn-primary rounded-pill px-4 fw-semibold" onclick="cambiarSeccionEmpresa('ofertas')">
                            <i class="bi bi-plus-circle me-1"></i>Publicar Oferta
                        </button>
                        <button type="button" class="btn btn-outline-primary rounded-pill px-4 fw-semibold" onclick="cambiarSeccionEmpresa('postulantes')">
                            <i class="bi bi-people me-1"></i>Revisar Postulantes
                        </button>
                    </div>
                </div>

                <!-- KPIs -->
                <div class="row g-4 mb-4">
                    <div class="col-12 col-sm-6 col-xl-3">
                        <div class="stat-card stat-card-blue">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <div class="stat-value"><%= ofertasActivasStat %></div>
                                    <h3 class="stat-label">Ofertas Activas</h3>
                                </div>
                                <div class="stat-icon-wrapper"><i class="bi bi-briefcase-fill"></i></div>
                            </div>
                            <div class="mt-2 small text-muted"><%= totalOfertasStat %> totales &bull; <%= totalVacantesActivas %> vacantes</div>
                        </div>
                    </div>
                    <div class="col-12 col-sm-6 col-xl-3">
                        <div class="stat-card stat-card-turquoise">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <div class="stat-value"><%= totalPostStat %></div>
                                    <h3 class="stat-label">Postulaciones Recibidas</h3>
                                </div>
                                <div class="stat-icon-wrapper"><i class="bi bi-people-fill"></i></div>
                            </div>
                            <div class="mt-2 small text-muted"><%= postuladosStat %> por revisar &bull; <%= enRevisionStat %> en revisión</div>
                        </div>
                    </div>
                    <div class="col-12 col-sm-6 col-xl-3">
                        <div class="stat-card stat-card-teal">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <div class="stat-value"><%= seleccionadosStat %></div>
                                    <h3 class="stat-label">Preseleccionados</h3>
                                </div>
                                <div class="stat-icon-wrapper"><i class="bi bi-person-check-fill"></i></div>
                            </div>
                            <div class="mt-2 small text-muted">Tasa de selección: <strong><%= tasaSeleccionStat %>%</strong></div>
                        </div>
                    </div>
                    <div class="col-12 col-sm-6 col-xl-3">
                        <div class="stat-card stat-card-indigo">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <div class="stat-value"><%= entProgramadasStat %></div>
                                    <h3 class="stat-label">Entrevistas Pendientes</h3>
                                </div>
                                <div class="stat-icon-wrapper"><i class="bi bi-calendar-event-fill"></i></div>
                            </div>
                            <div class="mt-2 small text-muted"><%= entRealizadasStat %> realizadas &bull; <%= totalEntStat %> registradas</div>
                        </div>
                    </div>
                </div>

                <div class="row g-4">
                    <div class="col-12 col-lg-7">
                        <!-- Embudo rápido -->
                        <div class="propath-card mb-4">
                            <div class="propath-card-header">
                                <h2 class="propath-card-title"><i class="bi bi-funnel-fill"></i><span>Embudo del Proceso de Selección</span></h2>
                                <button type="button" class="btn btn-sm btn-outline-primary rounded-pill" onclick="cambiarSeccionEmpresa('estadisticas')">
                                    Ver reporte completo <i class="bi bi-arrow-right ms-1"></i>
                                </button>
                            </div>
                            <div class="propath-card-body">
                                <%
                                    long basePost = totalPostStat > 0 ? totalPostStat : 1L;
                                    int pctPost = (int) Math.round((postuladosStat * 100.0) / basePost);
                                    int pctRev = (int) Math.round((enRevisionStat * 100.0) / basePost);
                                    int pctSel = (int) Math.round((seleccionadosStat * 100.0) / basePost);
                                    int pctRech = (int) Math.round((noSeleccionadosStat * 100.0) / basePost);
                                %>
                                <div class="mb-3">
                                    <div class="d-flex justify-content-between small fw-semibold mb-1">
                                        <span>Recibidas / Pendientes (<%= postuladosStat %>)</span>
                                        <span class="text-primary"><%= totalPostStat > 0 ? pctPost : 0 %>%</span>
                                    </div>
                                    <div class="propath-progress-track">
                                        <div class="propath-progress-fill bg-primary" style="width: <%= totalPostStat > 0 ? pctPost : 0 %>%"></div>
                                    </div>
                                </div>
                                <div class="mb-3">
                                    <div class="d-flex justify-content-between small fw-semibold mb-1">
                                        <span>En Revisión (<%= enRevisionStat %>)</span>
                                        <span class="text-warning"><%= totalPostStat > 0 ? pctRev : 0 %>%</span>
                                    </div>
                                    <div class="propath-progress-track">
                                        <div class="propath-progress-fill bg-warning" style="width: <%= totalPostStat > 0 ? pctRev : 0 %>%"></div>
                                    </div>
                                </div>
                                <div class="mb-3">
                                    <div class="d-flex justify-content-between small fw-semibold mb-1">
                                        <span>Seleccionados para Entrevista (<%= seleccionadosStat %>)</span>
                                        <span class="text-success"><%= totalPostStat > 0 ? pctSel : 0 %>%</span>
                                    </div>
                                    <div class="propath-progress-track">
                                        <div class="propath-progress-fill bg-success" style="width: <%= totalPostStat > 0 ? pctSel : 0 %>%"></div>
                                    </div>
                                </div>
                                <div>
                                    <div class="d-flex justify-content-between small fw-semibold mb-1">
                                        <span>No Seleccionados (<%= noSeleccionadosStat %>)</span>
                                        <span class="text-danger"><%= totalPostStat > 0 ? pctRech : 0 %>%</span>
                                    </div>
                                    <div class="propath-progress-track">
                                        <div class="propath-progress-fill bg-danger" style="width: <%= totalPostStat > 0 ? pctRech : 0 %>%"></div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Próximas Entrevistas -->
                        <div class="propath-card mb-0">
                            <div class="propath-card-header">
                                <h2 class="propath-card-title"><i class="bi bi-calendar2-week-fill"></i><span>Próximas Entrevistas Programadas</span></h2>
                                <button type="button" class="btn btn-sm btn-outline-info rounded-pill" onclick="cambiarSeccionEmpresa('entrevistas')">Ver historial</button>
                            </div>
                            <div class="propath-card-body">
                                <%
                                    List<Entrevista> proximas = new ArrayList<>();
                                    for (Entrevista en : misEntrevistas) {
                                        if ("PROGRAMADA".equalsIgnoreCase(en.getEstado())) {
                                            proximas.add(en);
                                            if (proximas.size() >= 4) break;
                                        }
                                    }
                                    if (proximas.isEmpty()) {
                                %>
                                <div class="text-center py-4 text-muted small">
                                    <i class="bi bi-calendar-x fs-2 d-block mb-2"></i>
                                    No tienes entrevistas pendientes programadas.
                                </div>
                                <% } else { %>
                                <div class="d-flex flex-column gap-2">
                                    <% for (Entrevista en : proximas) { %>
                                    <div class="p-3 border rounded-3 bg-light d-flex justify-content-between align-items-center flex-wrap gap-2">
                                        <div>
                                            <div class="fw-bold text-dark"><%= escaparHtml(en.getEstudiante()) %></div>
                                            <div class="small text-muted">
                                                <i class="bi bi-briefcase me-1"></i><%= escaparHtml(en.getOferta()) %> &bull;
                                                <i class="bi bi-calendar3 ms-1 me-1"></i><%= escaparHtml(en.getFecha()) %>
                                                <i class="bi bi-clock ms-1 me-1"></i><%= en.getHora() != null && en.getHora().length() >= 5 ? escaparHtml(en.getHora().substring(0, 5)) : escaparHtml(en.getHora()) %>
                                            </div>
                                        </div>
                                        <div class="d-flex gap-2 align-items-center">
                                            <span class="badge-modalidad"><%= escaparHtml(en.getModalidad()) %></span>
                                            <button type="button" class="btn btn-sm btn-outline-primary rounded-pill"
                                                    data-id="<%= en.getId() %>"
                                                    data-estudiante="<%= escaparHtml(en.getEstudiante()) %>"
                                                    data-oferta="<%= escaparHtml(en.getOferta()) %>"
                                                    data-fecha="<%= escaparHtml(en.getFecha()) %>"
                                                    data-hora="<%= escaparHtml(en.getHora()) %>"
                                                    data-modalidad="<%= escaparHtml(en.getModalidad()) %>"
                                                    data-lugar="<%= escaparHtml(en.getEnlaceLugar()) %>"
                                                    data-estado="<%= escaparHtml(en.getEstado()) %>"
                                                    data-observaciones="<%= escaparHtml(en.getObservaciones()) %>"
                                                    onclick="verDetalleEntrevistaEmpresa(this)">
                                                <i class="bi bi-eye"></i>
                                            </button>
                                        </div>
                                    </div>
                                    <% } %>
                                </div>
                                <% } %>
                            </div>
                        </div>
                    </div>

                    <div class="col-12 col-lg-5">
                        <!-- Datos corporativos -->
                        <div class="propath-card mb-4">
                            <div class="propath-card-header">
                                <h2 class="propath-card-title"><i class="bi bi-building-fill"></i><span>Perfil Corporativo</span></h2>
                                <span class="badge <%= badgeEstadoOferta(esAprobada ? "ACTIVA" : estadoEmpresa) %> border rounded-pill"><%= escaparHtml(estadoEmpresa) %></span>
                            </div>
                            <div class="propath-card-body">
                                <h3 class="h5 fw-bold text-dark mb-1"><%= escaparHtml(razon) %></h3>
                                <p class="small text-muted mb-3"><i class="bi bi-envelope me-1"></i><%= escaparHtml(usuario.getCorreo()) %></p>
                                <div class="small text-secondary mb-2"><i class="bi bi-card-text me-2 text-primary"></i><strong>RUC:</strong> <%= miEmpresa != null && miEmpresa.getRuc() != null ? escaparHtml(miEmpresa.getRuc()) : "—" %></div>
                                <div class="small text-secondary mb-2"><i class="bi bi-telephone me-2 text-primary"></i><strong>Teléfono:</strong> <%= (miEmpresa != null && miEmpresa.getTelefono() != null && !miEmpresa.getTelefono().isEmpty()) ? escaparHtml(miEmpresa.getTelefono()) : "No registrado" %></div>
                                <div class="small text-secondary mb-2"><i class="bi bi-geo-alt me-2 text-primary"></i><strong>Dirección:</strong> <%= (miEmpresa != null && miEmpresa.getDireccion() != null && !miEmpresa.getDireccion().isEmpty()) ? escaparHtml(miEmpresa.getDireccion()) : "No registrada" %></div>
                                <% if (miEmpresa != null && miEmpresa.getDescripcion() != null && !miEmpresa.getDescripcion().isEmpty()) { %>
                                <div class="small text-muted mt-2 pt-2 border-top"><%= escaparHtml(miEmpresa.getDescripcion()) %></div>
                                <% } %>
                                <button type="button" class="btn btn-sm btn-outline-primary w-100 rounded-pill mt-3 fw-semibold"
                                        data-bs-toggle="modal" data-bs-target="#modalEditarPerfilEmpresa">
                                    <i class="bi bi-pencil-square me-1"></i>Actualizar Información
                                </button>
                            </div>
                        </div>

                        <!-- Postulantes recientes pendientes -->
                        <div class="propath-card mb-0">
                            <div class="propath-card-header">
                                <h2 class="propath-card-title"><i class="bi bi-inbox-fill"></i><span>Postulantes Recientes</span></h2>
                                <button type="button" class="btn btn-sm btn-outline-primary rounded-pill" onclick="cambiarSeccionEmpresa('postulantes')">Ver todos</button>
                            </div>
                            <div class="propath-card-body">
                                <%
                                    List<Postulacion> recientes = new ArrayList<>();
                                    for (Postulacion p : misPostulaciones) {
                                        if ("POSTULADO".equalsIgnoreCase(p.getEstado()) || "EN_REVISION".equalsIgnoreCase(p.getEstado())) {
                                            recientes.add(p);
                                            if (recientes.size() >= 4) break;
                                        }
                                    }
                                    if (recientes.isEmpty()) {
                                %>
                                <div class="text-center py-4 text-muted small">
                                    <i class="bi bi-check2-all fs-2 d-block mb-2"></i>
                                    No hay postulaciones pendientes de evaluación.
                                </div>
                                <% } else { %>
                                <div class="d-flex flex-column gap-2">
                                    <% for (Postulacion p : recientes) { %>
                                    <div class="p-3 border rounded-3 bg-light d-flex justify-content-between align-items-center gap-2">
                                        <div>
                                            <div class="fw-bold text-dark small"><%= escaparHtml(p.getEstudiante()) %></div>
                                            <div class="text-muted" style="font-size: 0.78rem;"><%= escaparHtml(p.getOferta()) %></div>
                                        </div>
                                        <button type="button" class="btn btn-sm btn-primary rounded-pill px-3"
                                                data-id-postulacion="<%= p.getId() %>"
                                                data-estudiante="<%= escaparHtml(p.getEstudiante()) %>"
                                                data-oferta="<%= escaparHtml(p.getOferta()) %>"
                                                data-estado="<%= escaparHtml(p.getEstado()) %>"
                                                data-evaluado="false"
                                                data-bs-toggle="modal"
                                                data-bs-target="#modalEvaluacion"
                                                onclick="abrirEvaluacion(this)"
                                                <%= !puedeOperar ? "disabled" : "" %>>
                                            Evaluar
                                        </button>
                                    </div>
                                    <% } %>
                                </div>
                                <% } %>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <!-- ============================================================== -->
            <!-- SECCIÓN 2: MIS OFERTAS LABORALES                               -->
            <!-- ============================================================== -->
            <section id="sec-ofertas" class="dashboard-section <%= "ofertas".equals(activeTab) ? "active" : "" %>">
                <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
                    <div>
                        <h2 class="h4 fw-bold text-dark mb-1">Gestión de Ofertas Laborales</h2>
                        <p class="text-muted small mb-0">Publica nuevas convocatorias, edita requisitos, cierra o elimina tus ofertas.</p>
                    </div>
                    <button type="button" class="btn btn-primary rounded-pill px-4 fw-semibold"
                            data-bs-toggle="collapse" data-bs-target="#collapseNuevaOferta"
                            <%= !puedeOperar ? "disabled" : "" %>>
                        <i class="bi bi-plus-lg me-1"></i>Nueva Convocatoria
                    </button>
                </div>

                <!-- Formulario Publicar Nueva Oferta -->
                <div class="collapse show mb-4" id="collapseNuevaOferta">
                    <div class="propath-card mb-0">
                        <div class="propath-card-header">
                            <h3 class="propath-card-title"><i class="bi bi-plus-circle-fill"></i><span>Publicar Nueva Oferta de Prácticas</span></h3>
                            <% if (!puedeOperar) { %>
                            <span class="badge bg-warning-subtle text-warning border border-warning-subtle rounded-pill">
                                <i class="bi bi-lock-fill me-1"></i>Requiere aprobación de empresa
                            </span>
                            <% } %>
                        </div>
                        <div class="propath-card-body">
                            <form action="${pageContext.request.contextPath}/empresa/oferta" method="POST"
                                  onsubmit="const b = this.querySelector('button[type=submit]'); if (b && b.dataset.submitted) return false; if (b) { b.dataset.submitted='true'; b.disabled=true; b.innerHTML='<span class=\'spinner-border spinner-border-sm me-1\'></span>Publicando...'; } return true;"
                                  class="row g-3">
                                <input type="hidden" name="accion" value="crear">
                                <div class="col-12 col-md-6">
                                    <label class="form-label small fw-semibold text-secondary">Título de la Convocatoria *</label>
                                    <input type="text" class="form-control" name="titulo" maxlength="150"
                                           placeholder="Ej. Practicante de Desarrollo Java" <%= !puedeOperar ? "disabled" : "" %> required>
                                </div>
                                <div class="col-12 col-md-6">
                                    <label class="form-label small fw-semibold text-secondary">Carrera Profesional *</label>
                                    <input type="text" class="form-control" name="carrera" maxlength="150"
                                           placeholder="Ej. Ingeniería de Sistemas / Software" <%= !puedeOperar ? "disabled" : "" %> required>
                                </div>
                                <div class="col-12 col-md-4">
                                    <label class="form-label small fw-semibold text-secondary">Modalidad *</label>
                                    <select class="form-select" name="modalidad" <%= !puedeOperar ? "disabled" : "" %>>
                                        <option value="PRESENCIAL">Presencial</option>
                                        <option value="HIBRIDA" selected>Híbrida</option>
                                        <option value="VIRTUAL">Virtual / Remota</option>
                                    </select>
                                </div>
                                <div class="col-12 col-md-4">
                                    <label class="form-label small fw-semibold text-secondary">Vacantes Disponibles *</label>
                                    <input type="number" class="form-control" name="vacantes" min="1" max="100" value="1" <%= !puedeOperar ? "disabled" : "" %> required>
                                </div>
                                <div class="col-12 col-md-4">
                                    <label class="form-label small fw-semibold text-secondary">Fecha Límite de Postulación *</label>
                                    <input type="date" class="form-control" name="fechaLimite" min="<%= hoyIso %>" <%= !puedeOperar ? "disabled" : "" %> required>
                                </div>
                                <div class="col-12 col-md-6">
                                    <label class="form-label small fw-semibold text-secondary">Ubicación / Distrito</label>
                                    <input type="text" class="form-control" name="ubicacion" maxlength="150"
                                           placeholder="Ej. San Isidro, Lima" <%= !puedeOperar ? "disabled" : "" %>>
                                </div>
                                <div class="col-12 col-md-6">
                                    <label class="form-label small fw-semibold text-secondary">Subvención Económica Mensual (Opcional)</label>
                                    <div class="input-group">
                                        <span class="input-group-text">S/</span>
                                        <input type="number" class="form-control" name="remuneracion" min="0" step="0.01"
                                               placeholder="1025.00" <%= !puedeOperar ? "disabled" : "" %>>
                                    </div>
                                </div>
                                <div class="col-12">
                                    <label class="form-label small fw-semibold text-secondary">Descripción de Funciones *</label>
                                    <textarea class="form-control" name="descripcion" rows="3"
                                              placeholder="Describe las actividades principales, equipo de trabajo y objetivos del puesto..." <%= !puedeOperar ? "disabled" : "" %> required></textarea>
                                </div>
                                <div class="col-12">
                                    <label class="form-label small fw-semibold text-secondary">Requisitos y Conocimientos</label>
                                    <textarea class="form-control" name="requisitos" rows="2"
                                              placeholder="Ciclo mínimo, tecnologías, herramientas, idiomas o competencias deseables..." <%= !puedeOperar ? "disabled" : "" %>></textarea>
                                </div>
                                <div class="col-12 d-flex justify-content-end">
                                    <button type="submit" class="btn btn-primary rounded-pill px-4 fw-semibold"
                                            <%= !puedeOperar ? "disabled style='opacity:0.5;cursor:not-allowed;'" : "" %>>
                                        <i class="bi bi-send-plus me-1"></i>Publicar Oferta
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>

                <!-- Lista de Ofertas Publicadas -->
                <div class="propath-card mb-0">
                    <div class="propath-card-header">
                        <h3 class="propath-card-title"><i class="bi bi-list-check"></i><span>Mis Ofertas Publicadas (<%= misOfertas.size() %>)</span></h3>
                        <div class="d-flex flex-wrap gap-2 align-items-center">
                            <div class="search-box-custom" style="max-width: 240px;">
                                <i class="bi bi-search search-icon"></i>
                                <input type="text" id="buscarOfertaEmpresa" class="form-control" placeholder="Buscar oferta o carrera..." oninput="filtrarOfertasEmpresa()">
                            </div>
                            <select id="filtroEstadoOfertaEmpresa" class="form-select" style="width: auto;" onchange="filtrarOfertasEmpresa()">
                                <option value="TODAS">Todos los estados</option>
                                <option value="ACTIVA">Activas</option>
                                <option value="CERRADA">Cerradas</option>
                                <option value="PENDIENTE">Pendientes</option>
                                <option value="RECHAZADA">Rechazadas</option>
                            </select>
                        </div>
                    </div>
                    <div class="propath-card-body">
                        <% if (misOfertas.isEmpty()) { %>
                        <div class="text-center py-5 text-muted">
                            <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                            Aún no has publicado ofertas de prácticas. Utiliza el formulario superior para crear tu primera convocatoria.
                        </div>
                        <% } else { %>
                        <div class="d-flex flex-column gap-3" id="listaOfertasEmpresa">
                            <% for (OfertaPractica o : misOfertas) {
                                String estOferta = o.getEstado() != null ? o.getEstado().trim().toUpperCase() : "ACTIVA";
                            %>
                            <div class="p-3 border rounded-3 bg-light bg-opacity-50 oferta-empresa-card"
                                 data-estado="<%= escaparHtml(estOferta) %>"
                                 data-texto="<%= escaparHtml((o.getTitulo() + " " + o.getCarrera() + " " + o.getModalidad() + " " + o.getUbicacion()).toLowerCase()) %>">
                                <div class="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-2">
                                    <div>
                                        <span class="badge bg-primary-subtle text-primary mb-1"><%= escaparHtml(o.getCarrera()) %></span>
                                        <h4 class="h6 fw-bold text-dark mb-1"><%= escaparHtml(o.getTitulo()) %></h4>
                                        <div class="d-flex flex-wrap gap-2 mt-1">
                                            <span class="offer-meta-chip"><i class="bi bi-laptop"></i><%= escaparHtml(o.getModalidad()) %></span>
                                            <span class="offer-meta-chip"><i class="bi bi-people"></i><%= o.getVacantes() %> vacante(s)</span>
                                            <span class="offer-meta-chip"><i class="bi bi-file-earmark-person"></i><%= o.getTotalPostulaciones() %> postulante(s)</span>
                                            <% if (o.getUbicacion() != null && !o.getUbicacion().trim().isEmpty()) { %>
                                            <span class="offer-meta-chip"><i class="bi bi-geo-alt"></i><%= escaparHtml(o.getUbicacion()) %></span>
                                            <% } %>
                                            <% if (o.getRemuneracion() != null && !o.getRemuneracion().trim().isEmpty()) {
                                                String remTxt = o.getRemuneracion().trim();
                                                String remMostrar = remTxt.toUpperCase().startsWith("S/") ? remTxt : "S/ " + remTxt;
                                            %>
                                            <span class="offer-meta-chip"><i class="bi bi-cash-stack"></i><%= escaparHtml(remMostrar) %></span>
                                            <% } %>
                                            <span class="offer-meta-chip"><i class="bi bi-calendar-x"></i>Límite: <%= o.getFechaLimite() != null ? escaparHtml(o.getFechaLimite()) : "—" %></span>
                                        </div>
                                    </div>
                                    <span class="badge <%= badgeEstadoOferta(estOferta) %> border rounded-pill px-3 py-2"><%= escaparHtml(estOferta) %></span>
                                </div>

                                <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 pt-2 border-top mt-2">
                                    <span class="small text-muted">Publicado: <%= o.getFechaPublicacion() != null ? escaparHtml(o.getFechaPublicacion()) : "—" %></span>
                                    <div class="d-flex flex-wrap gap-2">
                                        <button type="button" class="btn btn-sm btn-outline-info rounded-pill px-3"
                                                onclick="verPostulantesDeOferta(<%= o.getId() %>)">
                                            <i class="bi bi-people me-1"></i>Postulantes (<%= o.getTotalPostulaciones() %>)
                                        </button>
                                        <button type="button" class="btn btn-sm btn-outline-secondary rounded-pill px-3"
                                                data-id="<%= o.getId() %>"
                                                data-titulo="<%= escaparHtml(o.getTitulo()) %>"
                                                data-carrera="<%= escaparHtml(o.getCarrera()) %>"
                                                data-empresa="<%= escaparHtml(o.getEmpresa() != null ? o.getEmpresa() : razon) %>"
                                                data-modalidad="<%= escaparHtml(o.getModalidad()) %>"
                                                data-vacantes="<%= o.getVacantes() %>"
                                                data-publicacion="<%= escaparHtml(o.getFechaPublicacion()) %>"
                                                data-fecha="<%= o.getFechaLimite() != null ? escaparHtml(o.getFechaLimite().trim()) : "" %>"
                                                data-ubicacion="<%= escaparHtml(o.getUbicacion()) %>"
                                                data-descripcion="<%= escaparHtml(o.getDescripcion()) %>"
                                                data-estado="<%= escaparHtml(estOferta) %>"
                                                data-requisitos="<%= escaparHtml(o.getRequisitos()) %>"
                                                data-remuneracion="<%= escaparHtml(o.getRemuneracion()) %>"
                                                onclick="verDetalleOfertaEmpresa(this)">
                                            <i class="bi bi-eye me-1"></i>Ver detalle
                                        </button>
                                        <button type="button" class="btn btn-sm btn-outline-primary rounded-pill px-3"
                                                <%= !puedeOperar ? "disabled" : "" %>
                                                data-id="<%= o.getId() %>"
                                                data-titulo="<%= escaparHtml(o.getTitulo()) %>"
                                                data-carrera="<%= escaparHtml(o.getCarrera()) %>"
                                                data-modalidad="<%= escaparHtml(o.getModalidad()) %>"
                                                data-vacantes="<%= o.getVacantes() %>"
                                                data-fecha="<%= o.getFechaLimite() != null ? escaparHtml(o.getFechaLimite().trim()) : "" %>"
                                                data-ubicacion="<%= escaparHtml(o.getUbicacion()) %>"
                                                data-descripcion="<%= escaparHtml(o.getDescripcion()) %>"
                                                data-estado="<%= escaparHtml(estOferta) %>"
                                                data-requisitos="<%= escaparHtml(o.getRequisitos()) %>"
                                                data-remuneracion="<%= escaparHtml(o.getRemuneracion()) %>"
                                                onclick="<%= puedeOperar ? "abrirModalEditarOferta(this)" : "" %>">
                                            <i class="bi bi-pencil-square me-1"></i>Editar
                                        </button>
                                        <% if (puedeOperar && !"RECHAZADA".equals(estOferta)) {
                                            boolean estaActiva = "ACTIVA".equals(estOferta);
                                        %>
                                        <form action="${pageContext.request.contextPath}/empresa/oferta" method="POST" class="m-0"
                                              onsubmit="event.preventDefault(); const f = this; mostrarModalConfirmacion('<%= estaActiva ? "Cerrar Convocatoria" : "Reactivar Convocatoria" %>', '<%= estaActiva ? "¿Deseas cerrar esta oferta para dejar de recibir postulaciones?" : "¿Deseas reactivar esta oferta para volver a recibir postulaciones?" %>', function(){ f.submit(); });">
                                            <input type="hidden" name="accion" value="cambiarEstadoOferta">
                                            <input type="hidden" name="id" value="<%= o.getId() %>">
                                            <input type="hidden" name="nuevoEstado" value="<%= estaActiva ? "CERRADA" : "ACTIVA" %>">
                                            <button type="submit" class="btn btn-sm <%= estaActiva ? "btn-outline-warning text-dark" : "btn-outline-success" %> rounded-pill px-3">
                                                <i class="bi <%= estaActiva ? "bi-lock" : "bi-unlock" %> me-1"></i><%= estaActiva ? "Cerrar" : "Reactivar" %>
                                            </button>
                                        </form>
                                        <% } %>
                                        <form action="${pageContext.request.contextPath}/empresa/oferta" method="POST" class="m-0"
                                              onsubmit="event.preventDefault(); const f = this; mostrarModalConfirmacion('Eliminar Oferta', '¿Seguro que deseas eliminar esta oferta de práctica? Si ya tiene postulantes, se cerrará para proteger el historial.', function(){ f.submit(); });">
                                            <input type="hidden" name="accion" value="eliminar">
                                            <input type="hidden" name="id" value="<%= o.getId() %>">
                                            <button type="submit" class="btn btn-sm btn-outline-danger rounded-pill px-3" <%= !puedeOperar ? "disabled" : "" %>>
                                                <i class="bi bi-trash me-1"></i>Eliminar
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                            <% } %>
                        </div>
                        <% } %>
                    </div>
                </div>
            </section>

            <!-- ============================================================== -->
            <!-- SECCIÓN 3: POSTULANTES Y GESTIÓN DE ESTADOS                   -->
            <!-- ============================================================== -->
            <section id="sec-postulantes" class="dashboard-section <%= "postulantes".equals(activeTab) ? "active" : "" %>">
                <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
                    <div>
                        <h2 class="h4 fw-bold text-dark mb-1">Revisión y Gestión de Postulantes</h2>
                        <p class="text-muted small mb-0">Evalúa el CV, documentos y respuestas del cuestionario; actualiza estados y programa entrevistas.</p>
                    </div>
                    <span class="badge bg-info-subtle text-info border border-info-subtle rounded-pill px-3 py-2 fw-semibold">
                        <%= misPostulaciones.size() %> postulación(es) en total
                    </span>
                </div>

                <!-- Filtros de Postulantes -->
                <div class="propath-card mb-4">
                    <div class="propath-card-body py-3">
                        <div class="row g-3 align-items-center">
                            <div class="col-12 col-md-4">
                                <div class="search-box-custom" style="max-width: 100%;">
                                    <i class="bi bi-search search-icon"></i>
                                    <input type="text" id="buscarPostulanteEmpresa" class="form-control"
                                           placeholder="Buscar estudiante, carrera u oferta..." oninput="filtrarPostulantesEmpresa()">
                                </div>
                            </div>
                            <div class="col-12 col-md-4">
                                <select id="filtroOfertaPostulantes" class="form-select" onchange="filtrarPostulantesEmpresa()">
                                    <option value="TODAS">Todas mis ofertas (<%= misOfertas.size() %>)</option>
                                    <% for (OfertaPractica o : misOfertas) { %>
                                    <option value="<%= o.getId() %>"><%= escaparHtml(o.getTitulo()) %></option>
                                    <% } %>
                                </select>
                            </div>
                            <div class="col-12 col-md-4">
                                <select id="filtroEstadoPostulantes" class="form-select" onchange="filtrarPostulantesEmpresa()">
                                    <option value="TODOS">Todos los estados (<%= totalPostStat %>)</option>
                                    <option value="POSTULADO">Postulado (<%= postuladosStat %>)</option>
                                    <option value="EN_REVISION">En Revisión (<%= enRevisionStat %>)</option>
                                    <option value="SELECCIONADO">Seleccionado (<%= seleccionadosStat %>)</option>
                                    <option value="NO_SELECCIONADO">No Seleccionado (<%= noSeleccionadosStat %>)</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Lista de Postulantes -->
                <div class="propath-card mb-0">
                    <div class="propath-card-body">
                        <% if (misPostulaciones.isEmpty()) { %>
                        <div class="text-center py-5 text-muted">
                            <i class="bi bi-person-x fs-1 d-block mb-2"></i>
                            Aún no hay postulaciones registradas en tus ofertas.
                        </div>
                        <% } else { %>
                        <div class="d-flex flex-column gap-3" id="listaPostulantesEmpresa">
                            <% for (Postulacion p : misPostulaciones) {
                                String estPost = p.getEstado() != null ? p.getEstado().trim().toUpperCase() : "POSTULADO";
                                boolean esSeleccionado = "SELECCIONADO".equals(estPost);
                                boolean esRechazado = "NO_SELECCIONADO".equals(estPost);
                                Entrevista entrevistaActiva = null;
                                boolean tieneEntrevistaNoCancelada = false;
                                for (Entrevista en : misEntrevistas) {
                                    if (en.getIdPostulacion() == p.getId()) {
                                        if (entrevistaActiva == null) entrevistaActiva = en;
                                        if (!"CANCELADA".equalsIgnoreCase(en.getEstado())) {
                                            tieneEntrevistaNoCancelada = true;
                                            entrevistaActiva = en;
                                            break;
                                        }
                                    }
                                }
                            %>
                            <div class="p-3 border rounded-3 bg-light bg-opacity-25 postulante-empresa-card"
                                 data-id-oferta="<%= p.getIdOferta() %>"
                                 data-estado="<%= escaparHtml(estPost) %>"
                                 data-texto="<%= escaparHtml((p.getEstudiante() + " " + p.getOferta() + " " + p.getCarreraEstudiante() + " " + p.getUniversidadEstudiante()).toLowerCase()) %>">
                                <div class="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-2">
                                    <div>
                                        <div class="d-flex align-items-center gap-2 flex-wrap">
                                            <h3 class="h6 fw-bold text-dark mb-0"><%= escaparHtml(p.getEstudiante()) %></h3>
                                            <span class="badge-status <%= badgeEstadoPostulacion(estPost) %>"><%= labelEstadoPostulacion(estPost) %></span>
                                            <% if (p.isCvDisponible()) { %>
                                            <span class="badge bg-success-subtle text-success border border-success-subtle rounded-pill small">
                                                <i class="bi bi-file-earmark-pdf me-1"></i>CV PDF
                                            </span>
                                            <% } %>
                                        </div>
                                        <div class="small text-muted mt-1">
                                            <i class="bi bi-briefcase me-1 text-primary"></i>Oferta: <strong><%= escaparHtml(p.getOferta()) %></strong>
                                            <% if (p.getCarreraEstudiante() != null && !p.getCarreraEstudiante().isEmpty()) { %>
                                            &bull; <i class="bi bi-mortarboard ms-1 me-1"></i><%= escaparHtml(p.getCarreraEstudiante()) %>
                                            <% } %>
                                            <% if (p.getUniversidadEstudiante() != null && !p.getUniversidadEstudiante().isEmpty()) { %>
                                            (<%= escaparHtml(p.getUniversidadEstudiante()) %>)
                                            <% } %>
                                        </div>
                                    </div>
                                    <span class="small text-secondary"><i class="bi bi-calendar3 me-1"></i><%= escaparHtml(p.getFechaPostulacion()) %></span>
                                </div>

                                <% if (esRechazado && p.getMotivoRechazo() != null && !p.getMotivoRechazo().trim().isEmpty()) { %>
                                <div class="alert alert-danger py-2 px-3 small mb-2">
                                    <strong>Motivo registrado:</strong> <%= escaparHtml(p.getMotivoRechazo()) %>
                                </div>
                                <% } %>

                                <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 pt-2 border-top">
                                    <div class="d-flex flex-wrap gap-2">
                                        <button type="button" class="btn btn-sm btn-outline-primary rounded-pill px-3"
                                                data-id-postulacion="<%= p.getId() %>"
                                                data-estudiante="<%= escaparHtml(p.getEstudiante()) %>"
                                                data-oferta="<%= escaparHtml(p.getOferta()) %>"
                                                data-estado="<%= escaparHtml(estPost) %>"
                                                data-evaluado="<%= tieneEntrevistaNoCancelada %>"
                                                data-bs-toggle="modal"
                                                data-bs-target="#modalEvaluacion"
                                                onclick="abrirEvaluacion(this)">
                                            <i class="bi bi-file-earmark-text me-1"></i><%= ("POSTULADO".equals(estPost) || "EN_REVISION".equals(estPost)) ? "Evaluar postulante" : "Ver expediente y CV" %>
                                        </button>

                                        <% if (puedeOperar && "POSTULADO".equals(estPost)) { %>
                                        <form action="${pageContext.request.contextPath}/empresa/oferta" method="POST" class="m-0">
                                            <input type="hidden" name="accion" value="cambiarEstadoPostulacion">
                                            <input type="hidden" name="idPostulacion" value="<%= p.getId() %>">
                                            <input type="hidden" name="estado" value="EN_REVISION">
                                            <button type="submit" class="btn btn-sm btn-outline-warning text-dark rounded-pill px-3">
                                                <i class="bi bi-hourglass-split me-1"></i>Marcar En Revisión
                                            </button>
                                        </form>
                                        <% } %>

                                        <% if (puedeOperar && esSeleccionado) { %>
                                            <% if (tieneEntrevistaNoCancelada && entrevistaActiva != null) { %>
                                            <button type="button" class="btn btn-sm btn-outline-info rounded-pill px-3"
                                                    data-id="<%= entrevistaActiva.getId() %>"
                                                    data-estudiante="<%= escaparHtml(entrevistaActiva.getEstudiante()) %>"
                                                    data-oferta="<%= escaparHtml(entrevistaActiva.getOferta()) %>"
                                                    data-fecha="<%= escaparHtml(entrevistaActiva.getFecha()) %>"
                                                    data-hora="<%= escaparHtml(entrevistaActiva.getHora()) %>"
                                                    data-modalidad="<%= escaparHtml(entrevistaActiva.getModalidad()) %>"
                                                    data-lugar="<%= escaparHtml(entrevistaActiva.getEnlaceLugar()) %>"
                                                    data-estado="<%= escaparHtml(entrevistaActiva.getEstado()) %>"
                                                    data-observaciones="<%= escaparHtml(entrevistaActiva.getObservaciones()) %>"
                                                    onclick="verDetalleEntrevistaEmpresa(this)">
                                                <i class="bi bi-calendar-check me-1"></i>Entrevista (<%= escaparHtml(entrevistaActiva.getEstado()) %>)
                                            </button>
                                            <% } else { %>
                                            <button type="button" class="btn btn-sm btn-primary rounded-pill px-3"
                                                    data-id-postulacion="<%= p.getId() %>"
                                                    data-bs-toggle="modal"
                                                    data-bs-target="#modalEntrevista"
                                                    onclick="abrirEntrevista(this.dataset.idPostulacion, 'postulantes')">
                                                <i class="bi bi-calendar-plus me-1"></i>Programar Entrevista
                                            </button>
                                            <% } %>
                                        <% } %>
                                    </div>
                                </div>
                            </div>
                            <% } %>
                        </div>
                        <% } %>
                    </div>
                </div>
            </section>

            <!-- ============================================================== -->
            <!-- SECCIÓN 4: HISTORIAL Y AGENDA DE ENTREVISTAS                   -->
            <!-- ============================================================== -->
            <section id="sec-entrevistas" class="dashboard-section <%= "entrevistas".equals(activeTab) ? "active" : "" %>">
                <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
                    <div>
                        <h2 class="h4 fw-bold text-dark mb-1">Entrevistas y Seguimiento</h2>
                        <p class="text-muted small mb-0">Gestiona citas programadas, actualiza fechas u horarios y consulta el historial completo.</p>
                    </div>
                </div>

                <!-- Mini KPIs de Entrevistas -->
                <div class="row g-3 mb-4">
                    <div class="col-12 col-sm-4">
                        <div class="bg-white p-3 rounded-4 border shadow-sm d-flex justify-content-between align-items-center">
                            <div>
                                <div class="small text-muted fw-semibold">Programadas</div>
                                <div class="fs-3 fw-bold text-primary"><%= entProgramadasStat %></div>
                            </div>
                            <div class="rounded-circle bg-primary-subtle text-primary p-3"><i class="bi bi-calendar-event fs-4"></i></div>
                        </div>
                    </div>
                    <div class="col-12 col-sm-4">
                        <div class="bg-white p-3 rounded-4 border shadow-sm d-flex justify-content-between align-items-center">
                            <div>
                                <div class="small text-muted fw-semibold">Realizadas</div>
                                <div class="fs-3 fw-bold text-success"><%= entRealizadasStat %></div>
                            </div>
                            <div class="rounded-circle bg-success-subtle text-success p-3"><i class="bi bi-check-circle fs-4"></i></div>
                        </div>
                    </div>
                    <div class="col-12 col-sm-4">
                        <div class="bg-white p-3 rounded-4 border shadow-sm d-flex justify-content-between align-items-center">
                            <div>
                                <div class="small text-muted fw-semibold">Canceladas</div>
                                <div class="fs-3 fw-bold text-danger"><%= entCanceladasStat %></div>
                            </div>
                            <div class="rounded-circle bg-danger-subtle text-danger p-3"><i class="bi bi-x-circle fs-4"></i></div>
                        </div>
                    </div>
                </div>

                <!-- Filtro + Listado Historial de Entrevistas -->
                <div class="propath-card mb-0">
                    <div class="propath-card-header">
                        <h3 class="propath-card-title"><i class="bi bi-clock-history"></i><span>Historial de Entrevistas (<%= misEntrevistas.size() %>)</span></h3>
                        <div class="d-flex flex-wrap gap-2 align-items-center">
                            <div class="search-box-custom" style="max-width: 240px;">
                                <i class="bi bi-search search-icon"></i>
                                <input type="text" id="buscarEntrevistaEmpresa" class="form-control" placeholder="Buscar estudiante o puesto..." oninput="filtrarEntrevistasEmpresa()">
                            </div>
                            <select id="filtroEstadoEntrevistaEmpresa" class="form-select" style="width: auto;" onchange="filtrarEntrevistasEmpresa()">
                                <option value="TODAS">Todos los estados</option>
                                <option value="PROGRAMADA">Programadas</option>
                                <option value="REALIZADA">Realizadas</option>
                                <option value="CANCELADA">Canceladas</option>
                            </select>
                        </div>
                    </div>
                    <div class="propath-card-body">
                        <% if (misEntrevistas.isEmpty()) { %>
                        <div class="text-center py-5 text-muted">
                            <i class="bi bi-calendar2-x fs-1 d-block mb-2"></i>
                            Aún no hay entrevistas registradas. Selecciona a un postulante desde la pestaña <strong>Postulantes</strong> para agendar su entrevista.
                        </div>
                        <% } else { %>
                        <div class="d-flex flex-column gap-3" id="listaEntrevistasEmpresa">
                            <% for (Entrevista en : misEntrevistas) {
                                String estEn = en.getEstado() != null ? en.getEstado().trim().toUpperCase() : "PROGRAMADA";
                                String horaFmt = (en.getHora() != null && en.getHora().length() >= 5) ? en.getHora().substring(0, 5) : en.getHora();
                            %>
                            <div class="p-3 border rounded-3 bg-light bg-opacity-50 entrevista-empresa-card"
                                 data-estado="<%= escaparHtml(estEn) %>"
                                 data-texto="<%= escaparHtml((en.getEstudiante() + " " + en.getOferta() + " " + en.getModalidad() + " " + en.getEnlaceLugar()).toLowerCase()) %>">
                                <div class="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-2">
                                    <div>
                                        <div class="d-flex align-items-center gap-2 flex-wrap">
                                            <h4 class="h6 fw-bold text-dark mb-0"><%= escaparHtml(en.getEstudiante()) %></h4>
                                            <span class="badge-status <%= badgeEstadoEntrevista(estEn) %>"><%= escaparHtml(estEn) %></span>
                                            <span class="badge-modalidad"><i class="bi <%= "VIRTUAL".equalsIgnoreCase(en.getModalidad()) ? "bi-camera-video" : "bi-geo-alt" %> me-1"></i><%= escaparHtml(en.getModalidad()) %></span>
                                        </div>
                                        <div class="small text-muted mt-1">
                                            <i class="bi bi-briefcase me-1 text-primary"></i>Oferta: <strong><%= escaparHtml(en.getOferta()) %></strong>
                                            &bull; <i class="bi bi-calendar3 ms-1 me-1"></i><strong><%= escaparHtml(en.getFecha()) %></strong> a las <strong><%= escaparHtml(horaFmt) %></strong>
                                        </div>
                                    </div>
                                    <% if ("VIRTUAL".equalsIgnoreCase(en.getModalidad()) && en.getEnlaceLugar() != null && en.getEnlaceLugar().startsWith("http")) { %>
                                    <a href="<%= escaparHtml(en.getEnlaceLugar()) %>" target="_blank" rel="noopener noreferrer" class="btn btn-sm btn-outline-primary rounded-pill px-3">
                                        <i class="bi bi-camera-video me-1"></i>Abrir reunión
                                    </a>
                                    <% } %>
                                </div>

                                <% if (en.getObservaciones() != null && !en.getObservaciones().trim().isEmpty()) { %>
                                <div class="small text-secondary bg-white border rounded p-2 mb-2">
                                    <i class="bi bi-chat-left-text me-1 text-primary"></i><strong>Observaciones:</strong> <%= escaparHtml(en.getObservaciones()) %>
                                </div>
                                <% } else if (!"VIRTUAL".equalsIgnoreCase(en.getModalidad()) && en.getEnlaceLugar() != null) { %>
                                <div class="small text-secondary mb-2">
                                    <i class="bi bi-geo-alt me-1 text-primary"></i><strong>Sede:</strong> <%= escaparHtml(en.getEnlaceLugar()) %>
                                </div>
                                <% } %>

                                <div class="d-flex flex-wrap gap-2 pt-2 border-top">
                                    <button type="button" class="btn btn-sm btn-outline-info rounded-pill px-3"
                                            data-id="<%= en.getId() %>"
                                            data-estudiante="<%= escaparHtml(en.getEstudiante()) %>"
                                            data-oferta="<%= escaparHtml(en.getOferta()) %>"
                                            data-fecha="<%= escaparHtml(en.getFecha()) %>"
                                            data-hora="<%= escaparHtml(en.getHora()) %>"
                                            data-modalidad="<%= escaparHtml(en.getModalidad()) %>"
                                            data-lugar="<%= escaparHtml(en.getEnlaceLugar()) %>"
                                            data-estado="<%= escaparHtml(estEn) %>"
                                            data-observaciones="<%= escaparHtml(en.getObservaciones()) %>"
                                            onclick="verDetalleEntrevistaEmpresa(this)">
                                        <i class="bi bi-eye me-1"></i>Ver detalle
                                    </button>
                                    <% if (puedeOperar) { %>
                                    <button type="button" class="btn btn-sm btn-outline-primary rounded-pill px-3"
                                            data-bs-toggle="modal"
                                            data-bs-target="#modalEntrevista"
                                            data-id="<%= en.getId() %>"
                                            data-postulacion="<%= en.getIdPostulacion() %>"
                                            data-fecha="<%= escaparHtml(en.getFecha()) %>"
                                            data-hora="<%= escaparHtml(en.getHora()) %>"
                                            data-modalidad="<%= escaparHtml(en.getModalidad()) %>"
                                            data-lugar="<%= escaparHtml(en.getEnlaceLugar()) %>"
                                            data-estado="<%= escaparHtml(estEn) %>"
                                            data-observaciones="<%= escaparHtml(en.getObservaciones()) %>"
                                            onclick="editarEntrevista(this)">
                                        <i class="bi bi-pencil-square me-1"></i>Reprogramar / Editar
                                    </button>
                                    <% if ("PROGRAMADA".equals(estEn)) { %>
                                    <form action="${pageContext.request.contextPath}/empresa/entrevista" method="POST" class="m-0">
                                        <input type="hidden" name="accion" value="cambiarEstado">
                                        <input type="hidden" name="idEntrevista" value="<%= en.getId() %>">
                                        <input type="hidden" name="estado" value="REALIZADA">
                                        <button type="submit" class="btn btn-sm btn-outline-success rounded-pill px-3">
                                            <i class="bi bi-check2-circle me-1"></i>Marcar Realizada
                                        </button>
                                    </form>
                                    <form action="${pageContext.request.contextPath}/empresa/entrevista" method="POST" class="m-0"
                                          onsubmit="event.preventDefault(); const f = this; mostrarModalConfirmacion('Cancelar Entrevista', '¿Seguro que deseas marcar esta entrevista como CANCELADA?', function(){ f.submit(); });">
                                        <input type="hidden" name="accion" value="cambiarEstado">
                                        <input type="hidden" name="idEntrevista" value="<%= en.getId() %>">
                                        <input type="hidden" name="estado" value="CANCELADA">
                                        <button type="submit" class="btn btn-sm btn-outline-warning text-dark rounded-pill px-3">
                                            <i class="bi bi-slash-circle me-1"></i>Cancelar
                                        </button>
                                    </form>
                                    <% } %>
                                    <form action="${pageContext.request.contextPath}/empresa/entrevista" method="POST" class="m-0"
                                          onsubmit="event.preventDefault(); const f = this; mostrarModalConfirmacion('Eliminar Entrevista', '¿Seguro que deseas eliminar definitivamente esta entrevista?', function(){ f.submit(); });">
                                        <input type="hidden" name="accion" value="eliminar">
                                        <input type="hidden" name="idEntrevista" value="<%= en.getId() %>">
                                        <button type="submit" class="btn btn-sm btn-outline-danger rounded-pill px-3">
                                            <i class="bi bi-trash me-1"></i>Eliminar
                                        </button>
                                    </form>
                                    <% } %>
                                </div>
                            </div>
                            <% } %>
                        </div>
                        <% } %>
                    </div>
                </div>
            </section>

            <!-- ============================================================== -->
            <!-- SECCIÓN 5: REPORTES Y ESTADÍSTICAS DE SELECCIÓN                -->
            <!-- ============================================================== -->
            <section id="sec-estadisticas" class="dashboard-section <%= "estadisticas".equals(activeTab) ? "active" : "" %>">
                <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
                    <div>
                        <h2 class="h4 fw-bold text-dark mb-1">Reportes y Estadísticas del Proceso de Selección</h2>
                        <p class="text-muted small mb-0">Indicadores de conversión, evaluación por etapa, desempeño de entrevistas y desglose por oferta.</p>
                    </div>
                </div>

                <!-- Indicadores de Tasa -->
                <div class="row g-4 mb-4">
                    <div class="col-12 col-md-4">
                        <div class="bg-white p-4 rounded-4 border shadow-sm h-100">
                            <div class="small text-muted fw-semibold mb-1">Tasa de Revisión / Respuesta</div>
                            <div class="fs-2 fw-bold text-primary mb-2"><%= tasaRevisionStat %>%</div>
                            <div class="propath-progress-track mb-2">
                                <div class="propath-progress-fill bg-primary" style="width: <%= tasaRevisionStat %>%"></div>
                            </div>
                            <div class="small text-muted"><%= enRevisionStat + seleccionadosStat + noSeleccionadosStat %> de <%= totalPostStat %> postulantes evaluados.</div>
                        </div>
                    </div>
                    <div class="col-12 col-md-4">
                        <div class="bg-white p-4 rounded-4 border shadow-sm h-100">
                            <div class="small text-muted fw-semibold mb-1">Tasa de Selección</div>
                            <div class="fs-2 fw-bold text-success mb-2"><%= tasaSeleccionStat %>%</div>
                            <div class="propath-progress-track mb-2">
                                <div class="propath-progress-fill bg-success" style="width: <%= tasaSeleccionStat %>%"></div>
                            </div>
                            <div class="small text-muted"><%= seleccionadosStat %> candidato(s) seleccionados frente a <%= totalVacantesActivas %> vacante(s) activa(s).</div>
                        </div>
                    </div>
                    <div class="col-12 col-md-4">
                        <div class="bg-white p-4 rounded-4 border shadow-sm h-100">
                            <div class="small text-muted fw-semibold mb-1">Efectividad de Entrevistas</div>
                            <div class="fs-2 fw-bold text-info mb-2"><%= tasaEntRealizadaStat %>%</div>
                            <div class="propath-progress-track mb-2">
                                <div class="propath-progress-fill bg-info" style="width: <%= tasaEntRealizadaStat %>%"></div>
                            </div>
                            <div class="small text-muted"><%= entRealizadasStat %> realizadas &bull; <%= entProgramadasStat %> por realizar &bull; <%= entCanceladasStat %> canceladas.</div>
                        </div>
                    </div>
                </div>

                <!-- Tabla Detallada por Oferta -->
                <div class="propath-card mb-0">
                    <div class="propath-card-header">
                        <h3 class="propath-card-title"><i class="bi bi-table"></i><span>Estadísticas por Oferta de Prácticas</span></h3>
                        <span class="badge bg-light text-secondary border rounded-pill px-3 py-2"><%= resumenOfertas.size() %> oferta(s)</span>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-propath mb-0">
                            <thead>
                            <tr>
                                <th>Convocatoria</th>
                                <th>Estado</th>
                                <th class="text-center">Vacantes</th>
                                <th class="text-center">Postulantes</th>
                                <th class="text-center">Pendientes</th>
                                <th class="text-center">En Revisión</th>
                                <th class="text-center">Seleccionados</th>
                                <th class="text-center">No Selec.</th>
                                <th class="text-center">Entrevistas</th>
                                <th class="text-center">Conv. %</th>
                            </tr>
                            </thead>
                            <tbody>
                            <% if (resumenOfertas.isEmpty()) { %>
                            <tr>
                                <td colspan="10" class="text-center py-4 text-muted">No hay ofertas registradas para generar estadísticas.</td>
                            </tr>
                            <% } else {
                                for (Map<String, Object> r : resumenOfertas) {
                                    long totP = getLongStat(r, "totalPostulaciones");
                                    long selP = getLongStat(r, "seleccionados");
                                    long entP = getLongStat(r, "entProgramadas");
                                    long entR = getLongStat(r, "entRealizadas");
                                    double conv = totP > 0 ? Math.round((selP * 1000.0) / totP) / 10.0 : 0.0;
                                    String estR = String.valueOf(r.get("estado"));
                            %>
                            <tr>
                                <td>
                                    <div class="fw-semibold text-dark"><%= escaparHtml(String.valueOf(r.get("titulo"))) %></div>
                                    <div class="small text-muted">Límite: <%= escaparHtml(String.valueOf(r.get("fechaLimite"))) %></div>
                                </td>
                                <td><span class="badge <%= badgeEstadoOferta(estR) %> border rounded-pill"><%= escaparHtml(estR) %></span></td>
                                <td class="text-center fw-semibold"><%= getLongStat(r, "vacantes") %></td>
                                <td class="text-center fw-bold text-primary"><%= totP %></td>
                                <td class="text-center"><%= getLongStat(r, "postulados") %></td>
                                <td class="text-center text-warning fw-semibold"><%= getLongStat(r, "enRevision") %></td>
                                <td class="text-center text-success fw-bold"><%= selP %></td>
                                <td class="text-center text-danger"><%= getLongStat(r, "noSeleccionados") %></td>
                                <td class="text-center small"><%= entR %> real. / <%= entP %> prog.</td>
                                <td class="text-center fw-bold"><%= conv %>%</td>
                            </tr>
                            <% } } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>
        </div>
    </main>
</div>

<!-- ====================================================================== -->
<!-- MODALES                                                                -->
<!-- ====================================================================== -->

<!-- Modal Evaluación del Postulante -->
<div class="modal fade" id="modalEvaluacion" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog modal-dialog-centered modal-lg modal-dialog-scrollable">
        <div class="modal-content modal-content-propath">
            <div class="modal-header modal-header-propath">
                <h5 class="modal-title"><i class="bi bi-file-earmark-person me-2"></i>Evaluación del Postulante</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body modal-body-propath">
                <div class="card border-0 bg-light p-3 mb-3 rounded-3">
                    <div class="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-2">
                        <div>
                            <h5 class="fw-bold mb-1 text-dark" id="evaluacionEstudiante">Cargando postulante...</h5>
                            <div class="text-secondary small d-flex flex-wrap gap-3 mt-1">
                                <span><i class="bi bi-envelope me-1 text-primary"></i><span id="evaluacionCorreo">—</span></span>
                                <span><i class="bi bi-bank me-1 text-primary"></i><span id="evaluacionUniversidad">—</span></span>
                                <span><i class="bi bi-mortarboard me-1 text-primary"></i><span id="evaluacionCarrera">—</span></span>
                                <span><i class="bi bi-bookmark me-1 text-primary"></i>Ciclo <span id="evaluacionCiclo">—</span></span>
                                <span><i class="bi bi-telephone me-1 text-primary"></i><span id="evaluacionTelefono">—</span></span>
                            </div>
                        </div>
                        <span class="badge bg-primary rounded-pill px-3 py-2" id="evaluacionEstado">POSTULADO</span>
                    </div>
                    <div class="pt-2 border-top small text-muted d-flex justify-content-between flex-wrap gap-2">
                        <span><i class="bi bi-briefcase me-1 text-secondary"></i><strong id="evaluacionOferta">—</strong></span>
                        <span><i class="bi bi-calendar-event me-1 text-secondary"></i>Postulación: <span id="evaluacionFecha">—</span></span>
                    </div>
                </div>

                <div id="evaluacionMotivoRechazoBox" class="alert alert-danger small mb-3" style="display:none;">
                    <strong>Motivo de rechazo registrado:</strong> <span id="evaluacionMotivoRechazoTexto"></span>
                </div>

                <div class="d-flex justify-content-between align-items-center mb-2">
                    <h6 class="fw-bold mb-0 text-dark"><i class="bi bi-file-earmark-pdf me-2 text-danger"></i>Currículum Vitae y Documentos</h6>
                </div>
                <div id="evaluacionCV" class="mb-3 p-2 border rounded bg-white">
                    <span class="text-muted small">Cargando CV...</span>
                </div>
                <div id="evaluacionDocsAdicionales" class="mb-4"></div>

                <h6 class="fw-bold mb-2 text-dark"><i class="bi bi-patch-question me-2 text-info"></i>Cuestionario de Postulación (12 Preguntas y Respuestas)</h6>
                <div id="evaluacionRespuestas" class="mb-2">
                    <span class="text-muted small">Cargando respuestas...</span>
                </div>
            </div>
            <div class="modal-footer bg-light border-0 d-flex justify-content-between flex-wrap gap-2">
                <form action="${pageContext.request.contextPath}/empresa/oferta" method="POST" class="d-flex flex-wrap gap-2">
                    <input type="hidden" name="accion" value="cambiarEstadoPostulacion">
                    <input type="hidden" name="idPostulacion" id="evaluacionIdPostulacion">
                    <button type="submit" name="estado" value="EN_REVISION" id="btnEvalRevision" class="btn btn-warning text-dark rounded-pill px-3">
                        <i class="bi bi-hourglass-split me-1"></i>En Revisión
                    </button>
                    <button type="submit" name="estado" value="SELECCIONADO" id="btnEvalSeleccionar" class="btn btn-success rounded-pill px-3">
                        <i class="bi bi-check2-circle me-1"></i>Seleccionar
                    </button>
                    <button type="button" id="btnEvalRechazar" class="btn btn-danger rounded-pill px-3" onclick="abrirModalRechazo()">
                        <i class="bi bi-x-circle me-1"></i>Rechazar
                    </button>
                </form>
                <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cerrar</button>
            </div>
        </div>
    </div>
</div>

<!-- Modal Rechazar Postulante -->
<div class="modal fade" id="modalRechazo" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content modal-content-propath">
            <form action="${pageContext.request.contextPath}/empresa/oferta" method="POST">
                <div class="modal-header modal-header-propath">
                    <h5 class="modal-title"><i class="bi bi-x-circle me-2"></i>Rechazar Postulante</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body modal-body-propath">
                    <p class="small text-secondary">Escribe un motivo constructivo que podrá consultar el estudiante en su historial.</p>
                    <input type="hidden" name="accion" value="cambiarEstadoPostulacion">
                    <input type="hidden" name="estado" value="NO_SELECCIONADO">
                    <input type="hidden" name="idPostulacion" id="rechazoIdPostulacion">
                    <textarea name="motivoRechazo" class="form-control" maxlength="1000" rows="4"
                              placeholder="Ej. Buscamos disponibilidad horaria en turno mañana..." required></textarea>
                </div>
                <div class="modal-footer bg-light border-0">
                    <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-danger rounded-pill px-4">Confirmar Rechazo</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal Programar / Editar Entrevista -->
<div class="modal fade" id="modalEntrevista" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content modal-content-propath">
            <form action="${pageContext.request.contextPath}/empresa/entrevista" method="POST">
                <div class="modal-header modal-header-propath">
                    <h5 class="modal-title" id="modalEntrevistaTitulo"><i class="bi bi-calendar-plus me-2"></i>Programar Entrevista</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body modal-body-propath">
                    <input type="hidden" name="accion" id="entrevistaAccion" value="crear">
                    <input type="hidden" name="idPostulacion" id="entrevistaPostulacion">
                    <input type="hidden" name="idEntrevista" id="entrevistaId">
                    <input type="hidden" name="redirectTab" id="entrevistaRedirectTab" value="entrevistas">

                    <div class="row g-3">
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold">Fecha *</label>
                            <input type="date" name="fecha" id="entrevistaFecha" class="form-control" required>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold">Hora *</label>
                            <input type="time" name="hora" id="entrevistaHora" class="form-control" required>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold">Modalidad *</label>
                            <select name="modalidad" id="entrevistaModalidad" class="form-select" onchange="cambiarModalidadEntrevista()">
                                <option value="VIRTUAL">Virtual</option>
                                <option value="PRESENCIAL">Presencial</option>
                            </select>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold">Estado</label>
                            <select name="estado" id="entrevistaEstado" class="form-select">
                                <option value="PROGRAMADA">Programada</option>
                                <option value="REALIZADA">Realizada</option>
                                <option value="CANCELADA">Cancelada</option>
                            </select>
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold" id="entrevistaLugarLabel">Enlace de reunión *</label>
                            <input type="url" name="enlaceLugar" id="entrevistaLugar" class="form-control" placeholder="https://meet.google.com/..." maxlength="500" required>
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold">Observaciones / Indicaciones</label>
                            <textarea name="observaciones" id="entrevistaObservaciones" class="form-control" maxlength="1000" rows="3" placeholder="Indicaciones previas para el candidato o comentarios tras la entrevista..."></textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer bg-light border-0">
                    <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-primary rounded-pill px-4">Guardar Entrevista</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal Editar Oferta -->
<div class="modal fade" id="modalEditarOferta" tabindex="-1" aria-labelledby="modalEditarOfertaLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content modal-content-propath">
            <div class="modal-header modal-header-propath">
                <h2 class="modal-title h5" id="modalEditarOfertaLabel"><i class="bi bi-pencil-square me-2"></i>Editar Oferta de Prácticas</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <form action="${pageContext.request.contextPath}/empresa/oferta" method="POST">
                <input type="hidden" name="accion" value="actualizar">
                <input type="hidden" name="id" id="editOfertaId">
                <div class="modal-body modal-body-propath">
                    <div class="row g-3">
                        <div class="col-12 col-md-6">
                            <label class="form-label small fw-semibold">Título de la Convocatoria *</label>
                            <input type="text" class="form-control" name="titulo" id="editOfertaTitulo" maxlength="150" required>
                        </div>
                        <div class="col-12 col-md-6">
                            <label class="form-label small fw-semibold">Carrera Profesional *</label>
                            <input type="text" class="form-control" name="carrera" id="editOfertaCarrera" maxlength="150" required>
                        </div>
                        <div class="col-12 col-md-4">
                            <label class="form-label small fw-semibold">Modalidad</label>
                            <select class="form-select" name="modalidad" id="editOfertaModalidad">
                                <option value="PRESENCIAL">Presencial</option>
                                <option value="HIBRIDA">Híbrida</option>
                                <option value="VIRTUAL">Virtual / Remota</option>
                            </select>
                        </div>
                        <div class="col-12 col-md-4">
                            <label class="form-label small fw-semibold">Vacantes Disponibles *</label>
                            <input type="number" class="form-control" name="vacantes" id="editOfertaVacantes" min="1" max="100" required>
                        </div>
                        <div class="col-12 col-md-4">
                            <label class="form-label small fw-semibold">Fecha Límite *</label>
                            <input type="date" class="form-control" name="fechaLimite" id="editOfertaFecha" required>
                        </div>
                        <div class="col-12 col-md-6">
                            <label class="form-label small fw-semibold">Ubicación / Distrito</label>
                            <input type="text" class="form-control" name="ubicacion" id="editOfertaUbicacion" maxlength="150">
                        </div>
                        <div class="col-12 col-md-6">
                            <label class="form-label small fw-semibold">Subvención Económica (Opcional)</label>
                            <div class="input-group">
                                <span class="input-group-text">S/</span>
                                <input type="number" class="form-control" name="remuneracion" id="editOfertaRemuneracion" min="0" step="0.01">
                            </div>
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold">Estado Actual</label>
                            <input type="text" class="form-control" id="editOfertaEstado" readonly>
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold">Descripción</label>
                            <textarea class="form-control" name="descripcion" id="editOfertaDescripcion" rows="2" required></textarea>
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold">Requisitos</label>
                            <textarea class="form-control" name="requisitos" id="editOfertaRequisitos" rows="3"></textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer bg-light border-0">
                    <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-primary rounded-pill px-4"><i class="bi bi-check2 me-1"></i>Guardar Cambios</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal Editar Perfil Empresa -->
<div class="modal fade" id="modalEditarPerfilEmpresa" tabindex="-1" aria-labelledby="modalEditarPerfilEmpresaLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content modal-content-propath">
            <div class="modal-header modal-header-propath">
                <h2 class="modal-title h5" id="modalEditarPerfilEmpresaLabel"><i class="bi bi-building-gear me-2"></i>Actualizar Mi Empresa</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <form action="${pageContext.request.contextPath}/empresa/oferta" method="POST">
                <input type="hidden" name="accion" value="actualizarPerfilEmpresa">
                <div class="modal-body modal-body-propath">
                    <div class="row g-3">
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold">Nombres del Representante *</label>
                            <input type="text" class="form-control" name="representanteNombre"
                                   value="<%= escaparHtml((miEmpresa != null && miEmpresa.getRepresentanteNombre() != null && !miEmpresa.getRepresentanteNombre().isEmpty()) ? miEmpresa.getRepresentanteNombre() : usuario.getNombre()) %>" required>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold">Apellidos del Representante *</label>
                            <input type="text" class="form-control" name="representanteApellidos"
                                   value="<%= escaparHtml((miEmpresa != null && miEmpresa.getRepresentanteApellidos() != null && !miEmpresa.getRepresentanteApellidos().isEmpty()) ? miEmpresa.getRepresentanteApellidos() : usuario.getApellidos()) %>" required>
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold">Correo Electrónico *</label>
                            <input type="email" class="form-control" name="correo" value="<%= escaparHtml(usuario.getCorreo()) %>" required>
                        </div>
                        <div class="col-12 pt-2 border-top">
                            <div class="small fw-bold text-secondary mb-2"><i class="bi bi-shield-lock me-1"></i>Cambio de Contraseña (Opcional)</div>
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold text-secondary">Contraseña Actual</label>
                            <input type="password" class="form-control" name="contrasenaActual" placeholder="Requerida solo si vas a cambiar contraseña">
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">Nueva Contraseña</label>
                            <input type="password" class="form-control" name="contrasena" placeholder="Mínimo 8 caracteres" minlength="8">
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">Confirmar Nueva Contraseña</label>
                            <input type="password" class="form-control" name="confirmarContrasena" placeholder="Repite la contraseña" minlength="8">
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold">Razón Social *</label>
                            <input type="text" class="form-control" name="razonSocial" value="<%= escaparHtml(razon) %>" required>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold">RUC (11 dígitos) *</label>
                            <input type="text" class="form-control" name="ruc" value="<%= miEmpresa != null ? escaparHtml(miEmpresa.getRuc()) : "" %>" pattern="[0-9]{11}" maxlength="11" inputmode="numeric" required>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold">Teléfono</label>
                            <input type="text" class="form-control" name="telefono" value="<%= miEmpresa != null ? escaparHtml(miEmpresa.getTelefono()) : "" %>">
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold">Dirección</label>
                            <input type="text" class="form-control" name="direccion" value="<%= miEmpresa != null ? escaparHtml(miEmpresa.getDireccion()) : "" %>">
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold">Descripción Corporativa</label>
                            <textarea class="form-control" name="descripcion" rows="2"><%= miEmpresa != null ? escaparHtml(miEmpresa.getDescripcion()) : "" %></textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer bg-light border-0 d-flex justify-content-between">
                    <button type="button" class="btn btn-outline-danger rounded-pill px-3"
                            onclick="mostrarModalConfirmacion('Dar de baja empresa', '¿Seguro que deseas solicitar la baja de tu cuenta corporativa?', function() { document.getElementById('formBajaEmpresa').submit(); });">
                        <i class="bi bi-person-x me-1"></i>Dar de baja
                    </button>
                    <div class="d-flex gap-2">
                        <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-primary rounded-pill px-4"><i class="bi bi-check2 me-1"></i>Guardar Cambios</button>
                    </div>
                </div>
            </form>
            <form id="formBajaEmpresa" action="${pageContext.request.contextPath}/empresa/oferta" method="POST" style="display:none;">
                <input type="hidden" name="accion" value="darDeBaja">
            </form>
        </div>
    </div>
</div>

<!-- Modal Detalle Oferta Empresa -->
<div class="modal fade" id="modalDetalleOfertaEmpresa" tabindex="-1" aria-labelledby="modalDetalleOfertaEmpresaLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-lg modal-dialog-scrollable">
        <div class="modal-content modal-content-propath">
            <div class="modal-header modal-header-propath">
                <h5 class="modal-title" id="modalDetalleOfertaEmpresaLabel"><i class="bi bi-briefcase me-2"></i>Detalle de la Convocatoria</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body modal-body-propath">
                <div class="card border-0 bg-light p-3 mb-3 rounded-3">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <span class="badge bg-primary-subtle text-primary mb-1" id="detOfrCarrera">—</span>
                            <h4 class="h5 fw-bold text-dark mb-1" id="detOfrTitulo">—</h4>
                            <div class="small text-muted"><i class="bi bi-building me-1"></i><span id="detOfrEmpresa">—</span></div>
                        </div>
                        <span class="badge-status badge-activo" id="detOfrEstado">ACTIVA</span>
                    </div>
                </div>
                <div class="row g-3 mb-3">
                    <div class="col-sm-6 col-md-4"><div class="p-2 border rounded bg-white"><span class="text-muted small d-block">Modalidad</span><strong class="text-dark small" id="detOfrModalidad">—</strong></div></div>
                    <div class="col-sm-6 col-md-4"><div class="p-2 border rounded bg-white"><span class="text-muted small d-block">Vacantes</span><strong class="text-dark small" id="detOfrVacantes">—</strong></div></div>
                    <div class="col-sm-6 col-md-4"><div class="p-2 border rounded bg-white"><span class="text-muted small d-block">Subvención</span><strong class="text-dark small" id="detOfrRemuneracion">—</strong></div></div>
                    <div class="col-sm-6 col-md-4"><div class="p-2 border rounded bg-white"><span class="text-muted small d-block">Ubicación</span><strong class="text-dark small" id="detOfrUbicacion">—</strong></div></div>
                    <div class="col-sm-6 col-md-4"><div class="p-2 border rounded bg-white"><span class="text-muted small d-block">Publicado</span><strong class="text-dark small" id="detOfrPublicacion">—</strong></div></div>
                    <div class="col-sm-6 col-md-4"><div class="p-2 border rounded bg-white"><span class="text-muted small d-block">Fecha Límite</span><strong class="text-danger small" id="detOfrFechaLimite">—</strong></div></div>
                </div>
                <div class="mb-3">
                    <h6 class="fw-bold text-dark small mb-1">Descripción del Puesto</h6>
                    <div class="p-3 border rounded bg-white text-secondary small" id="detOfrDescripcion" style="white-space:pre-wrap;">—</div>
                </div>
                <div>
                    <h6 class="fw-bold text-dark small mb-1">Requisitos y Competencias</h6>
                    <div class="p-3 border rounded bg-white text-secondary small" id="detOfrRequisitos" style="white-space:pre-wrap;">—</div>
                </div>
            </div>
            <div class="modal-footer bg-light border-0">
                <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cerrar</button>
            </div>
        </div>
    </div>
</div>

<!-- Modal Detalle Entrevista Empresa -->
<div class="modal fade" id="modalDetalleEntrevistaEmpresa" tabindex="-1" aria-labelledby="modalDetalleEntrevistaEmpresaLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content modal-content-propath">
            <div class="modal-header modal-header-propath">
                <h5 class="modal-title" id="modalDetalleEntrevistaEmpresaLabel"><i class="bi bi-calendar2-check me-2"></i>Detalle de la Entrevista</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body modal-body-propath">
                <div class="card border-0 bg-light p-3 mb-3 rounded-3">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <span class="text-muted small d-block">Postulante Convocado</span>
                            <h5 class="fw-bold mb-1 text-dark" id="detEntEstudiante">—</h5>
                            <span class="small text-muted"><i class="bi bi-briefcase me-1"></i><strong id="detEntOferta">—</strong></span>
                        </div>
                        <span class="badge-status badge-activo" id="detEntEstado">PROGRAMADA</span>
                    </div>
                </div>
                <div class="row g-2 mb-3">
                    <div class="col-6"><div class="p-2 border rounded bg-white"><span class="text-muted small d-block">Fecha</span><strong class="text-dark small" id="detEntFecha">—</strong></div></div>
                    <div class="col-6"><div class="p-2 border rounded bg-white"><span class="text-muted small d-block">Hora</span><strong class="text-dark small" id="detEntHora">—</strong></div></div>
                    <div class="col-12"><div class="p-2 border rounded bg-white"><span class="text-muted small d-block">Modalidad</span><strong class="text-dark small" id="detEntModalidad">—</strong></div></div>
                    <div class="col-12"><div class="p-2 border rounded bg-white"><span class="text-muted small d-block" id="detEntLugarLabel">Lugar o Enlace</span><div class="small mt-1" id="detEntLugar">—</div></div></div>
                </div>
                <div>
                    <span class="text-muted small d-block mb-1">Observaciones</span>
                    <div class="p-2 border rounded bg-white text-secondary small" id="detEntObservaciones">—</div>
                </div>
            </div>
            <div class="modal-footer bg-light border-0">
                <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cerrar</button>
            </div>
        </div>
    </div>
</div>

<script>
const HOY_ISO = "<%= hoyIso %>";
const PUEDE_OPERAR = <%= puedeOperar %>;

function cambiarSeccionEmpresa(seccion) {
    document.querySelectorAll('.dashboard-section').forEach(s => s.classList.remove('active'));
    const target = document.getElementById('sec-' + seccion);
    if (target) target.classList.add('active');

    document.querySelectorAll('#empresaSidebarNav .btn-nav-section').forEach(btn => {
        btn.classList.toggle('active', btn.getAttribute('data-section') === seccion);
    });

    const sidebar = document.querySelector('.admin-sidebar');
    const backdrop = document.querySelector('.sidebar-backdrop');
    if (sidebar) sidebar.classList.remove('show');
    if (backdrop) backdrop.classList.remove('show');

    if (window.history && window.history.replaceState) {
        const url = new URL(window.location.href);
        url.searchParams.set('tab', seccion);
        window.history.replaceState({}, '', url.toString());
    }
}

function verPostulantesDeOferta(idOferta) {
    const sel = document.getElementById('filtroOfertaPostulantes');
    if (sel) sel.value = String(idOferta);
    const estSel = document.getElementById('filtroEstadoPostulantes');
    if (estSel) estSel.value = 'TODOS';
    filtrarPostulantesEmpresa();
    cambiarSeccionEmpresa('postulantes');
}

function filtrarOfertasEmpresa() {
    const q = (document.getElementById('buscarOfertaEmpresa').value || '').trim().toLowerCase();
    const est = document.getElementById('filtroEstadoOfertaEmpresa').value;
    document.querySelectorAll('.oferta-empresa-card').forEach(card => {
        const cardEst = (card.getAttribute('data-estado') || '').toUpperCase();
        const cardTxt = card.getAttribute('data-texto') || '';
        const matchEst = est === 'TODAS' || cardEst === est;
        const matchTxt = !q || cardTxt.includes(q);
        card.style.display = (matchEst && matchTxt) ? '' : 'none';
    });
}

function filtrarPostulantesEmpresa() {
    const q = (document.getElementById('buscarPostulanteEmpresa').value || '').trim().toLowerCase();
    const idOfr = document.getElementById('filtroOfertaPostulantes').value;
    const est = document.getElementById('filtroEstadoPostulantes').value;
    document.querySelectorAll('.postulante-empresa-card').forEach(card => {
        const cardOfr = card.getAttribute('data-id-oferta');
        const cardEst = (card.getAttribute('data-estado') || '').toUpperCase();
        const cardTxt = card.getAttribute('data-texto') || '';
        const matchOfr = idOfr === 'TODAS' || cardOfr === idOfr;
        const matchEst = est === 'TODOS' || cardEst === est;
        const matchTxt = !q || cardTxt.includes(q);
        card.style.display = (matchOfr && matchEst && matchTxt) ? '' : 'none';
    });
}

function filtrarEntrevistasEmpresa() {
    const q = (document.getElementById('buscarEntrevistaEmpresa').value || '').trim().toLowerCase();
    const est = document.getElementById('filtroEstadoEntrevistaEmpresa').value;
    document.querySelectorAll('.entrevista-empresa-card').forEach(card => {
        const cardEst = (card.getAttribute('data-estado') || '').toUpperCase();
        const cardTxt = card.getAttribute('data-texto') || '';
        const matchEst = est === 'TODAS' || cardEst === est;
        const matchTxt = !q || cardTxt.includes(q);
        card.style.display = (matchEst && matchTxt) ? '' : 'none';
    });
}

function abrirModalEditarOferta(btn) {
    document.getElementById('editOfertaId').value = (btn.getAttribute('data-id') || '').trim();
    document.getElementById('editOfertaTitulo').value = (btn.getAttribute('data-titulo') || '').trim();
    document.getElementById('editOfertaCarrera').value = (btn.getAttribute('data-carrera') || '').trim();
    document.getElementById('editOfertaModalidad').value = (btn.getAttribute('data-modalidad') || 'PRESENCIAL').trim();
    document.getElementById('editOfertaVacantes').value = (btn.getAttribute('data-vacantes') || '1').trim();
    let fecVal = (btn.getAttribute('data-fecha') || '').trim();
    if (fecVal.length > 10) fecVal = fecVal.substring(0, 10);
    document.getElementById('editOfertaFecha').value = fecVal;
    document.getElementById('editOfertaUbicacion').value = (btn.getAttribute('data-ubicacion') || '').trim();
    document.getElementById('editOfertaDescripcion').value = (btn.getAttribute('data-descripcion') || '').trim();
    document.getElementById('editOfertaEstado').value = (btn.getAttribute('data-estado') || 'ACTIVA').trim();
    document.getElementById('editOfertaRequisitos').value = (btn.getAttribute('data-requisitos') || '').trim();
    const remGuardada = (btn.getAttribute('data-remuneracion') || '').trim();
    document.getElementById('editOfertaRemuneracion').value = remGuardada.replace(/^S\/\s*/, '').trim();

    const modalEl = document.getElementById('modalEditarOferta');
    if (window.bootstrap && window.bootstrap.Modal) {
        window.bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }
}

function verDetalleOfertaEmpresa(btn) {
    const d = btn.dataset;
    document.getElementById('detOfrTitulo').textContent = d.titulo || '—';
    document.getElementById('detOfrCarrera').textContent = d.carrera || '—';
    document.getElementById('detOfrEmpresa').textContent = d.empresa || '—';
    document.getElementById('detOfrModalidad').textContent = d.modalidad || '—';
    document.getElementById('detOfrVacantes').textContent = (d.vacantes || '1') + ' vacante(s)';
    let rem = (d.remuneracion || '').trim();
    if (rem && !rem.toUpperCase().startsWith('S/')) rem = 'S/ ' + rem;
    document.getElementById('detOfrRemuneracion').textContent = rem || 'A convenir';
    document.getElementById('detOfrUbicacion').textContent = d.ubicacion || 'No especificada';
    document.getElementById('detOfrPublicacion').textContent = d.publicacion || '—';
    document.getElementById('detOfrFechaLimite').textContent = d.fecha || 'Sin fecha límite';
    document.getElementById('detOfrDescripcion').textContent = d.descripcion || 'Sin descripción detallada.';
    document.getElementById('detOfrRequisitos').textContent = d.requisitos || 'Sin requisitos específicos.';

    const badge = document.getElementById('detOfrEstado');
    const est = (d.estado || 'ACTIVA').toUpperCase();
    badge.textContent = est;
    badge.className = 'badge-status ' + (est === 'ACTIVA' ? 'badge-activo' : 'badge-bloqueado');

    window.bootstrap.Modal.getOrCreateInstance(document.getElementById('modalDetalleOfertaEmpresa')).show();
}

function verDetalleEntrevistaEmpresa(btn) {
    const d = btn.dataset;
    document.getElementById('detEntEstudiante').textContent = d.estudiante || '—';
    document.getElementById('detEntOferta').textContent = d.oferta || '—';
    document.getElementById('detEntFecha').textContent = d.fecha || '—';
    document.getElementById('detEntHora').textContent = d.hora ? d.hora.substring(0, 5) : '—';
    document.getElementById('detEntModalidad').textContent = d.modalidad || '—';

    const badge = document.getElementById('detEntEstado');
    const estNorm = (d.estado || 'PROGRAMADA').toUpperCase();
    badge.textContent = estNorm;
    badge.className = 'badge-status ' + (estNorm === 'REALIZADA' ? 'badge-seleccionado' : (estNorm === 'CANCELADA' ? 'badge-bloqueado' : 'badge-activo'));

    const lugarEl = document.getElementById('detEntLugar');
    const lugarLabel = document.getElementById('detEntLugarLabel');
    const lugarVal = (d.lugar || '').trim();
    if (d.modalidad === 'VIRTUAL') {
        lugarLabel.textContent = 'Enlace de Reunión';
        if (lugarVal.startsWith('http://') || lugarVal.startsWith('https://')) {
            lugarEl.replaceChildren();
            const link = document.createElement('a');
            link.href = lugarVal;
            link.target = '_blank';
            link.rel = 'noopener noreferrer';
            link.className = 'btn btn-sm btn-outline-primary rounded-pill';
            link.innerHTML = '<i class="bi bi-camera-video me-1"></i>Abrir enlace de reunión';
            lugarEl.appendChild(link);
        } else {
            lugarEl.textContent = lugarVal || 'Enlace no registrado';
        }
    } else {
        lugarLabel.textContent = 'Dirección o Sede';
        lugarEl.textContent = lugarVal || 'Dirección no registrada';
    }
    document.getElementById('detEntObservaciones').textContent = d.observaciones || 'Sin observaciones.';
    window.bootstrap.Modal.getOrCreateInstance(document.getElementById('modalDetalleEntrevistaEmpresa')).show();
}

function abrirModalRechazo() {
    document.getElementById("rechazoIdPostulacion").value = document.getElementById("evaluacionIdPostulacion").value;
    bootstrap.Modal.getOrCreateInstance(document.getElementById("modalEvaluacion")).hide();
    bootstrap.Modal.getOrCreateInstance(document.getElementById("modalRechazo")).show();
}

async function abrirEvaluacion(boton) {
    const id = boton.dataset.idPostulacion;
    const cv = document.getElementById("evaluacionCV");
    const docsExtra = document.getElementById("evaluacionDocsAdicionales");
    const respuestas = document.getElementById("evaluacionRespuestas");
    const preguntas = [
        "¿Por qué te interesa esta oportunidad?",
        "¿Qué conocimientos puedes aportar?",
        "¿Qué herramientas o tecnologías manejas?",
        "¿Has desarrollado proyectos relacionados?",
        "¿Qué esperas aprender?",
        "¿Cuál es tu disponibilidad horaria?",
        "¿Cuáles son tus principales fortalezas?",
        "¿Cómo organizas tus responsabilidades?",
        "¿Cómo reaccionas ante un problema inesperado?",
        "¿Cómo trabajas en equipo?",
        "¿Qué aspecto profesional deseas mejorar?",
        "¿Cuáles son tus objetivos profesionales?"
    ];

    const estadoActual = (boton.dataset.estado || "POSTULADO").toUpperCase();
    const tieneEntrevista = boton.dataset.evaluado === "true";

    document.getElementById("btnEvalRevision").disabled = !PUEDE_OPERAR || estadoActual !== "POSTULADO";
    document.getElementById("btnEvalSeleccionar").disabled = !PUEDE_OPERAR || estadoActual === "SELECCIONADO" || estadoActual === "NO_SELECCIONADO";
    document.getElementById("btnEvalRechazar").disabled = !PUEDE_OPERAR || estadoActual === "NO_SELECCIONADO" || tieneEntrevista;

    document.getElementById("evaluacionIdPostulacion").value = id;
    document.getElementById("evaluacionEstudiante").textContent = boton.dataset.estudiante || "Postulante";
    document.getElementById("evaluacionOferta").textContent = boton.dataset.oferta || "";
    document.getElementById("evaluacionEstado").textContent = estadoActual.replace('_', ' ');
    document.getElementById("evaluacionMotivoRechazoBox").style.display = "none";

    cv.textContent = "Cargando CV...";
    docsExtra.replaceChildren();
    respuestas.textContent = "Cargando respuestas...";

    const ruta = "${pageContext.request.contextPath}/empresa/oferta";
    try {
        const resultado = await fetch(ruta + "?accion=evaluacion&idPostulacion=" + encodeURIComponent(id), {
            credentials: "same-origin", cache: "no-store"
        });
        if (!resultado.ok) throw new Error("HTTP " + resultado.status);
        const datos = await resultado.json();

        document.getElementById("evaluacionEstudiante").textContent = datos.estudiante || "Postulante";
        document.getElementById("evaluacionCorreo").textContent = datos.correo || "—";
        document.getElementById("evaluacionUniversidad").textContent = datos.universidad || "—";
        document.getElementById("evaluacionCarrera").textContent = datos.carrera || "—";
        document.getElementById("evaluacionCiclo").textContent = datos.ciclo > 0 ? datos.ciclo : "—";
        document.getElementById("evaluacionTelefono").textContent = datos.telefono || "—";
        document.getElementById("evaluacionFecha").textContent = datos.fecha || "—";
        const estReal = (datos.estado || estadoActual).toUpperCase();
        const badgeEst = document.getElementById("evaluacionEstado");
        badgeEst.textContent = estReal.replace('_', ' ');
        badgeEst.className = "badge rounded-pill px-3 py-2 " + (estReal === "SELECCIONADO" ? "bg-success" : (estReal === "EN_REVISION" ? "bg-warning text-dark" : (estReal === "NO_SELECCIONADO" ? "bg-danger" : "bg-primary")));

        if (datos.motivoRechazo) {
            document.getElementById("evaluacionMotivoRechazoTexto").textContent = datos.motivoRechazo;
            document.getElementById("evaluacionMotivoRechazoBox").style.display = "block";
        }

        cv.replaceChildren();
        if (datos.cvDisponible) {
            const iframe = document.createElement("iframe");
            iframe.src = ruta + "?accion=cv&idPostulacion=" + encodeURIComponent(id);
            iframe.title = "CV del postulante";
            iframe.style.width = "100%";
            iframe.style.height = "400px";
            iframe.style.border = "1px solid #ddd";
            cv.appendChild(iframe);

            const accionesCv = document.createElement("div");
            accionesCv.className = "d-flex flex-wrap gap-2 mt-2";
            const enlaceVer = document.createElement("a");
            enlaceVer.href = iframe.src;
            enlaceVer.target = "_blank";
            enlaceVer.rel = "noopener";
            enlaceVer.className = "btn btn-sm btn-outline-primary rounded-pill";
            enlaceVer.innerHTML = '<i class="bi bi-box-arrow-up-right me-1"></i>Abrir en nueva pestaña';
            const enlaceDesc = document.createElement("a");
            enlaceDesc.href = iframe.src + "&download=1";
            enlaceDesc.className = "btn btn-sm btn-outline-secondary rounded-pill";
            enlaceDesc.innerHTML = '<i class="bi bi-download me-1"></i>Descargar CV';
            accionesCv.append(enlaceVer, enlaceDesc);
            cv.appendChild(accionesCv);
        } else {
            cv.textContent = "Este postulante no tiene CV registrado.";
        }

        const otrosDocs = Array.isArray(datos.documentos)
            ? datos.documentos.filter(d => (d.tipoDocumento || '').toUpperCase() !== 'CV') : [];
        if (otrosDocs.length > 0) {
            const tituloDocs = document.createElement("div");
            tituloDocs.className = "small fw-bold text-secondary mb-2";
            tituloDocs.textContent = "Documentos complementarios del postulante:";
            docsExtra.appendChild(tituloDocs);
            otrosDocs.forEach(doc => {
                const row = document.createElement("div");
                row.className = "d-flex justify-content-between align-items-center p-2 border rounded mb-1 bg-light small";
                const info = document.createElement("span");
                info.textContent = "[" + (doc.tipoDocumento || "DOC") + "] " + (doc.nombreArchivo || "Archivo") + " (" + (doc.tamanio || "") + ")";
                const btns = document.createElement("div");
                btns.className = "d-flex gap-2";
                const urlDoc = ruta + "?accion=documento&idPostulacion=" + encodeURIComponent(id) + "&idDocumento=" + encodeURIComponent(doc.id);
                const aVer = document.createElement("a");
                aVer.href = urlDoc;
                aVer.target = "_blank";
                aVer.className = "btn btn-sm btn-outline-primary rounded-pill py-0 px-2";
                aVer.textContent = "Ver";
                const aDesc = document.createElement("a");
                aDesc.href = urlDoc + "&download=1";
                aDesc.className = "btn btn-sm btn-outline-secondary rounded-pill py-0 px-2";
                aDesc.textContent = "Descargar";
                btns.append(aVer, aDesc);
                row.append(info, btns);
                docsExtra.appendChild(row);
            });
        }

        respuestas.replaceChildren();
        const lista = Array.isArray(datos.respuestas) ? datos.respuestas : [];
        if (lista.length === 0) respuestas.textContent = "No hay respuestas registradas.";
        lista.sort((x, y) => Number(x.numeroPregunta) - Number(y.numeroPregunta));
        lista.forEach(item => {
            const numero = Number(item.numeroPregunta);
            const bloque = document.createElement("div");
            bloque.className = "border rounded p-3 mb-2";
            const pregunta = document.createElement("strong");
            pregunta.textContent = numero + ". " + (preguntas[numero - 1] || "Pregunta");
            const respuesta = document.createElement("p");
            respuesta.className = "mb-0 mt-2 small text-secondary";
            respuesta.textContent = item.respuesta || "Sin respuesta";
            bloque.append(pregunta, respuesta);
            respuestas.appendChild(bloque);
        });
    } catch (err) {
        cv.textContent = "No se pudo cargar el CV.";
        respuestas.textContent = "Error al consultar la evaluación: " + err.message;
    }
}

function abrirEntrevista(idPostulacion, redirectTab) {
    document.getElementById("modalEntrevistaTitulo").innerHTML = '<i class="bi bi-calendar-plus me-2"></i>Programar Entrevista';
    document.getElementById("entrevistaAccion").value = "crear";
    document.getElementById("entrevistaId").value = "";
    document.getElementById("entrevistaPostulacion").value = idPostulacion;
    document.getElementById("entrevistaRedirectTab").value = redirectTab || "entrevistas";
    const fechaInput = document.getElementById("entrevistaFecha");
    fechaInput.value = "";
    fechaInput.min = HOY_ISO;
    document.getElementById("entrevistaHora").value = "";
    document.getElementById("entrevistaModalidad").value = "VIRTUAL";
    document.getElementById("entrevistaLugar").value = "";
    document.getElementById("entrevistaEstado").value = "PROGRAMADA";
    document.getElementById("entrevistaObservaciones").value = "";
    cambiarModalidadEntrevista();
}

function editarEntrevista(boton) {
    const d = boton.dataset;
    document.getElementById("modalEntrevistaTitulo").innerHTML = '<i class="bi bi-pencil-square me-2"></i>Editar / Reprogramar Entrevista';
    document.getElementById("entrevistaAccion").value = "actualizar";
    document.getElementById("entrevistaId").value = d.id;
    document.getElementById("entrevistaPostulacion").value = d.postulacion;
    document.getElementById("entrevistaRedirectTab").value = "entrevistas";
    const fechaInput = document.getElementById("entrevistaFecha");
    fechaInput.removeAttribute("min");
    fechaInput.value = d.fecha;
    const h = (d.hora || "").trim();
    document.getElementById("entrevistaHora").value = h.length >= 5 ? h.substring(0, 5) : h;
    document.getElementById("entrevistaModalidad").value = d.modalidad || "VIRTUAL";
    cambiarModalidadEntrevista();
    document.getElementById("entrevistaLugar").value = d.lugar || "";
    document.getElementById("entrevistaEstado").value = d.estado || "PROGRAMADA";
    document.getElementById("entrevistaObservaciones").value = d.observaciones || "";
}

function cambiarModalidadEntrevista() {
    const virtual = document.getElementById("entrevistaModalidad").value === "VIRTUAL";
    const campo = document.getElementById("entrevistaLugar");
    campo.type = virtual ? "url" : "text";
    campo.placeholder = virtual ? "https://meet.google.com/..." : "Dirección o sede de la entrevista";
    document.getElementById("entrevistaLugarLabel").textContent = virtual ? "Enlace de reunión *" : "Dirección o lugar *";
}

window.cambiarSeccionEmpresa = cambiarSeccionEmpresa;
window.verPostulantesDeOferta = verPostulantesDeOferta;
window.filtrarOfertasEmpresa = filtrarOfertasEmpresa;
window.filtrarPostulantesEmpresa = filtrarPostulantesEmpresa;
window.filtrarEntrevistasEmpresa = filtrarEntrevistasEmpresa;
window.abrirModalEditarOferta = abrirModalEditarOferta;
window.verDetalleOfertaEmpresa = verDetalleOfertaEmpresa;
window.verDetalleEntrevistaEmpresa = verDetalleEntrevistaEmpresa;
window.abrirEvaluacion = abrirEvaluacion;
window.abrirModalRechazo = abrirModalRechazo;
window.abrirEntrevista = abrirEntrevista;
window.editarEntrevista = editarEntrevista;
window.cambiarModalidadEntrevista = cambiarModalidadEntrevista;
</script>
<script type="module" src="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/js/bootstrap.bundle.min.js"></script>
<script type="module" src="${pageContext.request.contextPath}/js/script.js"></script>
</body>
</html>
