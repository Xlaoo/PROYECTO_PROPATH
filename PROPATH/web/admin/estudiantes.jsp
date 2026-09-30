<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.propath.modelo.Estudiante"%>
<%@page import="com.propath.dao.DatosSimuladosDAO"%>
<%
    request.setAttribute("activePage", "estudiantes");
    List<Estudiante> listaEstudiantes = DatosSimuladosDAO.obtenerEstudiantes();
%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Gestión de Estudiantes | ProPath</title>

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

                <!-- Cuerpo de Gestión de Estudiantes -->
                <div class="admin-content">
                    <!-- Cabecera de Página -->
                    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
                        <div>
                            <nav aria-label="breadcrumb">
                                <ol class="breadcrumb mb-1 small">
                                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/dashboard.jsp" class="text-decoration-none">Inicio</a></li>
                                    <li class="breadcrumb-item active" aria-current="page">Estudiantes</li>
                                </ol>
                            </nav>
                            <h1 class="h3 fw-bold text-dark mb-0">Gestión de Estudiantes</h1>
                            <p class="text-muted small mb-0">Administra los estudiantes postulantes registrados para prácticas preprofesionales</p>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            <span class="badge bg-primary-subtle text-primary border border-primary-subtle px-3 py-2 rounded-pill fw-semibold">
                                <i class="bi bi-people-fill me-1"></i> <%= listaEstudiantes.size() %> Estudiantes en vista
                            </span>
                        </div>
                    </div>

                    <!-- Tarjeta con Tabla de Estudiantes -->
                    <div class="propath-card">
                        <div class="propath-card-header">
                            <h2 class="propath-card-title">
                                <i class="bi bi-mortarboard-fill"></i>
                                <span>Listado de Postulantes Universitarios</span>
                            </h2>
                            <!-- Buscador en tiempo real -->
                            <div class="search-box-custom">
                                <i class="bi bi-search search-icon"></i>
                                <input type="text" 
                                       class="form-control table-search-input" 
                                       data-table="tablaEstudiantes" 
                                       placeholder="Buscar por nombre, carrera, universidad...">
                            </div>
                        </div>

                        <div class="table-responsive">
                            <table class="table table-propath" id="tablaEstudiantes">
                                <thead>
                                    <tr>
                                        <th>Nombre Completo</th>
                                        <th>Correo Electrónico</th>
                                        <th>Universidad</th>
                                        <th>Carrera</th>
                                        <th class="text-center">Ciclo</th>
                                        <th>Estado</th>
                                        <th class="text-center">Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (Estudiante est : listaEstudiantes) { 
                                        boolean esActivo = "Activo".equalsIgnoreCase(est.getEstado());
                                    %>
                                    <tr>
                                        <td>
                                            <div class="d-flex align-items-center gap-2">
                                                <div class="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white shadow-sm"
                                                     style="width: 36px; height: 36px; font-size: 0.82rem; background: <%= esActivo ? "linear-gradient(135deg, #1e40af, #06b6d4)" : "linear-gradient(135deg, #64748b, #94a3b8)" %>;">
                                                    <%= est.getNombre().substring(0, 1) %><%= est.getApellido().substring(0, 1) %>
                                                </div>
                                                <span class="fw-semibold text-dark"><%= est.getNombreCompleto() %></span>
                                            </div>
                                        </td>
                                        <td>
                                            <a href="mailto:<%= est.getCorreo() %>" class="text-muted text-decoration-none small">
                                                <i class="bi bi-envelope me-1"></i><%= est.getCorreo() %>
                                            </a>
                                        </td>
                                        <td>
                                            <span class="badge bg-light text-dark border px-2 py-1 fw-medium">
                                                <i class="bi bi-bank me-1 text-primary"></i><%= est.getUniversidad() %>
                                            </span>
                                        </td>
                                        <td>
                                            <span class="fw-medium text-secondary"><%= est.getCarrera() %></span>
                                        </td>
                                        <td class="text-center">
                                            <span class="badge rounded-pill bg-secondary-subtle text-secondary fw-semibold">
                                                Ciclo <%= est.getCiclo() %>
                                            </span>
                                        </td>
                                        <td>
                                            <span class="badge-status <%= esActivo ? "badge-activo" : "badge-bloqueado" %>">
                                                <i class="bi <%= esActivo ? "bi-check-circle-fill" : "bi-slash-circle-fill" %>"></i>
                                                <%= est.getEstado() %>
                                            </span>
                                        </td>
                                        <td class="text-center">
                                            <div class="action-btn-group justify-content-center">
                                                <!-- Botón Ver Detalle -->
                                                <button type="button" 
                                                        class="btn-action btn-action-view" 
                                                        title="Ver detalle"
                                                        data-bs-toggle="tooltip"
                                                        onclick="verDetalleEstudiante('<%= est.getNombreCompleto() %>', '<%= est.getCorreo() %>', '<%= est.getUniversidad() %>', '<%= est.getCarrera() %>', '<%= est.getCiclo() %>', '<%= est.getEstado() %>')">
                                                    <i class="bi bi-eye"></i>
                                                </button>

                                                <!-- Botón Activar -->
                                                <button type="button" 
                                                        class="btn-action btn-action-success" 
                                                        title="Activar estudiante"
                                                        data-bs-toggle="tooltip"
                                                        onclick="cambiarEstadoEstudiante(<%= est.getId() %>, 'Activo', this)">
                                                    <i class="bi bi-check-lg"></i>
                                                </button>

                                                <!-- Botón Bloquear -->
                                                <button type="button" 
                                                        class="btn-action btn-action-danger" 
                                                        title="Bloquear estudiante"
                                                        data-bs-toggle="tooltip"
                                                        onclick="cambiarEstadoEstudiante(<%= est.getId() %>, 'Bloqueado', this)">
                                                    <i class="bi bi-slash-circle"></i>
                                                </button>
                                            </div>
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

        <!-- Modal de Detalle del Estudiante -->
        <div class="modal fade" id="modalDetalleEstudiante" tabindex="-1" aria-labelledby="modalEstudianteTitle" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content modal-content-propath">
                    <div class="modal-header modal-header-propath">
                        <h2 class="modal-title h5" id="modalEstudianteTitle">
                            <i class="bi bi-person-badge-fill me-2"></i>
                            <span>Ficha del Estudiante</span>
                        </h2>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body modal-body-propath">
                        <div class="row g-3">
                            <div class="col-12">
                                <div class="detail-label">Nombre Completo</div>
                                <div class="detail-value fs-5" id="modalEstudianteNombre">Ana Torres</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Correo Institucional / Personal</div>
                                <div class="detail-value" id="modalEstudianteCorreo">ana@gmail.com</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Universidad de Origen</div>
                                <div class="detail-value" id="modalEstudianteUniversidad">UPN</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Carrera Profesional</div>
                                <div class="detail-value" id="modalEstudianteCarrera">Ingeniería de Sistemas</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Ciclo Académico</div>
                                <div class="detail-value" id="modalEstudianteCiclo">Ciclo 8</div>
                            </div>
                            <div class="col-12 pt-2 border-top">
                                <div class="detail-label">Estado de la Cuenta</div>
                                <span id="modalEstudianteEstado" class="badge-status badge-activo">Activo</span>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer bg-light border-0">
                        <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cerrar</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Bootstrap 5 Bundle JS -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <!-- Lógica JavaScript ProPath -->
        <script src="${pageContext.request.contextPath}/js/script.js"></script>
    </body>
</html>
