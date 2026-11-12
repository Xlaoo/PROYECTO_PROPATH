<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.propath.modelo.Estudiante"%>
<%@page import="com.propath.servicio.BackendCliente"%>
<%
    request.setAttribute("activePage", "estudiantes");
    List<Estudiante> listaEstudiantes = BackendCliente.listarEstudiantes();
%>
<!DOCTYPE html>
<html lang="es" data-bs-theme="light">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Gestión de Estudiantes | ProPath</title>

        
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
                                    <li class="breadcrumb-item active" aria-current="page">Estudiantes</li>
                                </ol>
                            </nav>
                            <h1 class="h3 fw-bold text-dark mb-0">Gestión de Estudiantes</h1>
                            <p class="text-muted small mb-0">Administra los estudiantes postulantes registrados para prácticas preprofesionales</p>
                        </div>
                        <div class="d-flex align-items-center gap-2 flex-wrap">
                            <span class="badge bg-primary-subtle text-primary border border-primary-subtle px-3 py-2 rounded-pill fw-semibold">
                                <i class="bi bi-people-fill me-1"></i> <%= listaEstudiantes.size() %> Estudiantes en vista
                            </span>
                            <button type="button" class="btn btn-success rounded-pill px-3 py-2 fw-semibold shadow-sm"
                                    onclick="abrirModalNuevoEstudiante()">
                                <i class="bi bi-person-plus-fill me-1"></i> Nuevo Estudiante
                            </button>
                        </div>
                    </div>

                    
                    <div class="propath-card">
                        <div class="propath-card-header">
                            <h2 class="propath-card-title">
                                <i class="bi bi-mortarboard-fill"></i>
                                <span>Listado de Postulantes Universitarios</span>
                            </h2>
                            
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
                                        <th>Teléfono</th>
                                        <th class="text-center">CV</th>
                                        <th>Estado</th>
                                        <th class="text-center">Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% if (listaEstudiantes == null || listaEstudiantes.isEmpty()) { %>
                                    <tr>
                                        <td colspan="9" class="text-center py-4 text-muted">
                                            <i class="bi bi-inbox fs-3 d-block mb-2 text-secondary"></i>
                                            <span>No hay registros disponibles</span>
                                        </td>
                                    </tr>
                                    <% } else {
                                        for (Estudiante est : listaEstudiantes) { 
                                            boolean esActivo = "Activo".equalsIgnoreCase(est.getEstado());
                                            String inicialNombre = (est.getNombre() != null && !est.getNombre().isEmpty()) ? est.getNombre().substring(0, 1).toUpperCase() : "E";
                                            String inicialApellido = (est.getApellido() != null && !est.getApellido().isEmpty()) ? est.getApellido().substring(0, 1).toUpperCase() : "";
                                    %>
                                    <tr>
                                        <td>
                                            <div class="d-flex align-items-center gap-2">
                                                <div class="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white shadow-sm <%= esActivo ? "avatar-bg-activo" : "avatar-bg-inactivo" %>"
                                                     style="width: 36px; height: 36px; font-size: 0.82rem;">
                                                    <%= inicialNombre %><%= inicialApellido %>
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
                                            <span class="small text-secondary">
                                                <i class="bi bi-telephone me-1"></i><%= (est.getTelefono() != null && !est.getTelefono().isEmpty()) ? est.getTelefono() : "—" %>
                                            </span>
                                        </td>
                                        <td class="text-center">
                                            <% if (est.getCv() != null && !est.getCv().trim().isEmpty()) { %>
                                                <a href="${pageContext.request.contextPath}/estudiante/documentos?accion=ver&idEstudiante=<%= est.getId() %>" target="_blank" class="btn btn-sm btn-outline-primary py-0 px-2" style="font-size: 0.75rem;">
                                                    <i class="bi bi-file-earmark-pdf me-1"></i>CV
                                                </a>
                                            <% } else { %>
                                                <span class="badge bg-light text-muted border" style="font-size: 0.72rem;">No adjunto</span>
                                            <% } %>
                                        </td>
                                        <td>
                                            <span class="badge-status <%= esActivo ? "badge-activo" : "badge-bloqueado" %>">
                                                <i class="bi <%= esActivo ? "bi-check-circle-fill" : "bi-slash-circle-fill" %>"></i>
                                                <%= est.getEstado() %>
                                            </span>
                                        </td>
                                        <td class="text-center">
                                            <div class="action-btn-group justify-content-center">
                                                <% if (esActivo) { %>
                                                <button type="button" 
                                                        class="btn-action btn-action-view" 
                                                        title="Ver detalle"
                                                        data-bs-toggle="tooltip"
                                                        data-id="<%= est.getId() %>"
                                                        data-nombre="<%= est.getNombreCompleto() %>"
                                                        data-correo="<%= est.getCorreo() %>"
                                                        data-universidad="<%= est.getUniversidad() %>"
                                                        data-carrera="<%= est.getCarrera() %>"
                                                        data-ciclo="<%= est.getCiclo() %>"
                                                        data-telefono="<%= est.getTelefono() != null ? est.getTelefono() : "" %>"
                                                        data-cv="<%= est.getCv() != null ? est.getCv() : "" %>"
                                                        data-estado="<%= est.getEstado() %>"
                                                        onclick="mostrarFichaEstudianteAdmin(this)">
                                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16"
     viewBox="0 0 24 24" fill="none"
     stroke="currentColor" stroke-width="2"
     stroke-linecap="round" stroke-linejoin="round"
     aria-hidden="true">
    <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z"/>
    <circle cx="12" cy="12" r="3"/>
</svg>
                                                </button>

                                                <button type="button" 
                                                        class="btn-action btn-action-edit" 
                                                        title="Editar estudiante"
                                                        data-bs-toggle="tooltip"
                                                        data-id="<%= est.getId() %>"
                                                        data-dni="<%= est.getDni() != null ? est.getDni() : "" %>"
                                                        data-nombres="<%= est.getNombre() != null ? est.getNombre() : "" %>"
                                                        data-apellidos="<%= est.getApellido() != null ? est.getApellido() : "" %>"
                                                        data-correo="<%= est.getCorreo() %>"
                                                        data-universidad="<%= est.getUniversidad() != null ? est.getUniversidad() : "" %>"
                                                        data-carrera="<%= est.getCarrera() != null ? est.getCarrera() : "" %>"
                                                        data-ciclo="<%= est.getCiclo() %>"
                                                        data-telefono="<%= est.getTelefono() != null ? est.getTelefono() : "" %>"
                                                        data-estado="<%= est.getEstado() %>"
                                                        onclick="abrirModalEditarEstudiante(this)">
                                                    <i class="bi bi-pencil-square"></i>
                                                </button>

                                                <% } %>
                                                <button type="button" 
                                                        class="btn-action btn-action-danger" 
                                                        title="Eliminar estudiante"
                                                        data-bs-toggle="tooltip"
                                                        data-id="<%= est.getId() %>"
                                                        data-nombre="<%= est.getNombreCompleto() %>"
                                                        onclick="eliminarEstudiante(this.dataset.id, this.dataset.nombre)">
                                                    <i class="bi bi-trash"></i>
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
                                <div class="detail-value fs-5" id="modalEstudianteNombre">—</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Correo Institucional / Personal</div>
                                <div class="detail-value" id="modalEstudianteCorreo">—</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Universidad de Origen</div>
                                <div class="detail-value" id="modalEstudianteUniversidad">—</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Carrera Profesional</div>
                                <div class="detail-value" id="modalEstudianteCarrera">—</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Ciclo Académico</div>
                                <div class="detail-value" id="modalEstudianteCiclo">—</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Teléfono o Celular</div>
                                <div class="detail-value" id="modalEstudianteTelefono">—</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Curriculum Vitae (CV)</div>
                                <div class="detail-value" id="modalEstudianteCv">—</div>
                            </div>
                            <div class="col-12 pt-2 border-top">
                                <div class="detail-label">Estado de la Cuenta</div>
                                <span id="modalEstudianteEstado" class="badge-status badge-activo">—</span>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer bg-light border-0">
                        <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cerrar</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="modal fade" id="modalEditarEstudiante" tabindex="-1" aria-labelledby="modalEditarEstudianteLabel" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content modal-content-propath">
                    <div class="modal-header modal-header-propath">
                        <h2 class="modal-title h5" id="modalEditarEstudianteLabel">
                            <i class="bi bi-pencil-square me-2 text-primary"></i> Editar Estudiante
                        </h2>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <form id="formEditarEstudiante" onsubmit="event.preventDefault(); ejecutarGuardarEditarEstudiante();">
                        <div class="modal-body modal-body-propath">
                            <input type="hidden" id="editEstudianteId" name="id">
                            <div class="row g-3">
                                <div class="col-sm-6">
                                    <label class="form-label small fw-semibold text-secondary">Nombres *</label>
                                    <input type="text" class="form-control" id="editEstudianteNombre" name="nombre" required>
                                </div>
                                <div class="col-sm-6">
                                    <label class="form-label small fw-semibold text-secondary">Apellidos *</label>
                                    <input type="text" class="form-control" id="editEstudianteApellidos" name="apellido" required>
                                </div>
                                <div class="col-sm-5">
                                    <label class="form-label small fw-semibold text-secondary">DNI (8 dígitos)</label>
                                    <input type="text" class="form-control" id="editEstudianteDni" name="dni" maxlength="8" pattern="[0-9]{8}" placeholder="Opcional">
                                </div>
                                <div class="col-sm-7">
                                    <label class="form-label small fw-semibold text-secondary">Correo Electrónico</label>
                                    <input type="email" class="form-control bg-light" id="editEstudianteCorreo" readonly disabled>
                                </div>
                                <div class="col-sm-6">
                                    <label class="form-label small fw-semibold text-secondary">Universidad *</label>
                                    <input type="text" class="form-control" id="editEstudianteUniversidad" name="universidad" required>
                                </div>
                                <div class="col-sm-6">
                                    <label class="form-label small fw-semibold text-secondary">Carrera *</label>
                                    <input type="text" class="form-control" id="editEstudianteCarrera" name="carrera" required>
                                </div>
                                <div class="col-sm-6">
                                    <label class="form-label small fw-semibold text-secondary">Ciclo Actual *</label>
                                    <select class="form-select" id="editEstudianteCiclo" name="ciclo" required>
                                        <% for (int c = 1; c <= 12; c++) { %>
                                            <option value="<%= c %>">Ciclo <%= c %></option>
                                        <% } %>
                                    </select>
                                </div>
                                <div class="col-sm-6">
                                    <label class="form-label small fw-semibold text-secondary">Teléfono</label>
                                    <input type="tel" class="form-control" id="editEstudianteTelefono" name="telefono" placeholder="Ej. 987654321">
                                </div>
                                <div class="col-12">
                                    <label class="form-label small fw-semibold text-secondary">Estado de Cuenta (Informativo)</label>
                                    <input type="text" class="form-control bg-light fw-semibold text-secondary" id="editEstudianteEstado" name="estado" readonly>
                                    <div class="form-text">El estado de cuenta es informativo y no puede modificarse desde este formulario.</div>
                                </div>
                            </div>
                        </div>
                        <div class="modal-footer bg-light border-0">
                            <button type="button" class="btn btn-outline-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                            <button type="submit" class="btn btn-primary rounded-pill px-4" id="btnGuardarEditarEstudiante">
                                <i class="bi bi-check2 me-1"></i> Guardar Cambios
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <div class="modal fade" id="modalNuevoEstudiante" tabindex="-1" aria-labelledby="modalNuevoEstudianteLabel" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered modal-lg">
                <div class="modal-content modal-content-propath">
                    <div class="modal-header modal-header-propath">
                        <h2 class="modal-title h5" id="modalNuevoEstudianteLabel">
                            <i class="bi bi-person-plus-fill me-2 text-primary"></i> Registrar Nuevo Estudiante
                        </h2>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <form id="formNuevoEstudianteAdmin" onsubmit="event.preventDefault(); guardarNuevoEstudianteAdmin();">
                        <div class="modal-body modal-body-propath">
                            <div class="row g-3">
                                <div class="col-sm-6">
                                    <label class="form-label small fw-semibold text-secondary">Nombres *</label>
                                    <input type="text" class="form-control" id="newEstNombre" required>
                                </div>
                                <div class="col-sm-6">
                                    <label class="form-label small fw-semibold text-secondary">Apellidos *</label>
                                    <input type="text" class="form-control" id="newEstApellidos" required>
                                </div>
                                <div class="col-sm-4">
                                    <label class="form-label small fw-semibold text-secondary">DNI (8 dígitos)</label>
                                    <input type="text" class="form-control" id="newEstDni" maxlength="8" pattern="[0-9]{8}" placeholder="Opcional">
                                </div>
                                <div class="col-sm-4">
                                    <label class="form-label small fw-semibold text-secondary">Correo Electrónico *</label>
                                    <input type="email" class="form-control" id="newEstCorreo" required>
                                </div>
                                <div class="col-sm-4">
                                    <label class="form-label small fw-semibold text-secondary">Contraseña Inicial *</label>
                                    <input type="password" class="form-control" id="newEstClave" minlength="6" required>
                                </div>
                                <div class="col-sm-6">
                                    <label class="form-label small fw-semibold text-secondary">Universidad *</label>
                                    <input type="text" class="form-control" id="newEstUniversidad" required>
                                </div>
                                <div class="col-sm-6">
                                    <label class="form-label small fw-semibold text-secondary">Carrera Profesional *</label>
                                    <input type="text" class="form-control" id="newEstCarrera" required>
                                </div>
                                <div class="col-sm-6">
                                    <label class="form-label small fw-semibold text-secondary">Ciclo Actual *</label>
                                    <select class="form-select" id="newEstCiclo" required>
                                        <% for (int c = 1; c <= 12; c++) { %>
                                            <option value="<%= c %>">Ciclo <%= c %></option>
                                        <% } %>
                                    </select>
                                </div>
                                <div class="col-sm-6">
                                    <label class="form-label small fw-semibold text-secondary">Teléfono</label>
                                    <input type="tel" class="form-control" id="newEstTelefono" maxlength="9" placeholder="987654321">
                                </div>
                            </div>
                        </div>
                        <div class="modal-footer bg-light border-0">
                            <button type="button" class="btn btn-outline-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                            <button type="submit" class="btn btn-primary rounded-pill px-4" id="btnGuardarNuevoEstudiante">
                                <i class="bi bi-check2 me-1"></i> Registrar Estudiante
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <script>
            function mostrarFichaEstudianteAdmin(btn) {
                const id = btn.getAttribute('data-id');
                const nombre = btn.getAttribute('data-nombre');
                const correo = btn.getAttribute('data-correo');
                const universidad = btn.getAttribute('data-universidad');
                const carrera = btn.getAttribute('data-carrera');
                const ciclo = btn.getAttribute('data-ciclo');
                const telefono = btn.getAttribute('data-telefono');
                const cv = btn.getAttribute('data-cv');
                const estado = (btn.getAttribute('data-estado') || '').trim().toUpperCase();

                document.getElementById('modalEstudianteNombre').textContent = nombre || '—';
                document.getElementById('modalEstudianteCorreo').textContent = correo || '—';
                document.getElementById('modalEstudianteUniversidad').textContent = universidad || '—';
                document.getElementById('modalEstudianteCarrera').textContent = carrera || '—';
                document.getElementById('modalEstudianteCiclo').textContent = ciclo ? 'Ciclo ' + ciclo : '—';
                document.getElementById('modalEstudianteTelefono').textContent = telefono || 'No registrado';

                const cvEl = document.getElementById('modalEstudianteCv');
                if (cv && cv.trim() !== '') {
                    cvEl.innerHTML = '<a href="${pageContext.request.contextPath}/estudiante/documentos?accion=ver&idEstudiante=' + id + '" target="_blank" class="btn btn-sm btn-outline-primary"><i class="bi bi-file-earmark-pdf me-1"></i>Ver PDF</a>';
                } else {
                    cvEl.textContent = 'No adjuntado';
                }

                const badge = document.getElementById('modalEstudianteEstado');
                badge.textContent = estado || 'NO DISPONIBLE';
badge.className = 'badge-status ' +
    (estado === 'ACTIVO' ? 'badge-activo' : 'badge-bloqueado');

                const modalEl = document.getElementById('modalDetalleEstudiante');
                const ModalClass = window.BootstrapModalCompat || (window.bootstrap && window.bootstrap.Modal);
                if (ModalClass) {
                    const inst = ModalClass.getOrCreateInstance(modalEl);
                    if (inst) inst.show();
                }
            }

            function abrirModalEditarEstudiante(btn) {
                document.getElementById('editEstudianteId').value = btn.getAttribute('data-id') || '';
                document.getElementById('editEstudianteDni').value = (btn.getAttribute('data-dni') || '').trim();
                document.getElementById('editEstudianteNombre').value = (btn.getAttribute('data-nombres') || '').trim();
                document.getElementById('editEstudianteApellidos').value = (btn.getAttribute('data-apellidos') || '').trim();
                document.getElementById('editEstudianteCorreo').value = (btn.getAttribute('data-correo') || '').trim();
                document.getElementById('editEstudianteUniversidad').value = (btn.getAttribute('data-universidad') || '').trim();
                document.getElementById('editEstudianteCarrera').value = (btn.getAttribute('data-carrera') || '').trim();
                document.getElementById('editEstudianteCiclo').value = btn.getAttribute('data-ciclo') || '1';
                document.getElementById('editEstudianteTelefono').value = (btn.getAttribute('data-telefono') || '').trim();
                const est = (btn.getAttribute('data-estado') || 'ACTIVO').trim().toUpperCase();
                document.getElementById('editEstudianteEstado').value = (est === 'ACTIVO' || est === 'ACTIVA') ? 'ACTIVO' : 'INACTIVO';

                const modalEl = document.getElementById('modalEditarEstudiante');
                const ModalClass = window.BootstrapModalCompat || (window.bootstrap && window.bootstrap.Modal);
                if (ModalClass) {
                    const inst = ModalClass.getOrCreateInstance(modalEl);
                    if (inst) inst.show();
                }
            }

            async function ejecutarGuardarEditarEstudiante() {
                const btn = document.getElementById('btnGuardarEditarEstudiante');
                btn.disabled = true;

                const body = new URLSearchParams({
                    accion: 'actualizarEstudiante',
                    id: document.getElementById('editEstudianteId').value,
                    dni: document.getElementById('editEstudianteDni').value.trim(),
                    nombre: document.getElementById('editEstudianteNombre').value,
                    apellido: document.getElementById('editEstudianteApellidos').value,
                    universidad: document.getElementById('editEstudianteUniversidad').value,
                    carrera: document.getElementById('editEstudianteCarrera').value,
                    ciclo: document.getElementById('editEstudianteCiclo').value,
                    telefono: document.getElementById('editEstudianteTelefono').value,
                    estado: document.getElementById('editEstudianteEstado').value
                });

                try {
                    const res = await fetch('${pageContext.request.contextPath}/admin/accion', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                        body: body.toString()
                    });

                    const data = await res.json();
                    const modalEl = document.getElementById('modalEditarEstudiante');
                    const ModalClass = window.BootstrapModalCompat || (window.bootstrap && window.bootstrap.Modal);
                    if (ModalClass) {
                        const modalInst = ModalClass.getInstance(modalEl);
                        if (modalInst) modalInst.hide();
                    }

                    if (data.exito) {
                        mostrarModalMensaje('Estudiante Actualizado', data.mensaje || 'Estudiante actualizado con éxito.', 'success', () => window.location.reload());
                    } else {
                        mostrarModalMensaje('Error al Actualizar', data.mensaje || 'No se pudo actualizar el estudiante.', 'danger');
                        btn.disabled = false;
                    }
                } catch (err) {
                    mostrarModalMensaje('Error de Conexión', 'No se pudo conectar con el servidor: ' + err.message, 'danger');
                    btn.disabled = false;
                }
            }

            function eliminarEstudiante(id, nombre) {
                mostrarModalConfirmacion(
                    'Eliminar Estudiante',
                    '¿Estás seguro de eliminar al estudiante "' + nombre + '"? Esta acción verificará y eliminará las relaciones del estudiante.',
                    async function () {
                        const body = new URLSearchParams({
                            accion: 'eliminar',
                            recurso: 'estudiantes',
                            id: id
                        });

                        try {
                            const res = await fetch('${pageContext.request.contextPath}/admin/accion', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                                body: body.toString()
                            });

                            const data = await res.json();
                            if (data.exito) {
                                mostrarModalMensaje('Estudiante Eliminado', data.mensaje || 'Estudiante eliminado con éxito.', 'success', () => window.location.reload());
                            } else {
                                mostrarModalMensaje('Error al Eliminar', data.mensaje || 'No se pudo eliminar el estudiante.', 'danger');
                            }
                        } catch (err) {
                            mostrarModalMensaje('Error de Conexión', 'No se pudo conectar con el servidor: ' + err.message, 'danger');
                        }
                    }
                );
            }

            function abrirModalNuevoEstudiante() {
                document.getElementById('formNuevoEstudianteAdmin').reset();
                const modalEl = document.getElementById('modalNuevoEstudiante');
                const ModalClass = window.BootstrapModalCompat || (window.bootstrap && window.bootstrap.Modal);
                if (ModalClass) {
                    const inst = ModalClass.getOrCreateInstance(modalEl);
                    if (inst) inst.show();
                }
            }

            async function guardarNuevoEstudianteAdmin() {
                const btn = document.getElementById('btnGuardarNuevoEstudiante');
                btn.disabled = true;
                const body = new URLSearchParams({
                    accion: 'crearUsuario',
                    rol: 'ESTUDIANTE',
                    nombre: document.getElementById('newEstNombre').value.trim(),
                    apellidos: document.getElementById('newEstApellidos').value.trim(),
                    dni: document.getElementById('newEstDni').value.trim(),
                    correo: document.getElementById('newEstCorreo').value.trim(),
                    contrasena: document.getElementById('newEstClave').value.trim(),
                    universidad: document.getElementById('newEstUniversidad').value.trim(),
                    carrera: document.getElementById('newEstCarrera').value.trim(),
                    ciclo: document.getElementById('newEstCiclo').value,
                    telefono: document.getElementById('newEstTelefono').value.trim()
                });
                try {
                    const res = await fetch('${pageContext.request.contextPath}/admin/accion', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                        body: body.toString()
                    });
                    const data = await res.json();
                    const modalEl = document.getElementById('modalNuevoEstudiante');
                    const ModalClass = window.BootstrapModalCompat || (window.bootstrap && window.bootstrap.Modal);
                    if (ModalClass && data.exito) {
                        const inst = ModalClass.getInstance(modalEl);
                        if (inst) inst.hide();
                    }
                    if (data.exito) {
                        mostrarModalMensaje('Estudiante Registrado', data.mensaje || 'Estudiante creado con éxito.', 'success', () => window.location.reload());
                    } else {
                        mostrarModalMensaje('Error al Registrar', data.mensaje || 'No se pudo registrar el estudiante.', 'danger');
                        btn.disabled = false;
                    }
                } catch (err) {
                    mostrarModalMensaje('Error de Conexión', 'No se pudo conectar con el servidor: ' + err.message, 'danger');
                    btn.disabled = false;
                }
            }

            window.abrirModalNuevoEstudiante = abrirModalNuevoEstudiante;
            window.guardarNuevoEstudianteAdmin = guardarNuevoEstudianteAdmin;
            window.mostrarFichaEstudianteAdmin = mostrarFichaEstudianteAdmin;
            window.abrirModalEditarEstudiante = abrirModalEditarEstudiante;
            window.ejecutarGuardarEditarEstudiante = ejecutarGuardarEditarEstudiante;
            window.eliminarEstudiante = eliminarEstudiante;
        </script>
        <script type="module" src="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/js/bootstrap.bundle.min.js"></script>
        <script type="module" src="${pageContext.request.contextPath}/js/script.js"></script>
    </body>
</html>
