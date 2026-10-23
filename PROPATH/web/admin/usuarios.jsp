<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@page import="java.util.List" %>
        <%@page import="com.propath.modelo.Usuario" %>
            <%@page import="com.propath.modelo.Estudiante" %>
                <%@page import="com.propath.modelo.Empresa" %>
                    <%@page import="com.propath.servicio.BackendCliente" %>
                        <% request.setAttribute("activePage", "usuarios" ); List<Usuario> listaUsuarios =
                            BackendCliente.listarUsuarios();
                            List<Estudiante> listaEstudiantes = BackendCliente.listarEstudiantes();
                                List<Empresa> listaEmpresas = BackendCliente.listarEmpresas();
                                    %>
                                    <!DOCTYPE html>
                                    <html lang="es" data-bs-theme="light">

                                    <head>
                                        <meta charset="UTF-8">
                                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                        <title>Gestión de Usuarios | ProPath</title>
                                        <link rel="preconnect" href="https://fonts.googleapis.com">
                                        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                                        <link
                                            href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
                                            rel="stylesheet">
                                        <link
                                            href="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/css/bootstrap.min.css"
                                            rel="stylesheet">
                                        <link rel="stylesheet"
                                            href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
                                        <link rel="stylesheet"
                                            href="${pageContext.request.contextPath}/css/estilos.css">
                                    </head>

                                    <body>
                                        <div class="admin-layout">
                                            <jsp:include page="/admin/includes/sidebar.jsp" />
                                            <main class="admin-main">
                                                <jsp:include page="/admin/includes/topbar.jsp" />
                                                <div class="admin-content">
                                                    <div
                                                        class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
                                                        <div>
                                                            <nav aria-label="breadcrumb">
                                                                <ol class="breadcrumb mb-1 small">
                                                                    <li class="breadcrumb-item"><a
                                                                            href="${pageContext.request.contextPath}/admin/dashboard.jsp"
                                                                            class="text-decoration-none">Inicio</a></li>
                                                                    <li class="breadcrumb-item active"
                                                                        aria-current="page">Usuarios</li>
                                                                </ol>
                                                            </nav>
                                                            <h1 class="h3 fw-bold text-dark mb-0">Gestión de Usuarios
                                                            </h1>
                                                            <p class="text-muted small mb-0">Supervisión y consulta de
                                                                cuentas registradas</p>
                                                        </div>
                                                        <div class="d-flex align-items-center gap-2">
                                                            <a href="${pageContext.request.contextPath}/admin/usuarios.jsp"
                                                                class="btn btn-primary rounded-pill px-3 py-2 fw-semibold d-flex align-items-center gap-2 shadow-sm"
                                                                title="Consultar y refrescar lista desde MariaDB">
                                                                <i class="bi bi-arrow-clockwise fs-6"></i> Actualizar
                                                            </a>
                                                            <span
                                                                class="badge bg-primary-subtle text-primary border border-primary-subtle px-3 py-2 rounded-pill fw-semibold">
                                                                <i class="bi bi-people-fill me-1"></i>
                                                                <%= (listaUsuarios !=null ? listaUsuarios.size() : 0) %>
                                                                    Usuarios en base de datos
                                                            </span>
                                                        </div>
                                                    </div>

                                                    <div class="propath-card">
                                                        <div class="propath-card-header">
                                                            <h2 class="propath-card-title">
                                                                <i class="bi bi-people-fill"></i>
                                                                <span>Listado de Cuentas de Usuario</span>
                                                            </h2>
                                                            <div class="search-box-custom">
                                                                <i class="bi bi-search search-icon"></i>
                                                                <input type="text"
                                                                    class="form-control table-search-input"
                                                                    data-table="tablaUsuarios"
                                                                    placeholder="Buscar usuario...">
                                                            </div>
                                                        </div>

                                                        <div class="table-responsive">
                                                            <table class="table table-propath" id="tablaUsuarios">
                                                                <thead>
                                                                    <tr>
                                                                        <th>ID</th>
                                                                        <th>Nombres</th>
                                                                        <th>Apellidos</th>
                                                                        <th>Correo Electrónico</th>
                                                                        <th>Rol</th>
                                                                        <th>Estado</th>
                                                                        <th class="text-center">Acciones</th>
                                                                    </tr>
                                                                </thead>
                                                                <tbody>
                                                                    <% if (listaUsuarios==null ||
                                                                        listaUsuarios.isEmpty()) { %>
                                                                        <tr>
                                                                            <td colspan="7"
                                                                                class="text-center py-4 text-muted">
                                                                                <i
                                                                                    class="bi bi-inbox fs-3 d-block mb-2 text-secondary"></i>
                                                                                <span>No hay registros
                                                                                    disponibles</span>
                                                                            </td>
                                                                        </tr>
                                                                        <% } else { for (Usuario u : listaUsuarios) {
                                                                            boolean esAdmin="ADMIN"
                                                                            .equalsIgnoreCase(u.getRol()); boolean
                                                                            esEst="ESTUDIANTE"
                                                                            .equalsIgnoreCase(u.getRol()); String
                                                                            badgeRol=esAdmin
                                                                            ? "bg-danger-subtle text-danger" : (esEst
                                                                            ? "bg-primary-subtle text-primary"
                                                                            : "bg-info-subtle text-info" ); boolean
                                                                            esActivo="ACTIVO"
                                                                            .equalsIgnoreCase(u.getEstado()); Estudiante
                                                                            est=null; if (esEst && listaEstudiantes
                                                                            !=null) { for (Estudiante e :
                                                                            listaEstudiantes) { if (e.getCorreo() !=null
                                                                            &&
                                                                            e.getCorreo().equalsIgnoreCase(u.getCorreo()))
                                                                            { est=e; break; } } } Empresa emp=null; if
                                                                            ("EMPRESA".equalsIgnoreCase(u.getRol()) &&
                                                                            listaEmpresas !=null) { for (Empresa em :
                                                                            listaEmpresas) { if (em.getCorreo() !=null
                                                                            &&
                                                                            em.getCorreo().equalsIgnoreCase(u.getCorreo()))
                                                                            { emp=em; break; } } } %>
                                                                            <tr>
                                                                                <td>#<%= u.getId() %>
                                                                                </td>
                                                                                <td>
                                                                                    <span class="fw-semibold text-dark">
                                                                                        <%= u.getNombre() %>
                                                                                    </span>
                                                                                </td>
                                                                                <td>
                                                                                    <span>
                                                                                        <%= u.getApellidos() !=null &&
                                                                                            !u.getApellidos().isEmpty()
                                                                                            ? u.getApellidos() : "—" %>
                                                                                    </span>
                                                                                </td>
                                                                                <td>
                                                                                    <span class="text-muted">
                                                                                        <%= u.getCorreo() %>
                                                                                    </span>
                                                                                </td>
                                                                                <td>
                                                                                    <span
                                                                                        class="badge <%= badgeRol %> px-2 py-1 rounded-pill">
                                                                                        <%= u.getRol() %>
                                                                                    </span>
                                                                                </td>
                                                                                <td>
<span class="badge-status <%= esActivo ? "badge-activo" : "badge-bloqueado" %>">
    <%= u.getEstado() %>
</span>
                                                                                </td>
                                                                                <td class="text-center">
                                                                                    <div
                                                                                        class="action-btn-group justify-content-center">
                                                                                        <button type="button"
                                                                                            class="btn-action btn-action-view"
                                                                                            title="Ver detalles del usuario"
                                                                                            data-id="<%= u.getId() %>"
                                                                                            data-nombre="<%= u.getNombre() %>"
                                                                                            data-apellidos="<%= u.getApellidos() != null ? u.getApellidos() : "" %>"
                                                                                            data-correo="<%= u.getCorreo() %>"
                                                                                            data-rol="<%= u.getRol() %>"
                                                                                            data-estado="<%= u.getEstado() %>"
                                                                                            data-fecharegistro="<%= u.getFechaRegistro() %>"
                                                                                            data-universidad="<%= est != null && est.getUniversidad() != null ? est.getUniversidad() : "" %>"
                                                                                            data-carrera="<%= est != null && est.getCarrera() != null ? est.getCarrera() : "" %>"
                                                                                            data-ciclo="<%= est != null && est.getCiclo() > 0 ? est.getCiclo() : "" %>"
                                                                                            data-telefono="<%= est != null && est.getTelefono() != null ? est.getTelefono() : (emp != null && emp.getTelefono() != null ? emp.getTelefono() : "") %>"
                                                                                            data-cv="<%= est != null && est.getCv() != null ? est.getCv() : "" %>"
                                                                                            data-razonsocial="<%= emp != null && emp.getRazonSocial() != null ? emp.getRazonSocial() : "" %>"
                                                                                            data-ruc="<%= emp != null && emp.getRuc() != null ? emp.getRuc() : "" %>"
                                                                                            data-representante="<%= emp != null ? (emp.getRepresentanteNombre() + " " + emp.getRepresentanteApellidos()).trim() : (u.getNombre() + " " + (u.getApellidos() != null ? u.getApellidos() : "")).trim() %>"
                                                                                            data-direccion="<%= emp != null && emp.getDireccion() != null ? emp.getDireccion() : "" %>"
                                                                                            data-descripcion="<%= emp != null && emp.getDescripcion() != null ? emp.getDescripcion() : "" %>"
                                                                                            data-cargo="<%= esAdmin ? (u.getId() == 60 ? "Administrador Principal del Sistema" : "Administrador del Sistema") : "" %>"
                                                                                            onclick="verDetallesUsuario(this)">
                                                                                            <i class="bi bi-eye"></i>
                                                                                        </button>
                                                                                        <button type="button"
                                                                                            class="btn-action btn-action-danger"
                                                                                            title="Eliminar usuario"
                                                                                            data-id="<%= u.getId() %>"
                                                                                            data-nombre="<%= u.getNombre() %> <%= u.getApellidos() != null ? u.getApellidos() : "" %>"
                                                                                            onclick="eliminarUsuario(this.dataset.id, this.dataset.nombre)">
                                                                                            <i class="bi bi-trash"></i>
                                                                                        </button>
                                                                                    </div>
                                                                                </td>
                                                                            </tr>
                                                                            <% } } %>
                                                                </tbody>
                                                            </table>
                                                        </div>
                                                    </div>
                                                </div>
                                            </main>
                                        </div>

                                        <div class="modal fade" id="modalDetallesUsuario" tabindex="-1"
                                            aria-labelledby="modalDetallesUsuarioLabel" aria-hidden="true">
                                            <div class="modal-dialog modal-dialog-centered modal-lg">
                                                <div class="modal-content modal-content-propath">
                                                    <div class="modal-header modal-header-propath">
                                                        <h2 class="modal-title h5" id="modalDetallesUsuarioLabel">
                                                            <i class="bi bi-person-badge me-2 text-primary"></i>
                                                            Detalles del Usuario
                                                        </h2>
                                                        <button type="button" class="btn-close" data-bs-dismiss="modal"
                                                            aria-label="Cerrar"></button>
                                                    </div>
                                                    <div class="modal-body modal-body-propath">
                                                        <div class="p-3 bg-light rounded-3 mb-3 border">
                                                            <div
                                                                class="d-flex justify-content-between align-items-center mb-2">
                                                                <span
                                                                    class="small fw-bold text-muted text-uppercase tracking-wider">Información
                                                                    de Cuenta</span>
                                                                <div class="d-flex gap-2">
                                                                    <span id="detRolBadge"
                                                                        class="badge px-3 py-1 rounded-pill"></span>
                                                                    <span id="detEstadoBadge"
                                                                        class="badge-status"></span>
                                                                </div>
                                                            </div>
                                                            <div class="row g-2">
                                                                <div class="col-sm-4">
                                                                    <span class="small text-muted d-block">ID de
                                                                        Usuario:</span>
                                                                    <span id="detId" class="fw-bold text-dark"></span>
                                                                </div>
                                                                <div class="col-sm-4">
                                                                    <span
                                                                        class="small text-muted d-block">Nombres:</span>
                                                                    <span id="detNombre"
                                                                        class="fw-semibold text-dark"></span>
                                                                </div>
                                                                <div class="col-sm-4">
                                                                    <span
                                                                        class="small text-muted d-block">Apellidos:</span>
                                                                    <span id="detApellidos"
                                                                        class="fw-semibold text-dark"></span>
                                                                </div>
                                                                <div class="col-sm-6">
                                                                    <span class="small text-muted d-block">Correo
                                                                        Electrónico:</span>
                                                                    <span id="detCorreo"
                                                                        class="fw-semibold text-dark"></span>
                                                                </div>
                                                                <div class="col-sm-6">
                                                                    <span class="small text-muted d-block">Fecha de
                                                                        Registro:</span>
                                                                    <span id="detFechaRegistro"
                                                                        class="text-secondary"></span>
                                                                </div>
                                                            </div>
                                                        </div>

                                                        <div id="seccionEstudiante"
                                                            class="p-3 bg-white rounded-3 mb-3 border d-none">
                                                            <div
                                                                class="small fw-bold text-primary text-uppercase tracking-wider mb-2">
                                                                <i class="bi bi-mortarboard-fill me-1"></i> Perfil
                                                                Académico del Estudiante
                                                            </div>
                                                            <div class="row g-2">
                                                                <div class="col-sm-6">
                                                                    <span class="small text-muted d-block">Universidad o
                                                                        Instituto:</span>
                                                                    <span id="detUniversidad"
                                                                        class="fw-semibold text-dark"></span>
                                                                </div>
                                                                <div class="col-sm-6">
                                                                    <span class="small text-muted d-block">Carrera
                                                                        Profesional:</span>
                                                                    <span id="detCarrera"
                                                                        class="fw-semibold text-dark"></span>
                                                                </div>
                                                                <div class="col-sm-4">
                                                                    <span class="small text-muted d-block">Ciclo
                                                                        Académico:</span>
                                                                    <span id="detCiclo"
                                                                        class="fw-semibold text-dark"></span>
                                                                </div>
                                                                <div class="col-sm-4">
                                                                    <span class="small text-muted d-block">Teléfono de
                                                                        Contacto:</span>
                                                                    <span id="detTelefonoEst"
                                                                        class="fw-semibold text-dark"></span>
                                                                </div>
                                                                <div class="col-sm-4">
                                                                    <span class="small text-muted d-block">Currículum
                                                                        Vitae (CV):</span>
                                                                    <span id="detCv" class="text-secondary"></span>
                                                                </div>
                                                            </div>
                                                        </div>

                                                        <div id="seccionEmpresa"
                                                            class="p-3 bg-white rounded-3 mb-3 border d-none">
                                                            <div class="small fw-bold text-info text-uppercase tracking-wider mb-2"
                                                                style="color: #0891b2 !important;">
                                                                <i class="bi bi-building-fill me-1"></i> Información
                                                                Corporativa de la Empresa
                                                            </div>
                                                            <div class="row g-2">
                                                                <div class="col-sm-8">
                                                                    <span class="small text-muted d-block">Razón
                                                                        Social:</span>
                                                                    <span id="detRazonSocial"
                                                                        class="fw-bold text-dark"></span>
                                                                </div>
                                                                <div class="col-sm-4">
                                                                    <span class="small text-muted d-block">RUC (11
                                                                        dígitos):</span>
                                                                    <span id="detRuc" class="fw-bold text-dark"></span>
                                                                </div>
                                                                <div class="col-sm-6">
                                                                    <span
                                                                        class="small text-muted d-block">Representante:</span>
                                                                    <span id="detRepresentante"
                                                                        class="fw-semibold text-dark"></span>
                                                                </div>
                                                                <div class="col-sm-6">
                                                                    <span class="small text-muted d-block">Teléfono
                                                                        Corporativo:</span>
                                                                    <span id="detTelefonoEmp"
                                                                        class="fw-semibold text-dark"></span>
                                                                </div>
                                                                <div class="col-12">
                                                                    <span
                                                                        class="small text-muted d-block">Dirección:</span>
                                                                    <span id="detDireccion" class="text-dark"></span>
                                                                </div>
                                                                <div class="col-12">
                                                                    <span class="small text-muted d-block">Descripción
                                                                        de la Empresa:</span>
                                                                    <span id="detDescripcion"
                                                                        class="text-secondary small fst-italic"></span>
                                                                </div>
                                                            </div>
                                                        </div>

                                                        <div id="seccionAdmin"
                                                            class="p-3 bg-white rounded-3 mb-3 border d-none">
                                                            <div
                                                                class="small fw-bold text-danger text-uppercase tracking-wider mb-2">
                                                                <i class="bi bi-shield-lock-fill me-1"></i> Perfil
                                                                Administrativo
                                                            </div>
                                                            <div class="row g-2">
                                                                <div class="col-sm-6">
                                                                    <span class="small text-muted d-block">Cargo /
                                                                        Nivel:</span>
                                                                    <span id="detCargoAdmin"
                                                                        class="fw-bold text-dark"></span>
                                                                </div>
                                                                <div class="col-sm-6">
                                                                    <span
                                                                        class="small text-muted d-block">Permisos:</span>
                                                                    <span class="text-secondary small">Supervisión
                                                                        integral de cuentas y registros</span>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div class="modal-footer bg-light border-0">
                                                        <button type="button"
                                                            class="btn btn-secondary rounded-pill px-4"
                                                            data-bs-dismiss="modal">
                                                            <i class="bi bi-x-lg me-1"></i> Cerrar
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>

                                        <script>
                                            document.addEventListener('DOMContentLoaded', function () {
                                                function verDetallesUsuario(btn) {
                                                    const id = btn.getAttribute('data-id');
                                                    const nombre = btn.getAttribute('data-nombre');
                                                    const apellidos = btn.getAttribute('data-apellidos') || '—';
                                                    const correo = btn.getAttribute('data-correo');
                                                    const rol = btn.getAttribute('data-rol');
                                                    const estado = btn.getAttribute('data-estado');
                                                    const fecha = btn.getAttribute('data-fecharegistro') || 'Registrado';

                                                    document.getElementById('detId').textContent = '#' + id;
                                                    document.getElementById('detNombre').textContent = nombre;
                                                    document.getElementById('detApellidos').textContent = apellidos;
                                                    document.getElementById('detCorreo').textContent = correo;
                                                    document.getElementById('detFechaRegistro').textContent = fecha;

                                                    const rolBadge = document.getElementById('detRolBadge');
                                                    rolBadge.textContent = rol;
                                                    if (rol === 'ADMIN') {
                                                        rolBadge.className = 'badge bg-danger-subtle text-danger border border-danger-subtle px-3 py-1 rounded-pill';
                                                    } else if (rol === 'ESTUDIANTE') {
                                                        rolBadge.className = 'badge bg-primary-subtle text-primary border border-primary-subtle px-3 py-1 rounded-pill';
                                                    } else {
                                                        rolBadge.className = 'badge bg-info-subtle text-info border border-info-subtle px-3 py-1 rounded-pill';
                                                    }

                                                    const estadoBadge = document.getElementById('detEstadoBadge');
                                                    estadoBadge.textContent = estado;
                                                    estadoBadge.className = 'badge-status ' + (estado === 'ACTIVO' ? 'badge-activo' : 'badge-bloqueado');

                                                    const secEst = document.getElementById('seccionEstudiante');
                                                    const secEmp = document.getElementById('seccionEmpresa');
                                                    const secAdm = document.getElementById('seccionAdmin');

                                                    secEst.classList.add('d-none');
                                                    secEmp.classList.add('d-none');
                                                    secAdm.classList.add('d-none');

                                                    if (rol === 'ESTUDIANTE') {
                                                        secEst.classList.remove('d-none');
                                                        document.getElementById('detUniversidad').textContent = btn.getAttribute('data-universidad') || 'No especificada';
                                                        document.getElementById('detCarrera').textContent = btn.getAttribute('data-carrera') || 'No especificada';
                                                        document.getElementById('detCiclo').textContent = btn.getAttribute('data-ciclo') ? ('Ciclo ' + btn.getAttribute('data-ciclo')) : 'No registrado';
                                                        document.getElementById('detTelefonoEst').textContent = btn.getAttribute('data-telefono') || '—';
                                                        document.getElementById('detCv').textContent = btn.getAttribute('data-cv') || 'No adjuntado';
                                                    } else if (rol === 'EMPRESA') {
                                                        secEmp.classList.remove('d-none');
                                                        document.getElementById('detRazonSocial').textContent = btn.getAttribute('data-razonsocial') || 'No especificada';
                                                        document.getElementById('detRuc').textContent = btn.getAttribute('data-ruc') || '—';
                                                        document.getElementById('detRepresentante').textContent = btn.getAttribute('data-representante') || (nombre + ' ' + apellidos);
                                                        document.getElementById('detTelefonoEmp').textContent = btn.getAttribute('data-telefono') || '—';
                                                        document.getElementById('detDireccion').textContent = btn.getAttribute('data-direccion') || 'No registrada';
                                                        document.getElementById('detDescripcion').textContent = btn.getAttribute('data-descripcion') || 'Sin descripción corporativa registrada';
                                                    } else if (rol === 'ADMIN') {
                                                        secAdm.classList.remove('d-none');
                                                        document.getElementById('detCargoAdmin').textContent = btn.getAttribute('data-cargo') || (id === '60' ? 'Administrador Principal' : 'Administrador');
                                                    }

                                                    const modalEl = document.getElementById('modalDetallesUsuario');
                                                    if (window.BootstrapModalCompat) {
                                                        const inst = window.BootstrapModalCompat.getOrCreateInstance(modalEl);
                                                        if (inst) inst.show();
                                                    }
                                                }

                                                window.verDetallesUsuario = verDetallesUsuario;
                                                window.abrirModalDetallesUsuario = verDetallesUsuario;

                                                function eliminarUsuario(id, nombre) {
                                                    if (id === '60') {
                                                        if (window.mostrarModalProhibido) mostrarModalProhibido('No está permitido eliminar la cuenta del administrador principal del sistema.');
                                                        return;
                                                    }

                                                    if (window.mostrarModalConfirmacion) {
                                                        mostrarModalConfirmacion(
                                                            'Eliminar Usuario',
                                                            '¿Estás seguro de eliminar al usuario #' + id + ' "' + nombre + '"? Esta acción verificará y eliminará las relaciones de la cuenta.',
                                                            async function () {
                                                                const body = new URLSearchParams({
                                                                    accion: 'eliminar',
                                                                    recurso: 'usuarios',
                                                                    id: id
                                                                });
                                                                try {
                                                                    const res = await fetch('${pageContext.request.contextPath}/admin/accion', {
                                                                        method: 'POST',
                                                                        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                                                                        body: body.toString()
                                                                    });
                                                                    if (res.status === 403) {
                                                                        const data = await res.json().catch(() => ({}));
                                                                        if (window.mostrarModalProhibido) mostrarModalProhibido(data.mensaje || 'No está permitido eliminar esta cuenta.');
                                                                        return;
                                                                    }
                                                                    const data = await res.json();
                                                                    if (data.exito) {
                                                                        if (window.mostrarModalMensaje) mostrarModalMensaje('Usuario Eliminado', data.mensaje || 'Usuario eliminado con éxito.', 'success', () => window.location.reload());
                                                                    } else {
                                                                        if (data.prohibido) {
                                                                            if (window.mostrarModalProhibido) mostrarModalProhibido(data.mensaje);
                                                                        } else {
                                                                            if (window.mostrarModalMensaje) mostrarModalMensaje('Error al Eliminar', data.mensaje || 'No se pudo eliminar el usuario.', 'danger');
                                                                        }
                                                                    }
                                                                } catch (err) {
                                                                    if (window.mostrarModalMensaje) mostrarModalMensaje('Error de Conexión', 'No se pudo conectar con el servidor: ' + err.message, 'danger');
                                                                }
                                                            }
                                                        );
                                                    }
                                                }

                                                window.eliminarUsuario = eliminarUsuario;
                                            });
                                        </script>
                                        <script type="module"
                                            src="${pageContext.request.contextPath}/js/script.js"></script>
                                    </body>

                                    </html>