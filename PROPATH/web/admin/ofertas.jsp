<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.propath.modelo.OfertaPractica"%>
<%@page import="com.propath.modelo.Empresa"%>
<%@page import="com.propath.conexion.SeguridadUtil"%>
<%@page import="com.propath.servicio.BackendCliente"%>
<%
    request.setAttribute("activePage", "ofertas");
    List<OfertaPractica> listaOfertas = BackendCliente.listarOfertas();
    List<Empresa> listaEmpresas = BackendCliente.listarEmpresas();
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
                                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/dashboard" class="text-decoration-none">Inicio</a></li>
                                    <li class="breadcrumb-item active" aria-current="page">Ofertas</li>
                                </ol>
                            </nav>
                            <h1 class="h3 fw-bold text-dark mb-0">Gestión de Ofertas de Prácticas</h1>
                            <p class="text-muted small mb-0">Revisa, modera y valida las convocatorias para prácticas preprofesionales publicadas</p>
                        </div>
                        <div class="d-flex align-items-center gap-2 flex-wrap">
                            <span class="badge bg-teal-subtle border px-3 py-2 rounded-pill fw-semibold" style="background-color: #ccfbf1; color: #0f766e; border-color: #99f6e4 !important;">
                                <i class="bi bi-briefcase-fill me-1"></i> <%= (listaOfertas != null ? listaOfertas.size() : 0) %> Ofertas en plataforma
                            </span>
                            <button type="button" class="btn btn-success rounded-pill px-3 py-2 fw-semibold shadow-sm"
                                    onclick="abrirModalNuevaOfertaAdmin()">
                                <i class="bi bi-plus-circle me-1"></i> Nueva Oferta
                            </button>
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
                                                        title="Ver detalle completo"
                                                        data-bs-toggle="tooltip"
                                                        data-titulo="<%= SeguridadUtil.escaparHtml(ofr.getTitulo()) %>"
                                                        data-empresa="<%= SeguridadUtil.escaparHtml(ofr.getEmpresa()) %>"
                                                        data-carrera="<%= SeguridadUtil.escaparHtml(ofr.getCarrera()) %>"
                                                        data-modalidad="<%= SeguridadUtil.escaparHtml(ofr.getModalidad()) %>"
                                                        data-vacantes="<%= ofr.getVacantes() %>"
                                                        data-fecha="<%= SeguridadUtil.escaparHtml(ofr.getFechaPublicacion()) %>"
                                                        data-fecha-limite="<%= ofr.getFechaLimite() != null ? SeguridadUtil.escaparHtml(ofr.getFechaLimite()) : "" %>"
                                                        data-ubicacion="<%= ofr.getUbicacion() != null ? SeguridadUtil.escaparHtml(ofr.getUbicacion()) : "" %>"
                                                        data-remuneracion="<%= ofr.getRemuneracion() != null ? SeguridadUtil.escaparHtml(ofr.getRemuneracion()) : "" %>"
                                                        data-descripcion="<%= ofr.getDescripcion() != null ? SeguridadUtil.escaparHtml(ofr.getDescripcion()) : "" %>"
                                                        data-requisitos="<%= ofr.getRequisitos() != null ? SeguridadUtil.escaparHtml(ofr.getRequisitos()) : "" %>"
                                                        data-estado="<%= SeguridadUtil.escaparHtml(ofr.getEstado()) %>"
                                                        onclick="verDetalleOfertaBtn(this)">
                                                    <i class="bi bi-eye"></i>
                                                </button>
                                                <button type="button"
                                                        class="btn-action btn-action-edit"
                                                        title="Editar oferta"
                                                        data-bs-toggle="tooltip"
                                                        data-id="<%= ofr.getId() %>"
                                                        data-idempresa="<%= ofr.getIdEmpresa() %>"
                                                        data-titulo="<%= SeguridadUtil.escaparHtml(ofr.getTitulo()) %>"
                                                        data-carrera="<%= SeguridadUtil.escaparHtml(ofr.getCarrera()) %>"
                                                        data-modalidad="<%= SeguridadUtil.escaparHtml(ofr.getModalidad()) %>"
                                                        data-vacantes="<%= ofr.getVacantes() %>"
                                                        data-fecha-limite="<%= ofr.getFechaLimite() != null ? SeguridadUtil.escaparHtml(ofr.getFechaLimite()) : "" %>"
                                                        data-ubicacion="<%= ofr.getUbicacion() != null ? SeguridadUtil.escaparHtml(ofr.getUbicacion()) : "" %>"
                                                        data-remuneracion="<%= ofr.getRemuneracion() != null ? SeguridadUtil.escaparHtml(ofr.getRemuneracion()) : "" %>"
                                                        data-descripcion="<%= ofr.getDescripcion() != null ? SeguridadUtil.escaparHtml(ofr.getDescripcion()) : "" %>"
                                                        data-requisitos="<%= ofr.getRequisitos() != null ? SeguridadUtil.escaparHtml(ofr.getRequisitos()) : "" %>"
                                                        data-estado="<%= SeguridadUtil.escaparHtml(ofr.getEstado()) %>"
                                                        onclick="abrirModalEditarOfertaAdmin(this)">
                                                    <i class="bi bi-pencil-square"></i>
                                                </button>
                                                <button type="button"
                                                        class="btn-action <%= esActiva ? "btn-action-warning" : "btn-action-approve" %>"
                                                        title="<%= esActiva ? "Cerrar oferta" : "Activar oferta" %>"
                                                        data-bs-toggle="tooltip"
                                                        data-id="<%= ofr.getId() %>"
                                                        data-estado="<%= esActiva ? "CERRADA" : "ACTIVA" %>"
                                                        data-titulo="<%= SeguridadUtil.escaparHtml(ofr.getTitulo()) %>"
                                                        onclick="cambiarEstadoOfertaAdmin(this.dataset.id, this.dataset.estado, this.dataset.titulo)">
                                                    <i class="bi <%= esActiva ? "bi-lock-fill" : "bi-check-circle-fill" %>"></i>
                                                </button>
                                                <button type="button"
                                                        class="btn-action btn-action-danger"
                                                        title="Eliminar oferta"
                                                        data-bs-toggle="tooltip"
                                                        data-id="<%= ofr.getId() %>"
                                                        data-titulo="<%= SeguridadUtil.escaparHtml(ofr.getTitulo()) %>"
                                                        onclick="eliminarOfertaAdmin(this.dataset.id, this.dataset.titulo)">
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
                                <div class="detail-value fs-5 fw-bold text-dark" id="modalOfertaPuesto">—</div>
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
                                <div class="detail-label">Fecha Límite de Postulación</div>
                                <div class="detail-value text-danger fw-semibold" id="modalOfertaFechaLimite">—</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Ubicación / Sede</div>
                                <div class="detail-value" id="modalOfertaUbicacion">—</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Subvención Económica</div>
                                <div class="detail-value text-success fw-bold" id="modalOfertaRemuneracion">—</div>
                            </div>
                            <div class="col-12">
                                <div class="detail-label">Estado de Publicación</div>
                                <span id="modalOfertaEstado" class="badge-status badge-activo">—</span>
                            </div>
                            <div class="col-12 pt-2 border-top">
                                <div class="detail-label">Descripción de las Prácticas</div>
                                <div class="detail-value small bg-light p-3 rounded text-secondary" id="modalOfertaDescripcion" style="white-space: pre-wrap;">—</div>
                            </div>
                            <div class="col-12">
                                <div class="detail-label">Requisitos Solicitados</div>
                                <div class="detail-value small bg-light p-3 rounded text-secondary" id="modalOfertaRequisitos" style="white-space: pre-wrap;">—</div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer bg-light border-0">
                        <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cerrar</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="modal fade" id="modalFormOfertaAdmin" tabindex="-1" aria-labelledby="modalFormOfertaAdminLabel" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered modal-lg">
                <div class="modal-content modal-content-propath">
                    <div class="modal-header modal-header-propath">
                        <h2 class="modal-title h5" id="modalFormOfertaAdminLabel">
                            <i class="bi bi-briefcase me-2 text-primary"></i>
                            <span id="modalFormOfertaTituloHdr">Nueva Oferta</span>
                        </h2>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <form id="formOfertaAdmin" onsubmit="event.preventDefault(); guardarOfertaAdmin();">
                        <input type="hidden" id="ofertaAdminId" value="">
                        <div class="modal-body modal-body-propath">
                            <div class="row g-3">
                                <div class="col-md-6" id="grupoEmpresaOfertaAdmin">
                                    <label class="form-label small fw-semibold text-secondary">Empresa Publicadora *</label>
                                    <select class="form-select" id="ofertaAdminEmpresa" required>
                                        <option value="">Selecciona una empresa...</option>
                                        <% if (listaEmpresas != null) {
                                            for (Empresa em : listaEmpresas) { %>
                                        <option value="<%= em.getId() %>"><%= SeguridadUtil.escaparHtml(em.getRazonSocial()) %> (<%= SeguridadUtil.escaparHtml(em.getEstado()) %>)</option>
                                        <% } } %>
                                    </select>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label small fw-semibold text-secondary">Título del Puesto *</label>
                                    <input type="text" class="form-control" id="ofertaAdminTitulo" maxlength="150" required>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label small fw-semibold text-secondary">Carrera Solicitada *</label>
                                    <input type="text" class="form-control" id="ofertaAdminCarrera" maxlength="150" required>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label small fw-semibold text-secondary">Modalidad *</label>
                                    <select class="form-select" id="ofertaAdminModalidad" required>
                                        <option value="PRESENCIAL">Presencial</option>
                                        <option value="HIBRIDA">Híbrida</option>
                                        <option value="VIRTUAL">Virtual / Remoto</option>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label small fw-semibold text-secondary">Vacantes *</label>
                                    <input type="number" class="form-control" id="ofertaAdminVacantes" min="1" max="100" value="1" required>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label small fw-semibold text-secondary">Fecha Límite *</label>
                                    <input type="date" class="form-control" id="ofertaAdminFechaLimite" required>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label small fw-semibold text-secondary">Ubicación / Sede</label>
                                    <input type="text" class="form-control" id="ofertaAdminUbicacion" placeholder="Ej. Miraflores, Lima">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label small fw-semibold text-secondary">Subvención (S/)</label>
                                    <input type="text" class="form-control" id="ofertaAdminRemuneracion" placeholder="Ej. 1200">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label small fw-semibold text-secondary">Estado</label>
                                    <select class="form-select" id="ofertaAdminEstado">
                                        <option value="ACTIVA">ACTIVA</option>
                                        <option value="CERRADA">CERRADA</option>
                                        <option value="PENDIENTE">PENDIENTE</option>
                                        <option value="RECHAZADA">RECHAZADA</option>
                                    </select>
                                </div>
                                <div class="col-12">
                                    <label class="form-label small fw-semibold text-secondary">Descripción</label>
                                    <textarea class="form-control" id="ofertaAdminDescripcion" rows="2"></textarea>
                                </div>
                                <div class="col-12">
                                    <label class="form-label small fw-semibold text-secondary">Requisitos</label>
                                    <textarea class="form-control" id="ofertaAdminRequisitos" rows="2"></textarea>
                                </div>
                            </div>
                        </div>
                        <div class="modal-footer bg-light border-0">
                            <button type="button" class="btn btn-outline-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                            <button type="submit" class="btn btn-primary rounded-pill px-4" id="btnGuardarOfertaAdmin">
                                <i class="bi bi-check2 me-1"></i> Guardar Oferta
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <script>
            function abrirModalNuevaOfertaAdmin() {
                document.getElementById('formOfertaAdmin').reset();
                document.getElementById('ofertaAdminId').value = '';
                document.getElementById('modalFormOfertaTituloHdr').textContent = 'Publicar Nueva Oferta';
                document.getElementById('grupoEmpresaOfertaAdmin').classList.remove('d-none');
                document.getElementById('ofertaAdminEmpresa').required = true;
                document.getElementById('ofertaAdminEstado').value = 'ACTIVA';
                const modalEl = document.getElementById('modalFormOfertaAdmin');
                const ModalClass = window.BootstrapModalCompat || (window.bootstrap && window.bootstrap.Modal);
                if (ModalClass) {
                    const inst = ModalClass.getOrCreateInstance(modalEl);
                    if (inst) inst.show();
                }
            }

            function abrirModalEditarOfertaAdmin(btn) {
                document.getElementById('ofertaAdminId').value = btn.getAttribute('data-id') || '';
                document.getElementById('modalFormOfertaTituloHdr').textContent = 'Editar Oferta #' + (btn.getAttribute('data-id') || '');
                document.getElementById('grupoEmpresaOfertaAdmin').classList.add('d-none');
                document.getElementById('ofertaAdminEmpresa').required = false;
                document.getElementById('ofertaAdminEmpresa').value = btn.getAttribute('data-idempresa') || '';
                document.getElementById('ofertaAdminTitulo').value = btn.getAttribute('data-titulo') || '';
                document.getElementById('ofertaAdminCarrera').value = btn.getAttribute('data-carrera') || '';
                const modRaw = (btn.getAttribute('data-modalidad') || 'PRESENCIAL').toUpperCase();
                document.getElementById('ofertaAdminModalidad').value = modRaw.includes('HIBRID') ? 'HIBRIDA' : (modRaw.includes('VIRTUAL') || modRaw.includes('REMOT') ? 'VIRTUAL' : 'PRESENCIAL');
                document.getElementById('ofertaAdminVacantes').value = btn.getAttribute('data-vacantes') || '1';
                document.getElementById('ofertaAdminFechaLimite').value = btn.getAttribute('data-fecha-limite') || '';
                document.getElementById('ofertaAdminUbicacion').value = btn.getAttribute('data-ubicacion') || '';
                document.getElementById('ofertaAdminRemuneracion').value = btn.getAttribute('data-remuneracion') || '';
                document.getElementById('ofertaAdminDescripcion').value = btn.getAttribute('data-descripcion') || '';
                document.getElementById('ofertaAdminRequisitos').value = btn.getAttribute('data-requisitos') || '';
                document.getElementById('ofertaAdminEstado').value = (btn.getAttribute('data-estado') || 'ACTIVA').toUpperCase();
                const modalEl = document.getElementById('modalFormOfertaAdmin');
                const ModalClass = window.BootstrapModalCompat || (window.bootstrap && window.bootstrap.Modal);
                if (ModalClass) {
                    const inst = ModalClass.getOrCreateInstance(modalEl);
                    if (inst) inst.show();
                }
            }

            async function guardarOfertaAdmin() {
                const btn = document.getElementById('btnGuardarOfertaAdmin');
                btn.disabled = true;
                const id = document.getElementById('ofertaAdminId').value.trim();
                const params = {
                    accion: id ? 'actualizarOferta' : 'crearOferta',
                    titulo: document.getElementById('ofertaAdminTitulo').value.trim(),
                    carrera: document.getElementById('ofertaAdminCarrera').value.trim(),
                    modalidad: document.getElementById('ofertaAdminModalidad').value,
                    vacantes: document.getElementById('ofertaAdminVacantes').value,
                    fechaLimite: document.getElementById('ofertaAdminFechaLimite').value,
                    ubicacion: document.getElementById('ofertaAdminUbicacion').value.trim(),
                    remuneracion: document.getElementById('ofertaAdminRemuneracion').value.trim(),
                    descripcion: document.getElementById('ofertaAdminDescripcion').value.trim(),
                    requisitos: document.getElementById('ofertaAdminRequisitos').value.trim(),
                    estado: document.getElementById('ofertaAdminEstado').value
                };
                if (id) params.id = id;
                else params.idEmpresa = document.getElementById('ofertaAdminEmpresa').value;

                try {
                    const res = await fetch('${pageContext.request.contextPath}/admin/accion', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                        body: new URLSearchParams(params).toString()
                    });
                    const data = await res.json();
                    const modalEl = document.getElementById('modalFormOfertaAdmin');
                    const ModalClass = window.BootstrapModalCompat || (window.bootstrap && window.bootstrap.Modal);
                    if (ModalClass && data.exito) {
                        const inst = ModalClass.getInstance(modalEl);
                        if (inst) inst.hide();
                    }
                    if (data.exito) {
                        mostrarModalMensaje('Oferta Guardada', data.mensaje || 'Operación exitosa.', 'success', () => window.location.reload());
                    } else {
                        mostrarModalMensaje('Error', data.mensaje || 'No se pudo guardar la oferta.', 'danger');
                        btn.disabled = false;
                    }
                } catch (err) {
                    mostrarModalMensaje('Error de Conexión', 'No se pudo conectar: ' + err.message, 'danger');
                    btn.disabled = false;
                }
            }

            function cambiarEstadoOfertaAdmin(id, estado, titulo) {
                const verbo = estado === 'ACTIVA' ? 'activar' : 'cerrar';
                mostrarModalConfirmacion('Confirmar cambio de estado',
                    '¿Deseas ' + verbo + ' la oferta "' + titulo + '"?',
                    async function () {
                        try {
                            const res = await fetch('${pageContext.request.contextPath}/admin/accion', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                                body: new URLSearchParams({ accion: 'cambiarEstadoOferta', id: id, estado: estado }).toString()
                            });
                            const data = await res.json();
                            if (data.exito) mostrarModalMensaje('Estado Actualizado', data.mensaje, 'success', () => window.location.reload());
                            else mostrarModalMensaje('Error', data.mensaje || 'No se pudo cambiar el estado.', 'danger');
                        } catch (err) {
                            mostrarModalMensaje('Error de Conexión', err.message, 'danger');
                        }
                    });
            }

            function eliminarOfertaAdmin(id, titulo) {
                mostrarModalConfirmacion('Eliminar Oferta',
                    '¿Deseas eliminar la oferta "' + titulo + '"? Si ya tiene postulaciones registradas, se cerrará para preservar el historial.',
                    async function () {
                        try {
                            const res = await fetch('${pageContext.request.contextPath}/admin/accion', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                                body: new URLSearchParams({ accion: 'eliminar', recurso: 'ofertas', id: id }).toString()
                            });
                            const data = await res.json();
                            if (data.exito) mostrarModalMensaje('Oferta Procesada', data.mensaje, 'success', () => window.location.reload());
                            else mostrarModalMensaje('Error', data.mensaje || 'No se pudo eliminar la oferta.', 'danger');
                        } catch (err) {
                            mostrarModalMensaje('Error de Conexión', err.message, 'danger');
                        }
                    });
            }

            window.abrirModalNuevaOfertaAdmin = abrirModalNuevaOfertaAdmin;
            window.abrirModalEditarOfertaAdmin = abrirModalEditarOfertaAdmin;
            window.guardarOfertaAdmin = guardarOfertaAdmin;
            window.cambiarEstadoOfertaAdmin = cambiarEstadoOfertaAdmin;
            window.eliminarOfertaAdmin = eliminarOfertaAdmin;
        </script>
        <script type="module" src="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/js/bootstrap.bundle.min.js"></script>
        <script type="module" src="${pageContext.request.contextPath}/js/script.js"></script>
    </body>
</html>
