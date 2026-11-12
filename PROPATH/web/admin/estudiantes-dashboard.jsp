<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="com.propath.dto.DashboardEstudiantesDTO"%>
<%@page import="com.propath.util.HtmlUtil"%>
<%
    DashboardEstudiantesDTO dashboard = (DashboardEstudiantesDTO) request.getAttribute("dashboard");
    if (dashboard == null) {
        response.sendRedirect(request.getContextPath() + "/admin/estudiantes/dashboard");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es" data-bs-theme="light">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Dashboard de Estudiantes | ProPath</title>

        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

        <link href="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
        <style>
            .cycle-col {
                transition: height 0.3s ease;
            }
            .loading-overlay {
                position: absolute;
                top: 0; left: 0; right: 0; bottom: 0;
                background: rgba(255, 255, 255, 0.65);
                backdrop-filter: blur(1px);
                z-index: 20;
                display: none;
                align-items: center;
                justify-content: center;
                border-radius: inherit;
            }
        </style>
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
                                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/estudiantes" class="text-decoration-none">Estudiantes</a></li>
                                    <li class="breadcrumb-item active" aria-current="page">Dashboard de Gestión</li>
                                </ol>
                            </nav>
                            <h1 class="h3 fw-bold text-dark mb-0">Dashboard de Estudiantes</h1>
                            <p class="text-muted small mb-0">Métricas analíticas en tiempo real sobre postulantes, ciclo, carrera y perfil académico</p>
                        </div>
                        <div class="d-flex align-items-center gap-2 flex-wrap">
                            <a href="${pageContext.request.contextPath}/admin/estudiantes" 
                               class="btn btn-outline-secondary rounded-pill px-3 py-2 fw-semibold shadow-sm">
                                <i class="bi bi-arrow-left me-1"></i> Volver a Estudiantes
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
                                <i class="bi bi-funnel-fill text-primary"></i>
                                <span>Segmentadores de Estudiantes</span>
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
                            <form id="formSegmentadoresEstudiantes" class="row g-3 align-items-end" onsubmit="return false;">
                                <div class="col-12 col-md-6 col-xl-3">
                                    <label for="selectUniversidad" class="form-label small fw-semibold text-muted mb-1">Universidad</label>
                                    <select id="selectUniversidad" name="universidad" class="form-select form-select-sm segmenter-input">
                                        <option value="TODOS">Todas las universidades</option>
                                        <% for (String u : dashboard.getOpcionesUniversidades()) { %>
                                            <option value="<%= HtmlUtil.escape(u) %>"><%= HtmlUtil.escape(u) %></option>
                                        <% } %>
                                    </select>
                                </div>

                                <div class="col-12 col-md-6 col-xl-3">
                                    <label for="selectCarrera" class="form-label small fw-semibold text-muted mb-1">Carrera Profesional</label>
                                    <select id="selectCarrera" name="carrera" class="form-select form-select-sm segmenter-input">
                                        <option value="TODOS">Todas las carreras</option>
                                        <% for (String c : dashboard.getOpcionesCarreras()) { %>
                                            <option value="<%= HtmlUtil.escape(c) %>"><%= HtmlUtil.escape(c) %></option>
                                        <% } %>
                                    </select>
                                </div>

                                <div class="col-6 col-md-4 col-xl-2">
                                    <label for="selectCiclo" class="form-label small fw-semibold text-muted mb-1">Ciclo Académico</label>
                                    <select id="selectCiclo" name="ciclo" class="form-select form-select-sm segmenter-input">
                                        <option value="0">Todos los ciclos</option>
                                        <% for (int cic = 1; cic <= 12; cic++) { %>
                                            <option value="<%= cic %>">Ciclo <%= cic %></option>
                                        <% } %>
                                    </select>
                                </div>

                                <div class="col-6 col-md-4 col-xl-2">
                                    <label for="selectEstadoCuenta" class="form-label small fw-semibold text-muted mb-1">Estado de Cuenta</label>
                                    <select id="selectEstadoCuenta" name="estadoCuenta" class="form-select form-select-sm segmenter-input">
                                        <option value="TODOS">Todos los estados</option>
                                        <option value="ACTIVO">Activo</option>
                                        <option value="INACTIVO">Inactivo (Baja Lógica)</option>
                                        <option value="BLOQUEADO">Bloqueado</option>
                                    </select>
                                </div>

                                <div class="col-12 col-md-4 col-xl-2">
                                    <label for="selectCv" class="form-label small fw-semibold text-muted mb-1">Curriculum Vitae (CV)</label>
                                    <select id="selectCv" name="cv" class="form-select form-select-sm segmenter-input">
                                        <option value="TODOS">Todos</option>
                                        <option value="CON_CV">Con CV adjunto</option>
                                        <option value="SIN_CV">Sin CV</option>
                                    </select>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- Estado Vacío (Zero Results Banner) -->
                    <div id="emptyStateBanner" class="alert alert-info border border-info-subtle rounded-4 p-4 text-center mb-4" style="display: none;">
                        <i class="bi bi-funnel fs-2 text-info d-block mb-2"></i>
                        <h5 class="fw-bold mb-1">No hay resultados para estos filtros</h5>
                        <p class="text-muted small mb-3">Intenta combinando otros segmentadores académicos o limpia la segmentación para ver el universo total de postulantes.</p>
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
                                        <i class="bi bi-mortarboard"></i>
                                    </div>
                                </div>
                                <div class="kpi-value text-primary" id="kpiTotal"><%= dashboard.getTotalEstudiantes() %></div>
                                <div class="text-muted small mt-1">Postulantes registrados</div>
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
                                    <span class="mx-1">•</span> <span id="kpiInactivos"><%= dashboard.getInactivos() %></span> inactivas
                                    <span class="mx-1">•</span> <span id="kpiBloqueados"><%= dashboard.getBloqueados() %></span> bloq.
                                </div>
                            </div>
                        </div>

                        <div class="col-12 col-sm-6 col-xl-3">
                            <div class="kpi-card shadow-sm h-100">
                                <div class="d-flex align-items-center justify-content-between mb-2">
                                    <span class="kpi-label mb-0">Con CV Adjunto</span>
                                    <div class="kpi-icon bg-info-subtle text-info">
                                        <i class="bi bi-file-earmark-pdf"></i>
                                    </div>
                                </div>
                                <div class="kpi-value text-info" id="kpiConCv"><%= dashboard.getConCv() %></div>
                                <div class="text-muted small mt-1">
                                    <span id="kpiConCvPct"><%= dashboard.getPorcentaje(dashboard.getConCv()) + "%" %></span> con perfil postulable
                                    <span class="mx-1">•</span> <span id="kpiSinCv"><%= dashboard.getSinCv() %></span> sin CV
                                </div>
                            </div>
                        </div>

                        <div class="col-12 col-sm-6 col-xl-3">
                            <div class="kpi-card shadow-sm h-100">
                                <div class="d-flex align-items-center justify-content-between mb-2">
                                    <span class="kpi-label mb-0">Diversidad Académica</span>
                                    <div class="kpi-icon" style="background-color: #f3e8ff; color: #7e22ce;">
                                        <i class="bi bi-buildings"></i>
                                    </div>
                                </div>
                                <div class="kpi-value" style="color: #7e22ce;" id="kpiUniversidades"><%= dashboard.getTotalUniversidades() %></div>
                                <div class="text-muted small mt-1">
                                    Universidades <span class="mx-1">•</span> <span id="kpiCarreras"><%= dashboard.getTotalCarreras() %></span> Carreras
                                </div>
                            </div>
                        </div>
                    </div>

                    <section class="dashboard-chart-grid dashboard-chart-grid--estudiantes" aria-label="Gráficos del segmento">
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
                            <h2>Disponibilidad de CV</h2>
                            <p>Perfiles con y sin CV adjunto.</p>
                            <div id="graficoCobertura" class="dashboard-chart-donut"></div>
                        </article>

                    </section>

                    <!-- Visualizaciones Gráficas -->
                    <div class="row g-3 mb-4">
                        <!-- Universidades Top -->
                        <div class="col-12 col-lg-6 col-xl-4">
                            <div class="propath-card h-100 mb-0">
                                <div class="propath-card-header py-3">
                                    <h3 class="propath-card-title fs-6 mb-0">
                                        <i class="bi bi-building text-primary"></i>
                                        <span>Distribución por Universidad</span>
                                    </h3>
                                </div>
                                <div class="p-3" id="containerUniversidades">
                                    <% if (dashboard.getDistribucionUniversidades().isEmpty()) { %>
                                        <p class="text-muted small text-center my-4">No hay datos para mostrar</p>
                                    <% } else {
                                        for (Map.Entry<String, Integer> entry : dashboard.getDistribucionUniversidades().entrySet()) { 
                                            int pct = dashboard.getPorcentaje(entry.getValue());
                                    %>
                                        <div class="mb-3">
                                            <div class="d-flex justify-content-between align-items-center small mb-1">
                                                <span class="fw-semibold text-truncate me-2" title="<%= HtmlUtil.escape(entry.getKey()) %>"><%= HtmlUtil.escape(entry.getKey()) %></span>
                                                <span class="text-muted"><%= entry.getValue() %> (<%= pct + "%" %>)</span>
                                            </div>
                                            <div class="progress" style="height: 8px;">
                                                <div class="progress-bar bg-primary" role="progressbar" style="width: <%= pct + "%" %>;" aria-valuenow="<%= pct %>" aria-valuemin="0" aria-valuemax="100"></div>
                                            </div>
                                        </div>
                                    <%  } 
                                       } %>
                                </div>
                            </div>
                        </div>

                        <!-- Carreras Top -->
                        <div class="col-12 col-lg-6 col-xl-4">
                            <div class="propath-card h-100 mb-0">
                                <div class="propath-card-header py-3">
                                    <h3 class="propath-card-title fs-6 mb-0">
                                        <i class="bi bi-journal-bookmark-fill text-info"></i>
                                        <span>Carreras con Mayor Demanda</span>
                                    </h3>
                                </div>
                                <div class="p-3" id="containerCarreras">
                                    <% if (dashboard.getDistribucionCarreras().isEmpty()) { %>
                                        <p class="text-muted small text-center my-4">No hay datos para mostrar</p>
                                    <% } else {
                                        for (Map.Entry<String, Integer> entry : dashboard.getDistribucionCarreras().entrySet()) { 
                                            int pct = dashboard.getPorcentaje(entry.getValue());
                                    %>
                                        <div class="mb-3">
                                            <div class="d-flex justify-content-between align-items-center small mb-1">
                                                <span class="fw-semibold text-truncate me-2" title="<%= HtmlUtil.escape(entry.getKey()) %>"><%= HtmlUtil.escape(entry.getKey()) %></span>
                                                <span class="text-muted"><%= entry.getValue() %> (<%= pct + "%" %>)</span>
                                            </div>
                                            <div class="progress" style="height: 8px;">
                                                <div class="progress-bar bg-info" role="progressbar" style="width: <%= pct + "%" %>;" aria-valuenow="<%= pct %>" aria-valuemin="0" aria-valuemax="100"></div>
                                            </div>
                                        </div>
                                    <%  } 
                                       } %>
                                </div>
                            </div>
                        </div>

                        <!-- Ciclos y Distribución Estado/CV -->
                        <div class="col-12 col-lg-12 col-xl-4">
                            <div class="propath-card h-100 mb-0">
                                <div class="propath-card-header py-3">
                                    <h3 class="propath-card-title fs-6 mb-0">
                                        <i class="bi bi-bar-chart-steps text-secondary"></i>
                                        <span>Distribución por Ciclo y Estado</span>
                                    </h3>
                                </div>
                                <div class="p-3">
                                    <!-- Columnas de Ciclos 1 al 12 -->
                                    <h6 class="text-muted small fw-bold text-uppercase mb-2">Ciclos Académicos (1 al 12)</h6>
                                    <div id="containerCiclos" class="d-flex align-items-end justify-content-between gap-1 mb-3 pt-2 px-1 border-bottom pb-2" style="height: 95px;">
                                        <% 
                                            int maxCicloCount = 1;
                                            for (int c = 1; c <= 12; c++) {
                                                int val = dashboard.getDistribucionCiclos().getOrDefault(c, 0);
                                                if (val > maxCicloCount) maxCicloCount = val;
                                            }
                                            for (int c = 1; c <= 12; c++) {
                                                int count = dashboard.getDistribucionCiclos().getOrDefault(c, 0);
                                                int barHeight = maxCicloCount > 0 ? (count * 100 / maxCicloCount) : 0;
                                                if (barHeight < 10 && count > 0) barHeight = 10;
                                                String colClass = count > 0 ? "bg-primary" : "bg-light";
                                                String colHeight = barHeight + "%";
                                                String colMinHeight = count > 0 ? "4px" : "2px";
                                        %>
                                            <div class="d-flex flex-column align-items-center flex-grow-1" style="height: 100%; justify-content: flex-end;" title="Ciclo <%= c %>: <%= count %> estudiantes">
                                                <span style="font-size: 9px;" class="fw-bold text-secondary mb-1"><%= count > 0 ? count : "" %></span>
                                                <div class="w-100 rounded-top <%= colClass %> cycle-col" style="height: <%= colHeight %>; min-height: <%= colMinHeight %>;"></div>
                                                <span style="font-size: 9px;" class="text-muted mt-1">C<%= c %></span>
                                            </div>
                                        <% } %>
                                    </div>

                                    <!-- Estado de Cuentas -->
                                    <div class="mb-3">
                                        <div class="d-flex justify-content-between align-items-center small mb-1">
                                            <span class="text-muted fw-semibold">Estado de Cuentas:</span>
                                            <span class="fw-semibold">
                                                <span class="text-success"><span id="lblCountActivos"><%= dashboard.getActivos() %></span> Act.</span> / 
                                                <span class="text-secondary"><span id="lblCountInactivos"><%= dashboard.getInactivos() %></span> Inact.</span> / 
                                                <span class="text-danger"><span id="lblCountBloqueados"><%= dashboard.getBloqueados() %></span> Bloq.</span>
                                            </span>
                                        </div>
                                        <div class="progress" style="height: 7px;">
                                            <div id="barEstadoActivo" class="progress-bar bg-success" style="width: <%= dashboard.getPorcentaje(dashboard.getActivos()) + "%" %>;" title="Activos: <%= dashboard.getPorcentaje(dashboard.getActivos()) + "%" %>"></div>
                                            <div id="barEstadoInactivo" class="progress-bar bg-secondary" style="width: <%= dashboard.getPorcentaje(dashboard.getInactivos()) + "%" %>;" title="Inactivos: <%= dashboard.getPorcentaje(dashboard.getInactivos()) + "%" %>"></div>
                                            <div id="barEstadoBloqueado" class="progress-bar bg-danger" style="width: <%= dashboard.getPorcentaje(dashboard.getBloqueados()) + "%" %>;" title="Bloqueados: <%= dashboard.getPorcentaje(dashboard.getBloqueados()) + "%" %>"></div>
                                        </div>
                                    </div>

                                    <!-- Disponibilidad de CV -->
                                    <div>
                                        <div class="d-flex justify-content-between align-items-center small mb-1">
                                            <span class="text-muted fw-semibold">Disponibilidad de CV:</span>
                                            <span class="fw-semibold">
                                                <span class="text-info"><span id="lblCountConCv"><%= dashboard.getConCv() %></span> Con CV</span> / 
                                                <span class="text-warning"><span id="lblCountSinCv"><%= dashboard.getSinCv() %></span> Sin CV</span>
                                            </span>
                                        </div>
                                        <div class="progress" style="height: 7px;">
                                            <div id="barCvCon" class="progress-bar bg-info" style="width: <%= dashboard.getPorcentaje(dashboard.getConCv()) + "%" %>;" title="Con CV: <%= dashboard.getPorcentaje(dashboard.getConCv()) + "%" %>"></div>
                                            <div id="barCvSin" class="progress-bar bg-warning" style="width: <%= dashboard.getPorcentaje(dashboard.getSinCv()) + "%" %>;" title="Sin CV: <%= dashboard.getPorcentaje(dashboard.getSinCv()) + "%" %>"></div>
                                        </div>
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
            let activeEstudianteController = null;
            let activeEstudianteReqId = 0;

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
                const u = document.getElementById('selectUniversidad').value;
                const carr = document.getElementById('selectCarrera').value;
                const cic = document.getElementById('selectCiclo').value;
                const ec = document.getElementById('selectEstadoCuenta').value;
                const cv = document.getElementById('selectCv').value;

                const activos = (u !== 'TODOS') || (carr !== 'TODOS') || (cic !== '0') || (ec !== 'TODOS') || (cv !== 'TODOS');
                const badge = document.getElementById('badgeFiltrosActivos');
                if (badge) {
                    badge.style.display = activos ? 'inline-block' : 'none';
                }
            }

            async function actualizarDashboardEstudiantes() {
                verificarFiltrosActivos();

                if (activeEstudianteController) {
                    activeEstudianteController.abort();
                }
                activeEstudianteController = new AbortController();
                const signal = activeEstudianteController.signal;
                const currentReqId = ++activeEstudianteReqId;

                const params = new URLSearchParams({
                    universidad: document.getElementById('selectUniversidad').value,
                    carrera: document.getElementById('selectCarrera').value,
                    ciclo: document.getElementById('selectCiclo').value,
                    estadoCuenta: document.getElementById('selectEstadoCuenta').value,
                    cv: document.getElementById('selectCv').value
                });

                try {
                    const url = '${pageContext.request.contextPath}/admin/api/estudiantes-dashboard?' + params.toString();
                    const resp = await fetch(url, { signal });
                    if (!resp.ok) {
                        throw new Error('Error al consultar el servidor (' + resp.status + ')');
                    }
                    const data = await resp.json();

                    if (currentReqId !== activeEstudianteReqId) {
                        return; // Petición obsoleta descartada
                    }

                    renderDashboardEstudiantes(data);
                } catch (err) {
                    if (err.name === 'AbortError') {
                        return; // Cancelación esperada por nueva selección rápida
                    }
                    console.error('Error actualizando dashboard de estudiantes:', err);
                }
            }

            function renderDashboardEstudiantes(data) {
                window.graficosPropath.estudiantes(data);
                const total = data.total || 0;
                const emptyBanner = document.getElementById('emptyStateBanner');

                if (total === 0) {
                    if (emptyBanner) emptyBanner.style.display = 'block';
                } else {
                    if (emptyBanner) emptyBanner.style.display = 'none';
                }

                // Actualizar KPIs
                const activos = data.activos || 0;
                const inactivos = data.inactivos || 0;
                const bloqueados = data.bloqueados || 0;
                const conCv = data.conCv || 0;
                const sinCv = data.sinCv || 0;
                const totalUniv = data.totalUniversidades || 0;
                const totalCarr = data.totalCarreras || 0;

                const activosPct = calcPct(activos, total);
                const conCvPct = calcPct(conCv, total);

                document.getElementById('kpiTotal').textContent = total;
                document.getElementById('kpiActivos').textContent = activos;
                document.getElementById('kpiActivosPct').textContent = activosPct + '%';
                document.getElementById('kpiInactivos').textContent = inactivos;
                document.getElementById('kpiBloqueados').textContent = bloqueados;

                document.getElementById('kpiConCv').textContent = conCv;
                document.getElementById('kpiConCvPct').textContent = conCvPct + '%';
                document.getElementById('kpiSinCv').textContent = sinCv;

                document.getElementById('kpiUniversidades').textContent = totalUniv;
                document.getElementById('kpiCarreras').textContent = totalCarr;

                // Render Universidades
                const contUniv = document.getElementById('containerUniversidades');
                if (contUniv) {
                    if (!data.distribucionUniversidades || data.distribucionUniversidades.length === 0) {
                        contUniv.innerHTML = '<p class="text-muted small text-center my-4">No hay datos para mostrar</p>';
                    } else {
                        let html = '';
                        data.distribucionUniversidades.forEach(item => {
                            const pct = calcPct(item.cantidad, total);
                            html += '<div class="mb-3">' +
                                        '<div class="d-flex justify-content-between align-items-center small mb-1">' +
                                            '<span class="fw-semibold text-truncate me-2" title="' + escapeHtml(item.nombre) + '">' + escapeHtml(item.nombre) + '</span>' +
                                            '<span class="text-muted">' + item.cantidad + ' (' + pct + '%)</span>' +
                                        '</div>' +
                                        '<div class="progress" style="height: 8px;">' +
                                            '<div class="progress-bar bg-primary" role="progressbar" style="width: ' + pct + '%;" aria-valuenow="' + pct + '" aria-valuemin="0" aria-valuemax="100"></div>' +
                                        '</div>' +
                                    '</div>';
                        });
                        contUniv.innerHTML = html;
                    }
                }

                // Render Carreras
                const contCarr = document.getElementById('containerCarreras');
                if (contCarr) {
                    if (!data.distribucionCarreras || data.distribucionCarreras.length === 0) {
                        contCarr.innerHTML = '<p class="text-muted small text-center my-4">No hay datos para mostrar</p>';
                    } else {
                        let html = '';
                        data.distribucionCarreras.forEach(item => {
                            const pct = calcPct(item.cantidad, total);
                            html += '<div class="mb-3">' +
                                        '<div class="d-flex justify-content-between align-items-center small mb-1">' +
                                            '<span class="fw-semibold text-truncate me-2" title="' + escapeHtml(item.nombre) + '">' + escapeHtml(item.nombre) + '</span>' +
                                            '<span class="text-muted">' + item.cantidad + ' (' + pct + '%)</span>' +
                                        '</div>' +
                                        '<div class="progress" style="height: 8px;">' +
                                            '<div class="progress-bar bg-info" role="progressbar" style="width: ' + pct + '%;" aria-valuenow="' + pct + '" aria-valuemin="0" aria-valuemax="100"></div>' +
                                        '</div>' +
                                    '</div>';
                        });
                        contCarr.innerHTML = html;
                    }
                }

                // Render Ciclos
                const contCiclos = document.getElementById('containerCiclos');
                if (contCiclos) {
                    const cicloMap = {};
                    let maxCount = 1;
                    if (data.distribucionCiclos) {
                        data.distribucionCiclos.forEach(item => {
                            cicloMap[item.ciclo] = item.cantidad;
                            if (item.cantidad > maxCount) maxCount = item.cantidad;
                        });
                    }

                    let htmlCiclos = '';
                    for (let c = 1; c <= 12; c++) {
                        const count = cicloMap[c] || 0;
                        let barHeight = maxCount > 0 ? Math.round((count * 100) / maxCount) : 0;
                        if (barHeight < 10 && count > 0) barHeight = 10;
                        const bgClass = count > 0 ? 'bg-primary' : 'bg-light';
                        htmlCiclos += '<div class="d-flex flex-column align-items-center flex-grow-1" style="height: 100%; justify-content: flex-end;" title="Ciclo ' + c + ': ' + count + ' estudiantes">' +
                                          '<span style="font-size: 9px;" class="fw-bold text-secondary mb-1">' + (count > 0 ? count : '') + '</span>' +
                                          '<div class="w-100 rounded-top ' + bgClass + ' cycle-col" style="height: ' + barHeight + '%; min-height: ' + (count > 0 ? '4px' : '2px') + ';"></div>' +
                                          '<span style="font-size: 9px;" class="text-muted mt-1">C' + c + '</span>' +
                                      '</div>';
                    }
                    contCiclos.innerHTML = htmlCiclos;
                }

                // Actualizar barras Estado de Cuenta
                const inactivosPct = calcPct(inactivos, total);
                const bloqueadosPct = calcPct(bloqueados, total);

                document.getElementById('lblCountActivos').textContent = activos;
                document.getElementById('lblCountInactivos').textContent = inactivos;
                document.getElementById('lblCountBloqueados').textContent = bloqueados;

                const barAct = document.getElementById('barEstadoActivo');
                const barInact = document.getElementById('barEstadoInactivo');
                const barBloq = document.getElementById('barEstadoBloqueado');
                if (barAct) { barAct.style.width = activosPct + '%'; barAct.title = 'Activos: ' + activosPct + '%'; }
                if (barInact) { barInact.style.width = inactivosPct + '%'; barInact.title = 'Inactivos: ' + inactivosPct + '%'; }
                if (barBloq) { barBloq.style.width = bloqueadosPct + '%'; barBloq.title = 'Bloqueados: ' + bloqueadosPct + '%'; }

                // Actualizar barras CV
                const sinCvPct = calcPct(sinCv, total);
                document.getElementById('lblCountConCv').textContent = conCv;
                document.getElementById('lblCountSinCv').textContent = sinCv;

                const barCon = document.getElementById('barCvCon');
                const barSin = document.getElementById('barCvSin');
                if (barCon) { barCon.style.width = conCvPct + '%'; barCon.title = 'Con CV: ' + conCvPct + '%'; }
                if (barSin) { barSin.style.width = sinCvPct + '%'; barSin.title = 'Sin CV: ' + sinCvPct + '%'; }
            }

            function limpiarFiltros() {
                document.getElementById('selectUniversidad').value = 'TODOS';
                document.getElementById('selectCarrera').value = 'TODOS';
                document.getElementById('selectCiclo').value = '0';
                document.getElementById('selectEstadoCuenta').value = 'TODOS';
                document.getElementById('selectCv').value = 'TODOS';
                actualizarDashboardEstudiantes();
            }

            document.addEventListener('DOMContentLoaded', function() {
                document.querySelectorAll('.segmenter-input').forEach(function(el) {
                    el.addEventListener('change', actualizarDashboardEstudiantes);
                });

                const btnLimpiar = document.getElementById('btnLimpiarFiltros');
                if (btnLimpiar) {
                    btnLimpiar.addEventListener('click', limpiarFiltros);
                }

                const btnActualizar = document.getElementById('btnActualizarManual');
                if (btnActualizar) {
                    btnActualizar.addEventListener('click', actualizarDashboardEstudiantes);
                }
                actualizarDashboardEstudiantes();
            });
        </script>
    </body>
</html>
