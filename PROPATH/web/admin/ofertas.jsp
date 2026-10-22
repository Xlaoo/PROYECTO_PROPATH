<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.propath.modelo.OfertaPractica"%>
<%@page import="com.propath.servicio.BackendCliente"%>
<%
    request.setAttribute("activePage", "ofertas");
    List<OfertaPractica> listaOfertas = BackendCliente.listarOfertas();
%>
<!DOCTYPE html>
<html lang="es" data-bs-theme="light">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Gestión de Ofertas de Prácticas | ProPath</title>

        
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

        
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
    </head>
    <body>
        <div class="admin-layout">
            
            <jsp:include page="/admin/includes/sidebar.jsp" />

            
            <main class="admin-main">
                
                <jsp:include page="/admin/includes/topbar.jsp" />

                
                <div class="admin-content">
                    
                    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
                        <div>
                            <nav aria-label="breadcrumb">
                                <ol class="breadcrumb mb-1 small">
                                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/dashboard.jsp" class="text-decoration-none">Inicio</a></li>
                                    <li class="breadcrumb-item active" aria-current="page">Ofertas</li>
                                </ol>
                            </nav>
                            <h1 class="h3 fw-bold text-dark mb-0">Gestión de Ofertas de Prácticas</h1>
                            <p class="text-muted small mb-0">Revisa, modera y valida las convocatorias para prácticas preprofesionales publicadas</p>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            <span class="badge bg-teal-subtle border px-3 py-2 rounded-pill fw-semibold" style="background-color: #ccfbf1; color: #0f766e; border-color: #99f6e4 !important;">
                                <i class="bi bi-briefcase-fill me-1"></i> <%= (listaOfertas != null ? listaOfertas.size() : 0) %> Ofertas en plataforma
                            </span>
                        </div>
                    </div>

                    
                    <div class="propath-card">
                        <div class="propath-card-header">
                            <h2 class="propath-card-title">
                                <i class="bi bi-briefcase-fill"></i>
                                <span>Convocatorias de Prácticas Preprofesionales</span>
                            </h2>
                            
                            <div class="search-box-custom">
                                <i class="bi bi-search search-icon"></i>
                                <input type="text" 
                                       class="form-control table-search-input" 
                                       data-table="tablaOfertas" 
                                       placeholder="Buscar por puesto, empresa o carrera...">
                            </div>
                        </div>

                        <div class="table-responsive">
                            <table class="table table-propath" id="tablaOfertas">
                                <thead>
                                    <tr>
                                        <th>Puesto / Convocatoria</th>
                                        <th>Empresa</th>
                                        <th>Carrera Solicitada</th>
                                        <th>Modalidad</th>
                                        <th class="text-center">Vacantes</th>
                                        <th>Estado</th>
                                        <th class="text-center">Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% if (listaOfertas == null || listaOfertas.isEmpty()) { %>
                                    <tr>
                                        <td colspan="7" class="text-center py-4 text-muted">
                                            <i class="bi bi-inbox fs-3 d-block mb-2 text-secondary"></i>
                                            <span>No hay registros disponibles</span>
                                        </td>
                                    </tr>
                                    <% } else {
                                        for (OfertaPractica ofr : listaOfertas) { 
                                            boolean esActiva = "Activa".equalsIgnoreCase(ofr.getEstado());
                                            boolean esPendiente = "Pendiente".equalsIgnoreCase(ofr.getEstado());
                                            boolean esRechazada = "Rechazada".equalsIgnoreCase(ofr.getEstado());
                                    %>
                                    <tr>
                                        <td>
                                            <div class="d-flex align-items-center gap-2">
                                                <div class="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white shadow-sm"
                                                     style="width: 36px; height: 36px; font-size: 0.85rem; background: linear-gradient(135deg, #1e40af, #06b6d4);">
                                                    <i class="bi bi-award"></i>
                                                </div>
                                                <div>
                                                    <span class="fw-semibold text-dark d-block"><%= ofr.getTitulo() %></span>
                                                    <span class="text-muted" style="font-size: 0.76rem;">Publicada el <%= ofr.getFechaPublicacion() %></span>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <span class="fw-medium text-dark">
                                                <i class="bi bi-building me-1 text-primary"></i><%= ofr.getEmpresa() %>
                                            </span>
                                        </td>
                                        <td>
                                            <span class="badge bg-light text-secondary border px-2 py-1">
                                                <%= ofr.getCarrera() %>
                                            </span>
                                        </td>
                                        <td>
                                            <span class="badge-modalidad">
                                                <i class="bi <%= "Híbrida".equalsIgnoreCase(ofr.getModalidad()) ? "bi-laptop" : "bi-geo-alt" %> me-1"></i>
                                                <%= ofr.getModalidad() %>
                                            </span>
                                        </td>
                                        <td class="text-center">
                                            <span class="badge rounded-pill bg-primary-subtle text-primary fw-bold px-3">
                                                <%= ofr.getVacantes() %> <%= ofr.getVacantes() > 1 ? "vacantes" : "vacante" %>
                                            </span>
                                        </td>
                                        <td>
                                            <span class="badge-status <%= esActiva ? "badge-activo" : (esPendiente ? "badge-pendiente" : "badge-bloqueado") %>">
                                                <i class="bi <%= esActiva ? "bi-check-circle-fill" : (esPendiente ? "bi-clock-fill" : "bi-x-circle-fill") %>"></i>
                                                <%= ofr.getEstado() %>
                                            </span>
                                        </td>
                                        <td class="text-center">
                                            <div class="action-btn-group justify-content-center">
                                                <button type="button" 
                                                        class="btn-action btn-action-view" 
                                                        title="Ver detalle"
                                                        data-bs-toggle="tooltip"
                                                        data-titulo="<%= ofr.getTitulo() %>"
                                                        data-empresa="<%= ofr.getEmpresa() %>"
                                                        data-carrera="<%= ofr.getCarrera() %>"
                                                        data-modalidad="<%= ofr.getModalidad() %>"
                                                        data-vacantes="<%= ofr.getVacantes() %>"
                                                        data-fecha="<%= ofr.getFechaPublicacion() %>"
                                                        data-estado="<%= ofr.getEstado() %>"
                                                        onclick="verDetalleOfertaBtn(this)">
                                                    <i class="bi bi-eye"></i>
                                                </button>

                                                <button type="button" 
                                                        class="btn-action btn-action-success" 
                                                        title="Aprobar oferta"
                                                        data-bs-toggle="tooltip"
                                                        data-id="<%= ofr.getId() %>"
                                                        data-estado="Activa"
                                                        onclick="cambiarEstadoOfertaBtn(this)">
                                                    <i class="bi bi-check-lg"></i>
                                                </button>

                                                <button type="button" 
                                                        class="btn-action btn-action-danger" 
                                                        title="Rechazar oferta"
                                                        data-bs-toggle="tooltip"
                                                        data-id="<%= ofr.getId() %>"
                                                        data-estado="Rechazada"
                                                        onclick="cambiarEstadoOfertaBtn(this)">
                                                    <i class="bi bi-x-lg"></i>
                                                </button>

                                                <button type="button" 
                                                        class="btn-action btn-action-warning" 
                                                        title="Desactivar oferta"
                                                        data-bs-toggle="tooltip"
                                                        data-id="<%= ofr.getId() %>"
                                                        data-estado="Desactivada"
                                                        onclick="cambiarEstadoOfertaBtn(this)">
                                                    <i class="bi bi-pause-fill"></i>
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                    <%  }
                                       } %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </main>
        </div>

        
        <div class="modal fade" id="modalDetalleOferta" tabindex="-1" aria-labelledby="modalOfertaTitle" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content modal-content-propath">
                    <div class="modal-header modal-header-propath">
                        <h2 class="modal-title h5" id="modalOfertaTitle">
                            <i class="bi bi-briefcase me-2"></i>
                            <span>Detalle de Convocatoria</span>
                        </h2>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body modal-body-propath">
                        <div class="row g-3">
                            <div class="col-12">
                                <div class="detail-label">Puesto de Prácticas</div>
                                <div class="detail-value fs-5" id="modalOfertaPuesto">—</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Empresa Solicitante</div>
                                <div class="detail-value" id="modalOfertaEmpresa">—</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Carrera Requerida</div>
                                <div class="detail-value" id="modalOfertaCarrera">—</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Modalidad</div>
                                <div class="detail-value" id="modalOfertaModalidad">—</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Número de Vacantes</div>
                                <div class="detail-value" id="modalOfertaVacantes">—</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Fecha de Publicación</div>
                                <div class="detail-value" id="modalOfertaFecha">—</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Estado de Publicación</div>
                                <span id="modalOfertaEstado" class="badge-status badge-activo">—</span>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer bg-light border-0">
                        <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cerrar</button>
                    </div>
                </div>
            </div>
        </div>

        <script type="module" src="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/js/bootstrap.bundle.min.js"></script>
        <script type="module" src="${pageContext.request.contextPath}/js/script.js"></script>
    </body>
</html>
