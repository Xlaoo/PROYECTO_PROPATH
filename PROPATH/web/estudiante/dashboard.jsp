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

    private String labelTipoDoc(String tipo) {
        if ("CARTA_PRESENTACION".equalsIgnoreCase(tipo)) return "Carta de Presentación";
        if ("CERTIFICADO".equalsIgnoreCase(tipo)) return "Certificado / Notas";
        if ("CONSTANCIA".equalsIgnoreCase(tipo)) return "Constancia";
        if ("PORTAFOLIO".equalsIgnoreCase(tipo)) return "Portafolio";
        if ("CV".equalsIgnoreCase(tipo)) return "Currículum Vitae";
        return "Otro Documento";
    }
%>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@page import="com.propath.modelo.Usuario" %>
<%@page import="com.propath.modelo.Estudiante" %>
<%@page import="com.propath.modelo.OfertaPractica" %>
<%@page import="com.propath.modelo.Postulacion" %>
<%@page import="com.propath.modelo.Entrevista" %>
<%@page import="com.propath.modelo.DocumentoEstudiante" %>
<%@page import="com.propath.servicio.BackendCliente" %>
<%@page import="java.util.List" %>
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.Set" %>
<%@page import="java.util.TreeSet" %>
<%@page import="java.util.Map" %>
<%@page import="java.util.HashMap" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }

    Estudiante miEstudiante = null;
    if (usuario.getIdEstudiante() != null && usuario.getIdEstudiante() > 0) {
        miEstudiante = BackendCliente.obtenerEstudiantePorId(usuario.getIdEstudiante());
    }
    if (miEstudiante == null) {
        List<Estudiante> estudiantes = BackendCliente.listarEstudiantes();
        for (Estudiante e : estudiantes) {
            if (e.getCorreo() != null && e.getCorreo().equalsIgnoreCase(usuario.getCorreo())) {
                miEstudiante = e;
                usuario.setIdEstudiante(e.getId());
                break;
            }
        }
    }

    int idEstudiante = miEstudiante != null ? miEstudiante.getId()
            : (usuario.getIdEstudiante() != null ? usuario.getIdEstudiante() : 0);

    boolean estActivo = "ACTIVO".equalsIgnoreCase(usuario.getEstado())
            && (miEstudiante == null || "ACTIVO".equalsIgnoreCase(miEstudiante.getEstado()));

    DocumentoEstudiante miCv = idEstudiante > 0 ? BackendCliente.obtenerCvEstudiante(idEstudiante) : null;
    List<DocumentoEstudiante> misDocumentos = idEstudiante > 0
            ? BackendCliente.listarDocumentosEstudiante(idEstudiante) : new ArrayList<>();
    List<DocumentoEstudiante> docsComplementarios = new ArrayList<>();
    for (DocumentoEstudiante d : misDocumentos) {
        if (!"CV".equalsIgnoreCase(d.getTipoDocumento())) {
            docsComplementarios.add(d);
        }
    }

    List<OfertaPractica> ofertasDisponibles = BackendCliente.listarOfertasPublicas();
    Set<String> carrerasDisponibles = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    for (OfertaPractica o : ofertasDisponibles) {
        if (o.getCarrera() != null && !o.getCarrera().trim().isEmpty()) {
            carrerasDisponibles.add(o.getCarrera().trim());
        }
    }

    List<Postulacion> misPostulaciones = idEstudiante > 0
            ? BackendCliente.listarPostulacionesPorEstudiante(idEstudiante) : new ArrayList<>();
    List<Entrevista> misEntrevistas = idEstudiante > 0
            ? BackendCliente.listarEntrevistasPorEstudiante(idEstudiante) : new ArrayList<>();
    Map<String, Object> statsEst = idEstudiante > 0
            ? BackendCliente.obtenerEstadisticasEstudiante(idEstudiante) : new HashMap<>();

    long totalPostStat = getLongStat(statsEst, "totalPostulaciones");
    long postuladosStat = getLongStat(statsEst, "postulados");
    long enRevisionStat = getLongStat(statsEst, "enRevision");
    long seleccionadosStat = getLongStat(statsEst, "seleccionados");
    long noSeleccionadosStat = getLongStat(statsEst, "noSeleccionados");
    double tasaRespuestaStat = getDoubleStat(statsEst, "tasaRespuesta");
    double tasaSeleccionStat = getDoubleStat(statsEst, "tasaSeleccion");

    long totalEntStat = getLongStat(statsEst, "totalEntrevistas");
    long entProgramadasStat = getLongStat(statsEst, "entrevistasProgramadas");
    long entRealizadasStat = getLongStat(statsEst, "entrevistasRealizadas");
    long entCanceladasStat = getLongStat(statsEst, "entrevistasCanceladas");

    String tabParam = request.getParameter("tab");
    String activeTab = "inicio";
    if ("ofertas".equalsIgnoreCase(tabParam) || "postulaciones".equalsIgnoreCase(tabParam)
            || "entrevistas".equalsIgnoreCase(tabParam) || "documentos".equalsIgnoreCase(tabParam)) {
        activeTab = tabParam.toLowerCase();
    }

    String exito = request.getParameter("exito");
    String error = request.getParameter("error");
    String nombreMostrar = miEstudiante != null ? miEstudiante.getNombreCompleto() : usuario.getNombre();
