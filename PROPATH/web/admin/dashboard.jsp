<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("activePage", "dashboard");
%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Dashboard Administrador | ProPath</title>

        <!-- Google Fonts: Inter -->
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

        <!-- Bootstrap 5 CSS & Bootstrap Icons CDN -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

        <!-- Estilos institucionales ProPath -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
    </head>
    <body>
        <div class="admin-layout">
            <!-- Sidebar -->
            <jsp:include page="/admin/includes/sidebar.jsp" />

            <!-- Contenido Principal -->
            <main class="admin-main">
                <!-- Barra superior -->
                <jsp:include page="/admin/includes/topbar.jsp" />

                <!-- Cuerpo del Dashboard -->
                <div class="admin-content">
                    <!-- Banner de bienvenida / Lema -->
                    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
                        <div>
                            <h1 class="h3 fw-bold text-dark mb-1">Panel de Control</h1>
                            <p class="text-muted mb-0">
                                ProPath &bull; <span class="fw-medium text-primary">“Tu camino hacia el éxito profesional”</span>
                            </p>
                        </div>
                        <div class="d-flex gap-2">
                            <a href="${pageContext.request.contextPath}/admin/ofertas.jsp" class="btn btn-outline-primary rounded-3 px-3 py-2 fw-semibold" style="font-size: 0.9rem;">
                                <i class="bi bi-briefcase me-1"></i> Ver Ofertas
                            </a>
                            <a href="${pageContext.request.contextPath}/admin/estudiantes.jsp" class="btn btn-primary rounded-3 px-3 py-2 fw-semibold" style="font-size: 0.9rem; background: var(--propath-turquoise); border-color: var(--propath-turquoise);">
                                <i class="bi bi-mortarboard me-1"></i> Ver Estudiantes
                            </a>
                        </div>
                    </div>

                    <!-- 4 Tarjetas de Estadísticas Principales -->
                    <div class="row g-4 mb-4">
                        <!-- Estudiantes Registrados: 24 -->
                        <div class="col-12 col-sm-6 col-xl-3">
                            <div class="stat-card stat-card-blue">
                                <div class="d-flex justify-content-between align-items-start">
                                    <div>
                                        <div class="stat-value">24</div>
                                        <h3 class="stat-label">Estudiantes registrados</h3>
                                    </div>
                                    <div class="stat-icon-wrapper">
                                        <i class="bi bi-mortarboard-fill"></i>
                                    </div>
                                </div>
                                <div class="mt-3 pt-2 border-top d-flex align-items-center justify-content-between small text-muted">
                                    <span><i class="bi bi-arrow-up-short text-success fw-bold"></i> +3 esta semana</span>
                                    <a href="${pageContext.request.contextPath}/admin/estudiantes.jsp" class="text-decoration-none fw-semibold text-primary">Gestionar &rarr;</a>
                                </div>
                            </div>
                        </div>

                        <!-- Empresas Registradas: 8 -->
                        <div class="col-12 col-sm-6 col-xl-3">
                            <div class="stat-card stat-card-turquoise">
                                <div class="d-flex justify-content-between align-items-start">
                                    <div>
                                        <div class="stat-value">8</div>
                                        <h3 class="stat-label">Empresas registradas</h3>
                                    </div>
                                    <div class="stat-icon-wrapper">
                                        <i class="bi bi-building-fill"></i>
                                    </div>
                                </div>
                                <div class="mt-3 pt-2 border-top d-flex align-items-center justify-content-between small text-muted">
                                    <span><i class="bi bi-shield-check text-info fw-bold"></i> 6 verificadas</span>
                                    <a href="${pageContext.request.contextPath}/admin/empresas.jsp" class="text-decoration-none fw-semibold text-primary">Gestionar &rarr;</a>
                                </div>
                            </div>
                        </div>

                        <!-- Ofertas Publicadas: 15 -->
                        <div class="col-12 col-sm-6 col-xl-3">
                            <div class="stat-card stat-card-teal">
                                <div class="d-flex justify-content-between align-items-start">
                                    <div>
                                        <div class="stat-value">15</div>
                                        <h3 class="stat-label">Ofertas publicadas</h3>
                                    </div>
                                    <div class="stat-icon-wrapper">
                                        <i class="bi bi-briefcase-fill"></i>
                                    </div>
                                </div>
                                <div class="mt-3 pt-2 border-top d-flex align-items-center justify-content-between small text-muted">
                                    <span><i class="bi bi-clock-history text-warning fw-bold"></i> 2 pendientes</span>
                                    <a href="${pageContext.request.contextPath}/admin/ofertas.jsp" class="text-decoration-none fw-semibold text-primary">Gestionar &rarr;</a>
                                </div>
                            </div>
                        </div>

                        <!-- Postulaciones Realizadas: 42 -->
                        <div class="col-12 col-sm-6 col-xl-3">
                            <div class="stat-card stat-card-indigo">
                                <div class="d-flex justify-content-between align-items-start">
                                    <div>
                                        <div class="stat-value">42</div>
                                        <h3 class="stat-label">Postulaciones</h3>
                                    </div>
                                    <div class="stat-icon-wrapper">
                                        <i class="bi bi-file-earmark-person-fill"></i>
                                    </div>
                                </div>
                                <div class="mt-3 pt-2 border-top d-flex align-items-center justify-content-between small text-muted">
                                    <span><i class="bi bi-eye text-primary fw-bold"></i> Monitoreo activo</span>
                                    <a href="${pageContext.request.contextPath}/admin/postulaciones.jsp" class="text-decoration-none fw-semibold text-primary">Consultar &rarr;</a>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Secciones de Acceso Rápido y Resumen Visual -->
                    <div class="row g-4">
                        <!-- Accesos rápidos / Módulos de administración -->
                        <div class="col-12 col-lg-7">
                            <div class="propath-card mb-4">
                                <div class="propath-card-header">
                                    <h2 class="propath-card-title">
                                        <i class="bi bi-compass-fill"></i>
                                        <span>Módulos de Gestión del Administrador</span>
                                    </h2>
                                </div>
                                <div class="propath-card-body">
                                    <div class="row g-3">
                                        <div class="col-sm-6">
                                            <div class="p-3 border rounded-3 bg-light-subtle h-100 d-flex flex-column justify-content-between">
                                                <div>
                                                    <div class="d-flex align-items-center gap-2 mb-2 text-primary fw-bold">
                                                        <i class="bi bi-mortarboard fs-5"></i>
                                                        <span>Estudiantes Universitarios</span>
                                                    </div>
                                                    <p class="small text-muted mb-3">
                                                        Supervisa y activa/bloquea el acceso de los alumnos postulantes de diversas universidades.
                                                    </p>
                                                </div>
                                                <a href="${pageContext.request.contextPath}/admin/estudiantes.jsp" class="btn btn-sm btn-outline-primary rounded-pill align-self-start">
                                                    Ir a Estudiantes
                                                </a>
                                            </div>
                                        </div>

                                        <div class="col-sm-6">
                                            <div class="p-3 border rounded-3 bg-light-subtle h-100 d-flex flex-column justify-content-between">
                                                <div>
                                                    <div class="d-flex align-items-center gap-2 mb-2 text-info fw-bold" style="color: #0891b2 !important;">
                                                        <i class="bi bi-building fs-5"></i>
                                                        <span>Empresas Empleadoras</span>
                                                    </div>
                                                    <p class="small text-muted mb-3">
                                                        Valida RUC, aprueba empresas nuevas o bloquea cuentas que incumplan normativas.
                                                    </p>
                                                </div>
                                                <a href="${pageContext.request.contextPath}/admin/empresas.jsp" class="btn btn-sm btn-outline-info rounded-pill align-self-start" style="color: #0891b2; border-color: #0891b2;">
                                                    Ir a Empresas
                                                </a>
                                            </div>
                                        </div>

                                        <div class="col-sm-6">
                                            <div class="p-3 border rounded-3 bg-light-subtle h-100 d-flex flex-column justify-content-between">
                                                <div>
                                                    <div class="d-flex align-items-center gap-2 mb-2 text-teal fw-bold" style="color: #0d9488 !important;">
                                                        <i class="bi bi-briefcase fs-5"></i>
                                                        <span>Ofertas de Prácticas</span>
                                                    </div>
                                                    <p class="small text-muted mb-3">
                                                        Revisa convocatorias, aprueba vacantes o desactiva ofertas obsoletas.
                                                    </p>
                                                </div>
                                                <a href="${pageContext.request.contextPath}/admin/ofertas.jsp" class="btn btn-sm btn-outline-success rounded-pill align-self-start">
                                                    Ir a Ofertas
                                                </a>
                                            </div>
                                        </div>

                                        <div class="col-sm-6">
                                            <div class="p-3 border rounded-3 bg-light-subtle h-100 d-flex flex-column justify-content-between">
                                                <div>
                                                    <div class="d-flex align-items-center gap-2 mb-2 text-indigo fw-bold" style="color: #4338ca !important;">
                                                        <i class="bi bi-file-earmark-person fs-5"></i>
                                                        <span>Postulaciones</span>
                                                    </div>
                                                    <p class="small text-muted mb-3">
                                                        Consulta el historial de postulaciones de los estudiantes a ofertas de prácticas preprofesionales.
                                                    </p>
                                                </div>
                                                <a href="${pageContext.request.contextPath}/admin/postulaciones.jsp" class="btn btn-sm btn-outline-primary rounded-pill align-self-start" style="color: #4338ca; border-color: #4338ca;">
                                                    Ir a Postulaciones
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Estado del Sistema y Notificaciones -->
                        <div class="col-12 col-lg-5">
                            <div class="propath-card">
                                <div class="propath-card-header">
                                    <h2 class="propath-card-title">
                                        <i class="bi bi-bell-fill"></i>
                                        <span>Información del Sistema</span>
                                    </h2>
                                </div>
                                <div class="propath-card-body">
                                    <ul class="list-unstyled mb-0">
                                        <li class="d-flex gap-3 mb-3 pb-3 border-bottom">
                                            <div class="rounded-circle bg-primary-subtle text-primary p-2 d-flex align-items-center justify-content-center" style="width: 40px; height: 40px;">
                                                <i class="bi bi-info-circle-fill"></i>
                                            </div>
                                            <div>
                                                <div class="fw-semibold text-dark" style="font-size: 0.9rem;">Fase Inicial de Desarrollo</div>
                                                <p class="small text-muted mb-0">Módulo Administrador configurado con arquitectura MVC en Apache Tomcat.</p>
                                            </div>
                                        </li>
                                        <li class="d-flex gap-3 mb-3 pb-3 border-bottom">
                                            <div class="rounded-circle bg-warning-subtle text-warning p-2 d-flex align-items-center justify-content-center" style="width: 40px; height: 40px;">
                                                <i class="bi bi-database-fill-slash"></i>
                                            </div>
                                            <div>
                                                <div class="fw-semibold text-dark" style="font-size: 0.9rem;">Modo Simulación Activo</div>
                                                <p class="small text-muted mb-0">Las tablas y tarjetas muestran datos en memoria. La base de datos MySQL se vinculará en la siguiente etapa.</p>
                                            </div>
                                        </li>
                                        <li class="d-flex gap-3">
                                            <div class="rounded-circle bg-success-subtle text-success p-2 d-flex align-items-center justify-content-center" style="width: 40px; height: 40px;">
                                                <i class="bi bi-shield-check"></i>
                                            </div>
                                            <div>
                                                <div class="fw-semibold text-dark" style="font-size: 0.9rem;">Autenticación Segura</div>
                                                <p class="small text-muted mb-0">Sesión activa protegida para el rol <code>ADMINISTRADOR</code>.</p>
                                            </div>
                                        </li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>

        <!-- Bootstrap 5 Bundle JS -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <!-- Lógica JavaScript ProPath -->
        <script src="${pageContext.request.contextPath}/js/script.js"></script>
    </body>
</html>
