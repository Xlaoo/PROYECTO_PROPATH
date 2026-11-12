<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="com.propath.dto.DashboardAdministradoresDTO"%>
<%@page import="com.propath.modelo.Administrador"%>
<%@page import="com.propath.modelo.Usuario"%>
<%@page import="com.propath.util.HtmlUtil"%>
<%
    Usuario uAdminLogueado = (Usuario) session.getAttribute("usuarioLogueado");
    if (uAdminLogueado == null || !"ADMIN".equalsIgnoreCase(uAdminLogueado.getRol()) || !uAdminLogueado.esAdminPrincipal()) {
        response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=acceso_denegado");
        return;
    }

    DashboardAdministradoresDTO dashboard = (DashboardAdministradoresDTO) request.getAttribute("dashboard");
    if (dashboard == null) {
        response.sendRedirect(request.getContextPath() + "/admin/administradores/dashboard");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es" data-bs-theme="light">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Dashboard de Administradores | ProPath</title>

        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

        <link href="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard-graficos.css">
    </head>
    <body>
        <div class="admin-layout">
            <jsp:include page="/admin/includes/sidebar.jsp" />

            <main class="admin-main">
                <jsp:include page="/admin/includes/topbar.jsp" />

                <div class="admin-content position-relative">
                    <!-- Cabecera de Página -->
                    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
                        <div>
                            <nav aria-label="breadcrumb">
                                <ol class="breadcrumb mb-1 small">
                                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/dashboard" class="text-decoration-none">Inicio</a></li>
                                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/administradores" class="text-decoration-none">Administradores</a></li>
                                    <li class="breadcrumb-item active" aria-current="page">Dashboard de Gestión</li>
                                </ol>
                            </nav>
                            <h1 class="h3 fw-bold text-dark mb-0">Dashboard de Administradores</h1>
                            <p class="text-muted small mb-0">Métricas analíticas en tiempo real sobre administradores, cargos y estado de cuentas</p>
                        </div>
                        <div class="d-flex align-items-center gap-2 flex-wrap">
                            <a href="${pageContext.request.contextPath}/admin/administradores" 
                               class="btn btn-outline-secondary rounded-pill px-3 py-2 fw-semibold shadow-sm">
                                <i class="bi bi-arrow-left me-1"></i> Volver a Administradores
                            </a>
                            <button type="button" id="btnActualizarManual" 
                                    class="btn btn-outline-primary rounded-pill px-3 py-2 fw-semibold shadow-sm">
                                <i class="bi bi-arrow-clockwise me-1"></i> Actualizar datos
                            </button>
                        </div>
                    </div>

                    <!-- Tarjeta de Segmentadores Interactivos -->
                    <div class="propath-card mb-4 position-relative">
                        <div class="propath-card-header d-flex justify-content-between align-items-center flex-wrap gap-2">
                            <h2 class="propath-card-title mb-0">
                                <i class="bi bi-funnel-fill text-primary"></i>
                                <span>Segmentadores de Administradores</span>
                            </h2>
                            <div class="d-flex align-items-center gap-2">
                                <span id="badgeFiltrosActivos" class="badge bg-warning-subtle text-warning border border-warning-subtle px-3 py-1 rounded-pill fw-semibold" style="display: none;">
                                    <i class="bi bi-filter me-1"></i> Filtros aplicados
                                </span>
                                <button type="button" id="btnLimpiarFiltros" class="btn btn-sm btn-outline-secondary rounded-pill px-3 fw-semibold">
                                    <i class="bi bi-x-circle me-1"></i> Limpiar filtros
                                </button>
                            </div>
                        </div>
                        <div class="p-3">
                            <form id="formSegmentadoresAdministradores" class="row g-3 align-items-end" onsubmit="return false;">
                                <div class="col-12 col-md-6">
                                    <label for="selectEstado" class="form-label small fw-semibold text-muted mb-1">Estado de Cuenta</label>
                                    <select id="selectEstado" name="estado" class="form-select form-select-sm segmenter-input">
                                        <option value="TODOS">Todos los estados</option>
                                        <option value="ACTIVO">Activo</option>
                                        <option value="INACTIVO">Inactivo (Baja Lógica)</option>
                                        <option value="BLOQUEADO">Bloqueado</option>
                                    </select>
                                </div>

                                <div class="col-12 col-md-6">
                                    <label for="selectCargo" class="form-label small fw-semibold text-muted mb-1">Cargo o Función</label>
                                    <select id="selectCargo" name="cargo" class="form-select form-select-sm segmenter-input">
                                        <option value="TODOS">Todos los cargos</option>
                                        <% for (String c : dashboard.getOpcionesCargos()) { %>
                                            <option value="<%= HtmlUtil.escape(c) %>"><%= HtmlUtil.escape(c) %></option>
                                        <% } %>
                                    </select>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- Estado Vacío (Zero Results Banner) -->
                    <div id="emptyStateBanner" class="alert alert-info border border-info-subtle rounded-4 p-4 text-center mb-4" style="display: none;">
                        <i class="bi bi-funnel fs-2 text-info d-block mb-2"></i>
                        <h5 class="fw-bold mb-1">No hay administradores en este segmento</h5>
                        <p class="text-muted small mb-3">Intenta combinando otros filtros de estado o cargo, o limpia la segmentación para ver todas las cuentas.</p>
                        <button type="button" class="btn btn-outline-info rounded-pill px-4 fw-semibold btn-sm" onclick="limpiarFiltros()">
                            <i class="bi bi-arrow-counterclockwise me-1"></i> Restablecer filtros
                        </button>
                    </div>

                    <!-- Tarjetas KPI -->
                    <div class="row g-3 mb-4">
                        <div class="col-12 col-sm-6 col-xl-3">
                            <div class="kpi-card shadow-sm h-100">
                                <div class="d-flex align-items-center justify-content-between mb-2">
                                    <span class="kpi-label mb-0">Total del Segmento</span>
                                    <div class="kpi-icon bg-primary-subtle text-primary">
                                        <i class="bi bi-shield-lock"></i>
                                    </div>
                                </div>
                                <div class="kpi-value text-primary" id="kpiTotal"><%= dashboard.getTotalAdministradores() %></div>
                                <div class="text-muted small mt-1">Administradores registrados</div>
                            </div>
                        </div>

                        <div class="col-12 col-sm-6 col-xl-3">
                            <div class="kpi-card shadow-sm h-100">
                                <div class="d-flex align-items-center justify-content-between mb-2">
                                    <span class="kpi-label mb-0">Cuentas Activas</span>
                                    <div class="kpi-icon bg-success-subtle text-success">
                                        <i class="bi bi-person-check"></i>
                                    </div>
                                </div>
                                <div class="kpi-value text-success" id="kpiActivos"><%= dashboard.getActivos() %></div>
                                <div class="text-muted small mt-1">
                                    <span id="kpiActivosPct"><%= dashboard.getPorcentaje(dashboard.getActivos()) + "%" %></span> del segmento
                                </div>
                            </div>
                        </div>

                        <div class="col-12 col-sm-6 col-xl-3">
                            <div class="kpi-card shadow-sm h-100">
                                <div class="d-flex align-items-center justify-content-between mb-2">
                                    <span class="kpi-label mb-0">Cuentas Inactivas</span>
                                    <div class="kpi-icon bg-secondary-subtle text-secondary">
                                        <i class="bi bi-person-dash"></i>
                                    </div>
                                </div>
                                <div class="kpi-value text-secondary" id="kpiInactivos"><%= dashboard.getInactivos() %></div>
                                <div class="text-muted small mt-1">
                                    <span id="kpiInactivosPct"><%= dashboard.getPorcentaje(dashboard.getInactivos()) + "%" %></span> bajas lógicas
                                </div>
                            </div>
                        </div>

                        <div class="col-12 col-sm-6 col-xl-3">
                            <div class="kpi-card shadow-sm h-100">
                                <div class="d-flex align-items-center justify-content-between mb-2">
                                    <span class="kpi-label mb-0">Cuentas Bloqueadas</span>
                                    <div class="kpi-icon bg-danger-subtle text-danger">
                                        <i class="bi bi-slash-circle"></i>
                                    </div>
                                </div>
                                <div class="kpi-value text-danger" id="kpiBloqueados"><%= dashboard.getBloqueados() %></div>
                                <div class="text-muted small mt-1">
                                    <span id="kpiBloqueadosPct"><%= dashboard.getPorcentaje(dashboard.getBloqueados()) + "%" %></span> por seguridad
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Sección de Gráficos (Dona por Estado y Barras por Cargo) -->
                    <section class="dashboard-chart-grid" aria-label="Gráficos del segmento">
                        <article class="dashboard-chart-card" style="grid-column: span 6;">
                            <h2>Distribución por estado</h2>
                            <p>Proporción de cuentas activas, inactivas y bloqueadas del segmento.</p>
                            <div id="graficoEstado" class="dashboard-chart-donut"></div>
                        </article>
                        <article class="dashboard-chart-card" style="grid-column: span 6;">
                            <h2>Distribución por cargo</h2>
                            <p>Cantidad de administradores asignados a cada función o cargo.</p>
                            <div id="graficoCargos"></div>
                        </article>
                    </section>

                    <!-- Tabla de Administradores en el Segmento Filtrado -->
                    <div class="propath-card">
                        <div class="propath-card-header d-flex justify-content-between align-items-center flex-wrap gap-2">
                            <h3 class="propath-card-title fs-6 mb-0">
                                <i class="bi bi-people text-primary"></i>
                                <span>Administradores en el Segmento Actual</span>
                            </h3>
                            <span class="badge bg-light text-dark border px-3 py-1" id="badgeSegmentoCount">
                                <%= dashboard.getTotalAdministradores() %> cuentas
                            </span>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-propath mb-0" id="tablaSegmentoAdmins">
                                <thead>
                                    <tr>
                                        <th>Nombre Completo</th>
                                        <th>Correo Electrónico</th>
                                        <th>Cargo</th>
                                        <th>Estado</th>
                                    </tr>
                                </thead>
                                <tbody id="tbodySegmentoAdmins">
                                    <% if (dashboard.getAdministradoresFiltrados().isEmpty()) { %>
                                    <tr>
                                        <td colspan="4" class="text-center py-4 text-muted">
                                            <i class="bi bi-inbox fs-3 d-block mb-1 text-secondary"></i>
                                            <span>No hay administradores que coincidan con los filtros seleccionados</span>
                                        </td>
                                    </tr>
                                    <% } else {
                                        for (Administrador a : dashboard.getAdministradoresFiltrados()) {
                                            String est = a.getEstado() != null ? a.getEstado().toUpperCase() : "ACTIVO";
                                            boolean esAct = "ACTIVO".equals(est);
                                            String inicialN = (a.getNombre() != null && !a.getNombre().isEmpty()) ? a.getNombre().substring(0, 1).toUpperCase() : "A";
                                            String inicialA = (a.getApellidos() != null && !a.getApellidos().isEmpty()) ? a.getApellidos().substring(0, 1).toUpperCase() : "";
                                    %>
                                    <tr>
                                        <td>
                                            <div class="d-flex align-items-center gap-2">
                                                <div class="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white shadow-sm <%= esAct ? "avatar-bg-activo" : "avatar-bg-inactivo" %>"
                                                     style="width: 32px; height: 32px; font-size: 0.78rem;">
                                                    <%= inicialN %><%= inicialA %>
                                                </div>
                                                <span class="fw-semibold text-dark"><%= HtmlUtil.escape(a.getNombreCompleto()) %></span>
                                            </div>
                                        </td>
                                        <td>
                                            <span class="text-muted small"><%= HtmlUtil.escape(a.getCorreo()) %></span>
                                        </td>
                                        <td>
                                            <span class="badge bg-light text-dark border px-2 py-1"><%= HtmlUtil.escape(a.getCargo()) %></span>
                                        </td>
                                        <td>
                                            <span class="badge-status <%= esAct ? "badge-activo" : "badge-bloqueado" %>">
                                                <%= est %>
                                            </span>
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

        <script src="${pageContext.request.contextPath}/js/dashboard-graficos.js"></script>
        <script>
            function calcPct(val, total) {
                if (!total || total <= 0) return 0;
                return Math.round((val * 100) / total);
            }

            function escapeHtml(str) {
                if (!str) return '';
                return String(str)
                    .replace(/&/g, '&amp;')
                    .replace(/</g, '&lt;')
                    .replace(/>/g, '&gt;')
                    .replace(/"/g, '&quot;')
                    .replace(/'/g, '&#39;');
            }

            async function actualizarDashboardAdministradores() {
                const estado = document.getElementById('selectEstado').value;
                const cargo = document.getElementById('selectCargo').value;

                const badgeFiltros = document.getElementById('badgeFiltrosActivos');
                if (badgeFiltros) {
                    const activos = (estado !== 'TODOS' || cargo !== 'TODOS');
                    badgeFiltros.style.display = activos ? 'inline-flex' : 'none';
                }

                const params = new URLSearchParams({ estado, cargo });

                try {
                    const res = await fetch('${pageContext.request.contextPath}/admin/api/administradores-dashboard?' + params.toString(), {
                        headers: { 'Accept': 'application/json' }
                    });
                    if (!res.ok) throw new Error('HTTP ' + res.status);
                    const data = await res.json();
                    renderDashboardAdministradores(data);
                } catch (err) {
                    console.error('Error al actualizar dashboard de administradores:', err);
                }
            }

            function renderDashboardAdministradores(data) {
                // Actualizar gráficos usando dashboard-graficos.js
                if (window.graficosPropath && window.graficosPropath.administradores) {
                    window.graficosPropath.administradores(data);
                }

                const total = data.total || 0;
                const emptyBanner = document.getElementById('emptyStateBanner');
                if (emptyBanner) {
                    emptyBanner.style.display = (total === 0) ? 'block' : 'none';
                }

                const activos = data.activos || 0;
                const inactivos = data.inactivos || 0;
                const bloqueados = data.bloqueados || 0;

                const activosPct = calcPct(activos, total);
                const inactivosPct = calcPct(inactivos, total);
                const bloqueadosPct = calcPct(bloqueados, total);

                document.getElementById('kpiTotal').textContent = total;
                document.getElementById('kpiActivos').textContent = activos;
                document.getElementById('kpiActivosPct').textContent = activosPct + '%';
                document.getElementById('kpiInactivos').textContent = inactivos;
                document.getElementById('kpiInactivosPct').textContent = inactivosPct + '%';
                document.getElementById('kpiBloqueados').textContent = bloqueados;
                document.getElementById('kpiBloqueadosPct').textContent = bloqueadosPct + '%';

                const badgeCount = document.getElementById('badgeSegmentoCount');
                if (badgeCount) {
                    badgeCount.textContent = total + ' cuentas';
                }

                // Renderizar tabla de administradores del segmento
                const tbody = document.getElementById('tbodySegmentoAdmins');
                if (tbody) {
                    if (!data.administradores || data.administradores.length === 0) {
                        tbody.innerHTML = '<tr><td colspan="4" class="text-center py-4 text-muted">' +
                            '<i class="bi bi-inbox fs-3 d-block mb-1 text-secondary"></i>' +
                            '<span>No hay administradores que coincidan con los filtros seleccionados</span></td></tr>';
                    } else {
                        let html = '';
                        data.administradores.forEach(function (a) {
                            const est = (a.estado || 'ACTIVO').toUpperCase();
                            const esAct = (est === 'ACTIVO');
                            const iniN = (a.nombre && a.nombre.length > 0) ? a.nombre.substring(0, 1).toUpperCase() : 'A';
                            const iniA = (a.apellidos && a.apellidos.length > 0) ? a.apellidos.substring(0, 1).toUpperCase() : '';

                            html += '<tr>' +
                                '<td><div class="d-flex align-items-center gap-2">' +
                                '<div class="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white shadow-sm ' +
                                (esAct ? 'avatar-bg-activo' : 'avatar-bg-inactivo') + '" style="width: 32px; height: 32px; font-size: 0.78rem;">' +
                                iniN + iniA + '</div>' +
                                '<span class="fw-semibold text-dark">' + escapeHtml(a.nombreCompleto) + '</span>' +
                                '</div></td>' +
                                '<td><span class="text-muted small">' + escapeHtml(a.correo) + '</span></td>' +
                                '<td><span class="badge bg-light text-dark border px-2 py-1">' + escapeHtml(a.cargo) + '</span></td>' +
                                '<td><span class="badge-status ' + (esAct ? 'badge-activo' : 'badge-bloqueado') + '">' + est + '</span></td>' +
                                '</tr>';
                        });
                        tbody.innerHTML = html;
                    }
                }
            }

            function limpiarFiltros() {
                document.getElementById('selectEstado').value = 'TODOS';
                document.getElementById('selectCargo').value = 'TODOS';
                actualizarDashboardAdministradores();
            }

            document.addEventListener('DOMContentLoaded', function () {
                document.querySelectorAll('.segmenter-input').forEach(function (el) {
                    el.addEventListener('change', actualizarDashboardAdministradores);
                });

                const btnLimpiar = document.getElementById('btnLimpiarFiltros');
                if (btnLimpiar) {
                    btnLimpiar.addEventListener('click', limpiarFiltros);
                }

                const btnActualizar = document.getElementById('btnActualizarManual');
                if (btnActualizar) {
                    btnActualizar.addEventListener('click', actualizarDashboardAdministradores);
                }

                // Render inicial
                actualizarDashboardAdministradores();
            });
        </script>
        <script type="module" src="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/js/bootstrap.bundle.min.js"></script>
        <script type="module" src="${pageContext.request.contextPath}/js/script.js"></script>
    </body>
</html>