%>
<!DOCTYPE html>
<html lang="es" data-bs-theme="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Portal del Estudiante | ProPath</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<div class="admin-layout">
    <!-- Sidebar Estudiante -->
    <aside class="admin-sidebar">
        <div class="sidebar-brand">
            <div class="sidebar-logo-container">
                <a href="${pageContext.request.contextPath}/estudiante/dashboard.jsp" class="d-block text-decoration-none">
                    <img src="${pageContext.request.contextPath}/img/logo.png" alt="ProPath" class="sidebar-logo">
                </a>
            </div>
        </div>

        <div class="sidebar-nav-container">
            <div class="sidebar-heading">Portal Estudiante</div>
            <nav class="nav flex-column" id="estudianteSidebarNav">
                <button type="button" class="nav-link-admin btn-nav-section <%= "inicio".equals(activeTab) ? "active" : "" %>"
                        data-section="inicio" onclick="cambiarSeccionEstudiante('inicio')">
                    <i class="bi bi-grid-1x2-fill"></i>
                    <span>Inicio y Estadísticas</span>
                </button>
                <button type="button" class="nav-link-admin btn-nav-section <%= "ofertas".equals(activeTab) ? "active" : "" %>"
                        data-section="ofertas" onclick="cambiarSeccionEstudiante('ofertas')">
                    <i class="bi bi-briefcase-fill"></i>
                    <span>Ofertas Disponibles</span>
                    <span class="nav-badge-count"><%= ofertasDisponibles.size() %></span>
                </button>
                <button type="button" class="nav-link-admin btn-nav-section <%= "postulaciones".equals(activeTab) ? "active" : "" %>"
                        data-section="postulaciones" onclick="cambiarSeccionEstudiante('postulaciones')">
                    <i class="bi bi-send-check-fill"></i>
                    <span>Mis Postulaciones</span>
                    <span class="nav-badge-count"><%= misPostulaciones.size() %></span>
                </button>
                <button type="button" class="nav-link-admin btn-nav-section <%= "entrevistas".equals(activeTab) ? "active" : "" %>"
                        data-section="entrevistas" onclick="cambiarSeccionEstudiante('entrevistas')">
                    <i class="bi bi-calendar-check-fill"></i>
                    <span>Mis Entrevistas</span>
                    <span class="nav-badge-count"><%= entProgramadasStat %></span>
                </button>
                <button type="button" class="nav-link-admin btn-nav-section <%= "documentos".equals(activeTab) ? "active" : "" %>"
                        data-section="documentos" onclick="cambiarSeccionEstudiante('documentos')">
                    <i class="bi bi-file-earmark-pdf-fill"></i>
                    <span>CV y Documentos</span>
                    <span class="nav-badge-count"><%= misDocumentos.size() %></span>
                </button>
            </nav>

            <div class="sidebar-heading mt-3">Mi Cuenta</div>
            <button type="button" class="nav-link-admin btn-nav-section" data-bs-toggle="modal" data-bs-target="#modalEditarPerfilEstudiante">
                <i class="bi bi-person-gear"></i>
                <span>Editar Mi Perfil</span>
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
                    <h1 class="welcome-title h5 mb-0">Hola, <%= escaparHtml(nombreMostrar) %></h1>
                    <span class="welcome-date">
                        <i class="bi bi-mortarboard me-1"></i>
                        <%= (miEstudiante != null && miEstudiante.getCarrera() != null && !miEstudiante.getCarrera().isEmpty())
                                ? escaparHtml(miEstudiante.getCarrera()) : "Estudiante Universitario" %>
                        <% if (miEstudiante != null && miEstudiante.getUniversidad() != null && !miEstudiante.getUniversidad().isEmpty()) { %>
                        &bull; <%= escaparHtml(miEstudiante.getUniversidad()) %>
                        <% } %>
                    </span>
                </div>
            </div>
            <div class="topbar-right">
                <span class="badge <%= miCv != null ? "bg-success-subtle text-success border border-success-subtle" : "bg-warning-subtle text-warning border border-warning-subtle" %> rounded-pill px-3 py-2 fw-semibold d-none d-sm-inline-flex align-items-center"
                      role="button" onclick="cambiarSeccionEstudiante('documentos')">
                    <i class="bi <%= miCv != null ? "bi-file-earmark-check-fill" : "bi-exclamation-circle-fill" %> me-1"></i>
                    <%= miCv != null ? "CV PDF Listo" : "CV Pendiente" %>
                </span>
                <button type="button" class="btn btn-sm btn-outline-primary rounded-pill px-3 fw-semibold"
                        data-bs-toggle="modal" data-bs-target="#modalEditarPerfilEstudiante">
                    <i class="bi bi-person-circle me-1"></i>Mi Perfil
                </button>
            </div>
        </header>

        <div class="admin-content">
            <% if (exito != null && !exito.isEmpty()) {
                String msgExito = "¡Operación completada exitosamente!";
                if ("cv_guardado".equals(exito)) msgExito = "¡Currículum Vitae subido exitosamente en formato PDF!";
                else if ("cv_reemplazado".equals(exito)) msgExito = "¡Currículum Vitae reemplazado exitosamente!";
                else if ("cv_eliminado".equals(exito)) msgExito = "¡Currículum Vitae eliminado correctamente!";
                else if ("documento_guardado".equals(exito)) msgExito = "¡Documento complementario validado y guardado exitosamente!";
                else if ("documento_eliminado".equals(exito)) msgExito = "¡Documento eliminado correctamente!";
                else if ("postulacion".equals(exito) || "1".equals(exito) || "true".equals(exito)) msgExito = "¡Postulación enviada exitosamente! Puedes hacer seguimiento en tiempo real aquí.";
                else if ("postulacion_editada".equals(exito)) msgExito = "¡Respuestas de la postulación actualizadas correctamente!";
                else if ("postulacion_retirada".equals(exito)) msgExito = "Postulación retirada correctamente.";
                else if ("postulacion_eliminada".equals(exito)) msgExito = "Registro de postulación eliminado.";
                else if ("perfil_actualizado".equals(exito)) msgExito = "Tu perfil académico ha sido actualizado correctamente.";
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

            <!-- ============================================================== -->
            <!-- SECCIÓN 1: INICIO Y ESTADÍSTICAS DEL ESTUDIANTE                -->
            <!-- ============================================================== -->
            <section id="sec-inicio" class="dashboard-section <%= "inicio".equals(activeTab) ? "active" : "" %>">
                <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
                    <div>
                        <h2 class="h4 fw-bold text-dark mb-1">Mi Resumen y Estadísticas de Selección</h2>
                        <p class="text-muted small mb-0">Visualiza el estado de tus postulaciones, entrevistas y preparación de documentos.</p>
                    </div>
                    <div class="d-flex gap-2 flex-wrap">
                        <button type="button" class="btn btn-primary rounded-pill px-4 fw-semibold" onclick="cambiarSeccionEstudiante('ofertas')">
                            <i class="bi bi-search me-1"></i>Explorar Ofertas (<%= ofertasDisponibles.size() %>)
                        </button>
                        <button type="button" class="btn btn-outline-primary rounded-pill px-4 fw-semibold" onclick="cambiarSeccionEstudiante('documentos')">
                            <i class="bi bi-file-earmark-arrow-up me-1"></i>Gestionar CV
                        </button>
                    </div>
                </div>

                <!-- KPIs -->
                <div class="row g-4 mb-4">
                    <div class="col-12 col-sm-6 col-xl-3">
                        <div class="stat-card stat-card-blue">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <div class="stat-value"><%= ofertasDisponibles.size() %></div>
                                    <h3 class="stat-label">Ofertas Disponibles</h3>
                                </div>
                                <div class="stat-icon-wrapper"><i class="bi bi-briefcase-fill"></i></div>
                            </div>
                            <div class="mt-2 small text-muted">Convocatorias vigentes hoy</div>
                        </div>
                    </div>
                    <div class="col-12 col-sm-6 col-xl-3">
                        <div class="stat-card stat-card-turquoise">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <div class="stat-value"><%= totalPostStat %></div>
                                    <h3 class="stat-label">Mis Postulaciones</h3>
                                </div>
                                <div class="stat-icon-wrapper"><i class="bi bi-send-check-fill"></i></div>
                            </div>
                            <div class="mt-2 small text-muted"><%= postuladosStat %> en espera &bull; <%= enRevisionStat %> en revisión</div>
                        </div>
                    </div>
                    <div class="col-12 col-sm-6 col-xl-3">
                        <div class="stat-card stat-card-teal">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <div class="stat-value"><%= seleccionadosStat %></div>
                                    <h3 class="stat-label">Seleccionadas</h3>
                                </div>
                                <div class="stat-icon-wrapper"><i class="bi bi-award-fill"></i></div>
                            </div>
                            <div class="mt-2 small text-muted">Tasa de selección: <strong><%= tasaSeleccionStat %>%</strong></div>
                        </div>
                    </div>
                    <div class="col-12 col-sm-6 col-xl-3">
                        <div class="stat-card stat-card-indigo">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <div class="stat-value"><%= entProgramadasStat %></div>
                                    <h3 class="stat-label">Entrevistas Activas</h3>
                                </div>
                                <div class="stat-icon-wrapper"><i class="bi bi-calendar2-event-fill"></i></div>
                            </div>
                            <div class="mt-2 small text-muted"><%= entRealizadasStat %> realizadas &bull; <%= totalEntStat %> totales</div>
                        </div>
                    </div>
                </div>

                <div class="row g-4">
                    <div class="col-12 col-lg-7">
                        <!-- Estadísticas de Progreso -->
                        <div class="propath-card mb-4">
                            <div class="propath-card-header">
                                <h2 class="propath-card-title"><i class="bi bi-bar-chart-fill"></i><span>Estadísticas de Mis Procesos de Selección</span></h2>
                                <span class="badge bg-primary-subtle text-primary border border-primary-subtle rounded-pill px-3 py-2">Respuesta: <%= tasaRespuestaStat %>%</span>
                            </div>
                            <div class="propath-card-body">
                                <%
                                    long baseEst = totalPostStat > 0 ? totalPostStat : 1L;
                                    int pctP = (int) Math.round((postuladosStat * 100.0) / baseEst);
                                    int pctR = (int) Math.round((enRevisionStat * 100.0) / baseEst);
                                    int pctS = (int) Math.round((seleccionadosStat * 100.0) / baseEst);
                                    int pctN = (int) Math.round((noSeleccionadosStat * 100.0) / baseEst);
                                %>
                                <div class="mb-3">
                                    <div class="d-flex justify-content-between small fw-semibold mb-1">
                                        <span>Postulado / En espera (<%= postuladosStat %>)</span>
                                        <span class="text-primary"><%= totalPostStat > 0 ? pctP : 0 %>%</span>
                                    </div>
                                    <div class="propath-progress-track"><div class="propath-progress-fill bg-primary" style="width:<%= totalPostStat > 0 ? pctP : 0 %>%"></div></div>
                                </div>
                                <div class="mb-3">
                                    <div class="d-flex justify-content-between small fw-semibold mb-1">
                                        <span>En Revisión por Empresa (<%= enRevisionStat %>)</span>
                                        <span class="text-warning"><%= totalPostStat > 0 ? pctR : 0 %>%</span>
                                    </div>
                                    <div class="propath-progress-track"><div class="propath-progress-fill bg-warning" style="width:<%= totalPostStat > 0 ? pctR : 0 %>%"></div></div>
                                </div>
                                <div class="mb-3">
                                    <div class="d-flex justify-content-between small fw-semibold mb-1">
                                        <span>Seleccionado para Entrevista (<%= seleccionadosStat %>)</span>
                                        <span class="text-success"><%= totalPostStat > 0 ? pctS : 0 %>%</span>
                                    </div>
                                    <div class="propath-progress-track"><div class="propath-progress-fill bg-success" style="width:<%= totalPostStat > 0 ? pctS : 0 %>%"></div></div>
                                </div>
                                <div>
                                    <div class="d-flex justify-content-between small fw-semibold mb-1">
                                        <span>No Seleccionado (<%= noSeleccionadosStat %>)</span>
                                        <span class="text-danger"><%= totalPostStat > 0 ? pctN : 0 %>%</span>
                                    </div>
                                    <div class="propath-progress-track"><div class="propath-progress-fill bg-danger" style="width:<%= totalPostStat > 0 ? pctN : 0 %>%"></div></div>
                                </div>
                            </div>
                        </div>

                        <!-- Alerta / Próxima Entrevista -->
                        <div class="propath-card mb-0">
                            <div class="propath-card-header">
                                <h2 class="propath-card-title"><i class="bi bi-calendar-week-fill"></i><span>Mis Próximas Entrevistas</span></h2>
                                <button type="button" class="btn btn-sm btn-outline-primary rounded-pill" onclick="cambiarSeccionEstudiante('entrevistas')">Ver historial</button>
                            </div>
                            <div class="propath-card-body">
                                <%
                                    List<Entrevista> proxEst = new ArrayList<>();
                                    for (Entrevista en : misEntrevistas) {
                                        if ("PROGRAMADA".equalsIgnoreCase(en.getEstado())) {
                                            proxEst.add(en);
                                            if (proxEst.size() >= 3) break;
                                        }
                                    }
                                    if (proxEst.isEmpty()) {
                                %>
                                <div class="text-center py-4 text-muted small">
                                    <i class="bi bi-calendar-check fs-2 d-block mb-2"></i>
                                    No tienes entrevistas pendientes programadas en este momento.
                                </div>
                                <% } else { %>
                                <div class="d-flex flex-column gap-2">
                                    <% for (Entrevista en : proxEst) {
                                        String horaF = en.getHora() != null && en.getHora().length() >= 5 ? en.getHora().substring(0, 5) : en.getHora();
                                    %>
                                    <div class="p-3 border rounded-3 bg-primary-subtle d-flex justify-content-between align-items-center flex-wrap gap-2">
                                        <div>
                                            <div class="fw-bold text-dark"><%= escaparHtml(en.getOferta()) %></div>
                                            <div class="small text-secondary">
                                                <i class="bi bi-building me-1"></i><%= escaparHtml(en.getEmpresa()) %> &bull;
                                                <strong><%= escaparHtml(en.getFecha()) %></strong> &bull; <strong><%= escaparHtml(horaF) %></strong> (<%= escaparHtml(en.getModalidad()) %>)
                                            </div>
                                        </div>
                                        <div class="d-flex gap-2">
                                            <% if ("VIRTUAL".equalsIgnoreCase(en.getModalidad()) && en.getEnlaceLugar() != null && en.getEnlaceLugar().startsWith("http")) { %>
                                            <a href="<%= escaparHtml(en.getEnlaceLugar()) %>" target="_blank" rel="noopener noreferrer" class="btn btn-sm btn-primary rounded-pill px-3">
                                                <i class="bi bi-camera-video me-1"></i>Entrar
                                            </a>
                                            <% } %>
                                            <button type="button" class="btn btn-sm btn-outline-primary rounded-pill px-3"
                                                    data-empresa="<%= escaparHtml(en.getEmpresa()) %>"
                                                    data-oferta="<%= escaparHtml(en.getOferta()) %>"
                                                    data-fecha="<%= escaparHtml(en.getFecha()) %>"
                                                    data-hora="<%= escaparHtml(en.getHora()) %>"
                                                    data-modalidad="<%= escaparHtml(en.getModalidad()) %>"
                                                    data-lugar="<%= escaparHtml(en.getEnlaceLugar()) %>"
                                                    data-estado="<%= escaparHtml(en.getEstado()) %>"
                                                    data-observaciones="<%= escaparHtml(en.getObservaciones()) %>"
                                                    onclick="verDetalleEntrevistaEstudiante(this)">
                                                Detalle
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
                        <!-- Estado CV Card -->
                        <div class="propath-card mb-4">
                            <div class="propath-card-header">
                                <h2 class="propath-card-title"><i class="bi bi-file-earmark-pdf-fill"></i><span>Estado de mi CV</span></h2>
                                <span class="badge <%= miCv != null ? "bg-success-subtle text-success" : "bg-warning-subtle text-warning" %> border rounded-pill">
                                    <%= miCv != null ? "Cargado" : "Pendiente" %>
                                </span>
                            </div>
                            <div class="propath-card-body">
                                <% if (miCv == null) { %>
                                <p class="small text-muted mb-3">Aún no has subido tu currículum vitae en PDF. Es obligatorio contar con tu CV cargado para poder postular a las ofertas.</p>
                                <button type="button" class="btn btn-primary w-100 rounded-pill fw-semibold" data-bs-toggle="modal" data-bs-target="#modalSubirCv">
                                    <i class="bi bi-upload me-1"></i>Subir CV en PDF ahora
                                </button>
                                <% } else { %>
                                <div class="d-flex align-items-center gap-3 mb-3">
                                    <div class="rounded-3 p-3 bg-danger-subtle text-danger"><i class="bi bi-file-earmark-pdf-fill fs-3"></i></div>
                                    <div>
                                        <div class="fw-bold text-dark small text-break"><%= escaparHtml(miCv.getNombreArchivo()) %></div>
                                        <div class="small text-muted"><%= escaparHtml(miCv.getTamanioFormateado()) %> &bull; <%= escaparHtml(miCv.getFechaSubida()) %></div>
                                    </div>
                                </div>
                                <div class="d-flex flex-wrap gap-2">
                                    <button type="button" class="btn btn-sm btn-outline-primary rounded-pill flex-fill"
                                            onclick="previsualizarDocumento('${pageContext.request.contextPath}/estudiante/documentos?accion=ver', '<%= escaparHtml(miCv.getNombreArchivo()) %>', 'application/pdf')">
                                        <i class="bi bi-eye me-1"></i>Vista previa
                                    </button>
                                    <button type="button" class="btn btn-sm btn-outline-secondary rounded-pill flex-fill" onclick="cambiarSeccionEstudiante('documentos')">
                                        <i class="bi bi-folder2-open me-1"></i>Administrar (<%= misDocumentos.size() %>)
                                    </button>
                                </div>
                                <% } %>
                            </div>
                        </div>

                        <!-- Perfil Académico Card -->
                        <div class="propath-card mb-0">
                            <div class="propath-card-header">
                                <h2 class="propath-card-title"><i class="bi bi-person-badge-fill"></i><span>Perfil Académico</span></h2>
                                <span class="badge bg-success-subtle text-success border rounded-pill"><%= escaparHtml(usuario.getEstado()) %></span>
                            </div>
                            <div class="propath-card-body">
                                <h3 class="h5 fw-bold text-dark mb-1"><%= escaparHtml(nombreMostrar) %></h3>
                                <p class="small text-muted mb-3"><i class="bi bi-envelope me-1"></i><%= escaparHtml(usuario.getCorreo()) %></p>
                                <div class="small text-secondary mb-2"><i class="bi bi-bank me-2 text-primary"></i><strong>Universidad:</strong> <%= (miEstudiante != null && miEstudiante.getUniversidad() != null && !miEstudiante.getUniversidad().isEmpty()) ? escaparHtml(miEstudiante.getUniversidad()) : "No especificada" %></div>
                                <div class="small text-secondary mb-2"><i class="bi bi-mortarboard me-2 text-primary"></i><strong>Carrera:</strong> <%= (miEstudiante != null && miEstudiante.getCarrera() != null && !miEstudiante.getCarrera().isEmpty()) ? escaparHtml(miEstudiante.getCarrera()) : "No especificada" %></div>
                                <div class="small text-secondary mb-2"><i class="bi bi-calendar3 me-2 text-primary"></i><strong>Ciclo Actual:</strong> <%= (miEstudiante != null && miEstudiante.getCiclo() > 0) ? miEstudiante.getCiclo() : 1 %>° ciclo</div>
                                <div class="small text-secondary mb-3"><i class="bi bi-telephone me-2 text-primary"></i><strong>Teléfono:</strong> <%= (miEstudiante != null && miEstudiante.getTelefono() != null && !miEstudiante.getTelefono().isEmpty()) ? escaparHtml(miEstudiante.getTelefono()) : "No registrado" %></div>
                                <button type="button" class="btn btn-sm btn-outline-primary w-100 rounded-pill fw-semibold"
                                        data-bs-toggle="modal" data-bs-target="#modalEditarPerfilEstudiante">
                                    <i class="bi bi-pencil-square me-1"></i>Actualizar Datos Académicos
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <!-- ============================================================== -->
            <!-- SECCIÓN 2: OFERTAS DISPONIBLES                                 -->
            <!-- ============================================================== -->
            <section id="sec-ofertas" class="dashboard-section <%= "ofertas".equals(activeTab) ? "active" : "" %>">
                <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
                    <div>
                        <h2 class="h4 fw-bold text-dark mb-1">Convocatorias de Prácticas Disponibles</h2>
                        <p class="text-muted small mb-0">Explora vacantes activas verificadas por ProPath, filtra por carrera o modalidad y postula en minutos.</p>
                    </div>
                    <span class="badge bg-primary-subtle text-primary border border-primary-subtle rounded-pill px-3 py-2 fw-semibold" id="contadorOfertasEst">
                        <%= ofertasDisponibles.size() %> disponible(s)
                    </span>
                </div>

                <!-- Barra de Búsqueda y Filtros -->
                <div class="propath-card mb-4">
                    <div class="propath-card-body py-3">
                        <div class="row g-3 align-items-center">
                            <div class="col-12 col-md-5">
                                <div class="search-box-custom" style="max-width: 100%;">
                                    <i class="bi bi-search search-icon"></i>
                                    <input type="text" id="buscarOfertaEst" class="form-control"
                                           placeholder="Buscar puesto, empresa, ubicación..." oninput="filtrarOfertasEstudiante()">
                                </div>
                            </div>
                            <div class="col-12 col-sm-6 col-md-3">
                                <select id="filtroCarreraEst" class="form-select" onchange="filtrarOfertasEstudiante()">
                                    <option value="TODAS">Todas las carreras</option>
                                    <% for (String c : carrerasDisponibles) { %>
                                    <option value="<%= escaparHtml(c.toLowerCase()) %>"><%= escaparHtml(c) %></option>
                                    <% } %>
                                </select>
                            </div>
                            <div class="col-12 col-sm-6 col-md-2">
                                <select id="filtroModalidadEst" class="form-select" onchange="filtrarOfertasEstudiante()">
                                    <option value="TODAS">Modalidad</option>
                                    <option value="PRESENCIAL">Presencial</option>
                                    <option value="HIBRIDA">Híbrida</option>
                                    <option value="VIRTUAL">Virtual</option>
                                </select>
                            </div>
                            <div class="col-12 col-md-2 d-grid">
                                <button type="button" class="btn btn-outline-secondary rounded-pill" onclick="limpiarFiltrosOfertas()">
                                    <i class="bi bi-arrow-counterclockwise me-1"></i>Limpiar
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Listado de Ofertas Disponibles -->
                <div class="propath-card mb-0">
                    <div class="propath-card-body">
                        <% if (ofertasDisponibles.isEmpty()) { %>
                        <div class="text-center py-5 text-muted">
                            <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                            No hay ofertas de prácticas vigentes disponibles en este momento.
                        </div>
                        <% } else { %>
                        <div class="d-flex flex-column gap-3" id="listaOfertasEstudiante">
                            <% for (OfertaPractica ofr : ofertasDisponibles) {
                                boolean yaPostulado = false;
                                for (Postulacion p : misPostulaciones) {
                                    if (p.getIdOferta() == ofr.getId()) {
                                        yaPostulado = true;
                                        break;
                                    }
                                }
                                String modOfr = ofr.getModalidad() != null ? ofr.getModalidad().trim().toUpperCase() : "PRESENCIAL";
                                String carOfr = ofr.getCarrera() != null ? ofr.getCarrera().trim() : "";
                            %>
                            <div class="p-3 border rounded-3 bg-light bg-opacity-50 oferta-est-card"
                                 data-carrera="<%= escaparHtml(carOfr.toLowerCase()) %>"
                                 data-modalidad="<%= escaparHtml(modOfr) %>"
                                 data-texto="<%= escaparHtml((ofr.getTitulo() + " " + ofr.getEmpresa() + " " + carOfr + " " + ofr.getUbicacion() + " " + ofr.getDescripcion()).toLowerCase()) %>">
                                <div class="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-2">
                                    <div>
                                        <span class="badge bg-primary-subtle text-primary mb-1"><%= escaparHtml(carOfr) %></span>
                                        <h3 class="h6 fw-bold text-dark mb-1"><%= escaparHtml(ofr.getTitulo()) %></h3>
                                        <div class="small text-muted">
                                            <i class="bi bi-building me-1"></i><strong><%= ofr.getEmpresa() != null ? escaparHtml(ofr.getEmpresa()) : "Empresa Aliada" %></strong>
                                        </div>
                                    </div>
                                    <span class="badge-status badge-activo"><%= escaparHtml(ofr.getEstado()) %></span>
                                </div>

                                <% if (ofr.getDescripcion() != null && !ofr.getDescripcion().trim().isEmpty()) {
                                    String desc = ofr.getDescripcion().trim();
                                    String snippet = desc.length() > 170 ? desc.substring(0, 170) + "..." : desc;
                                %>
                                <p class="small text-secondary mb-2"><%= escaparHtml(snippet) %></p>
                                <% } %>

                                <div class="d-flex flex-wrap gap-2 mb-3">
                                    <span class="offer-meta-chip"><i class="bi bi-laptop"></i><%= escaparHtml(modOfr) %></span>
                                    <span class="offer-meta-chip"><i class="bi bi-people"></i><%= ofr.getVacantes() %> vacante(s)</span>
                                    <% if (ofr.getUbicacion() != null && !ofr.getUbicacion().trim().isEmpty()) { %>
                                    <span class="offer-meta-chip"><i class="bi bi-geo-alt"></i><%= escaparHtml(ofr.getUbicacion()) %></span>
                                    <% } %>
                                    <% if (ofr.getRemuneracion() != null && !ofr.getRemuneracion().trim().isEmpty()) {
                                        String remTxt = ofr.getRemuneracion().trim();
                                        String remMostrar = remTxt.toUpperCase().startsWith("S/") ? remTxt : "S/ " + remTxt;
                                    %>
                                    <span class="offer-meta-chip"><i class="bi bi-cash-stack"></i><%= escaparHtml(remMostrar) %></span>
                                    <% } %>
                                    <span class="offer-meta-chip"><i class="bi bi-calendar-x"></i>Cierra: <%= escaparHtml(ofr.getFechaLimite()) %></span>
                                </div>

                                <div class="d-flex justify-content-between align-items-center pt-2 border-top flex-wrap gap-2">
                                    <span class="small text-muted">Publicado: <%= escaparHtml(ofr.getFechaPublicacion()) %></span>
                                    <div class="d-flex align-items-center gap-2">
                                        <button type="button" class="btn btn-sm btn-outline-secondary rounded-pill px-3"
                                                data-id="<%= ofr.getId() %>"
                                                data-titulo="<%= escaparHtml(ofr.getTitulo()) %>"
                                                data-carrera="<%= escaparHtml(carOfr) %>"
                                                data-empresa="<%= escaparHtml(ofr.getEmpresa()) %>"
                                                data-modalidad="<%= escaparHtml(modOfr) %>"
                                                data-vacantes="<%= ofr.getVacantes() %>"
                                                data-publicacion="<%= escaparHtml(ofr.getFechaPublicacion()) %>"
                                                data-fecha="<%= ofr.getFechaLimite() != null ? escaparHtml(ofr.getFechaLimite().trim()) : "" %>"
                                                data-ubicacion="<%= escaparHtml(ofr.getUbicacion()) %>"
                                                data-descripcion="<%= escaparHtml(ofr.getDescripcion()) %>"
                                                data-estado="<%= escaparHtml(ofr.getEstado()) %>"
                                                data-requisitos="<%= escaparHtml(ofr.getRequisitos()) %>"
                                                data-remuneracion="<%= escaparHtml(ofr.getRemuneracion()) %>"
                                                data-yapostulado="<%= yaPostulado %>"
                                                data-activo="<%= estActivo %>"
                                                onclick="verDetalleOfertaEstudiante(this)">
                                            <i class="bi bi-eye me-1"></i>Ver detalle
                                        </button>
                                        <% if (yaPostulado) { %>
                                        <span class="badge bg-secondary-subtle text-secondary border rounded-pill py-2 px-3">
                                            <i class="bi bi-check-all me-1"></i>Ya postulado
                                        </span>
                                        <% } else if (!estActivo) { %>
                                        <span class="badge bg-danger-subtle text-danger border border-danger-subtle rounded-pill py-2 px-3">
                                            <i class="bi bi-slash-circle me-1"></i>Cuenta inactiva
                                        </span>
                                        <% } else { %>
                                        <button type="button" class="btn btn-sm btn-primary rounded-pill px-3"
                                                data-bs-toggle="modal"
                                                data-bs-target="#modalPostular"
                                                data-id-oferta="<%= ofr.getId() %>"
                                                data-titulo="<%= escaparHtml(ofr.getTitulo()) %>"
                                                onclick="seleccionarOfertaPostular(this)">
                                            <i class="bi bi-send me-1"></i>Postular
                                        </button>
                                        <% } %>
                                    </div>
                                </div>
                            </div>
                            <% } %>
                        </div>
                        <div id="sinResultadosOfertasEst" class="text-center py-5 text-muted" style="display:none;">
                            <i class="bi bi-search fs-2 d-block mb-2"></i>
                            No se encontraron ofertas con los filtros seleccionados.
                        </div>
                        <% } %>
                    </div>
                </div>
            </section>

            <!-- ============================================================== -->
            <!-- SECCIÓN 3: MIS POSTULACIONES                                   -->
            <!-- ============================================================== -->
            <section id="sec-postulaciones" class="dashboard-section <%= "postulaciones".equals(activeTab) ? "active" : "" %>">
                <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
                    <div>
                        <h2 class="h4 fw-bold text-dark mb-1">Seguimiento de Mis Postulaciones</h2>
                        <p class="text-muted small mb-0">Monitorea cada etapa de tus postulaciones, edita respuestas pendientes o consulta motivos de evaluación.</p>
                    </div>
                    <div class="d-flex gap-2 align-items-center flex-wrap">
                        <div class="search-box-custom" style="max-width: 220px;">
                            <i class="bi bi-search search-icon"></i>
                            <input type="text" id="buscarPostulacionEst" class="form-control" placeholder="Buscar..." oninput="filtrarPostulacionesEst()">
                        </div>
                        <select id="filtroEstadoPostulacionEst" class="form-select" style="width: auto;" onchange="filtrarPostulacionesEst()">
                            <option value="TODOS">Todos (<%= totalPostStat %>)</option>
                            <option value="POSTULADO">Postulado (<%= postuladosStat %>)</option>
                            <option value="EN_REVISION">En Revisión (<%= enRevisionStat %>)</option>
                            <option value="SELECCIONADO">Seleccionado (<%= seleccionadosStat %>)</option>
                            <option value="NO_SELECCIONADO">No Seleccionado (<%= noSeleccionadosStat %>)</option>
                        </select>
                    </div>
                </div>

                <div class="propath-card mb-0">
                    <div class="propath-card-body">
                        <% if (misPostulaciones.isEmpty()) { %>
                        <div class="text-center py-5 text-muted">
                            <i class="bi bi-file-earmark-text fs-1 d-block mb-2"></i>
                            Aún no te has postulado a ninguna vacante. Explora las ofertas disponibles y postula.
                        </div>
                        <% } else { %>
                        <div class="d-flex flex-column gap-3" id="listaPostulacionesEst">
                            <% for (Postulacion post : misPostulaciones) {
                                String estPost = post.getEstado() != null ? post.getEstado().trim().toUpperCase() : "POSTULADO";
                                boolean esPostulado = "POSTULADO".equals(estPost);
                                boolean esEnRevision = "EN_REVISION".equals(estPost);
                                boolean esSeleccionado = "SELECCIONADO".equals(estPost);
                                boolean esRechazado = "NO_SELECCIONADO".equals(estPost);
                            %>
                            <div class="p-3 border rounded-3 bg-light bg-opacity-25 postulacion-est-card"
                                 data-estado="<%= escaparHtml(estPost) %>"
                                 data-texto="<%= escaparHtml((post.getOferta() + " " + post.getEmpresa()).toLowerCase()) %>">
                                <div class="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-2">
                                    <div>
                                        <h3 class="h6 fw-bold text-dark mb-1"><%= escaparHtml(post.getOferta()) %></h3>
                                        <div class="small text-muted">
                                            <i class="bi bi-building me-1"></i><%= escaparHtml(post.getEmpresa()) %>
                                            &bull; <i class="bi bi-calendar3 ms-1 me-1"></i><%= escaparHtml(post.getFechaPostulacion()) %>
                                        </div>
                                    </div>
                                    <span class="badge-status <%= badgeEstadoPostulacion(estPost) %>"><%= labelEstadoPostulacion(estPost) %></span>
                                </div>

                                <!-- Indicador Visual de Progreso -->
                                <div class="step-pipeline my-2">
                                    <span class="step-pipeline-node <%= esPostulado ? "current" : "done" %>">
                                        <i class="bi bi-send-fill"></i>1. Postulado
                                    </span>
                                    <i class="bi bi-chevron-right small text-muted"></i>
                                    <span class="step-pipeline-node <%= esEnRevision ? "current" : (esSeleccionado ? "done" : "") %>">
                                        <i class="bi bi-hourglass-split"></i>2. En Revisión
                                    </span>
                                    <i class="bi bi-chevron-right small text-muted"></i>
                                    <% if (esRechazado) { %>
                                    <span class="step-pipeline-node rejected">
                                        <i class="bi bi-x-circle-fill"></i>3. No Seleccionado
                                    </span>
                                    <% } else { %>
                                    <span class="step-pipeline-node <%= esSeleccionado ? "current" : "" %>">
                                        <i class="bi bi-check-circle-fill"></i>3. Seleccionado
                                    </span>
                                    <% } %>
                                </div>

                                <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 pt-2 border-top mt-2">
                                    <div class="d-flex flex-wrap gap-2">
                                        <button type="button" class="btn btn-sm btn-outline-info rounded-pill px-3"
                                                data-id="<%= post.getId() %>"
                                                data-oferta="<%= escaparHtml(post.getOferta()) %>"
                                                data-empresa="<%= escaparHtml(post.getEmpresa()) %>"
                                                data-fecha="<%= escaparHtml(post.getFechaPostulacion()) %>"
                                                data-estado="<%= escaparHtml(estPost) %>"
                                                onclick="verDetallePostulacionEstudiante(this)">
                                            <i class="bi bi-eye me-1"></i>Ver respuestas
                                        </button>

                                        <% if (esPostulado) { %>
                                        <button type="button" class="btn btn-sm btn-outline-primary rounded-pill px-3"
                                                data-bs-toggle="modal"
                                                data-bs-target="#modalPostular"
                                                data-id-postulacion="<%= post.getId() %>"
                                                data-titulo="<%= escaparHtml(post.getOferta()) %>"
                                                onclick="cargarRespuestasPostulacion(this)">
                                            <i class="bi bi-pencil-square me-1"></i>Editar respuestas
                                        </button>
                                        <form action="${pageContext.request.contextPath}/estudiante/postular" method="POST" class="m-0"
                                              onsubmit="event.preventDefault(); const f = this; mostrarModalConfirmacion('Retirar postulación', '¿Seguro que deseas retirar esta postulación? Podrás volver a postular mientras la oferta siga vigente.', function(){ f.submit(); });">
                                            <input type="hidden" name="accion" value="retirar">
                                            <input type="hidden" name="idPostulacion" value="<%= post.getId() %>">
                                            <button type="submit" class="btn btn-sm btn-outline-danger rounded-pill px-3">
                                                <i class="bi bi-x-circle me-1"></i>Retirar
                                            </button>
                                        </form>
                                        <% } %>

                                        <% if (esRechazado) { %>
                                        <button type="button" class="btn btn-sm btn-outline-primary rounded-pill px-3"
                                                data-bs-toggle="modal"
                                                data-bs-target="#modalMotivoRechazo"
                                                data-motivo="<%= escaparHtml(post.getMotivoRechazo()) %>"
                                                onclick="mostrarMotivoRechazo(this)">
                                            <i class="bi bi-chat-left-text me-1"></i>Ver motivo
                                        </button>
                                        <form action="${pageContext.request.contextPath}/estudiante/postular" method="POST" class="m-0"
                                              onsubmit="event.preventDefault(); const f = this; mostrarModalConfirmacion('Eliminar registro', '¿Seguro que deseas eliminar esta postulación de tu historial?', function(){ f.submit(); });">
                                            <input type="hidden" name="accion" value="eliminar">
                                            <input type="hidden" name="idPostulacion" value="<%= post.getId() %>">
                                            <button type="submit" class="btn btn-sm btn-outline-danger rounded-pill px-3">
                                                <i class="bi bi-trash me-1"></i>Eliminar
                                            </button>
                                        </form>
                                        <% } %>
                                    </div>
                                </div>

                                <% if (esSeleccionado) {
                                    List<Entrevista> entPost = new ArrayList<>();
                                    for (Entrevista en : misEntrevistas) {
                                        if (en.getIdPostulacion() == post.getId()) entPost.add(en);
                                    }
                                    if (entPost.isEmpty()) {
                                %>
                                <div class="alert alert-success py-2 px-3 small mt-3 mb-0">
                                    <i class="bi bi-stars me-1"></i>¡Fuiste seleccionado! La empresa programará tu entrevista próximamente.
                                </div>
                                <% } else {
                                    for (Entrevista en : entPost) {
                                        String horaF = en.getHora() != null && en.getHora().length() >= 5 ? en.getHora().substring(0, 5) : en.getHora();
                                %>
                                <div class="border rounded-3 p-3 mt-3 bg-primary-subtle">
                                    <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-1">
                                        <div class="fw-bold small text-dark">
                                            <i class="bi bi-calendar-check me-1"></i>Entrevista: <%= escaparHtml(en.getFecha()) %> &bull; <%= escaparHtml(horaF) %> &bull; <%= escaparHtml(en.getModalidad()) %>
                                        </div>
                                        <span class="badge-status <%= badgeEstadoEntrevista(en.getEstado()) %>"><%= escaparHtml(en.getEstado()) %></span>
                                    </div>
                                    <div class="d-flex flex-wrap gap-2 mt-2">
                                        <% if ("VIRTUAL".equalsIgnoreCase(en.getModalidad()) && en.getEnlaceLugar() != null && en.getEnlaceLugar().startsWith("http")) { %>
                                        <a href="<%= escaparHtml(en.getEnlaceLugar()) %>" target="_blank" rel="noopener noreferrer" class="btn btn-sm btn-primary rounded-pill px-3">
                                            <i class="bi bi-camera-video me-1"></i>Entrar a la entrevista
                                        </a>
                                        <% } %>
                                        <button type="button" class="btn btn-sm btn-outline-primary rounded-pill px-3"
                                                data-empresa="<%= escaparHtml(post.getEmpresa()) %>"
                                                data-oferta="<%= escaparHtml(post.getOferta()) %>"
                                                data-fecha="<%= escaparHtml(en.getFecha()) %>"
                                                data-hora="<%= escaparHtml(en.getHora()) %>"
                                                data-modalidad="<%= escaparHtml(en.getModalidad()) %>"
                                                data-lugar="<%= escaparHtml(en.getEnlaceLugar()) %>"
                                                data-estado="<%= escaparHtml(en.getEstado()) %>"
                                                data-observaciones="<%= escaparHtml(en.getObservaciones()) %>"
                                                onclick="verDetalleEntrevistaEstudiante(this)">
                                            <i class="bi bi-eye me-1"></i>Ver detalle completo
                                        </button>
                                    </div>
                                </div>
                                <% } } } %>
                            </div>
                            <% } %>
                        </div>
                        <% } %>
                    </div>
                </div>
            </section>

            <!-- ============================================================== -->
            <!-- SECCIÓN 4: HISTORIAL DE ENTREVISTAS DEL ESTUDIANTE             -->
            <!-- ============================================================== -->
            <section id="sec-entrevistas" class="dashboard-section <%= "entrevistas".equals(activeTab) ? "active" : "" %>">
                <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
                    <div>
                        <h2 class="h4 fw-bold text-dark mb-1">Mi Historial de Entrevistas</h2>
                        <p class="text-muted small mb-0">Consulta tus entrevistas programadas, enlaces de videollamada, sedes presenciales y observaciones.</p>
                    </div>
                </div>

                <div class="row g-3 mb-4">
                    <div class="col-12 col-sm-4">
                        <div class="bg-white p-3 rounded-4 border shadow-sm d-flex justify-content-between align-items-center">
                            <div><div class="small text-muted fw-semibold">Programadas</div><div class="fs-3 fw-bold text-primary"><%= entProgramadasStat %></div></div>
                            <div class="rounded-circle bg-primary-subtle text-primary p-3"><i class="bi bi-calendar-event fs-4"></i></div>
                        </div>
                    </div>
                    <div class="col-12 col-sm-4">
                        <div class="bg-white p-3 rounded-4 border shadow-sm d-flex justify-content-between align-items-center">
                            <div><div class="small text-muted fw-semibold">Realizadas</div><div class="fs-3 fw-bold text-success"><%= entRealizadasStat %></div></div>
                            <div class="rounded-circle bg-success-subtle text-success p-3"><i class="bi bi-check-circle fs-4"></i></div>
                        </div>
                    </div>
                    <div class="col-12 col-sm-4">
                        <div class="bg-white p-3 rounded-4 border shadow-sm d-flex justify-content-between align-items-center">
                            <div><div class="small text-muted fw-semibold">Canceladas</div><div class="fs-3 fw-bold text-danger"><%= entCanceladasStat %></div></div>
                            <div class="rounded-circle bg-danger-subtle text-danger p-3"><i class="bi bi-x-circle fs-4"></i></div>
                        </div>
                    </div>
                </div>

                <div class="propath-card mb-0">
                    <div class="propath-card-header">
                        <h3 class="propath-card-title"><i class="bi bi-clock-history"></i><span>Entrevistas Registradas (<%= misEntrevistas.size() %>)</span></h3>
                        <select id="filtroEstadoEntrevistaEst" class="form-select" style="width: auto;" onchange="filtrarEntrevistasEstudiante()">
                            <option value="TODAS">Todos los estados</option>
                            <option value="PROGRAMADA">Programadas</option>
                            <option value="REALIZADA">Realizadas</option>
                            <option value="CANCELADA">Canceladas</option>
                        </select>
                    </div>
                    <div class="propath-card-body">
                        <% if (misEntrevistas.isEmpty()) { %>
                        <div class="text-center py-5 text-muted">
                            <i class="bi bi-calendar-x fs-1 d-block mb-2"></i>
                            Aún no tienes entrevistas registradas en tu historial.
                        </div>
                        <% } else { %>
                        <div class="d-flex flex-column gap-3" id="listaEntrevistasEst">
                            <% for (Entrevista en : misEntrevistas) {
                                String estEn = en.getEstado() != null ? en.getEstado().trim().toUpperCase() : "PROGRAMADA";
                                String horaF = en.getHora() != null && en.getHora().length() >= 5 ? en.getHora().substring(0, 5) : en.getHora();
                            %>
                            <div class="p-3 border rounded-3 bg-light bg-opacity-50 entrevista-est-card" data-estado="<%= escaparHtml(estEn) %>">
                                <div class="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-2">
                                    <div>
                                        <div class="d-flex align-items-center gap-2 flex-wrap">
                                            <h4 class="h6 fw-bold text-dark mb-0"><%= escaparHtml(en.getOferta()) %></h4>
                                            <span class="badge-status <%= badgeEstadoEntrevista(estEn) %>"><%= escaparHtml(estEn) %></span>
                                            <span class="badge-modalidad"><i class="bi <%= "VIRTUAL".equalsIgnoreCase(en.getModalidad()) ? "bi-camera-video" : "bi-geo-alt" %> me-1"></i><%= escaparHtml(en.getModalidad()) %></span>
                                        </div>
                                        <div class="small text-muted mt-1">
                                            <i class="bi bi-building me-1"></i><strong><%= escaparHtml(en.getEmpresa()) %></strong>
                                            &bull; <i class="bi bi-calendar3 ms-1 me-1"></i><%= escaparHtml(en.getFecha()) %> a las <strong><%= escaparHtml(horaF) %></strong>
                                        </div>
                                    </div>
                                    <div class="d-flex gap-2">
                                        <% if ("VIRTUAL".equalsIgnoreCase(en.getModalidad()) && !"CANCELADA".equals(estEn) && en.getEnlaceLugar() != null && en.getEnlaceLugar().startsWith("http")) { %>
                                        <a href="<%= escaparHtml(en.getEnlaceLugar()) %>" target="_blank" rel="noopener noreferrer" class="btn btn-sm btn-primary rounded-pill px-3">
                                            <i class="bi bi-camera-video me-1"></i>Unirse
                                        </a>
                                        <% } %>
                                        <button type="button" class="btn btn-sm btn-outline-primary rounded-pill px-3"
                                                data-empresa="<%= escaparHtml(en.getEmpresa()) %>"
                                                data-oferta="<%= escaparHtml(en.getOferta()) %>"
                                                data-fecha="<%= escaparHtml(en.getFecha()) %>"
                                                data-hora="<%= escaparHtml(en.getHora()) %>"
                                                data-modalidad="<%= escaparHtml(en.getModalidad()) %>"
                                                data-lugar="<%= escaparHtml(en.getEnlaceLugar()) %>"
                                                data-estado="<%= escaparHtml(estEn) %>"
                                                data-observaciones="<%= escaparHtml(en.getObservaciones()) %>"
                                                onclick="verDetalleEntrevistaEstudiante(this)">
                                            <i class="bi bi-eye me-1"></i>Ver detalle
                                        </button>
                                    </div>
                                </div>
                                <% if (!"VIRTUAL".equalsIgnoreCase(en.getModalidad()) && en.getEnlaceLugar() != null && !en.getEnlaceLugar().isEmpty()) { %>
                                <div class="small text-secondary mb-1"><i class="bi bi-geo-alt me-1 text-primary"></i><strong>Dirección:</strong> <%= escaparHtml(en.getEnlaceLugar()) %></div>
                                <% } %>
                                <% if (en.getObservaciones() != null && !en.getObservaciones().trim().isEmpty()) { %>
                                <div class="small text-secondary bg-white border rounded p-2 mt-2"><i class="bi bi-chat-left-text me-1 text-primary"></i><%= escaparHtml(en.getObservaciones()) %></div>
                                <% } %>
                            </div>
                            <% } %>
                        </div>
                        <% } %>
                    </div>
                </div>
            </section>

            <!-- ============================================================== -->
            <!-- SECCIÓN 5: CV Y DOCUMENTOS                                     -->
            <!-- ============================================================== -->
            <section id="sec-documentos" class="dashboard-section <%= "documentos".equals(activeTab) ? "active" : "" %>">
                <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
                    <div>
                        <h2 class="h4 fw-bold text-dark mb-1">Mi Currículum Vitae y Documentos</h2>
                        <p class="text-muted small mb-0">Gestiona tu CV obligatorio y adjunta documentos complementarios validados para tus postulaciones.</p>
                    </div>
                    <button type="button" class="btn btn-primary rounded-pill px-4 fw-semibold" data-bs-toggle="modal" data-bs-target="#modalSubirDocumento">
                        <i class="bi bi-file-earmark-plus me-1"></i>Subir Documento Complementario
                    </button>
                </div>

                <!-- Reglas de Validación -->
                <div class="alert alert-info rounded-3 mb-4 shadow-sm small">
                    <i class="bi bi-shield-check me-2 fs-5 align-middle"></i>
                    <strong>Validación automática de documentos:</strong> Tu <strong>CV</strong> debe estar en formato <strong>PDF válido</strong> (máx. 5 MB). Los <strong>documentos complementarios</strong> aceptan <strong>PDF, PNG o JPG</strong> (máx. 5 MB por archivo, hasta 10 documentos).
                </div>

                <div class="row g-4">
                    <!-- Tarjeta CV Principal -->
                    <div class="col-12 col-lg-5">
                        <div class="propath-card h-100 mb-0">
                            <div class="propath-card-header">
                                <h3 class="propath-card-title"><i class="bi bi-file-earmark-pdf-fill text-danger"></i><span>Currículum Vitae (Obligatorio)</span></h3>
                                <span class="badge <%= miCv != null ? "bg-success-subtle text-success" : "bg-warning-subtle text-warning" %> border rounded-pill">
                                    <%= miCv != null ? "Activo" : "Sin CV" %>
                                </span>
                            </div>
                            <div class="propath-card-body">
                                <% if (miCv == null) { %>
                                <div class="text-center py-4">
                                    <div class="rounded-circle bg-danger-subtle text-danger d-inline-flex align-items-center justify-content-center p-4 mb-3">
                                        <i class="bi bi-file-earmark-arrow-up fs-1"></i>
                                    </div>
                                    <h4 class="h6 fw-bold text-dark">Aún no has cargado tu CV</h4>
                                    <p class="small text-muted mb-3">Sube tu currículum en formato PDF (máx. 5 MB) para poder postular a las convocatorias.</p>
                                    <button type="button" class="btn btn-primary rounded-pill px-4" data-bs-toggle="modal" data-bs-target="#modalSubirCv">
                                        <i class="bi bi-upload me-1"></i>Subir CV en PDF
                                    </button>
                                </div>
                                <% } else { %>
                                <div class="p-3 border rounded-3 bg-light mb-3">
                                    <div class="d-flex align-items-start gap-3">
                                        <div class="rounded-3 p-3 bg-danger-subtle text-danger"><i class="bi bi-file-earmark-pdf-fill fs-2"></i></div>
                                        <div class="flex-grow-1">
                                            <div class="fw-bold text-dark text-break"><%= escaparHtml(miCv.getNombreArchivo()) %></div>
                                            <div class="small text-muted mt-1">
                                                <div><i class="bi bi-hdd me-1"></i>Tamaño: <%= escaparHtml(miCv.getTamanioFormateado()) %></div>
                                                <div><i class="bi bi-calendar3 me-1"></i>Actualizado: <%= escaparHtml(miCv.getFechaSubida()) %></div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="d-flex flex-wrap gap-2">
                                    <button type="button" class="btn btn-sm btn-primary rounded-pill px-3"
                                            onclick="previsualizarDocumento('${pageContext.request.contextPath}/estudiante/documentos?accion=ver', '<%= escaparHtml(miCv.getNombreArchivo()) %>', 'application/pdf')">
                                        <i class="bi bi-eye me-1"></i>Vista previa
                                    </button>
                                    <a href="${pageContext.request.contextPath}/estudiante/documentos?accion=ver" target="_blank" class="btn btn-sm btn-outline-primary rounded-pill px-3">
                                        <i class="bi bi-box-arrow-up-right me-1"></i>Abrir
                                    </a>
                                    <a href="${pageContext.request.contextPath}/estudiante/documentos?accion=descargar" class="btn btn-sm btn-outline-secondary rounded-pill px-3">
                                        <i class="bi bi-download me-1"></i>Descargar
                                    </a>
                                    <button type="button" class="btn btn-sm btn-outline-warning text-dark rounded-pill px-3" data-bs-toggle="modal" data-bs-target="#modalReemplazarCv">
                                        <i class="bi bi-arrow-repeat me-1"></i>Actualizar CV
                                    </button>
                                    <button type="button" class="btn btn-sm btn-outline-danger rounded-pill px-3" onclick="confirmarEliminarCv()">
                                        <i class="bi bi-trash me-1"></i>Eliminar
                                    </button>
                                </div>
                                <% } %>
                            </div>
                        </div>
                    </div>

                    <!-- Documentos Complementarios -->
                    <div class="col-12 col-lg-7">
                        <div class="propath-card h-100 mb-0">
                            <div class="propath-card-header">
                                <h3 class="propath-card-title"><i class="bi bi-folder2-open"></i><span>Documentos Complementarios (<%= docsComplementarios.size() %>)</span></h3>
                                <button type="button" class="btn btn-sm btn-outline-primary rounded-pill px-3" data-bs-toggle="modal" data-bs-target="#modalSubirDocumento">
                                    <i class="bi bi-plus-lg me-1"></i>Agregar
                                </button>
                            </div>
                            <div class="propath-card-body">
                                <% if (docsComplementarios.isEmpty()) { %>
                                <div class="text-center py-5 text-muted small">
                                    <i class="bi bi-files fs-1 d-block mb-2"></i>
                                    No has cargado documentos adicionales (carta de presentación, certificados, constancias o portafolio).
                                </div>
                                <% } else { %>
                                <div class="d-flex flex-column gap-2">
                                    <% for (DocumentoEstudiante doc : docsComplementarios) {
                                        String verUrl = request.getContextPath() + "/estudiante/documentos?accion=ver&idDocumento=" + doc.getId();
                                        String descUrl = request.getContextPath() + "/estudiante/documentos?accion=descargar&idDocumento=" + doc.getId();
                                        String ctype = doc.getContentType() != null ? doc.getContentType() : "application/pdf";
                                    %>
                                    <div class="p-3 border rounded-3 bg-light d-flex justify-content-between align-items-center flex-wrap gap-2">
                                        <div>
                                            <span class="badge bg-info-subtle text-info border border-info-subtle mb-1"><%= labelTipoDoc(doc.getTipoDocumento()) %></span>
                                            <div class="fw-semibold text-dark small text-break"><%= escaparHtml(doc.getNombreArchivo()) %></div>
                                            <div class="text-muted" style="font-size: 0.78rem;"><%= escaparHtml(doc.getTamanioFormateado()) %> &bull; <%= escaparHtml(doc.getFechaSubida()) %></div>
                                        </div>
                                        <div class="d-flex flex-wrap gap-2">
                                            <button type="button" class="btn btn-sm btn-outline-primary rounded-pill px-3"
                                                    onclick="previsualizarDocumento('<%= escaparHtml(verUrl) %>', '<%= escaparHtml(doc.getNombreArchivo()) %>', '<%= escaparHtml(ctype) %>')">
                                                <i class="bi bi-eye me-1"></i>Ver
                                            </button>
                                            <a href="<%= escaparHtml(descUrl) %>" class="btn btn-sm btn-outline-secondary rounded-pill px-3">
                                                <i class="bi bi-download me-1"></i>Descargar
                                            </a>
                                            <form action="${pageContext.request.contextPath}/estudiante/documentos" method="POST" class="m-0"
                                                  onsubmit="event.preventDefault(); const f = this; mostrarModalConfirmacion('Eliminar Documento', '¿Seguro que deseas eliminar este documento complementario?', function(){ f.submit(); });">
                                                <input type="hidden" name="accion" value="eliminarDocumento">
                                                <input type="hidden" name="idDocumento" value="<%= doc.getId() %>">
                                                <button type="submit" class="btn btn-sm btn-outline-danger rounded-pill px-2"><i class="bi bi-trash"></i></button>
                                            </form>
                                        </div>
                                    </div>
                                    <% } %>
                                </div>
                                <% } %>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </div>
    </main>
</div>

<!-- ====================================================================== -->
<!-- MODALES                                                                -->
<!-- ====================================================================== -->

<!-- Modal Postular (Wizard 4 Pasos) -->
<div class="modal fade" id="modalPostular" tabindex="-1">
    <div class="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
        <div class="modal-content modal-content-propath">
            <form id="formPostular" action="${pageContext.request.contextPath}/estudiante/postular" method="POST">
                <div class="modal-header modal-header-propath">
                    <h5 class="modal-title"><i class="bi bi-send-check me-2"></i>Postulación a Prácticas</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body modal-body-propath">
                    <input type="hidden" name="idOferta" id="postularIdOferta">
                    <input type="hidden" name="accion" id="postularAccion" value="">
                    <input type="hidden" name="idPostulacion" id="postularIdPostulacion" value="">
                    <h6 id="postularTituloOferta" class="text-primary fw-bold mb-3"></h6>

                    <div id="pasoPostular1">
                        <h6 class="fw-bold">Paso 1: Verificar CV Obligatorio</h6>
                        <% if (miCv != null) { %>
                        <div class="alert alert-success small">
                            <i class="bi bi-check-circle-fill me-1"></i>CV PDF registrado: <strong><%= escaparHtml(miCv.getNombreArchivo()) %></strong> (<%= escaparHtml(miCv.getTamanioFormateado()) %>)
                        </div>
                        <p class="small text-muted mb-0">Presiona <strong>Siguiente</strong> para completar las 12 preguntas de evaluación de la convocatoria.</p>
                        <% } else { %>
                        <div class="alert alert-warning small">
                            <i class="bi bi-exclamation-triangle-fill me-1"></i>Debes subir tu Currículum Vitae en formato PDF antes de continuar con la postulación.
                        </div>
                        <button type="button" class="btn btn-primary rounded-pill px-4" onclick="abrirSubidaCv()">
                            <i class="bi bi-upload me-1"></i>Subir CV en PDF
                        </button>
                        <% } %>
                    </div>

                    <%
                        String[] preguntas = {
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
                        };
                    %>

                    <div id="pasoPostular2" style="display:none">
                        <h6 class="fw-bold mb-3">Paso 2 de 4: Preguntas Técnicas y Laborales (1–6)</h6>
                        <% for (int i = 0; i < 6; i++) { %>
                        <div class="mb-3">
                            <label class="form-label small fw-semibold"><%= i + 1 %>. <%= preguntas[i] %></label>
                            <textarea name="respuesta<%= i + 1 %>" class="form-control" rows="2" maxlength="1000" required></textarea>
                        </div>
                        <% } %>
                    </div>

                    <div id="pasoPostular3" style="display:none">
                        <h6 class="fw-bold mb-3">Paso 3 de 4: Perfil Personal y Profesional (7–12)</h6>
                        <% for (int i = 6; i < 12; i++) { %>
                        <div class="mb-3">
                            <label class="form-label small fw-semibold"><%= i + 1 %>. <%= preguntas[i] %></label>
                            <textarea name="respuesta<%= i + 1 %>" class="form-control" rows="2" maxlength="1000" required></textarea>
                        </div>
                        <% } %>
                    </div>

                    <div id="pasoPostular4" style="display:none">
                        <h6 class="fw-bold mb-2">Paso 4 de 4: Confirmación de Envío</h6>
                        <p class="small text-secondary">Revisa cuidadosamente tus respuestas antes de confirmar la postulación.</p>
                        <div id="resumenPostulacion" class="border rounded-3 p-3 bg-light" style="max-height:360px; overflow-y:auto;"></div>
                    </div>
                </div>
                <div class="modal-footer bg-light border-0">
                    <button type="button" class="btn btn-outline-secondary rounded-pill px-4" id="btnAnteriorPostular" onclick="cambiarPasoPostular(-1)" style="display:none">Anterior</button>
                    <button type="button" class="btn btn-primary rounded-pill px-4" id="btnSiguientePostular" onclick="cambiarPasoPostular(1)">Siguiente</button>
                    <button type="submit" class="btn btn-success rounded-pill px-4" id="btnConfirmarPostular" style="display:none">Confirmar postulación</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal Detalle Postulación Estudiante -->
<div class="modal fade" id="modalDetallePostulacionEstudiante" tabindex="-1" aria-labelledby="modalDetallePostulacionEstudianteLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content modal-content-propath">
            <div class="modal-header modal-header-propath">
                <h2 class="modal-title h5" id="modalDetallePostulacionEstudianteLabel"><i class="bi bi-file-earmark-check me-2"></i>Mi Postulación Detallada</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body modal-body-propath">
                <div class="row g-3 mb-4">
                    <div class="col-sm-6"><div class="detail-label">Oferta de Práctica</div><div class="detail-value fs-5 fw-bold text-dark" id="estPostOferta">—</div></div>
                    <div class="col-sm-6"><div class="detail-label">Empresa</div><div class="detail-value fw-semibold text-primary" id="estPostEmpresa">—</div></div>
                    <div class="col-sm-6"><div class="detail-label">Fecha de Postulación</div><div class="detail-value" id="estPostFecha">—</div></div>
                    <div class="col-sm-6"><div class="detail-label">Estado</div><span id="estPostEstado" class="badge-status badge-activo">—</span></div>
                </div>
                <div class="border-top pt-3">
                    <h3 class="h6 fw-bold text-dark mb-3"><i class="bi bi-card-checklist me-1 text-primary"></i>Respuestas Registradas (12 Preguntas)</h3>
                    <div id="estPostRespuestas" class="d-flex flex-column gap-2" style="max-height:400px; overflow-y:auto;"></div>
                </div>
            </div>
            <div class="modal-footer bg-light border-0">
                <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cerrar</button>
            </div>
        </div>
    </div>
</div>

<!-- Modal Detalle Entrevista Estudiante -->
<div class="modal fade" id="modalDetalleEntrevistaEstudiante" tabindex="-1" aria-labelledby="modalDetalleEntrevistaEstudianteLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content modal-content-propath">
            <div class="modal-header modal-header-propath">
                <h2 class="modal-title h5" id="modalDetalleEntrevistaEstudianteLabel"><i class="bi bi-calendar-check me-2"></i>Detalle de mi Entrevista</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body modal-body-propath">
                <div class="row g-3">
                    <div class="col-12"><div class="detail-label">Empresa Convocante</div><div class="detail-value fs-5 fw-bold text-dark" id="estEntEmpresa">—</div></div>
                    <div class="col-12"><div class="detail-label">Oferta de Práctica</div><div class="detail-value fw-semibold text-primary" id="estEntOferta">—</div></div>
                    <div class="col-sm-6"><div class="detail-label">Fecha</div><div class="detail-value" id="estEntFecha">—</div></div>
                    <div class="col-sm-6"><div class="detail-label">Hora</div><div class="detail-value" id="estEntHora">—</div></div>
                    <div class="col-sm-6"><div class="detail-label">Modalidad</div><div class="detail-value" id="estEntModalidad">—</div></div>
                    <div class="col-sm-6"><div class="detail-label">Estado</div><span id="estEntEstado" class="badge-status badge-activo">—</span></div>
                    <div class="col-12"><div class="detail-label" id="estEntLugarLabel">Lugar o Enlace</div><div class="detail-value" id="estEntLugar">—</div></div>
                    <div class="col-12"><div class="detail-label">Observaciones de la Empresa</div><div class="small p-2 border rounded bg-light text-secondary" id="estEntObservaciones">—</div></div>
                </div>
            </div>
            <div class="modal-footer bg-light border-0">
                <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cerrar</button>
            </div>
        </div>
    </div>
</div>

<!-- Modal Subir CV -->
<div class="modal fade" id="modalSubirCv" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content modal-content-propath">
            <div class="modal-header modal-header-propath">
                <h2 class="modal-title h5"><i class="bi bi-file-earmark-arrow-up me-2"></i>Subir Currículum Vitae (CV)</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <form action="${pageContext.request.contextPath}/estudiante/documentos" method="POST" enctype="multipart/form-data"
                  onsubmit="return validarArchivoPdf(this.querySelector('input[type=file]'))">
                <input type="hidden" name="accion" value="subir">
                <div class="modal-body modal-body-propath">
                    <label class="form-label small fw-semibold">Seleccionar documento PDF *</label>
                    <input type="file" class="form-control" name="archivoPdf" accept=".pdf,application/pdf" required>
                    <div class="form-text small text-muted mt-2"><i class="bi bi-info-circle me-1"></i>Solo archivo PDF válido. Tamaño máximo: 5 MB.</div>
                </div>
                <div class="modal-footer bg-light border-0">
                    <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-primary rounded-pill px-4"><i class="bi bi-upload me-1"></i>Subir CV</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal Reemplazar CV -->
<div class="modal fade" id="modalReemplazarCv" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content modal-content-propath">
            <div class="modal-header modal-header-propath">
                <h2 class="modal-title h5"><i class="bi bi-arrow-repeat me-2"></i>Actualizar / Reemplazar CV</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <form action="${pageContext.request.contextPath}/estudiante/documentos" method="POST" enctype="multipart/form-data"
                  onsubmit="return validarArchivoPdf(this.querySelector('input[type=file]'))">
                <input type="hidden" name="accion" value="reemplazar">
                <div class="modal-body modal-body-propath">
                    <div class="alert alert-info py-2 small mb-3">
                        <i class="bi bi-shield-check me-1"></i>El CV anterior se mantendrá seguro hasta validar que el nuevo PDF se guardó correctamente.
                    </div>
                    <label class="form-label small fw-semibold">Seleccionar nuevo documento PDF *</label>
                    <input type="file" class="form-control" name="archivoPdf" accept=".pdf,application/pdf" required>
                    <div class="form-text small text-muted mt-2">Formato PDF válido. Tamaño máximo permitido: 5 MB.</div>
                </div>
                <div class="modal-footer bg-light border-0">
                    <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-warning text-dark rounded-pill px-4"><i class="bi bi-check2 me-1"></i>Reemplazar CV</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal Subir Documento Complementario -->
<div class="modal fade" id="modalSubirDocumento" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content modal-content-propath">
            <div class="modal-header modal-header-propath">
                <h2 class="modal-title h5"><i class="bi bi-file-earmark-plus me-2"></i>Subir Documento Complementario</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <form action="${pageContext.request.contextPath}/estudiante/documentos" method="POST" enctype="multipart/form-data"
                  onsubmit="return validarArchivoDocumento(this.querySelector('input[type=file]'))">
                <input type="hidden" name="accion" value="subirDocumento">
                <div class="modal-body modal-body-propath">
                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Tipo de Documento *</label>
                        <select name="tipoDocumento" class="form-select" required>
                            <option value="CARTA_PRESENTACION">Carta de Presentación</option>
                            <option value="CERTIFICADO">Certificado / Record Académico</option>
                            <option value="CONSTANCIA">Constancia de Estudios / Prácticas</option>
                            <option value="PORTAFOLIO">Portafolio</option>
                            <option value="OTRO">Otro Documento</option>
                        </select>
                    </div>
                    <div>
                        <label class="form-label small fw-semibold">Archivo (PDF, PNG o JPG) *</label>
                        <input type="file" class="form-control" name="archivoDocumento" accept=".pdf,.png,.jpg,.jpeg,application/pdf,image/png,image/jpeg" required>
                        <div class="form-text small text-muted mt-2"><i class="bi bi-shield-check me-1"></i>Formatos permitidos: PDF, PNG o JPG. Tamaño máximo: 5 MB.</div>
                    </div>
                </div>
                <div class="modal-footer bg-light border-0">
                    <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-primary rounded-pill px-4"><i class="bi bi-upload me-1"></i>Guardar Documento</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal Vista Previa Documento -->
<div class="modal fade" id="modalPrevisualizarDoc" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-lg modal-dialog-scrollable">
        <div class="modal-content modal-content-propath">
            <div class="modal-header modal-header-propath">
                <h2 class="modal-title h5"><i class="bi bi-file-earmark-text me-2"></i><span id="previewDocNombre">Vista previa</span></h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body modal-body-propath p-3" id="previewDocBody" style="min-height: 450px;"></div>
            <div class="modal-footer bg-light border-0">
                <a id="previewDocAbrir" href="#" target="_blank" class="btn btn-outline-primary rounded-pill px-3"><i class="bi bi-box-arrow-up-right me-1"></i>Abrir aparte</a>
                <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cerrar</button>
            </div>
        </div>
    </div>
</div>

<!-- Modal Eliminar CV -->
<div class="modal fade" id="modalEliminarCv" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content modal-content-propath">
            <div class="modal-header modal-header-propath">
                <h2 class="modal-title h5"><i class="bi bi-trash-fill me-2"></i>Eliminar Currículum</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body modal-body-propath py-4">
                <p class="mb-0 text-dark">¿Seguro que deseas eliminar tu CV? Necesitarás subir uno nuevo en PDF para futuras postulaciones.</p>
            </div>
            <div class="modal-footer bg-light border-0">
                <button type="button" class="btn btn-outline-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                <button type="button" class="btn btn-danger rounded-pill px-4" onclick="document.getElementById('formEliminarCv').submit()">
                    <i class="bi bi-trash me-1"></i>Sí, eliminar
                </button>
            </div>
        </div>
    </div>
</div>
<form id="formEliminarCv" action="${pageContext.request.contextPath}/estudiante/documentos" method="POST" style="display:none;">
    <input type="hidden" name="accion" value="eliminar">
</form>

<!-- Modal Editar Perfil Estudiante -->
<div class="modal fade" id="modalEditarPerfilEstudiante" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content modal-content-propath">
            <div class="modal-header modal-header-propath">
                <h2 class="modal-title h5"><i class="bi bi-person-gear me-2"></i>Actualizar Mi Perfil</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <form id="formEditarPerfilEstudiante" action="${pageContext.request.contextPath}/estudiante/postular" method="POST">
                <input type="hidden" name="accion" value="actualizarPerfil">
                <div class="modal-body modal-body-propath">
                    <div class="row g-3">
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold">Nombres *</label>
                            <input type="text" class="form-control" name="nombre" value="<%= escaparHtml(usuario.getNombre()) %>" required>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold">Apellidos *</label>
                            <input type="text" class="form-control" name="apellidos"
                                   value="<%= escaparHtml((miEstudiante != null && miEstudiante.getApellido() != null && !miEstudiante.getApellido().isEmpty()) ? miEstudiante.getApellido() : usuario.getApellidos()) %>" required>
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold">Universidad *</label>
                            <input type="text" class="form-control" name="universidad" value="<%= miEstudiante != null ? escaparHtml(miEstudiante.getUniversidad()) : "" %>" required>
                        </div>
                        <div class="col-sm-8">
                            <label class="form-label small fw-semibold">Carrera Profesional *</label>
                            <input type="text" class="form-control" name="carrera" value="<%= miEstudiante != null ? escaparHtml(miEstudiante.getCarrera()) : "" %>" required>
                        </div>
                        <div class="col-sm-4">
                            <label class="form-label small fw-semibold">Ciclo Actual *</label>
                            <input type="number" class="form-control" name="ciclo" min="1" max="14" value="<%= (miEstudiante != null && miEstudiante.getCiclo() > 0) ? miEstudiante.getCiclo() : 1 %>" required>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold">DNI (8 dígitos) *</label>
                            <input type="text" class="form-control" name="dni" maxlength="8" pattern="[0-9]{8}"
                                   value="<%= (miEstudiante != null && miEstudiante.getDni() != null) ? escaparHtml(miEstudiante.getDni()) : "" %>"
                                   placeholder="12345678" required>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold">Teléfono de Contacto (9 dígitos)</label>
                            <input type="text" class="form-control" name="telefono" maxlength="9" pattern="[0-9]{9}" placeholder="987654321"
                                   value="<%= miEstudiante != null ? escaparHtml(miEstudiante.getTelefono()) : "" %>">
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold">Correo Electrónico *</label>
                            <div class="input-group">
                                <input type="email" class="form-control" id="correoPerfilEstudiante" name="correo"
                                       data-correo-actual="<%= escaparHtml(usuario.getCorreo()) %>"
                                       value="<%= escaparHtml(usuario.getCorreo()) %>" required>
                                <button type="button" class="btn btn-primary d-none fw-semibold" id="btnValidarCorreoEstudiante">
                                    <i class="bi bi-patch-check me-1"></i> VALIDAR CORREO
                                </button>
                            </div>
                            <div class="d-none mt-1 small text-success fw-semibold" id="estadoCorreoEstudiante">
                                <i class="bi bi-check-circle-fill me-1"></i> Correo verificado correctamente
                            </div>
                        </div>
                        <div class="col-12 mt-2 pt-2 border-top">
                            <div class="small fw-bold text-secondary mb-2"><i class="bi bi-shield-lock me-1"></i>Cambio de Contraseña (Opcional)</div>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">Nueva Contraseña</label>
                            <input type="password" class="form-control" name="contrasena" placeholder="Mínimo 8 caracteres" minlength="8">
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">Confirmar Contraseña</label>
                            <input type="password" class="form-control" name="confirmarContrasena" placeholder="Repite la contraseña" minlength="8">
                        </div>
                    </div>
                </div>
                <div class="modal-footer bg-light border-0 d-flex justify-content-between">
                    <button type="button" class="btn btn-outline-danger rounded-pill px-3"
                            onclick="mostrarModalConfirmacion('Dar de baja perfil', '¿Seguro que deseas solicitar la baja de tu cuenta?', function() { document.getElementById('formBajaEstudiante').submit(); });">
                        <i class="bi bi-person-x me-1"></i>Dar de baja
                    </button>
                    <div class="d-flex gap-2">
                        <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" id="btnGuardarPerfilEstudiante" class="btn btn-primary rounded-pill px-4"><i class="bi bi-check2 me-1"></i>Guardar Cambios</button>
                    </div>
                </div>
            </form>
            <form id="formBajaEstudiante" action="${pageContext.request.contextPath}/estudiante/postular" method="POST" style="display:none;">
                <input type="hidden" name="accion" value="darDeBaja">
            </form>
        </div>
    </div>
</div>

<!-- Modal Motivo Rechazo -->
<div class="modal fade" id="modalMotivoRechazo" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content modal-content-propath">
            <div class="modal-header modal-header-propath">
                <h5 class="modal-title"><i class="bi bi-chat-left-text me-2"></i>Motivo de Evaluación</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body modal-body-propath">
                <p id="textoMotivoRechazo" class="mb-0 text-break text-secondary"></p>
            </div>
            <div class="modal-footer bg-light border-0">
                <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cerrar</button>
            </div>
        </div>
    </div>
</div>

<!-- Modal Detalle Oferta Estudiante -->
<div class="modal fade" id="modalDetalleOfertaEstudiante" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-lg modal-dialog-scrollable">
        <div class="modal-content modal-content-propath">
            <div class="modal-header modal-header-propath">
                <h5 class="modal-title"><i class="bi bi-briefcase me-2"></i>Detalle de la Convocatoria</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body modal-body-propath">
                <div class="card border-0 bg-light p-3 mb-3 rounded-3">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <span class="badge bg-primary-subtle text-primary mb-1" id="detEstOfrCarrera">—</span>
                            <h4 class="h5 fw-bold text-dark mb-1" id="detEstOfrTitulo">—</h4>
                            <div class="small text-muted"><i class="bi bi-building me-1"></i><span id="detEstOfrEmpresa">—</span></div>
                        </div>
                        <span class="badge-status badge-activo" id="detEstOfrEstado">ACTIVA</span>
                    </div>
                </div>
                <div class="row g-3 mb-3">
                    <div class="col-sm-6 col-md-4"><div class="p-2 border rounded bg-white"><span class="text-muted small d-block">Modalidad</span><strong class="text-dark small" id="detEstOfrModalidad">—</strong></div></div>
                    <div class="col-sm-6 col-md-4"><div class="p-2 border rounded bg-white"><span class="text-muted small d-block">Vacantes</span><strong class="text-dark small" id="detEstOfrVacantes">—</strong></div></div>
                    <div class="col-sm-6 col-md-4"><div class="p-2 border rounded bg-white"><span class="text-muted small d-block">Subvención</span><strong class="text-dark small" id="detEstOfrRemuneracion">—</strong></div></div>
                    <div class="col-sm-6 col-md-4"><div class="p-2 border rounded bg-white"><span class="text-muted small d-block">Ubicación</span><strong class="text-dark small" id="detEstOfrUbicacion">—</strong></div></div>
                    <div class="col-sm-6 col-md-4"><div class="p-2 border rounded bg-white"><span class="text-muted small d-block">Publicado</span><strong class="text-dark small" id="detEstOfrPublicacion">—</strong></div></div>
                    <div class="col-sm-6 col-md-4"><div class="p-2 border rounded bg-white"><span class="text-muted small d-block">Fecha Límite</span><strong class="text-danger small" id="detEstOfrFechaLimite">—</strong></div></div>
                </div>
                <div class="mb-3">
                    <h6 class="fw-bold text-dark small mb-1">Descripción del Puesto</h6>
                    <div class="p-3 border rounded bg-white text-secondary small" id="detEstOfrDescripcion" style="white-space:pre-wrap;">—</div>
                </div>
                <div>
                    <h6 class="fw-bold text-dark small mb-1">Requisitos y Competencias</h6>
                    <div class="p-3 border rounded bg-white text-secondary small" id="detEstOfrRequisitos" style="white-space:pre-wrap;">—</div>
                </div>
            </div>
            <div class="modal-footer bg-light border-0 d-flex justify-content-between">
                <button type="button" class="btn btn-primary rounded-pill px-4" id="btnDetEstPostular" style="display:none;" onclick="postularDesdeDetalle()">
                    <i class="bi bi-send me-1"></i>Postular ahora
                </button>
                <button type="button" class="btn btn-secondary rounded-pill px-4 ms-auto" data-bs-dismiss="modal">Cerrar</button>
            </div>
        </div>
    </div>
</div>

<script>
const TIENE_CV = <%= miCv != null %>;
const PREGUNTAS_BASE = [
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

let pasoActualPostular = 1;
let ofertaDetalleActual = null;
const modalPostular = document.getElementById("modalPostular");

function cambiarSeccionEstudiante(seccion) {
    document.querySelectorAll('.dashboard-section').forEach(s => s.classList.remove('active'));
    const target = document.getElementById('sec-' + seccion);
    if (target) target.classList.add('active');

    document.querySelectorAll('#estudianteSidebarNav .btn-nav-section').forEach(btn => {
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

function filtrarOfertasEstudiante() {
    const q = (document.getElementById('buscarOfertaEst').value || '').trim().toLowerCase();
    const car = document.getElementById('filtroCarreraEst').value;
    const mod = document.getElementById('filtroModalidadEst').value;
    let visibles = 0;
    document.querySelectorAll('.oferta-est-card').forEach(card => {
        const cCar = card.getAttribute('data-carrera') || '';
        const cMod = (card.getAttribute('data-modalidad') || '').toUpperCase();
        const cTxt = card.getAttribute('data-texto') || '';
        const ok = (car === 'TODAS' || cCar === car) && (mod === 'TODAS' || cMod === mod) && (!q || cTxt.includes(q));
        card.style.display = ok ? '' : 'none';
        if (ok) visibles++;
    });
    const sinRes = document.getElementById('sinResultadosOfertasEst');
    if (sinRes) sinRes.style.display = visibles === 0 ? 'block' : 'none';
    const cont = document.getElementById('contadorOfertasEst');
    if (cont) cont.textContent = visibles + ' disponible(s)';
}

function limpiarFiltrosOfertas() {
    document.getElementById('buscarOfertaEst').value = '';
    document.getElementById('filtroCarreraEst').value = 'TODAS';
    document.getElementById('filtroModalidadEst').value = 'TODAS';
    filtrarOfertasEstudiante();
}

function filtrarPostulacionesEst() {
    const q = (document.getElementById('buscarPostulacionEst').value || '').trim().toLowerCase();
    const est = document.getElementById('filtroEstadoPostulacionEst').value;
    document.querySelectorAll('.postulacion-est-card').forEach(card => {
        const cEst = (card.getAttribute('data-estado') || '').toUpperCase();
        const cTxt = card.getAttribute('data-texto') || '';
        const ok = (est === 'TODOS' || cEst === est) && (!q || cTxt.includes(q));
        card.style.display = ok ? '' : 'none';
    });
}

function filtrarEntrevistasEstudiante() {
    const est = document.getElementById('filtroEstadoEntrevistaEst').value;
    document.querySelectorAll('.entrevista-est-card').forEach(card => {
        const cEst = (card.getAttribute('data-estado') || '').toUpperCase();
        card.style.display = (est === 'TODAS' || cEst === est) ? '' : 'none';
    });
}

function validarArchivoPdf(input) {
    if (!input || !input.files || input.files.length === 0) {
        mostrarModalMensaje('Archivo Requerido', 'Por favor selecciona un archivo PDF.', 'warning');
        return false;
    }
    const archivo = input.files[0];
    if (!archivo.name.toLowerCase().endsWith('.pdf')) {
        mostrarModalMensaje('Formato Inválido', 'El archivo seleccionado debe tener extensión .pdf.', 'danger');
        return false;
    }
    if (archivo.size <= 0) {
        mostrarModalMensaje('Archivo Vacío', 'El archivo PDF seleccionado está vacío.', 'danger');
        return false;
    }
    if (archivo.size > 5 * 1024 * 1024) {
        mostrarModalMensaje('Límite Excedido', 'El archivo supera el tamaño máximo permitido de 5 MB.', 'danger');
        return false;
    }
    return true;
}

function validarArchivoDocumento(input) {
    if (!input || !input.files || input.files.length === 0) {
        mostrarModalMensaje('Archivo Requerido', 'Por favor selecciona un documento (PDF, PNG o JPG).', 'warning');
        return false;
    }
    const archivo = input.files[0];
    const nombre = archivo.name.toLowerCase();
    if (!(nombre.endsWith('.pdf') || nombre.endsWith('.png') || nombre.endsWith('.jpg') || nombre.endsWith('.jpeg'))) {
        mostrarModalMensaje('Formato Inválido', 'Solo se aceptan archivos PDF, PNG o JPG.', 'danger');
        return false;
    }
    if (archivo.size <= 0) {
        mostrarModalMensaje('Archivo Vacío', 'El archivo seleccionado está vacío.', 'danger');
        return false;
    }
    if (archivo.size > 5 * 1024 * 1024) {
        mostrarModalMensaje('Límite Excedido', 'El archivo supera el tamaño máximo permitido de 5 MB.', 'danger');
        return false;
    }
    return true;
}

function previsualizarDocumento(url, nombre, contentType) {
    document.getElementById('previewDocNombre').textContent = nombre || 'Vista previa de documento';
    document.getElementById('previewDocAbrir').href = url;
    const body = document.getElementById('previewDocBody');
    body.replaceChildren();
    if ((contentType || '').startsWith('image/')) {
        const img = document.createElement('img');
        img.src = url;
        img.alt = nombre || 'Documento';
        img.className = 'img-fluid rounded mx-auto d-block';
        img.style.maxHeight = '520px';
        body.appendChild(img);
    } else {
        const iframe = document.createElement('iframe');
        iframe.src = url;
        iframe.title = nombre || 'Vista previa PDF';
        iframe.style.width = '100%';
        iframe.style.height = '500px';
        iframe.style.border = '1px solid #dce4ef';
        iframe.className = 'rounded';
        body.appendChild(iframe);
    }
    window.bootstrap.Modal.getOrCreateInstance(document.getElementById('modalPrevisualizarDoc')).show();
}

function confirmarEliminarCv() {
    window.bootstrap.Modal.getOrCreateInstance(document.getElementById('modalEliminarCv')).show();
}

function seleccionarOfertaPostular(boton) {
    const idOferta = boton.getAttribute("data-id-oferta");
    const titulo = boton.getAttribute("data-titulo");
    const formulario = document.getElementById("formPostular");
    formulario.reset();

    document.getElementById("postularAccion").value = "";
    document.getElementById("postularIdPostulacion").value = "";
    document.getElementById("postularIdOferta").value = idOferta || "";
    document.getElementById("postularTituloOferta").textContent = titulo || "";

    const btnConfirmar = document.getElementById("btnConfirmarPostular");
    btnConfirmar.textContent = "Confirmar postulación";
    btnConfirmar.disabled = false;
    document.getElementById("btnSiguientePostular").disabled = !TIENE_CV;

    pasoActualPostular = 1;
    mostrarPasoPostular();
}

async function cargarRespuestasPostulacion(boton) {
    const formulario = document.getElementById("formPostular");
    const idPostulacion = boton.getAttribute("data-id-postulacion");
    const btnConfirmar = document.getElementById("btnConfirmarPostular");

    formulario.reset();
    pasoActualPostular = 2;

    document.getElementById("postularAccion").value = "editarRespuestas";
    document.getElementById("postularIdPostulacion").value = idPostulacion;
    document.getElementById("postularIdOferta").value = "";
    document.getElementById("postularTituloOferta").textContent = boton.getAttribute("data-titulo") || "";

    btnConfirmar.textContent = "Guardar cambios";
    btnConfirmar.disabled = true;
    document.getElementById("btnSiguientePostular").disabled = false;
    mostrarPasoPostular();

    try {
        const url = formulario.action + "?idPostulacion=" + encodeURIComponent(idPostulacion);
        const response = await fetch(url, { credentials: "same-origin", cache: "no-store" });
        if (!response.ok) throw new Error("HTTP " + response.status);
        const registros = await response.json();
        if (!Array.isArray(registros) || registros.length !== 12) {
            throw new Error("El servidor no devolvió las 12 respuestas.");
        }
        for (const registro of registros) {
            const numero = Number(registro.numeroPregunta);
            const campo = formulario.elements.namedItem("respuesta" + numero);
            if (!campo || numero < 1 || numero > 12) throw new Error("Respuestas inválidas.");
            campo.value = registro.respuesta || "";
        }
        btnConfirmar.disabled = false;
    } catch (error) {
        mostrarModalMensaje("Error al cargar respuestas", error.message, "danger");
    }
}

function mostrarPasoPostular() {
    for (let i = 1; i <= 4; i++) {
        document.getElementById("pasoPostular" + i).style.display = (i === pasoActualPostular) ? "block" : "none";
    }
    const esEdicion = document.getElementById("postularAccion").value === "editarRespuestas";
    const minPaso = esEdicion ? 2 : 1;
    document.getElementById("btnAnteriorPostular").style.display = pasoActualPostular > minPaso ? "inline-block" : "none";
    document.getElementById("btnSiguientePostular").style.display = pasoActualPostular < 4 ? "inline-block" : "none";
    document.getElementById("btnConfirmarPostular").style.display = pasoActualPostular === 4 ? "inline-block" : "none";
}

function cambiarPasoPostular(direccion) {
    if (direccion === 1 && pasoActualPostular >= 2 && pasoActualPostular <= 3) {
        const campos = document.querySelectorAll("#pasoPostular" + pasoActualPostular + " textarea");
        for (const campo of campos) {
            if (!campo.value.trim()) {
                campo.setCustomValidity("Esta respuesta es obligatoria.");
                campo.reportValidity();
                campo.setCustomValidity("");
                campo.focus();
                return;
            }
        }
    }

    const siguiente = pasoActualPostular + direccion;
    if (siguiente < 1 || siguiente > 4) return;
    pasoActualPostular = siguiente;

    if (pasoActualPostular === 4) {
        const resumen = document.getElementById("resumenPostulacion");
        resumen.replaceChildren();
        document.querySelectorAll("#formPostular textarea").forEach((campo, indice) => {
            const bloque = document.createElement("div");
            bloque.className = "mb-2 pb-2 border-bottom";
            const titulo = document.createElement("strong");
            titulo.className = "small d-block text-dark";
            titulo.textContent = (indice + 1) + ". " + (PREGUNTAS_BASE[indice] || ("Pregunta " + (indice + 1)));
            const respuesta = document.createElement("p");
            respuesta.className = "mb-0 small text-secondary mt-1";
            respuesta.textContent = campo.value;
            bloque.append(titulo, respuesta);
            resumen.appendChild(bloque);
        });
    }
    mostrarPasoPostular();
}

function abrirSubidaCv() {
    modalPostular.addEventListener("hidden.bs.modal", function () {
        bootstrap.Modal.getOrCreateInstance(document.getElementById("modalSubirCv")).show();
    }, { once: true });
    bootstrap.Modal.getOrCreateInstance(modalPostular).hide();
}

document.getElementById("formPostular").addEventListener("submit", function (event) {
    if (pasoActualPostular !== 4) {
        event.preventDefault();
        return;
    }
    const campos = this.querySelectorAll("textarea");
    for (const campo of campos) {
        if (!campo.value.trim()) {
            event.preventDefault();
            mostrarModalMensaje("Campos Incompletos", "Completa todas las 12 preguntas antes de enviar.", "warning");
            return;
        }
    }
    const idOferta = document.getElementById("postularIdOferta").value;
    const esEdicion = document.getElementById("postularAccion").value === "editarRespuestas";
    if (!esEdicion && (!idOferta || !/^[1-9]\d*$/.test(idOferta))) {
        event.preventDefault();
        mostrarModalMensaje("Error", "No se identificó correctamente la oferta.", "danger");
        return;
    }
    document.getElementById("btnConfirmarPostular").disabled = true;
});

async function verDetallePostulacionEstudiante(btn) {
    const id = btn.getAttribute('data-id');
    const oferta = btn.getAttribute('data-oferta') || '—';
    const empresa = btn.getAttribute('data-empresa') || '—';
    const fecha = btn.getAttribute('data-fecha') || '—';
    const estado = btn.getAttribute('data-estado') || '—';

    document.getElementById('estPostOferta').textContent = oferta;
    document.getElementById('estPostEmpresa').textContent = empresa;
    document.getElementById('estPostFecha').textContent = fecha;

    const badge = document.getElementById('estPostEstado');
    const estNorm = estado.toUpperCase();
    badge.textContent = estNorm.replace('_', ' ');
    badge.className = 'badge-status ' + (estNorm === 'SELECCIONADO' ? 'badge-seleccionado' : (estNorm === 'NO_SELECCIONADO' ? 'badge-bloqueado' : (estNorm === 'EN_REVISION' ? 'badge-en-revision' : 'badge-postulado')));

    const contenedor = document.getElementById('estPostRespuestas');
    contenedor.innerHTML = '<div class="text-center py-4 text-muted"><div class="spinner-border spinner-border-sm me-2"></div><span>Cargando respuestas...</span></div>';
    window.bootstrap.Modal.getOrCreateInstance(document.getElementById('modalDetallePostulacionEstudiante')).show();

    try {
        const res = await fetch('${pageContext.request.contextPath}/estudiante/postular?idPostulacion=' + encodeURIComponent(id), {
            credentials: 'same-origin', cache: 'no-store'
        });
        const respuestas = await res.json();
        contenedor.replaceChildren();
        if (!Array.isArray(respuestas) || respuestas.length === 0) {
            contenedor.innerHTML = '<div class="alert alert-light border text-muted small py-3 text-center">No hay respuestas registradas.</div>';
            return;
        }
        respuestas.sort((a, b) => Number(a.numeroPregunta) - Number(b.numeroPregunta));
        respuestas.forEach(r => {
            const num = Number(r.numeroPregunta);
            const card = document.createElement('div');
            card.className = 'border rounded-3 p-3 bg-light bg-opacity-50';
            const qEl = document.createElement('div');
            qEl.className = 'fw-semibold text-dark small mb-1';
            qEl.textContent = num + '. ' + (PREGUNTAS_BASE[num - 1] || r.pregunta || ('Pregunta ' + num));
            const aEl = document.createElement('div');
            aEl.className = 'small text-secondary ps-3 border-start border-2 border-primary';
            aEl.textContent = r.respuesta || 'Sin respuesta registrada.';
            card.append(qEl, aEl);
            contenedor.appendChild(card);
        });
    } catch (err) {
        contenedor.innerHTML = '<div class="alert alert-danger small py-2">Error al cargar respuestas: ' + err.message + '</div>';
    }
}

function verDetalleEntrevistaEstudiante(btn) {
    const d = btn.dataset;
    document.getElementById('estEntEmpresa').textContent = d.empresa || '—';
    document.getElementById('estEntOferta').textContent = d.oferta || '—';
    document.getElementById('estEntFecha').textContent = d.fecha || '—';
    document.getElementById('estEntHora').textContent = d.hora ? d.hora.substring(0, 5) : '—';
    document.getElementById('estEntModalidad').textContent = d.modalidad || '—';

    const badge = document.getElementById('estEntEstado');
    const estNorm = (d.estado || 'PROGRAMADA').toUpperCase();
    badge.textContent = estNorm;
    badge.className = 'badge-status ' + (estNorm === 'REALIZADA' ? 'badge-seleccionado' : (estNorm === 'CANCELADA' ? 'badge-bloqueado' : 'badge-activo'));

    const lugarEl = document.getElementById('estEntLugar');
    const lugarLabel = document.getElementById('estEntLugarLabel');
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
            link.innerHTML = '<i class="bi bi-camera-video me-1"></i>Entrar a la reunión virtual';
            lugarEl.appendChild(link);
        } else {
            lugarEl.textContent = lugarVal || 'Enlace no registrado';
        }
    } else {
        lugarLabel.textContent = 'Dirección o Sede';
        lugarEl.textContent = lugarVal || 'Dirección no registrada';
    }
    document.getElementById('estEntObservaciones').textContent = d.observaciones || 'Sin observaciones.';
    window.bootstrap.Modal.getOrCreateInstance(document.getElementById('modalDetalleEntrevistaEstudiante')).show();
}

function mostrarMotivoRechazo(boton) {
    document.getElementById("textoMotivoRechazo").textContent = boton.getAttribute("data-motivo") || "No se registró un motivo.";
}

function verDetalleOfertaEstudiante(btn) {
    if (!btn) return;
    ofertaDetalleActual = btn;
    const d = btn.dataset;
    document.getElementById('detEstOfrTitulo').textContent = d.titulo || '—';
    document.getElementById('detEstOfrCarrera').textContent = d.carrera || '—';
    document.getElementById('detEstOfrEmpresa').textContent = d.empresa || '—';
    document.getElementById('detEstOfrModalidad').textContent = d.modalidad || '—';
    document.getElementById('detEstOfrVacantes').textContent = (d.vacantes || '1') + ' vacante(s)';
    let rem = (d.remuneracion || '').trim();
    if (rem && !rem.toUpperCase().startsWith('S/')) rem = 'S/ ' + rem;
    document.getElementById('detEstOfrRemuneracion').textContent = rem || 'A convenir';
    document.getElementById('detEstOfrUbicacion').textContent = d.ubicacion || 'No especificada';
    document.getElementById('detEstOfrPublicacion').textContent = d.publicacion || '—';
    document.getElementById('detEstOfrFechaLimite').textContent = d.fecha || 'Sin fecha límite';
    document.getElementById('detEstOfrDescripcion').textContent = d.descripcion || 'Sin descripción detallada.';
    document.getElementById('detEstOfrRequisitos').textContent = d.requisitos || 'Sin requisitos específicos.';

    const badge = document.getElementById('detEstOfrEstado');
    const est = (d.estado || 'ACTIVA').trim().toUpperCase();
    badge.textContent = est;
    badge.className = 'badge-status ' + (est === 'ACTIVA' ? 'badge-activo' : 'badge-bloqueado');

    const btnPostDet = document.getElementById('btnDetEstPostular');
    if (btnPostDet) {
        const puedePost = (d.yapostulado !== 'true') && (d.activo === 'true') && (est === 'ACTIVA');
        btnPostDet.style.display = puedePost ? 'inline-block' : 'none';
    }
    window.bootstrap.Modal.getOrCreateInstance(document.getElementById('modalDetalleOfertaEstudiante')).show();
}

function postularDesdeDetalle() {
    if (!ofertaDetalleActual) return;
    const detModal = document.getElementById('modalDetalleOfertaEstudiante');
    detModal.addEventListener('hidden.bs.modal', function () {
        seleccionarOfertaPostular({
            getAttribute: function (attr) {
                if (attr === 'data-id-oferta') return ofertaDetalleActual.getAttribute('data-id');
                if (attr === 'data-titulo') return ofertaDetalleActual.getAttribute('data-titulo');
                return '';
            }
        });
        window.bootstrap.Modal.getOrCreateInstance(document.getElementById('modalPostular')).show();
    }, { once: true });
    window.bootstrap.Modal.getOrCreateInstance(detModal).hide();
}

window.cambiarSeccionEstudiante = cambiarSeccionEstudiante;
window.filtrarOfertasEstudiante = filtrarOfertasEstudiante;
window.limpiarFiltrosOfertas = limpiarFiltrosOfertas;
window.filtrarPostulacionesEst = filtrarPostulacionesEst;
window.filtrarEntrevistasEstudiante = filtrarEntrevistasEstudiante;
window.validarArchivoPdf = validarArchivoPdf;
window.validarArchivoDocumento = validarArchivoDocumento;
window.previsualizarDocumento = previsualizarDocumento;
window.confirmarEliminarCv = confirmarEliminarCv;
window.seleccionarOfertaPostular = seleccionarOfertaPostular;
window.cargarRespuestasPostulacion = cargarRespuestasPostulacion;
window.cambiarPasoPostular = cambiarPasoPostular;
window.abrirSubidaCv = abrirSubidaCv;
window.verDetallePostulacionEstudiante = verDetallePostulacionEstudiante;
window.verDetalleEntrevistaEstudiante = verDetalleEntrevistaEstudiante;
window.mostrarMotivoRechazo = mostrarMotivoRechazo;
window.verDetalleOfertaEstudiante = verDetalleOfertaEstudiante;
window.postularDesdeDetalle = postularDesdeDetalle;
</script>
<script type="module" src="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/js/bootstrap.bundle.min.js"></script>
<script type="module" src="${pageContext.request.contextPath}/js/script.js"></script>
<%@include file="/includes/modal-cambio-correo.jsp"%>
<script src="${pageContext.request.contextPath}/js/verificacion-correo-perfil.js"></script>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        if (window.ProPathCambioCorreo) {
            window.ProPathCambioCorreo.inicializar('${pageContext.request.contextPath}');
            window.ProPathCambioCorreo.vincularCampoCorreo({
                inputId: 'correoPerfilEstudiante',
                btnValidarId: 'btnValidarCorreoEstudiante',
                estadoId: 'estadoCorreoEstudiante',
                formId: 'formEditarPerfilEstudiante',
                btnGuardarId: 'btnGuardarPerfilEstudiante',
                contextPath: '${pageContext.request.contextPath}'
            });
        }
    });
</script>
</body>
</html>
