<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.text.SimpleDateFormat, java.util.Date, java.util.Locale"%>
<%@page import="com.propath.modelo.Usuario"%>
<%
    SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
    String fechaActual = sdf.format(new Date());
    
    if (fechaActual != null && !fechaActual.isEmpty()) {
        fechaActual = fechaActual.substring(0, 1).toUpperCase() + fechaActual.substring(1);
    }

    Usuario uAdminLog = (Usuario) session.getAttribute("usuarioLogueado");
    int idAdminLog = uAdminLog != null ? uAdminLog.getId() : 60;
    String nomAdminLog = uAdminLog != null && uAdminLog.getNombre() != null ? uAdminLog.getNombre() : "Administrador";
    String apeAdminLog = uAdminLog != null && uAdminLog.getApellidos() != null ? uAdminLog.getApellidos() : "";
    String corAdminLog = uAdminLog != null && uAdminLog.getCorreo() != null ? uAdminLog.getCorreo() : "admin@propath.com";

    String nombreAdmin = (String) session.getAttribute("nombreUsuario");
    if (nombreAdmin == null || nombreAdmin.trim().isEmpty()) {
        nombreAdmin = nomAdminLog;
    }
    String iniciales = "AD";
    String[] partes = nombreAdmin.trim().split("\\s+");
    if (partes.length >= 2 && !partes[0].isEmpty() && !partes[1].isEmpty()) {
        iniciales = ("" + partes[0].charAt(0) + partes[1].charAt(0)).toUpperCase();
    } else if (!nombreAdmin.isEmpty()) {
        iniciales = nombreAdmin.substring(0, Math.min(2, nombreAdmin.length())).toUpperCase();
    }
%>
<header class="admin-topbar">
    <div class="topbar-left">
        <button class="sidebar-toggle-btn" id="sidebarToggle" type="button" aria-label="Abrir menú">
            <i class="bi bi-list"></i>
        </button>

        <div class="topbar-welcome">
            <h2 class="welcome-title">Bienvenido, <%= nombreAdmin %></h2>
            <span class="welcome-date"><i class="bi bi-calendar3 me-1"></i> <%= fechaActual %></span>
        </div>
    </div>

    <div class="topbar-right">
        <div class="d-none d-md-flex align-items-center me-2">
            <span class="badge bg-success-subtle text-success border border-success-subtle rounded-pill px-3 py-2 fw-semibold">
                <i class="bi bi-circle-fill fs-6 me-1" style="font-size: 8px !important;"></i> Sistema en Línea
            </span>
        </div>

        <button type="button" class="btn btn-sm btn-outline-primary rounded-pill px-3 fw-semibold me-2 d-none d-sm-inline-flex align-items-center" data-bs-toggle="modal" data-bs-target="#modalMiCuentaAdmin">
            <i class="bi bi-person-gear me-1"></i> Mi Cuenta
        </button>

        <div class="user-profile-badge" role="button" data-bs-toggle="modal" data-bs-target="#modalMiCuentaAdmin" title="Gestionar mi cuenta">
            <div class="user-avatar">
                <%= iniciales %>
            </div>
            <div class="user-info d-none d-sm-flex">
                <span class="user-name"><%= nombreAdmin %></span>
                <span class="user-role">Panel ProPath</span>
            </div>
        </div>
    </div>
</header>

<div class="modal fade" id="modalMiCuentaAdmin" tabindex="-1" aria-labelledby="modalMiCuentaAdminLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content modal-content-propath">
            <div class="modal-header modal-header-propath">
                <h2 class="modal-title h5" id="modalMiCuentaAdminLabel">
                    <i class="bi bi-person-gear me-2 text-primary"></i> Mi Cuenta de Administrador
                </h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <form id="formMiCuentaAdmin" onsubmit="guardarMiCuentaAdmin(event)">
                <input type="hidden" id="adminLogId" value="<%= idAdminLog %>">
                <div class="modal-body modal-body-propath">
                    <div class="row g-3">
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold">Nombres *</label>
                            <input type="text" class="form-control" id="adminLogNombre" value="<%= nomAdminLog %>" required>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold">Apellidos *</label>
                            <input type="text" class="form-control" id="adminLogApellidos" value="<%= apeAdminLog %>" required>
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold">Correo Electrónico *</label>
                            <input type="email" class="form-control" id="adminLogCorreo" value="<%= corAdminLog %>" required>
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold">Nueva Contraseña (opcional)</label>
                            <input type="password" class="form-control" id="adminLogContrasena" placeholder="Dejar en blanco para conservar actual" minlength="6">
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold">Rol Asignado</label>
                            <input type="text" class="form-control bg-light" value="ADMIN (Protegido)" readonly disabled>
                        </div>
                    </div>
                </div>
                <div class="modal-footer bg-light border-0">
                    <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-primary rounded-pill px-4" id="btnGuardarMiCuentaAdmin">
                        <i class="bi bi-check2 me-1"></i> Guardar Cambios
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
<script>
    async function guardarMiCuentaAdmin(e) {
        e.preventDefault();
        const btn = document.getElementById('btnGuardarMiCuentaAdmin');
        btn.disabled = true;

        const body = new URLSearchParams({
            accion: 'actualizarUsuario',
            id: document.getElementById('adminLogId').value,
            nombre: document.getElementById('adminLogNombre').value.trim(),
            apellidos: document.getElementById('adminLogApellidos').value.trim(),
            correo: document.getElementById('adminLogCorreo').value.trim(),
            contrasena: document.getElementById('adminLogContrasena').value.trim(),
            estado: 'ACTIVO'
        });

        try {
            const res = await fetch('${pageContext.request.contextPath}/admin/accion', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                body: body.toString()
            });
            const data = await res.json();
            if (data.exito) {
                mostrarModalMensaje('Mi Cuenta Actualizada', data.mensaje || 'Datos actualizados exitosamente.', 'success', () => window.location.reload());
            } else {
                mostrarModalMensaje('Error', data.mensaje || 'No se pudieron actualizar los datos.', 'danger');
                btn.disabled = false;
            }
        } catch (err) {
            mostrarModalMensaje('Error de Conexión', 'No se pudo conectar: ' + err.message, 'danger');
            btn.disabled = false;
        }
    }
</script>
