<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="com.propath.dto.DashboardEmpresasDTO"%>
<%@page import="com.propath.util.HtmlUtil"%>
<%
    DashboardEmpresasDTO dashboard = (DashboardEmpresasDTO) request.getAttribute("dashboard");
    if (dashboard == null) {
        response.sendRedirect(request.getContextPath() + "/admin/empresas/dashboard");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es" data-bs-theme="light">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Dashboard de Empresas | ProPath</title>

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
                                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/empresas" class="text-decoration-none">Empresas</a></li>
                                    <li class="breadcrumb-item active" aria-current="page">Dashboard de Gestión</li>
                                </ol>
                            </nav>
                            <h1 class="h3 fw-bold text-dark mb-0">Dashboard de Empresas</h1>
                            <p class="text-muted small mb-0">Métricas analíticas en tiempo real sobre verificación corporativa, convocatorias y estado operativo</p>
                        </div>
                        <div class="d-flex align-items-center gap-2 flex-wrap">
                            <a href="${pageContext.request.contextPath}/admin/empresas" 
                               class="btn btn-outline-secondary rounded-pill px-3 py-2 fw-semibold shadow-sm">
                                <i class="bi bi-arrow-left me-1"></i> Volver a Empresas
                            </a>
                            <button type="button" id="btnActualizarManual" 
                                    class="btn btn-outline-primary rounded-pill px-3 py-2 fw-semibold shadow-sm">
                                <i class="bi bi-arrow-clockwise me-1"></i> Actualizar datos
                            </button>
                        </div>
                    </div>

                    <!-- Tarjeta de Segmentadores Interactivos (Sin segmentador de fechas) -->
                    <div class="propath-card mb-4 position-relative">
                        <div class="propath-card-header d-flex justify-content-between align-items-center flex-wrap gap-2">
                            <h2 class="propath-card-title mb-0">
                                <i class="bi bi-funnel-fill text-info" style="color: #0891b2 !important;"></i>
                                <span>Segmentadores de Empresas</span>
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
                            <form id="formSegmentadoresEmpresas" class="row g-3 align-items-end" onsubmit="return false;">
                                <div class="col-12 col-md-6 col-xl-3">
                                    <label for="selectEstadoPerfil" class="form-label small fw-semibold text-muted mb-1">Estado de Verificación (Perfil)</label>
                                    <select id="selectEstadoPerfil" name="estadoPerfil" class="form-select form-select-sm segmenter-input">
                                        <option value="TODOS">Todos los perfiles</option>
                                        <option value="APROBADA">Aprobada (Autorizada)</option>
                                        <option value="PENDIENTE">Pendiente de revisión</option>
                                        <option value="RECHAZADA">Rechazada</option>
                                        <option value="SUSPENDIDA">Suspendida</option>
                                    </select>
                                </div>

                                <div class="col-12 col-md-6 col-xl-3">
                                    <label for="selectEstadoCuenta" class="form-label small fw-semibold text-muted mb-1">Estado de la Cuenta de Acceso</label>
                                    <select id="selectEstadoCuenta" name="estadoCuenta" class="form-select form-select-sm segmenter-input">
                                        <option value="TODOS">Todas las cuentas</option>
                                        <option value="ACTIVO">Activo</option>
                                        <option value="INACTIVO">Inactivo (Baja Lógica)</option>
                                        <option value="BLOQUEADO">Bloqueado</option>
                                    </select>
                                </div>

                                <div class="col-12 col-md-6 col-xl-3">
                                    <label for="selectConOfertas" class="form-label small fw-semibold text-muted mb-1">Presencia de Ofertas Publicadas</label>
                                    <select id="selectConOfertas" name="conOfertas" class="form-select form-select-sm segmenter-input">
                                        <option value="TODOS">Todas las empresas</option>
                                        <option value="CON_OFERTAS">Con ofertas registradas</option>
                                        <option value="SIN_OFERTAS">Sin ofertas aún</option>
                                    </select>
                                </div>

                                <div class="col-12 col-md-6 col-xl-3">
                                    <label for="selectConOfertasActivas" class="form-label small fw-semibold text-muted mb-1">Estado activo de las ofertas</label>
                                    <select id="selectConOfertasActivas" name="conOfertasActivas" class="form-select form-select-sm segmenter-input">
                                        <option value="TODOS">Todas</option>
                                        <option value="CON_ACTIVAS">Con ofertas abiertas hoy</option>
                                        <option value="SIN_ACTIVAS">Sin ofertas abiertas hoy</option>
                                    </select>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- Estado Vacío (Zero Results Banner) -->
                    <div id="emptyStateBanner" class="alert alert-info border border-info-subtle rounded-4 p-4 text-center mb-4" style="display: none;">
                        <i class="bi bi-funnel fs-2 text-info d-block mb-2"></i>
                        <h5 class="fw-bold mb-1">No hay resultados para estos filtros</h5>
                        <p class="text-muted small mb-3">Intenta combinando otros segmentadores corporativos o limpia los filtros para ver el universo total de organizaciones.</p>
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
                                    <div class="kpi-icon bg-info-subtle text-info" style="color: #0891b2 !important;">
                                        <i class="bi bi-buildings"></i>
                                    </div>
                                </div>
                                <div class="kpi-value" style="color: #0891b2;" id="kpiTotal"><%= dashboard.getTotalEmpresas() %></div>
                                <div class="text-muted small mt-1">Organizaciones empleadoras</div>
                            </div>
                        </div>

                        <div class="col-12 col-sm-6 col-xl-3">
                            <div class="kpi-card shadow-sm h-100">
                                <div class="d-flex align-items-center justify-content-between mb-2">
                                    <span class="kpi-label mb-0">Perfiles Aprobados</span>
                                    <div class="kpi-icon bg-success-subtle text-success">
                                        <i class="bi bi-patch-check"></i>
                                    </div>
                                </div>
                                <div class="kpi-value text-success" id="kpiAprobadas"><%= dashboard.getAprobadas() %></div>
                                <div class="text-muted small mt-1">
                                    <span id="kpiAprobadasPct"><%= dashboard.getPorcentaje(dashboard.getAprobadas()) + "%" %></span> autorizadas
                                    <span class="mx-1">•</span> <span id="kpiPendientes"><%= dashboard.getPendientes() %></span> pendientes
                                </div>
                            </div>
                        </div>

                        <div class="col-12 col-sm-6 col-xl-3">
                            <div class="kpi-card shadow-sm h-100">
                                <div class="d-flex align-items-center justify-content-between mb-2">
                                    <span class="kpi-label mb-0">Con Ofertas Creadas</span>
                                    <div class="kpi-icon bg-primary-subtle text-primary">
                                        <i class="bi bi-briefcase"></i>
                                    </div>
                                </div>
                                <div class="kpi-value text-primary" id="kpiConOfertas"><%= dashboard.getEmpresasConOfertas() %></div>
                                <div class="text-muted small mt-1">
                                    <span id="kpiConOfertasPct"><%= dashboard.getPorcentaje(dashboard.getEmpresasConOfertas()) + "%" %></span> con convocatorias
                                    <span class="mx-1">•</span> <span id="kpiSinOfertas"><%= dashboard.getEmpresasSinOfertas() %></span> sin ofertas
                                </div>
                            </div>
                        </div>

                        <div class="col-12 col-sm-6 col-xl-3">
                            <div class="kpi-card shadow-sm h-100">
                                <div class="d-flex align-items-center justify-content-between mb-2">
                                    <span class="kpi-label mb-0">Empresas con ofertas activas</span>
                                    <div class="kpi-icon" style="background-color: #fef3c7; color: #d97706;">
                                        <i class="bi bi-lightning-charge"></i>
                                    </div>
                                </div>
                                <div class="kpi-value" style="color: #d97706;" id="kpiConOfertasActivas"><%= dashboard.getEmpresasConOfertasActivas() %></div>
                                <div class="text-muted small mt-1">
                                    Empresas reclutando actualmente
                                </div>
                            </div>
                        </div>
                    </div>

                    <section class="dashboard-chart-grid dashboard-chart-grid--empresas" aria-label="Gráficos del segmento">
                        <article class="dashboard-chart-card dashboard-chart-trend">
                            <h2>Registros por mes</h2>
                            <p>Hasta 12 meses con registros del segmento actual.</p>
                            <div id="graficoAltas"></div>
                        </article>
                        <article class="dashboard-chart-card">
                            <h2>Estado de las cuentas</h2>
                            <p>Distribución de las cuentas dentro del segmento.</p>
                            <div id="graficoCuentas" class="dashboard-chart-donut"></div>
                        </article>
                        <article class="dashboard-chart-card">
                            <h2>Actividad de reclutamiento</h2>
                            <p>Empresas agrupadas según el estado de sus ofertas.</p>
                            <div id="graficoCobertura" class="dashboard-chart-donut"></div>
                        </article>

                        <article class="dashboard-chart-card dashboard-chart-wide">
                            <h2>Empresas con más ofertas</h2>
                            <p>Hasta 8 empresas del segmento, por total histórico de ofertas.</p>
                            <div id="graficoRanking"></div>
                        </article>
                    </section>

                    <!-- Visualizaciones Gráficas -->
                    <div class="row g-3 mb-4">
                        <!-- Estado del Perfil -->
                        <div class="col-12 col-lg-6 col-xl-3">
                            <div class="propath-card h-100 mb-0">
                                <div class="propath-card-header py-3">
                                    <h3 class="propath-card-title fs-6 mb-0">
                                        <i class="bi bi-patch-question text-success"></i>
                                        <span>Estado del Perfil</span>
                                    </h3>
                                </div>
                                <div class="p-3" id="containerEstadoPerfil">
                                    <div class="mb-3">
                                        <div class="d-flex justify-content-between align-items-center small mb-1">
                                            <span class="fw-semibold text-success">Aprobadas</span>
                                            <span class="text-muted"><span id="lblPerfilAprobadas"><%= dashboard.getAprobadas() %></span> (<span id="lblPerfilAprobadasPct"><%= dashboard.getPorcentaje(dashboard.getAprobadas()) + "%" %></span>)</span>
                                        </div>
                                        <div class="progress" style="height: 8px;">
                                            <div id="barPerfilAprobadas" class="progress-bar bg-success" style="width: <%= dashboard.getPorcentaje(dashboard.getAprobadas()) + "%" %>;"></div>
                                        </div>
                                    </div>
                                    <div class="mb-3">
                                        <div class="d-flex justify-content-between align-items-center small mb-1">
                                            <span class="fw-semibold text-warning">Pendientes</span>
                                            <span class="text-muted"><span id="lblPerfilPendientes"><%= dashboard.getPendientes() %></span> (<span id="lblPerfilPendientesPct"><%= dashboard.getPorcentaje(dashboard.getPendientes()) + "%" %></span>)</span>
                                        </div>
                                        <div class="progress" style="height: 8px;">
                                            <div id="barPerfilPendientes" class="progress-bar bg-warning" style="width: <%= dashboard.getPorcentaje(dashboard.getPendientes()) + "%" %>;"></div>
                                        </div>
                                    </div>
                                    <div class="mb-3">
                                        <div class="d-flex justify-content-between align-items-center small mb-1">
                                            <span class="fw-semibold text-danger">Rechazadas</span>
                                            <span class="text-muted"><span id="lblPerfilRechazadas"><%= dashboard.getRechazadas() %></span> (<span id="lblPerfilRechazadasPct"><%= dashboard.getPorcentaje(dashboard.getRechazadas()) + "%" %></span>)</span>
                                        </div>
                                        <div class="progress" style="height: 8px;">
                                            <div id="barPerfilRechazadas" class="progress-bar bg-danger" style="width: <%= dashboard.getPorcentaje(dashboard.getRechazadas()) + "%" %>;"></div>
                                        </div>
                                    </div>
                                    <div class="mb-2">
                                        <div class="d-flex justify-content-between align-items-center small mb-1">
                                            <span class="fw-semibold text-secondary">Suspendidas</span>
                                            <span class="text-muted"><span id="lblPerfilSuspendidas"><%= dashboard.getSuspendidas() %></span> (<span id="lblPerfilSuspendidasPct"><%= dashboard.getPorcentaje(dashboard.getSuspendidas()) + "%" %></span>)</span>
                                        </div>
                                        <div class="progress" style="height: 8px;">
                                            <div id="barPerfilSuspendidas" class="progress-bar bg-secondary" style="width: <%= dashboard.getPorcentaje(dashboard.getSuspendidas()) + "%" %>;"></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Estado de la Cuenta -->
                        <div class="col-12 col-lg-6 col-xl-3">
                            <div class="propath-card h-100 mb-0">
                                <div class="propath-card-header py-3">
                                    <h3 class="propath-card-title fs-6 mb-0">
                                        <i class="bi bi-shield-check text-primary"></i>
                                        <span>Estado de la Cuenta</span>
                                    </h3>
                                </div>
                                <div class="p-3" id="containerEstadoCuenta">
                                    <div class="mb-3">
                                        <div class="d-flex justify-content-between align-items-center small mb-1">
                                            <span class="fw-semibold text-primary">Activas</span>
                                            <span class="text-muted"><span id="lblCuentaActivas"><%= dashboard.getCuentasActivas() %></span> (<span id="lblCuentaActivasPct"><%= dashboard.getPorcentaje(dashboard.getCuentasActivas()) + "%" %></span>)</span>
                                        </div>
                                        <div class="progress" style="height: 8px;">
                                            <div id="barCuentaActivas" class="progress-bar bg-primary" style="width: <%= dashboard.getPorcentaje(dashboard.getCuentasActivas()) + "%" %>;"></div>
                                        </div>
                                    </div>
                                    <div class="mb-3">
                                        <div class="d-flex justify-content-between align-items-center small mb-1">
                                            <span class="fw-semibold text-secondary">Inactivas (Baja Lógica)</span>
                                            <span class="text-muted"><span id="lblCuentaInactivas"><%= dashboard.getCuentasInactivas() %></span> (<span id="lblCuentaInactivasPct"><%= dashboard.getPorcentaje(dashboard.getCuentasInactivas()) + "%" %></span>)</span>
                                        </div>
                                        <div class="progress" style="height: 8px;">
                                            <div id="barCuentaInactivas" class="progress-bar bg-secondary" style="width: <%= dashboard.getPorcentaje(dashboard.getCuentasInactivas()) + "%" %>;"></div>
                                        </div>
                                    </div>
                                    <div class="mb-2">
                                        <div class="d-flex justify-content-between align-items-center small mb-1">
                                            <span class="fw-semibold text-dark">Bloqueadas</span>
                                            <span class="text-muted"><span id="lblCuentaBloqueadas"><%= dashboard.getCuentasBloqueadas() %></span> (<span id="lblCuentaBloqueadasPct"><%= dashboard.getPorcentaje(dashboard.getCuentasBloqueadas()) + "%" %></span>)</span>
                                        </div>
                                        <div class="progress" style="height: 8px;">
                                            <div id="barCuentaBloqueadas" class="progress-bar bg-dark" style="width: <%= dashboard.getPorcentaje(dashboard.getCuentasBloqueadas()) + "%" %>;"></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Presencia de Ofertas -->
                        <div class="col-12 col-lg-6 col-xl-3">
                            <div class="propath-card h-100 mb-0">
                                <div class="propath-card-header py-3">
                                    <h3 class="propath-card-title fs-6 mb-0">
                                        <i class="bi bi-briefcase text-info"></i>
                                        <span>Presencia de Ofertas</span>
                                    </h3>
                                </div>
                                <div class="p-3" id="containerOfertasPresencia">
                                    <div class="mb-3">
                                        <div class="d-flex justify-content-between align-items-center small mb-1">
                                            <span class="fw-semibold text-info">Con Ofertas</span>
                                            <span class="text-muted"><span id="lblOfertasCon"><%= dashboard.getEmpresasConOfertas() %></span> (<span id="lblOfertasConPct"><%= dashboard.getPorcentaje(dashboard.getEmpresasConOfertas()) + "%" %></span>)</span>
                                        </div>
                                        <div class="progress" style="height: 8px;">
                                            <div id="barOfertasCon" class="progress-bar bg-info" style="width: <%= dashboard.getPorcentaje(dashboard.getEmpresasConOfertas()) + "%" %>;"></div>
                                        </div>
                                    </div>
                                    <div class="mb-3">
                                        <div class="d-flex justify-content-between align-items-center small mb-1">
                                            <span class="fw-semibold text-muted">Sin Ofertas</span>
                                            <span class="text-muted"><span id="lblOfertasSin"><%= dashboard.getEmpresasSinOfertas() %></span> (<span id="lblOfertasSinPct"><%= dashboard.getPorcentaje(dashboard.getEmpresasSinOfertas()) + "%" %></span>)</span>
                                        </div>
                                        <div class="progress" style="height: 8px;">
                                            <div id="barOfertasSin" class="progress-bar bg-warning" style="width: <%= dashboard.getPorcentaje(dashboard.getEmpresasSinOfertas()) + "%" %>;"></div>
                                        </div>
                                    </div>
                                    <div class="mb-2 pt-2 border-top">
                                        <div class="d-flex justify-content-between align-items-center small mb-1">
                                            <span class="fw-semibold text-success">Con Empresas con ofertas activas</span>
                                            <span class="badge bg-success-subtle text-success px-2 py-1 rounded-pill"><span id="lblOfertasActivas"><%= dashboard.getEmpresasConOfertasActivas() %></span></span>
                                        </div>
                                        <small class="text-muted d-block" style="font-size: 11px;">Empresas con al menos una oferta en estado ACTIVA</small>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Ranking de Empresas por Ofertas Creadas (Total histórico) -->
                        <div class="col-12 col-lg-6 col-xl-3">
                            <div class="propath-card h-100 mb-0">
                                <div class="propath-card-header py-3">
                                    <h3 class="propath-card-title fs-6 mb-0">
                                        <i class="bi bi-trophy text-warning"></i>
                                        <span>Ranking por Ofertas</span>
                                    </h3>
                                </div>
                                <div class="p-3">
                                    <small class="text-muted d-block mb-3" style="font-size: 11px;">Top organizaciones por volumen total de ofertas creadas (acumulado histórico):</small>
                                    <div id="containerRankingEmpresas">
                                        <% if (dashboard.getRankingEmpresasOfertas().isEmpty()) { %>
                                            <p class="text-muted small text-center my-4">No hay ofertas registradas</p>
                                        <% } else {
                                            for (Map.Entry<String, Integer> entry : dashboard.getRankingEmpresasOfertas().entrySet()) { 
                                        %>
                                            <div class="d-flex justify-content-between align-items-center py-1 border-bottom border-light">
                                                <span class="text-truncate fw-semibold small me-2" title="<%= HtmlUtil.escape(entry.getKey()) %>">
                                                    <%= HtmlUtil.escape(entry.getKey()) %>
                                                </span>
                                                <span class="badge bg-primary-subtle text-primary rounded-pill px-2 py-1 fw-bold">
                                                    <%= entry.getValue() %> <%= entry.getValue() == 1 ? "oferta" : "ofertas" %>
                                                </span>
                                            </div>
                                        <%  } 
                                           } %>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/js/bootstrap.bundle.min.js"></script>
        <script src="${pageContext.request.contextPath}/js/dashboard-graficos.js"></script>
        <script>
            let activeEmpresaController = null;
            let activeEmpresaReqId = 0;

            function escapeHtml(text) {
                if (!text) return '';
                const map = {
                    '&': '&amp;',
                    '<': '&lt;',
                    '>': '&gt;',
                    '"': '&quot;',
                    "'": '&#039;'
                };
                return text.toString().replace(/[&<>"']/g, function(m) { return map[m]; });
            }

            function calcPct(val, total) {
                if (!total || total <= 0) return 0;
                return Math.round((val * 100) / total);
            }

            function verificarFiltrosActivos() {
                const ep = document.getElementById('selectEstadoPerfil').value;
                const ec = document.getElementById('selectEstadoCuenta').value;
                const co = document.getElementById('selectConOfertas').value;
                const ca = document.getElementById('selectConOfertasActivas').value;

                const activos = (ep !== 'TODOS') || (ec !== 'TODOS') || (co !== 'TODOS') || (ca !== 'TODOS');
                const badge = document.getElementById('badgeFiltrosActivos');
                if (badge) {
                    badge.style.display = activos ? 'inline-block' : 'none';
                }
            }

            async function actualizarDashboardEmpresas() {
                verificarFiltrosActivos();

                if (activeEmpresaController) {
                    activeEmpresaController.abort();
                }
                activeEmpresaController = new AbortController();
                const signal = activeEmpresaController.signal;
                const currentReqId = ++activeEmpresaReqId;

                const params = new URLSearchParams({
                    estadoPerfil: document.getElementById('selectEstadoPerfil').value,
                    estadoCuenta: document.getElementById('selectEstadoCuenta').value,
                    conOfertas: document.getElementById('selectConOfertas').value,
                    conOfertasActivas: document.getElementById('selectConOfertasActivas').value
                });

                try {
                    const url = '${pageContext.request.contextPath}/admin/api/empresas-dashboard?' + params.toString();
                    const resp = await fetch(url, { signal });
                    if (!resp.ok) {
                        throw new Error('Error al consultar el servidor (' + resp.status + ')');
                    }
                    const data = await resp.json();

                    if (currentReqId !== activeEmpresaReqId) {
                        return; // Petición obsoleta descartada
                    }

                    renderDashboardEmpresas(data);
                } catch (err) {
                    if (err.name === 'AbortError') {
                        return; // Cancelación esperada
                    }
                    console.error('Error actualizando dashboard de empresas:', err);
                }
            }

            function renderDashboardEmpresas(data) {
                window.graficosPropath.empresas(data);
                const total = data.total || 0;
                const emptyBanner = document.getElementById('emptyStateBanner');

                if (total === 0) {
                    if (emptyBanner) emptyBanner.style.display = 'block';
                } else {
                    if (emptyBanner) emptyBanner.style.display = 'none';
                }

                // KPIs
                const aprobadas = data.aprobadas || 0;
                const pendientes = data.pendientes || 0;
                const rechazadas = data.rechazadas || 0;
                const suspendidas = data.suspendidas || 0;

                const cuentasActivas = data.cuentasActivas || 0;
                const cuentasInactivas = data.cuentasInactivas || 0;
                const cuentasBloqueadas = data.cuentasBloqueadas || 0;

                const conOfertas = data.conOfertas || 0;
                const sinOfertas = data.sinOfertas || 0;
                const conOfertasActivas = data.conOfertasActivas || 0;

                const aprobadasPct = calcPct(aprobadas, total);
                const conOfertasPct = calcPct(conOfertas, total);

                document.getElementById('kpiTotal').textContent = total;
                document.getElementById('kpiAprobadas').textContent = aprobadas;
                document.getElementById('kpiAprobadasPct').textContent = aprobadasPct + '%';
                document.getElementById('kpiPendientes').textContent = pendientes;

                document.getElementById('kpiConOfertas').textContent = conOfertas;
                document.getElementById('kpiConOfertasPct').textContent = conOfertasPct + '%';
                document.getElementById('kpiSinOfertas').textContent = sinOfertas;

                document.getElementById('kpiConOfertasActivas').textContent = conOfertasActivas;

                // Estado Perfil
                const pendientesPct = calcPct(pendientes, total);
                const rechazadasPct = calcPct(rechazadas, total);
                const suspendidasPct = calcPct(suspendidas, total);

                document.getElementById('lblPerfilAprobadas').textContent = aprobadas;
                document.getElementById('lblPerfilAprobadasPct').textContent = aprobadasPct + '%';
                document.getElementById('lblPerfilPendientes').textContent = pendientes;
                document.getElementById('lblPerfilPendientesPct').textContent = pendientesPct + '%';
                document.getElementById('lblPerfilRechazadas').textContent = rechazadas;
                document.getElementById('lblPerfilRechazadasPct').textContent = rechazadasPct + '%';
                document.getElementById('lblPerfilSuspendidas').textContent = suspendidas;
                document.getElementById('lblPerfilSuspendidasPct').textContent = suspendidasPct + '%';

                document.getElementById('barPerfilAprobadas').style.width = aprobadasPct + '%';
                document.getElementById('barPerfilPendientes').style.width = pendientesPct + '%';
                document.getElementById('barPerfilRechazadas').style.width = rechazadasPct + '%';
                document.getElementById('barPerfilSuspendidas').style.width = suspendidasPct + '%';

                // Estado Cuenta
                const cuentasActivasPct = calcPct(cuentasActivas, total);
                const cuentasInactivasPct = calcPct(cuentasInactivas, total);
                const cuentasBloqueadasPct = calcPct(cuentasBloqueadas, total);

                document.getElementById('lblCuentaActivas').textContent = cuentasActivas;
                document.getElementById('lblCuentaActivasPct').textContent = cuentasActivasPct + '%';
                document.getElementById('lblCuentaInactivas').textContent = cuentasInactivas;
                document.getElementById('lblCuentaInactivasPct').textContent = cuentasInactivasPct + '%';
                document.getElementById('lblCuentaBloqueadas').textContent = cuentasBloqueadas;
                document.getElementById('lblCuentaBloqueadasPct').textContent = cuentasBloqueadasPct + '%';

                document.getElementById('barCuentaActivas').style.width = cuentasActivasPct + '%';
                document.getElementById('barCuentaInactivas').style.width = cuentasInactivasPct + '%';
                document.getElementById('barCuentaBloqueadas').style.width = cuentasBloqueadasPct + '%';

                // Ofertas Presencia
                const sinOfertasPct = calcPct(sinOfertas, total);
                document.getElementById('lblOfertasCon').textContent = conOfertas;
                document.getElementById('lblOfertasConPct').textContent = conOfertasPct + '%';
                document.getElementById('lblOfertasSin').textContent = sinOfertas;
                document.getElementById('lblOfertasSinPct').textContent = sinOfertasPct + '%';
                document.getElementById('lblOfertasActivas').textContent = conOfertasActivas;

                document.getElementById('barOfertasCon').style.width = conOfertasPct + '%';
                document.getElementById('barOfertasSin').style.width = sinOfertasPct + '%';

                // Ranking de Empresas
                const contRanking = document.getElementById('containerRankingEmpresas');
                if (contRanking) {
                    if (!data.rankingEmpresasOfertas || data.rankingEmpresasOfertas.length === 0) {
                        contRanking.innerHTML = '<p class="text-muted small text-center my-4">No hay ofertas registradas</p>';
                    } else {
                        let html = '';
                        data.rankingEmpresasOfertas.forEach(item => {
                            const word = item.ofertas === 1 ? 'oferta' : 'ofertas';
                            html += '<div class="d-flex justify-content-between align-items-center py-1 border-bottom border-light">' +
                                        '<span class="text-truncate fw-semibold small me-2" title="' + escapeHtml(item.empresa) + '">' +
                                            escapeHtml(item.empresa) +
                                        '</span>' +
                                        '<span class="badge bg-primary-subtle text-primary rounded-pill px-2 py-1 fw-bold">' +
                                            item.ofertas + ' ' + word +
                                        '</span>' +
                                    '</div>';
                        });
                        contRanking.innerHTML = html;
                    }
                }
            }

            function limpiarFiltros() {
                document.getElementById('selectEstadoPerfil').value = 'TODOS';
                document.getElementById('selectEstadoCuenta').value = 'TODOS';
                document.getElementById('selectConOfertas').value = 'TODOS';
                document.getElementById('selectConOfertasActivas').value = 'TODOS';
                actualizarDashboardEmpresas();
            }

            document.addEventListener('DOMContentLoaded', function() {
                document.querySelectorAll('.segmenter-input').forEach(function(el) {
                    el.addEventListener('change', actualizarDashboardEmpresas);
                });

                const btnLimpiar = document.getElementById('btnLimpiarFiltros');
                if (btnLimpiar) {
                    btnLimpiar.addEventListener('click', limpiarFiltros);
                }

                const btnActualizar = document.getElementById('btnActualizarManual');
                if (btnActualizar) {
                    btnActualizar.addEventListener('click', actualizarDashboardEmpresas);
                }
                actualizarDashboardEmpresas();
            });
        </script>
    </body>
</html>
