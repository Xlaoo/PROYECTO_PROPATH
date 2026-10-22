<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@page import="java.util.List" %>
        <%@page import="com.propath.modelo.Empresa" %>
            <%@page import="com.propath.servicio.BackendCliente" %>
                <% request.setAttribute("activePage", "empresas" ); List<Empresa> listaEmpresas =
                    BackendCliente.listarEmpresas();
                    %>
                    <!DOCTYPE html>
                    <html lang="es" data-bs-theme="light">

                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>Gestión de Empresas | ProPath</title>


                        <link rel="preconnect" href="https://fonts.googleapis.com">
                        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                        <link
                            href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
                            rel="stylesheet">


                        <link href="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/css/bootstrap.min.css"
                            rel="stylesheet">
                        <link rel="stylesheet"
                            href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">


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
                                                    <li class="breadcrumb-item"><a
                                                            href="${pageContext.request.contextPath}/admin/dashboard.jsp"
                                                            class="text-decoration-none">Inicio</a></li>
                                                    <li class="breadcrumb-item active" aria-current="page">Empresas</li>
                                                </ol>
                                            </nav>
                                            <h1 class="h3 fw-bold text-dark mb-0">Gestión de Empresas</h1>
                                            <p class="text-muted small mb-0">Valida y autoriza a las organizaciones
                                                empleadoras registradas en ProPath</p>
                                        </div>
                                        <div class="d-flex align-items-center gap-2">
                                            <span
                                                class="badge bg-info-subtle text-info border border-info-subtle px-3 py-2 rounded-pill fw-semibold"
                                                style="color: #0891b2 !important; border-color: #0891b2 !important;">
                                                <i class="bi bi-building-fill me-1"></i>
                                                <%= (listaEmpresas !=null ? listaEmpresas.size() : 0) %> Empresas
                                                    registradas
                                            </span>
                                            <button type="button"
                                                class="btn btn-primary rounded-pill px-3 py-2 fw-semibold shadow-sm"
                                                onclick="window.location.reload()">
                                                <i class="bi bi-arrow-clockwise me-1"></i> Actualizar
                                            </button>
                                        </div>
                                    </div>


                                    <div class="propath-card">
                                        <div class="propath-card-header">
                                            <h2 class="propath-card-title">
                                                <i class="bi bi-buildings-fill"></i>
                                                <span>Listado de Empresas Empleadoras</span>
                                            </h2>

                                            <div class="search-box-custom">
                                                <i class="bi bi-search search-icon"></i>
                                                <input type="text" class="form-control table-search-input"
                                                    data-table="tablaEmpresas"
                                                    placeholder="Buscar por razón social, RUC o correo...">
                                            </div>
                                        </div>

                                        <div class="table-responsive">
                                            <table class="table table-propath" id="tablaEmpresas">
                                                <thead>
                                                    <tr>
                                                        <th>Razón Social</th>
                                                        <th>RUC</th>
                                                        <th>Correo Corporativo</th>
                                                        <th>Teléfono</th>
                                                        <th>Estado</th>
                                                        <th class="text-center">Acciones</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <% if (listaEmpresas==null || listaEmpresas.isEmpty()) { %>
                                                        <tr>
                                                            <td colspan="6" class="text-center py-4 text-muted">
                                                                <i
                                                                    class="bi bi-inbox fs-3 d-block mb-2 text-secondary"></i>
                                                                <span>No hay registros disponibles</span>
                                                            </td>
                                                        </tr>
                                                        <% } else { for (Empresa emp : listaEmpresas) { boolean
                                                            esAprobada="Aprobada" .equalsIgnoreCase(emp.getEstado());
                                                            boolean esPendiente="Pendiente"
                                                            .equalsIgnoreCase(emp.getEstado()); %>
                                                            <tr>
                                                                <td>
                                                                    <div class="d-flex align-items-center gap-2">
                                                                        <%
    String claseAvatar = esAprobada
            ? "avatar-bg-aprobada"
            : "avatar-bg-pendiente";
%>

<div class="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white shadow-sm <%= claseAvatar %>"
     style="width: 36px; height: 36px; font-size: 0.82rem;">
    <i class="bi bi-building"></i>
</div>
                                                                        <div>
                                                                            <span class="fw-semibold text-dark d-block">
                                                                                <%= emp.getRazonSocial() %>
                                                                            </span>
                                                                            <span class="text-muted"
                                                                                style="font-size: 0.76rem;">
                                                                                Empleador Oficial
                                                                            </span>
                                                                        </div>
                                                                    </div>
                                                                </td>
                                                                <td>
                                                                    <code
                                                                        class="px-2 py-1 bg-light border rounded text-dark fw-bold"><%= emp.getRuc() %></code>
                                                                </td>
                                                                <td>
                                                                    <a href="mailto:<%= emp.getCorreo() %>"
                                                                        class="text-muted text-decoration-none small">
                                                                        <i class="bi bi-envelope me-1"></i>
                                                                        <%= emp.getCorreo() %>
                                                                    </a>
                                                                </td>
                                                                <td>
                                                                    <span class="text-secondary small">
                                                                        <i class="bi bi-telephone me-1"></i>
                                                                        <%= emp.getTelefono() %>
                                                                    </span>
                                                                </td>
                                                                <td>
                                                                    <div class="d-flex flex-column gap-1">
    <%
        boolean cuentaActiva = "ACTIVO".equalsIgnoreCase(emp.getEstadoCuenta());
        boolean empresaAprobada = "APROBADA".equalsIgnoreCase(emp.getEstado());
        boolean empresaPendiente = "PENDIENTE".equalsIgnoreCase(emp.getEstado());

        String claseCuenta = cuentaActiva ? "bg-success" : "bg-secondary";
        String claseEmpresa = empresaAprobada
                ? "bg-primary"
                : (empresaPendiente ? "bg-warning text-dark" : "bg-danger");
    %>

    <span class="badge <%= claseCuenta %>"
          style="width: fit-content; font-size: 0.73rem;">
        Cuenta: <%= emp.getEstadoCuenta() %>
    </span>

    <span class="badge <%= claseEmpresa %>"
          style="width: fit-content; font-size: 0.73rem;">
        Empresa: <%= emp.getEstado() %>
    </span>
</div>
                                                                </td>
                                                                <td class="text-center">
                                                                    <div
                                                                        class="action-btn-group justify-content-center">
                                                                        <button type="button"
                                                                            class="btn-action btn-action-view"
                                                                            title="Ver detalle" data-bs-toggle="tooltip"
                                                                            data-id="<%= emp.getId() %>"
                                                                            data-razon="<%= emp.getRazonSocial() %>"
                                                                            data-ruc="<%= emp.getRuc() %>"
                                                                            data-correo="<%= emp.getCorreo() %>"
                                                                            data-telefono="<%= emp.getTelefono() %>"
                                                                            data-direccion="<%= emp.getDireccion() != null ? emp.getDireccion() : "" %>"
                                                                            data-descripcion="<%= emp.getDescripcion() != null ? emp.getDescripcion() : "" %>"
                                                                            data-representante="<%= emp.getRepresentanteCompleto() %>"
                                                                            data-estado="<%= emp.getEstado() %>"
                                                                            data-estado-cuenta="<%= emp.getEstadoCuenta() %>"
                                                                            onclick="verDetalleEmpresaBtn(this)">
                                                                            <i class="bi bi-eye"></i>
                                                                        </button>

                                                                        <%
    boolean cuentaInactiva = "INACTIVO".equalsIgnoreCase(emp.getEstadoCuenta());
    String claseActivar = cuentaInactiva
            ? "btn-action-success"
            : "btn-action-secondary";

    String claseEliminar = cuentaInactiva
            ? "btn-action-danger"
            : "btn-action-secondary";
%>

<button type="button"
        class="btn-action <%= claseActivar %>"
        title="<%= cuentaInactiva ? "Activar empresa" : "Empresa ya activa" %>"
        data-id="<%= emp.getId() %>"
        data-razon="<%= emp.getRazonSocial() %>"
        <%= cuentaInactiva ? "" : "disabled" %>
        onclick="confirmarActivarEmpresa(this.dataset.id, this.dataset.razon)">
    <i class="bi bi-check-lg"></i>
</button>

<button type="button"
        class="btn-action <%= claseEliminar %>"
        title="<%= cuentaInactiva ? "Eliminar empresa inactiva" : "No se puede eliminar una empresa activa" %>"
        data-id="<%= emp.getId() %>"
        data-razon="<%= emp.getRazonSocial() %>"
        <%= cuentaInactiva ? "" : "disabled" %>
        onclick="confirmarEliminarEmpresa(this.dataset.id, this.dataset.razon)">
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

                        <div class="modal fade" id="modalDetalleEmpresa" tabindex="-1"
                            aria-labelledby="modalEmpresaTitle" aria-hidden="true">
                            <div class="modal-dialog modal-dialog-centered">
                                <div class="modal-content modal-content-propath">
                                    <div class="modal-header modal-header-propath">
                                        <h2 class="modal-title h5" id="modalEmpresaTitle">
                                            <i class="bi bi-building-check me-2"></i>
                                            <span>Ficha de la Empresa</span>
                                        </h2>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"
                                            aria-label="Cerrar"></button>
                                    </div>
                                    <div class="modal-body modal-body-propath">
                                        <div class="row g-3">
                                            <div class="col-12">
                                                <div class="detail-label">Razón Social</div>
                                                <div class="detail-value fs-5" id="modalEmpresaRazon">—</div>
                                            </div>
                                            <div class="col-sm-6">
                                                <div class="detail-label">RUC</div>
                                                <div class="detail-value" id="modalEmpresaRuc">—</div>
                                            </div>
                                            <div class="col-sm-6">
                                                <div class="detail-label">Teléfono de Contacto</div>
                                                <div class="detail-value" id="modalEmpresaTelefono">—</div>
                                            </div>
                                            <div class="col-12">
                                                <div class="detail-label">Representante Legal</div>
                                                <div class="detail-value" id="modalEmpresaRepresentante">—</div>
                                            </div>
                                            <div class="col-12">
                                                <div class="detail-label">Correo Electrónico Corporativo</div>
                                                <div class="detail-value" id="modalEmpresaCorreo">—</div>
                                            </div>
                                            <div class="col-12">
                                                <div class="detail-label">Dirección Fiscal / Sede</div>
                                                <div class="detail-value" id="modalEmpresaDireccion">—</div>
                                            </div>
                                            <div class="col-12">
                                                <div class="detail-label">Descripción de la Empresa</div>
                                                <div class="detail-value" id="modalEmpresaDescripcion">—</div>
                                            </div>
                                            <div class="col-sm-6 pt-2 border-top">
                                                <div class="detail-label">Estado de la Cuenta</div>
                                                <span id="modalEmpresaEstadoCuenta"
                                                    class="badge bg-success">ACTIVO</span>
                                            </div>
                                            <div class="col-sm-6 pt-2 border-top">
                                                <div class="detail-label">Estado de Aprobación</div>
                                                <span id="modalEmpresaEstado" class="badge bg-primary">APROBADA</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="modal-footer bg-light border-0 d-flex justify-content-between">
                                        <button type="button" class="btn btn-success rounded-pill px-4 d-none"
                                            id="btnModalAprobarEmpresa" style="display: none;"
                                            onclick="confirmarAprobarEmpresaModal()">
                                            <i class="bi bi-check2-circle me-1"></i> Aprobar Empresa
                                        </button>
                                        <button type="button" class="btn btn-secondary rounded-pill px-4 ms-auto"
                                            data-bs-dismiss="modal">Cerrar</button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="modal fade" id="modalNuevaEmpresa" tabindex="-1"
                            aria-labelledby="modalNuevaEmpresaLabel" aria-hidden="true">
                            <div class="modal-dialog modal-dialog-centered modal-lg">
                                <div class="modal-content modal-content-propath">
                                    <div class="modal-header modal-header-propath">
                                        <h2 class="modal-title h5" id="modalNuevaEmpresaLabel">
                                            <i class="bi bi-building-add me-2"></i> Registrar Nueva Empresa
                                        </h2>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"
                                            aria-label="Cerrar"></button>
                                    </div>
                                    <form id="formNuevaEmpresa" onsubmit="guardarNuevaEmpresa(event)">
                                        <div class="modal-body modal-body-propath">
                                            <div class="row g-3">
                                                <div class="col-md-6">
                                                    <label class="form-label small fw-semibold">Razón Social *</label>
                                                    <input type="text" class="form-control" id="nuevaRazonSocial"
                                                        required>
                                                </div>
                                                <div class="col-md-6">
                                                    <label class="form-label small fw-semibold">RUC (11 dígitos)
                                                        *</label>
                                                    <input type="text" class="form-control" id="nuevoRuc" maxlength="11"
                                                        pattern="\d{11}" required>
                                                </div>
                                                <div class="col-md-6">
                                                    <label class="form-label small fw-semibold">Correo Corporativo
                                                        *</label>
                                                    <input type="email" class="form-control" id="nuevoCorreoEmpresa"
                                                        required>
                                                </div>
                                                <div class="col-md-6">
                                                    <label class="form-label small fw-semibold">Contraseña Inicial
                                                        *</label>
                                                    <input type="password" class="form-control" id="nuevaClaveEmpresa"
                                                        minlength="6" required>
                                                </div>
                                                <div class="col-md-6">
                                                    <label class="form-label small fw-semibold">Teléfono de
                                                        Contacto</label>
                                                    <input type="text" class="form-control" id="nuevoTelEmpresa">
                                                </div>
                                                <div class="col-md-6">
                                                    <label class="form-label small fw-semibold">Estado Inicial</label>
                                                    <select class="form-select" id="nuevoEstadoEmpresa">
                                                        <option value="Aprobada" selected>Aprobada</option>
                                                        <option value="Pendiente">Pendiente</option>
                                                        <option value="Bloqueada">Bloqueada</option>
                                                    </select>
                                                </div>
                                                <div class="col-12">
                                                    <label class="form-label small fw-semibold">Dirección</label>
                                                    <input type="text" class="form-control" id="nuevaDirEmpresa"
                                                        placeholder="Av. Principal 123, Lima">
                                                </div>
                                                <div class="col-12">
                                                    <label class="form-label small fw-semibold">Descripción</label>
                                                    <textarea class="form-control" id="nuevaDescEmpresa" rows="2"
                                                        placeholder="Giro del negocio o rubro..."></textarea>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="modal-footer bg-light border-0">
                                            <button type="button" class="btn btn-secondary rounded-pill px-4"
                                                data-bs-dismiss="modal">Cancelar</button>
                                            <button type="submit" class="btn btn-primary rounded-pill px-4"
                                                id="btnGuardarNuevaEmpresa">
                                                <i class="bi bi-check2 me-1"></i> Registrar Empresa
                                            </button>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        </div>
                        <script>
                            async function guardarNuevaEmpresa(e) {
                                e.preventDefault();
                                const btn = document.getElementById('btnGuardarNuevaEmpresa');
                                btn.disabled = true;
                                const body = new URLSearchParams({
                                    accion: 'crearEmpresa',
                                    nombre: document.getElementById('nuevaRazonSocial').value.trim(),
                                    razonSocial: document.getElementById('nuevaRazonSocial').value.trim(),
                                    ruc: document.getElementById('nuevoRuc').value.trim(),
                                    correo: document.getElementById('nuevoCorreoEmpresa').value.trim(),
                                    contrasena: document.getElementById('nuevaClaveEmpresa').value.trim(),
                                    telefono: document.getElementById('nuevoTelEmpresa').value.trim(),
                                    direccion: document.getElementById('nuevaDirEmpresa').value.trim(),
                                    descripcion: document.getElementById('nuevaDescEmpresa').value.trim(),
                                    estado: document.getElementById('nuevoEstadoEmpresa').value
                                });

                                try {
                                    const res = await fetch('${pageContext.request.contextPath}/admin/accion', {
                                        method: 'POST',
                                        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                                        body: body.toString()
                                    });
                                    const data = await res.json();
                                    if (data.exito) {
                                        mostrarModalMensaje('Empresa Registrada', data.mensaje || 'Empresa registrada exitosamente en el sistema.', 'success', () => window.location.reload());
                                    } else {
                                        mostrarModalMensaje('Error al Registrar', data.mensaje || 'No se pudo registrar la empresa.', 'danger');
                                        btn.disabled = false;
                                    }
                                } catch (err) {
                                    mostrarModalMensaje('Error de Conexión', 'No se pudo conectar con el servidor: ' + err.message, 'danger');
                                    btn.disabled = false;
                                }
                            }

                            function confirmarActivarEmpresa(id, razon) {
                                mostrarModalConfirmacion(
                                    'Activar Empresa',
                                    '¿Desea reactivar la cuenta de la empresa "' + razon + '"? El representante podrá volver a acceder a la plataforma.',
                                    async function () {
                                        const body = new URLSearchParams({
                                            accion: 'activarCuentaEmpresa',
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
                                                mostrarModalMensaje('Empresa Reactivada', data.mensaje || 'Cuenta activada exitosamente.', 'success', () => window.location.reload());
                                            } else {
                                                mostrarModalMensaje('Error al Activar', data.mensaje || 'No se pudo activar la empresa.', 'danger');
                                            }
                                        } catch (err) {
                                            mostrarModalMensaje('Error de Conexión', 'No se pudo conectar con el servidor: ' + err.message, 'danger');
                                        }
                                    }
                                );
                            }

                            function confirmarEliminarEmpresa(id, razon) {
                                mostrarModalConfirmacion(
                                    'Eliminar Empresa',
                                    '¿Está seguro de eliminar definitivamente a la empresa "' + razon + '"? Esta acción no se puede deshacer y validará que la cuenta esté inactiva y no tenga ofertas asociadas.',
                                    async function () {
                                        const body = new URLSearchParams({
                                            accion: 'eliminar',
                                            recurso: 'empresas',
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
                                                mostrarModalMensaje('Empresa Eliminada', data.mensaje || 'Empresa eliminada con éxito.', 'success', () => window.location.reload());
                                            } else {
                                                mostrarModalMensaje('No se puede eliminar', data.mensaje || 'No se pudo eliminar la empresa.', 'danger');
                                            }
                                        } catch (err) {
                                            mostrarModalMensaje('Error de Conexión', 'No se pudo conectar con el servidor: ' + err.message, 'danger');
                                        }
                                    }
                                );
                            }

                            function confirmarAprobarEmpresaModal() {
                                const btn = document.getElementById('btnModalAprobarEmpresa');
                                if (!btn) return;
                                const id = btn.dataset.id;
                                const razon = btn.dataset.razon;

                                const modalEl = document.getElementById('modalDetalleEmpresa');
                                if (modalEl && window.BootstrapModalCompat) {
                                    const inst = window.BootstrapModalCompat.getOrCreateInstance(modalEl);
                                    inst.hide();
                                }
                                setTimeout(() => {
                                    mostrarModalConfirmacion(
                                        'Aprobar Empresa',
                                        '¿Desea aprobar institucionalmente a la empresa "' + razon + '"? La empresa quedará habilitada para publicar ofertas y gestionar postulaciones.',
                                        async function () {
                                            const body = new URLSearchParams({
                                                accion: 'aprobarEmpresa',
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
                                                    mostrarModalMensaje('Empresa Aprobada', data.mensaje || 'Empresa aprobada exitosamente.', 'success', () => window.location.reload());
                                                } else {
                                                    mostrarModalMensaje('Error al Aprobar', data.mensaje || 'No se pudo aprobar la empresa.', 'danger');
                                                }
                                            } catch (err) {
                                                mostrarModalMensaje('Error de Conexión', 'No se pudo conectar con el servidor: ' + err.message, 'danger');
                                            }
                                        }
                                    );
                                }, 200);
                            }

                            window.confirmarActivarEmpresa = confirmarActivarEmpresa;
                            window.confirmarEliminarEmpresa = confirmarEliminarEmpresa;
                            window.confirmarAprobarEmpresaModal = confirmarAprobarEmpresaModal;
                        </script>

                        <script type="module" src="${pageContext.request.contextPath}/js/script.js"></script>
                    </body>

                    </html>