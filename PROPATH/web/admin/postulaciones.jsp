<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.propath.modelo.Postulacion"%>
<%@page import="com.propath.dao.DatosSimuladosDAO"%>
<%
    request.setAttribute("activePage", "postulaciones");
    List<Postulacion> listaPostulaciones = DatosSimuladosDAO.obtenerPostulaciones();
%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Gestión de Postulaciones | ProPath</title>

        <!-- Google Fonts: Inter -->
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

        <!-- Bootstrap 5 CSS & Bootstrap Icons CDN -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

        <!-- Estilos institucionales ProPath -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
    </head>
    <body>
        <div class="admin-layout">
            <!-- Sidebar -->
            <jsp:include page="/admin/includes/sidebar.jsp" />

            <!-- Contenido Principal -->
            <main class="admin-main">
                <!-- Barra superior -->
                <jsp:include page="/admin/includes/topbar.jsp" />

                <!-- Cuerpo de Gestión de Postulaciones -->
                <div class="admin-content">
                    <!-- Cabecera de Página -->
                    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
                        <div>
                            <nav aria-label="breadcrumb">
                                <ol class="breadcrumb mb-1 small">
                                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/dashboard.jsp" class="text-decoration-none">Inicio</a></li>
                                    <li class="breadcrumb-item active" aria-current="page">Postulaciones</li>
                                </ol>
                            </nav>
                            <h1 class="h3 fw-bold text-dark mb-0">Gestión de Postulaciones</h1>
                            <p class="text-muted small mb-0">Visualización centralizada de las postulaciones realizadas por estudiantes a ofertas laborales</p>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            <span class="badge bg-indigo-subtle border px-3 py-2 rounded-pill fw-semibold" style="background-color: #e0e7ff; color: #4338ca; border-color: #c7d2fe !important;">
                                <i class="bi bi-file-earmark-person-fill me-1"></i> <%= listaPostulaciones.size() %> Postulaciones monitoreadas
                            </span>
                        </div>
                    </div>

                    <!-- Mensaje informativo de solo lectura -->
                    <div class="alert alert-primary d-flex align-items-center mb-4 py-3 px-4 rounded-3 border-primary-subtle shadow-sm" role="alert">
                        <i class="bi bi-info-circle-fill fs-4 me-3 text-primary flex-shrink-0"></i>
                        <div>
                            <div class="fw-bold text-primary mb-1">Módulo de Solo Consulta</div>
                            <div class="small text-secondary">
                                Como administrador, tienes acceso de auditoría y supervisión global. La modificación y decisión final sobre cada postulación (<em>Postulado, En revisión, Seleccionado, No seleccionado</em>) es potestad exclusiva de la empresa evaluadora.
                            </div>
                        </div>
                    </div>

                    <!-- Tarjeta con Tabla de Postulaciones -->
                    <div class="propath-card">
                        <div class="propath-card-header">
                            <h2 class="propath-card-title">
                                <i class="bi bi-file-earmark-person-fill"></i>
                                <span>Registro de Postulaciones</span>
                            </h2>
                            <!-- Buscador en tiempo real -->
                            <div class="search-box-custom">
                                <i class="bi bi-search search-icon"></i>
                                <input type="text" 
                                       class="form-control table-search-input" 
                                       data-table="tablaPostulaciones" 
                                       placeholder="Buscar por estudiante, oferta o empresa...">
                            </div>
                        </div>

                        <div class="table-responsive">
                            <table class="table table-propath" id="tablaPostulaciones">
                                <thead>
                                    <tr>
                                        <th>Estudiante</th>
                                        <th>Oferta de Prácticas</th>
                                        <th>Empresa</th>
                                        <th>Fecha de Postulación</th>
                                        <th>Estado Actual</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (Postulacion pos : listaPostulaciones) { 
                                        String badgeClass = "badge-postulado";
                                        String iconClass = "bi-send-fill";
                                        
                                        if ("En revisión".equalsIgnoreCase(pos.getEstado())) {
                                            badgeClass = "badge-en-revision";
                                            iconClass = "bi-clock-history";
                                        } else if ("Seleccionado".equalsIgnoreCase(pos.getEstado())) {
                                            badgeClass = "badge-seleccionado";
                                            iconClass = "bi-check2-circle";
                                        } else if ("No seleccionado".equalsIgnoreCase(pos.getEstado())) {
                                            badgeClass = "badge-bloqueado";
                                            iconClass = "bi-x-circle-fill";
                                        }
                                    %>
                                    <tr>
                                        <td>
                                            <div class="d-flex align-items-center gap-2">
                                                <div class="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white shadow-sm"
                                                     style="width: 36px; height: 36px; font-size: 0.82rem; background: linear-gradient(135deg, #1e40af, #3b82f6);">
                                                    <i class="bi bi-person"></i>
                                                </div>
                                                <div>
                                                    <span class="fw-semibold text-dark d-block"><%= pos.getEstudiante() %></span>
                                                    <span class="text-muted" style="font-size: 0.75rem;">Postulante registrado</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <span class="fw-semibold text-primary">
                                                <i class="bi bi-briefcase me-1"></i><%= pos.getOferta() %>
                                            </span>
                                        </td>
                                        <td>
                                            <span class="text-secondary fw-medium">
                                                <i class="bi bi-building me-1 text-muted"></i><%= pos.getEmpresa() %>
                                            </span>
                                        </td>
                                        <td>
                                            <span class="text-muted small">
                                                <i class="bi bi-calendar-event me-1"></i><%= pos.getFechaPostulacion() %>
                                            </span>
                                        </td>
                                        <td>
                                            <span class="badge-status <%= badgeClass %>">
                                                <i class="bi <%= iconClass %>"></i>
                                                <%= pos.getEstado() %>
                                            </span>
                                        </td>
                                    </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </main>
        </div>

        <!-- Bootstrap 5 Bundle JS -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <!-- Lógica JavaScript ProPath -->
        <script src="${pageContext.request.contextPath}/js/script.js"></script>
    </body>
</html>
