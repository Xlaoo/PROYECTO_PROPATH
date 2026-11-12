<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.propath.modelo.Administrador"%>
<%@page import="com.propath.modelo.Usuario"%>
<%@page import="com.propath.util.HtmlUtil"%>
<%
    Usuario uAdminLogueado = (Usuario) session.getAttribute("usuarioLogueado");
    if (uAdminLogueado == null || !"ADMIN".equalsIgnoreCase(uAdminLogueado.getRol()) || !uAdminLogueado.esAdminPrincipal()) {
        response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=acceso_denegado");
        return;
    }

    if (request.getAttribute("listaAdministradores") == null) {
        response.sendRedirect(request.getContextPath() + "/admin/administradores");
        return;
    }

    @SuppressWarnings("unchecked")
    List<Administrador> listaAdministradores = (List<Administrador>) request.getAttribute("listaAdministradores");
    String busqueda = (String) request.getAttribute("busqueda");
    if (busqueda == null) busqueda = "";
    int totalAdmins = listaAdministradores != null ? listaAdministradores.size() : 0;
%>
<!DOCTYPE html>
<html lang="es" data-bs-theme="light">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Gestión de Administradores | ProPath</title>

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
                    <!-- Cabecera de Página -->
                    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
                        <div>
                            <nav aria-label="breadcrumb">
                                <ol class="breadcrumb mb-1 small">
                                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/dashboard" class="text-decoration-none">Inicio</a></li>
                                    <li class="breadcrumb-item active" aria-current="page">Administradores</li>
                                </ol>
                            </nav>
                            <h1 class="h3 fw-bold text-dark mb-0">Gestión de Administradores</h1>
                            <p class="text-muted small mb-0">Supervisa, registra y administra las cuentas con rol administrativo del sistema</p>
                        </div>
                        <div class="d-flex align-items-center gap-2 flex-wrap">
                            <button type="button" 
                                    class="btn btn-primary rounded-pill px-3 py-2 fw-semibold shadow-sm"
                                    data-bs-toggle="modal" 
                                    data-bs-target="#modalNuevoAdministrador">
                                <i class="bi bi-person-plus-fill me-1"></i> Nuevo administrador
                            </button>
                            <a href="${pageContext.request.contextPath}/admin/administradores/dashboard" 
                               class="btn btn-outline-primary rounded-pill px-3 py-2 fw-semibold shadow-sm">
                                <i class="bi bi-bar-chart-line-fill me-1"></i> Ver dashboard
                            </a>
                            <a href="${pageContext.request.contextPath}/admin/administradores" 
                               class="btn btn-outline-secondary rounded-pill px-3 py-2 fw-semibold shadow-sm">
                                <i class="bi bi-arrow-clockwise me-1"></i> Actualizar
                            </a>
                            <span class="badge bg-primary-subtle text-primary border border-primary-subtle px-3 py-2 rounded-pill fw-semibold">
                                <i class="bi bi-shield-lock-fill me-1"></i> <%= totalAdmins %> Administradores registrados
                            </span>
                        </div>
                    </div>

                    <!-- Tarjeta del Listado -->
                    <div class="propath-card">
                        <div class="propath-card-header">
                            <h2 class="propath-card-title">
                                <i class="bi bi-shield-lock-fill text-primary"></i>
                                <span>Listado de Administradores del Sistema</span>
                            </h2>
                            <div class="search-box-custom">
                                <i class="bi bi-search search-icon"></i>
                                <input type="text" 
                                       class="form-control table-search-input" 
                                       data-table="tablaAdministradores" 
                                       id="inputBuscarAdmin"
                                       placeholder="Buscar por nombre o correo..."
                                       value="<%= HtmlUtil.escape(busqueda) %>">
                            </div>
                        </div>

                        <div class="table-responsive">
                            <table class="table table-propath" id="tablaAdministradores">
                                <thead>
                                    <tr>
                                        <th>Nombre Completo</th>
                                        <th>Correo Electrónico</th>
                                        <th>Cargo</th>
                                        <th>Estado</th>
                                        <th class="text-center">Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% if (listaAdministradores == null || listaAdministradores.isEmpty()) { %>
                                    <tr>
                                        <td colspan="5" class="text-center py-5 text-muted">
                                            <i class="bi bi-inbox fs-2 d-block mb-2 text-secondary"></i>
                                            <span class="fw-medium">No se encontraron administradores que coincidan con la búsqueda</span>
                                        </td>
                                    </tr>
                                    <% } else {
                                        for (Administrador adm : listaAdministradores) {
                                            String estado = adm.getEstado() != null ? adm.getEstado().toUpperCase() : "ACTIVO";
                                            boolean esActivo = "ACTIVO".equals(estado);
                                            boolean esInactivo = "INACTIVO".equals(estado);

                                            boolean esPrincipal = (adm.getIdUsuario() == 60 || 
                                                (adm.getUsuario() != null && adm.getUsuario().esAdminPrincipal()));
                                            boolean esCuentaSesion = (adm.getIdUsuario() == uAdminLogueado.getId());

                                            String inicialNombre = (adm.getNombre() != null && !adm.getNombre().isEmpty()) ? adm.getNombre().substring(0, 1).toUpperCase() : "A";
                                            String inicialApellido = (adm.getApellidos() != null && !adm.getApellidos().isEmpty()) ? adm.getApellidos().substring(0, 1).toUpperCase() : "";
                                    %>
                                    <tr data-search-target>
                                        <td>
                                            <div class="d-flex align-items-center gap-2">
                                                <div class="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white shadow-sm <%= esActivo ? "avatar-bg-activo" : "avatar-bg-inactivo" %>"
                                                     style="width: 36px; height: 36px; font-size: 0.82rem;">
                                                    <%= inicialNombre %><%= inicialApellido %>
                                                </div>
                                                <div>
                                                    <span class="fw-semibold text-dark search-field"><%= HtmlUtil.escape(adm.getNombreCompleto()) %></span>
                                                    <% if (esPrincipal) { %>
                                                        <span class="badge bg-warning-subtle text-warning border border-warning-subtle rounded-pill ms-1" style="font-size: 0.68rem;">
                                                            Principal
                                                        </span>
                                                    <% } %>
                                                    <% if (esCuentaSesion) { %>
                                                        <span class="badge bg-primary-subtle text-primary border border-primary-subtle rounded-pill ms-1" style="font-size: 0.68rem;">
                                                            Tu cuenta
                                                        </span>
                                                    <% } %>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <a href="mailto:<%= HtmlUtil.escape(adm.getCorreo()) %>" class="text-muted text-decoration-none small search-field">
                                                <i class="bi bi-envelope me-1"></i><%= HtmlUtil.escape(adm.getCorreo()) %>
                                            </a>
                                        </td>
                                        <td>
                                            <span class="badge bg-light text-dark border px-2 py-1 fw-medium">
                                                <i class="bi bi-briefcase me-1 text-primary"></i><%= HtmlUtil.escape(adm.getCargo() != null ? adm.getCargo() : "Administrador General") %>
                                            </span>
                                        </td>
                                        <td>
                                            <span class="badge-status <%= esActivo ? "badge-activo" : "badge-bloqueado" %>">
                                                <i class="bi <%= esActivo ? "bi-check-circle-fill" : "bi-slash-circle-fill" %>"></i>
                                                <%= estado %>
                                            </span>
                                        </td>
                                        <td class="text-center">
                                            <div class="action-btn-group justify-content-center">
                                                <!-- Botón Ver Detalle -->
                                                <button type="button" 
                                                        class="btn-action btn-action-view" 
                                                        title="Ver detalle"
                                                        data-bs-toggle="tooltip"
                                                        data-id="<%= adm.getId() %>"
                                                        data-nombre="<%= HtmlUtil.escape(adm.getNombreCompleto()) %>"
                                                        data-correo="<%= HtmlUtil.escape(adm.getCorreo()) %>"
                                                        data-cargo="<%= HtmlUtil.escape(adm.getCargo() != null ? adm.getCargo() : "Administrador General") %>"
                                                        data-estado="<%= HtmlUtil.escape(estado) %>"
                                                        data-fecha="<%= HtmlUtil.escape(adm.getFechaRegistro()) %>"
                                                        onclick="mostrarFichaAdministrador(this)">
                                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16"
                                                         viewBox="0 0 24 24" fill="none"
                                                         stroke="currentColor" stroke-width="2"
                                                         stroke-linecap="round" stroke-linejoin="round"
                                                         aria-hidden="true">
                                                        <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z"/>
                                                        <circle cx="12" cy="12" r="3"/>
                                                    </svg>
                                                </button>

                                                <!-- Botón Editar (Lápiz) -->
                                                <button type="button" 
                                                        class="btn-action btn-action-edit" 
                                                        title="Editar administrador"
                                                        data-bs-toggle="tooltip"
                                                        data-id="<%= adm.getId() %>"
                                                        data-nombre="<%= HtmlUtil.escape(adm.getNombre()) %>"
                                                        data-apellidos="<%= HtmlUtil.escape(adm.getApellidos()) %>"
                                                        data-correo="<%= HtmlUtil.escape(adm.getCorreo()) %>"
                                                        data-cargo="<%= HtmlUtil.escape(adm.getCargo() != null ? adm.getCargo() : "Administrador General") %>"
                                                        data-estado="<%= HtmlUtil.escape(estado) %>"
                                                        onclick="abrirModalEditarAdministrador(this)">
                                                    <i class="bi bi-pencil-square"></i>
                                                </button>

                                                <!-- Botón Dar de Baja (con confirmación y protección) -->
                                                <% if (esPrincipal) { %>
                                                    <button type="button" 
                                                            class="btn-action btn-action-disabled text-muted opacity-50" 
                                                            title="El administrador principal del sistema está protegido"
                                                            data-bs-toggle="tooltip"
                                                            disabled>
                                                        <i class="bi bi-trash"></i>
                                                    </button>
                                                <% } else if (esCuentaSesion) { %>
                                                    <button type="button" 
                                                            class="btn-action btn-action-disabled text-muted opacity-50" 
                                                            title="No puedes dar de baja tu propia cuenta en sesión"
                                                            data-bs-toggle="tooltip"
                                                            disabled>
                                                        <i class="bi bi-trash"></i>
                                                    </button>
                                                <% } else if (esInactivo) { %>
                                                    <button type="button" 
                                                            class="btn-action btn-action-disabled text-muted opacity-50" 
                                                            title="La cuenta ya se encuentra dada de baja (inactiva)"
                                                            data-bs-toggle="tooltip"
                                                            disabled>
                                                        <i class="bi bi-slash-circle"></i>
                                                    </button>
                                                <% } else { %>
                                                    <button type="button" 
                                                            class="btn-action btn-action-danger" 
                                                            title="Dar de baja administrador"
                                                            data-bs-toggle="tooltip"
                                                            data-id="<%= adm.getId() %>"
                                                            data-nombre="<%= HtmlUtil.escape(adm.getNombreCompleto()) %>"
                                                            onclick="confirmarDarDeBajaAdministrador(this.dataset.id, this.dataset.nombre)">
                                                        <i class="bi bi-trash"></i>
                                                    </button>
                                                <% } %>
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

        <!-- ================================================================= -->
        <!-- MODAL 1: NUEVO ADMINISTRADOR -->
        <!-- ================================================================= -->
        <div class="modal fade" id="modalNuevoAdministrador" tabindex="-1" aria-labelledby="modalNuevoAdminLabel" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content modal-content-propath">
                    <div class="modal-header modal-header-propath">
                        <h2 class="modal-title h5" id="modalNuevoAdminLabel">
                            <i class="bi bi-person-plus-fill me-2 text-primary"></i> Registrar Nuevo Administrador
                        </h2>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <form id="formNuevoAdministrador" onsubmit="event.preventDefault(); guardarNuevoAdministrador();">
                        <div class="modal-body modal-body-propath">
                            <div class="row g-3">
                                <div class="col-sm-6">
                                    <label class="form-label small fw-semibold text-secondary">Nombres *</label>
                                    <input type="text" class="form-control" id="nuevoAdminNombre" required placeholder="Ej. Carlos">
                                </div>
                                <div class="col-sm-6">
                                    <label class="form-label small fw-semibold text-secondary">Apellidos *</label>
                                    <input type="text" class="form-control" id="nuevoAdminApellidos" required placeholder="Ej. Mendoza">
                                </div>
                                <div class="col-12">
                                    <label class="form-label small fw-semibold text-secondary">Correo Electrónico *</label>
                                    <input type="email" class="form-control" id="nuevoAdminCorreo" required placeholder="admin.nuevo@propath.com">
                                    <div class="form-text small">Debe ser un correo único que no esté registrado en el sistema.</div>
                                </div>
                                <div class="col-12">
                                    <label class="form-label small fw-semibold text-secondary">Cargo o Función *</label>
                                    <input type="text" class="form-control" id="nuevoAdminCargo" required placeholder="Ej. Coordinador de Prácticas" value="Administrador General">
                                </div>
                                <div class="col-sm-6">
                                    <label class="form-label small fw-semibold text-secondary">Contraseña Inicial *</label>
                                    <input type="password" class="form-control" id="nuevoAdminContrasena" required minlength="8" placeholder="Mínimo 8 caracteres">
                                </div>
                                <div class="col-sm-6">
                                    <label class="form-label small fw-semibold text-secondary">Confirmar Contraseña *</label>
                                    <input type="password" class="form-control" id="nuevoAdminConfirmar" required minlength="8" placeholder="Repite la contraseña">
                                </div>
                                <div class="col-12">
                                    <div class="alert alert-info py-2 px-3 small border-0 rounded-3 mb-0">
                                        <i class="bi bi-info-circle me-1"></i> El rol asignado será <strong>ADMIN</strong> en estado <strong>ACTIVO</strong>. La contraseña se guardará cifrada con BCrypt en una sola transacción JDBC.
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="modal-footer bg-light border-0">
                            <button type="button" class="btn btn-outline-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                            <button type="submit" class="btn btn-primary rounded-pill px-4" id="btnGuardarNuevoAdmin">
                                <i class="bi bi-check2 me-1"></i> Registrar Administrador
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <!-- ================================================================= -->
        <!-- MODAL 2: FICHA DE DETALLE DEL ADMINISTRADOR (SIN PASSWORD/HASH) -->
        <!-- ================================================================= -->
        <div class="modal fade" id="modalDetalleAdministrador" tabindex="-1" aria-labelledby="modalDetalleAdminLabel" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content modal-content-propath">
                    <div class="modal-header modal-header-propath">
                        <h2 class="modal-title h5" id="modalDetalleAdminLabel">
                            <i class="bi bi-shield-check me-2 text-primary"></i>
                            <span>Ficha del Administrador</span>
                        </h2>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body modal-body-propath">
                        <div class="row g-3">
                            <div class="col-12">
                                <div class="detail-label">Nombre Completo</div>
                                <div class="detail-value fs-5 fw-bold text-dark" id="modalAdminNombreCompleto">—</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Correo Electrónico</div>
                                <div class="detail-value" id="modalAdminCorreo">—</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Cargo Asignado</div>
                                <div class="detail-value fw-semibold text-primary" id="modalAdminCargo">—</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Rol del Sistema</div>
                                <div class="detail-value">
                                    <span class="badge bg-primary-subtle text-primary border border-primary-subtle px-2 py-1">ADMIN</span>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Fecha de Registro</div>
                                <div class="detail-value text-muted" id="modalAdminFechaRegistro">—</div>
                            </div>
                            <div class="col-12 pt-2 border-top">
                                <div class="detail-label">Estado de la Cuenta</div>
                                <span id="modalAdminEstado" class="badge-status badge-activo">—</span>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer bg-light border-0">
                        <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cerrar</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- ================================================================= -->
        <!-- MODAL 3: EDITAR ADMINISTRADOR (NOMBRE, APELLIDOS Y CARGO) -->
        <!-- ================================================================= -->
        <div class="modal fade" id="modalEditarAdministrador" tabindex="-1" aria-labelledby="modalEditarAdminLabel" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content modal-content-propath">
                    <div class="modal-header modal-header-propath">
                        <h2 class="modal-title h5" id="modalEditarAdminLabel">
                            <i class="bi bi-pencil-square me-2 text-primary"></i> Editar Administrador
                        </h2>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <form id="formEditarAdministrador" onsubmit="event.preventDefault(); guardarEditarAdministrador();">
                        <input type="hidden" id="editAdminId">
                        <div class="modal-body modal-body-propath">
                            <div class="row g-3">
                                <div class="col-sm-6">
                                    <label class="form-label small fw-semibold text-secondary">Nombres *</label>
                                    <input type="text" class="form-control" id="editAdminNombre" required>
                                </div>
                                <div class="col-sm-6">
                                    <label class="form-label small fw-semibold text-secondary">Apellidos *</label>
                                    <input type="text" class="form-control" id="editAdminApellidos" required>
                                </div>
                                <div class="col-12">
                                    <label class="form-label small fw-semibold text-secondary">Cargo *</label>
                                    <input type="text" class="form-control" id="editAdminCargo" required>
                                </div>
                                <div class="col-12">
                                    <label class="form-label small fw-semibold text-secondary">Correo Electrónico (Solo Lectura)</label>
                                    <input type="email" class="form-control bg-light" id="editAdminCorreo" readonly disabled>
                                    <div class="form-text small text-muted">
                                        <i class="bi bi-info-circle me-1"></i> El cambio de correo y contraseña se gestiona a través del flujo de seguridad en <strong>Mi Cuenta</strong>.
                                    </div>
                                </div>
                                <div class="col-12">
                                    <label class="form-label small fw-semibold text-secondary">Estado de Cuenta</label>
                                    <input type="text" class="form-control bg-light fw-semibold text-secondary" id="editAdminEstado" readonly disabled>
                                </div>
                            </div>
                        </div>
                        <div class="modal-footer bg-light border-0">
                            <button type="button" class="btn btn-outline-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                            <button type="submit" class="btn btn-primary rounded-pill px-4" id="btnGuardarEditarAdmin">
                                <i class="bi bi-check2 me-1"></i> Guardar Cambios
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <script>
            // Búsqueda en tiempo real por nombre o correo
            document.addEventListener('DOMContentLoaded', function () {
                const searchInput = document.getElementById('inputBuscarAdmin');
                if (searchInput) {
                    searchInput.addEventListener('input', function () {
                        const term = this.value.toLowerCase().trim();
                        const rows = document.querySelectorAll('#tablaAdministradores tbody tr[data-search-target]');
                        let visibles = 0;
                        rows.forEach(function (row) {
                            const fields = row.querySelectorAll('.search-field');
                            let match = false;
                            fields.forEach(function (f) {
                                if (f.textContent.toLowerCase().includes(term)) {
                                    match = true;
                                }
                            });
                            if (match) {
                                row.style.display = '';
                                visibles++;
                            } else {
                                row.style.display = 'none';
                            }
                        });
                    });
                }
            });

            // Guardar nuevo administrador
            async function guardarNuevoAdministrador() {
                const btn = document.getElementById('btnGuardarNuevoAdmin');
                btn.disabled = true;

                const pass = document.getElementById('nuevoAdminContrasena').value.trim();
                const confPass = document.getElementById('nuevoAdminConfirmar').value.trim();

                if (pass.length < 8) {
                    mostrarModalMensaje('Seguridad', 'La contraseña inicial debe tener al menos 8 caracteres.', 'warning');
                    btn.disabled = false;
                    return;
                }

                if (!/^(?=.*[A-Za-z])(?=.*\d).{8,}$/.test(pass)) {
                    mostrarModalMensaje('Seguridad', 'La contraseña debe combinar letras y números.', 'warning');
                    btn.disabled = false;
                    return;
                }

                if (pass !== confPass) {
                    mostrarModalMensaje('Seguridad', 'Las contraseñas no coinciden.', 'warning');
                    btn.disabled = false;
                    return;
                }

                const body = new URLSearchParams({
                    accion: 'crearAdministrador',
                    nombre: document.getElementById('nuevoAdminNombre').value.trim(),
                    apellidos: document.getElementById('nuevoAdminApellidos').value.trim(),
                    correo: document.getElementById('nuevoAdminCorreo').value.trim(),
                    cargo: document.getElementById('nuevoAdminCargo').value.trim(),
                    contrasena: pass,
                    confirmarContrasena: confPass
                });

                try {
                    const res = await fetch('${pageContext.request.contextPath}/admin/accion', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                        body: body.toString()
                    });
                    const data = await res.json();
                    if (data.exito) {
                        cerrarModal('modalNuevoAdministrador');
                        mostrarModalMensaje('Administrador Creado', data.mensaje || 'Administrador registrado con éxito.', 'success', () => window.location.reload());
                    } else {
                        mostrarModalMensaje('Error al Registrar', data.mensaje || 'No se pudo crear el administrador.', 'danger');
                        btn.disabled = false;
                    }
                } catch (err) {
                    mostrarModalMensaje('Error de Conexión', 'No se pudo comunicar con el servidor: ' + err.message, 'danger');
                    btn.disabled = false;
                }
            }

            // Ver detalle de administrador
            function mostrarFichaAdministrador(btn) {
                const nombre = btn.getAttribute('data-nombre');
                const correo = btn.getAttribute('data-correo');
                const cargo = btn.getAttribute('data-cargo');
                const estado = (btn.getAttribute('data-estado') || '').trim().toUpperCase();
                const fecha = btn.getAttribute('data-fecha');

                document.getElementById('modalAdminNombreCompleto').textContent = nombre || '—';
                document.getElementById('modalAdminCorreo').textContent = correo || '—';
                document.getElementById('modalAdminCargo').textContent = cargo || 'Administrador General';
                document.getElementById('modalAdminFechaRegistro').textContent = fecha || 'Registrado';

                const badge = document.getElementById('modalAdminEstado');
                badge.textContent = estado || 'ACTIVO';
                badge.className = 'badge-status ' + (estado === 'ACTIVO' ? 'badge-activo' : 'badge-bloqueado');

                abrirModal('modalDetalleAdministrador');
            }

            // Abrir modal de edición
            function abrirModalEditarAdministrador(btn) {
                document.getElementById('editAdminId').value = btn.getAttribute('data-id') || '';
                document.getElementById('editAdminNombre').value = btn.getAttribute('data-nombre') || '';
                document.getElementById('editAdminApellidos').value = btn.getAttribute('data-apellidos') || '';
                document.getElementById('editAdminCorreo').value = btn.getAttribute('data-correo') || '';
                document.getElementById('editAdminCargo').value = btn.getAttribute('data-cargo') || 'Administrador General';
                document.getElementById('editAdminEstado').value = btn.getAttribute('data-estado') || 'ACTIVO';

                abrirModal('modalEditarAdministrador');
            }

            // Guardar cambios de edición
            async function guardarEditarAdministrador() {
                const btn = document.getElementById('btnGuardarEditarAdmin');
                btn.disabled = true;

                const body = new URLSearchParams({
                    accion: 'actualizarAdministrador',
                    id: document.getElementById('editAdminId').value,
                    nombre: document.getElementById('editAdminNombre').value.trim(),
                    apellidos: document.getElementById('editAdminApellidos').value.trim(),
                    cargo: document.getElementById('editAdminCargo').value.trim()
                });

                try {
                    const res = await fetch('${pageContext.request.contextPath}/admin/accion', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                        body: body.toString()
                    });
                    const data = await res.json();
                    if (data.exito) {
                        cerrarModal('modalEditarAdministrador');
                        mostrarModalMensaje('Administrador Actualizado', data.mensaje || 'Datos actualizados con éxito.', 'success', () => window.location.reload());
                    } else {
                        mostrarModalMensaje('Error al Actualizar', data.mensaje || 'No se pudo actualizar el administrador.', 'danger');
                        btn.disabled = false;
                    }
                } catch (err) {
                    mostrarModalMensaje('Error de Conexión', 'No se pudo comunicar con el servidor: ' + err.message, 'danger');
                    btn.disabled = false;
                }
            }

            // Confirmar y procesar baja lógica de administrador
            function confirmarDarDeBajaAdministrador(id, nombre) {
                mostrarModalConfirmacion(
                    'Dar de Baja Administrador',
                    '¿Estás seguro de dar de baja al administrador "' + nombre + '"? La cuenta pasará a estado INACTIVO y perderá inmediatamente el acceso al panel administrativo, conservando su registro y perfil en el sistema.',
                    async function () {
                        const body = new URLSearchParams({
                            accion: 'darBajaAdministrador',
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
                                mostrarModalMensaje('Operación Exitosa', data.mensaje || 'Administrador dado de baja con éxito.', 'success', () => window.location.reload());
                            } else {
                                mostrarModalMensaje('No se pudo procesar', data.mensaje || 'Error al procesar la baja del administrador.', 'danger');
                            }
                        } catch (err) {
                            mostrarModalMensaje('Error de Conexión', 'No se pudo comunicar con el servidor: ' + err.message, 'danger');
                        }
                    }
                );
            }

            function abrirModal(id) {
                const el = document.getElementById(id);
                const ModalClass = window.BootstrapModalCompat || (window.bootstrap && window.bootstrap.Modal);
                if (ModalClass && el) {
                    const inst = ModalClass.getOrCreateInstance(el);
                    if (inst) inst.show();
                }
            }

            function cerrarModal(id) {
                const el = document.getElementById(id);
                const ModalClass = window.BootstrapModalCompat || (window.bootstrap && window.bootstrap.Modal);
                if (ModalClass && el) {
                    const inst = ModalClass.getInstance(el);
                    if (inst) inst.hide();
                }
            }

            window.guardarNuevoAdministrador = guardarNuevoAdministrador;
            window.mostrarFichaAdministrador = mostrarFichaAdministrador;
            window.abrirModalEditarAdministrador = abrirModalEditarAdministrador;
            window.guardarEditarAdministrador = guardarEditarAdministrador;
            window.confirmarDarDeBajaAdministrador = confirmarDarDeBajaAdministrador;
        </script>
        <script type="module" src="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/js/bootstrap.bundle.min.js"></script>
        <script type="module" src="${pageContext.request.contextPath}/js/script.js"></script>
    </body>
</html>

