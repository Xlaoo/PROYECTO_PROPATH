<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>ProPath | Tu camino hacia el éxito profesional</title>
        <meta name="description" content="Plataforma de inserción laboral para estudiantes universitarios que buscan prácticas preprofesionales.">

        <!-- Google Fonts: Inter -->
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">

        <!-- Bootstrap 5 CSS & Bootstrap Icons CDN -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

        <!-- Estilos institucionales ProPath -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
    </head>
    <body class="landing-body">

        <!-- ========================================================
             1. ENCABEZADO / NAVBAR PÚBLICO
             ======================================================== -->
        <nav class="navbar navbar-expand-lg landing-navbar sticky-top">
            <div class="container">
                <!-- Logotipo Oficial ProPath -->
                <a class="navbar-brand d-flex align-items-center" href="${pageContext.request.contextPath}/index.jsp">
                    <div class="landing-logo-container">
                        <img src="${pageContext.request.contextPath}/img/logo.png" 
                             alt="ProPath" 
                             class="landing-navbar-logo">
                    </div>
                </a>

                <!-- Botón Toggle para Móviles -->
                <button class="navbar-toggler border-0 shadow-none" 
                        type="button" 
                        data-bs-toggle="collapse" 
                        data-bs-target="#navbarMain" 
                        aria-controls="navbarMain" 
                        aria-expanded="false" 
                        aria-label="Abrir navegación">
                    <i class="bi bi-list fs-2 text-dark"></i>
                </button>

                <!-- Enlaces del Menú -->
                <div class="collapse navbar-collapse" id="navbarMain">
                    <ul class="navbar-nav mx-auto mb-2 mb-lg-0 align-items-lg-center gap-1 gap-lg-3">
                        <li class="nav-item">
                            <a class="nav-link landing-nav-link active" href="#inicio">Inicio</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link landing-nav-link" href="#como-funciona">Cómo funciona</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link landing-nav-link" href="#beneficios">Beneficios</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link landing-nav-link" href="#oportunidades">Oportunidades</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link landing-nav-link" href="#roles">Ingresar</a>
                        </li>
                    </ul>

                    <!-- Botón Destacado: Iniciar Sesión -->
                    <div class="d-flex align-items-center gap-2 mt-3 mt-lg-0">
                        <a href="#roles" class="btn btn-landing-login">
                            <i class="bi bi-box-arrow-in-right me-1"></i>
                            <span>Iniciar sesión</span>
                        </a>
                    </div>
                </div>
            </div>
        </nav>

        <!-- ========================================================
             2. SECCIÓN PRINCIPAL / HERO
             ======================================================== -->
        <section class="landing-hero" id="inicio">
            <div class="container">
                <div class="row align-items-center g-5">
                    <!-- Columna Izquierda: Mensajes Principales -->
                    <div class="col-12 col-lg-7">
                        <div class="hero-badge">
                            <i class="bi bi-stars text-warning"></i>
                            <span>Prácticas Preprofesionales para Universitarios</span>
                        </div>

                        <h1 class="hero-title">
                            Impulsa tu <span class="highlight-hero">futuro profesional</span>
                        </h1>

                        <p class="hero-subtitle">
                            Conectamos estudiantes universitarios con empresas que ofrecen oportunidades de prácticas preprofesionales.
                        </p>

                        <p class="hero-description">
                            Encuentra oportunidades relacionadas con tu carrera, postula de manera sencilla y da el primer paso hacia tu desarrollo profesional.
                        </p>

                        <!-- Botones de Acción -->
                        <div class="d-flex flex-wrap gap-3 mt-4">
                            <button type="button" 
                                    class="btn btn-hero-primary" 
                                    onclick="mostrarAvisoDesarrollo('Estudiantes', 'La búsqueda y postulación a prácticas para estudiantes estará disponible muy pronto.')">
                                <i class="bi bi-search me-2"></i>
                                <span>Buscar oportunidades</span>
                            </button>

                            <button type="button" 
                                    class="btn btn-hero-outline" 
                                    onclick="mostrarAvisoDesarrollo('Empresas', 'El portal para que las empresas publiquen convocatorias de prácticas se habilitará en breve.')">
                                <i class="bi bi-building me-2"></i>
                                <span>Soy empresa</span>
                            </button>
                        </div>

                        <!-- Indicadores de confianza -->
                        <div class="hero-trust-bar">
                            <div class="d-flex align-items-center gap-2">
                                <i class="bi bi-check-circle-fill text-success fs-5"></i>
                                <span class="small fw-semibold text-secondary">Convenios universitarios</span>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <i class="bi bi-check-circle-fill text-success fs-5"></i>
                                <span class="small fw-semibold text-secondary">Empresas verificadas</span>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <i class="bi bi-check-circle-fill text-success fs-5"></i>
                                <span class="small fw-semibold text-secondary">Postulación 100% gratuita</span>
                            </div>
                        </div>
                    </div>

                    <!-- Columna Derecha: Composición Visual Moderna -->
                    <div class="col-12 col-lg-5">
                        <div class="hero-composition-wrapper">
                            <!-- Tarjeta Central: Oportunidad Destacada -->
                            <div class="hero-float-card hero-main-preview shadow-lg">
                                <div class="d-flex justify-content-between align-items-start mb-3">
                                    <div class="d-flex align-items-center gap-3">
                                        <div class="hero-preview-avatar">
                                            <i class="bi bi-mortarboard-fill"></i>
                                        </div>
                                        <div>
                                            <h6 class="mb-0 fw-bold text-dark">Practicante Preprofesional</h6>
                                            <small class="text-muted">Tech Solutions SAC</small>
                                        </div>
                                    </div>
                                    <span class="badge bg-success-subtle text-success border border-success-subtle rounded-pill px-3 py-1">
                                        Activa
                                    </span>
                                </div>
                                <div class="hero-preview-tags mb-3">
                                    <span class="badge bg-light text-dark border">Ingeniería de Sistemas</span>
                                    <span class="badge bg-light text-dark border">Modalidad Híbrida</span>
                                    <span class="badge bg-info-subtle text-info border border-info-subtle">2 Vacantes</span>
                                </div>
                                <div class="p-3 bg-light rounded-3 d-flex justify-content-between align-items-center">
                                    <div class="small text-muted">
                                        <i class="bi bi-clock me-1"></i> Publicado recientemente
                                    </div>
                                    <span class="small fw-bold text-primary">ProPath Match 98%</span>
                                </div>
                            </div>

                            <!-- Mini Tarjeta Flotante 1: Notificación de Postulación -->
                            <div class="hero-float-card hero-badge-float-1 shadow">
                                <div class="d-flex align-items-center gap-2">
                                    <div class="rounded-circle bg-success text-white p-2 d-flex align-items-center justify-content-center" style="width: 32px; height: 32px;">
                                        <i class="bi bi-check2"></i>
                                    </div>
                                    <div>
                                        <div class="fw-bold" style="font-size: 0.8rem; color: var(--propath-navy);">¡Postulación enviada!</div>
                                        <small class="text-muted" style="font-size: 0.72rem;">Ana Torres &bull; UPN</small>
                                    </div>
                                </div>
                            </div>

                            <!-- Mini Tarjeta Flotante 2: Empresa Verificada -->
                            <div class="hero-float-card hero-badge-float-2 shadow">
                                <div class="d-flex align-items-center gap-2">
                                    <div class="rounded-circle bg-primary text-white p-2 d-flex align-items-center justify-content-center" style="width: 32px; height: 32px;">
                                        <i class="bi bi-shield-check"></i>
                                    </div>
                                    <div>
                                        <div class="fw-bold" style="font-size: 0.8rem; color: var(--propath-navy);">Empresa Verificada</div>
                                        <small class="text-muted" style="font-size: 0.72rem;">Innovate Perú SAC</small>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- ========================================================
             3. SECCIÓN DE ROLES DEL SISTEMA
             ======================================================== -->
        <section class="landing-section bg-white" id="roles">
            <div class="container">
                <div class="section-header text-center">
                    <span class="section-tag">ACCESO A LA PLATAFORMA</span>
                    <h2 class="section-title">¿Cómo quieres usar ProPath?</h2>
                    <p class="section-subtitle">
                        Elige tu perfil para acceder a las herramientas pensadas especialmente para ti.
                    </p>
                </div>

                <div class="row g-4 mt-2 justify-content-center">
                    <!-- Tarjeta 1: ESTUDIANTE -->
                    <div class="col-12 col-md-6 col-lg-4">
                        <div class="role-card role-card-student h-100">
                            <div class="role-icon-box">
                                <i class="bi bi-mortarboard-fill"></i>
                            </div>
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <h3 class="role-title mb-0">Estudiante</h3>
                                <span class="badge bg-warning-subtle text-warning-emphasis border border-warning-subtle px-2 py-1 rounded-pill small">
                                    Próximamente
                                </span>
                            </div>
                            <p class="role-text">
                                Crea tu perfil, encuentra prácticas relacionadas con tu carrera y realiza seguimiento a tus postulaciones.
                            </p>
                            <div class="mt-auto pt-3">
                                <button type="button" 
                                        class="btn btn-outline-primary w-100 rounded-pill py-2 fw-semibold"
                                        onclick="mostrarAvisoDesarrollo('Estudiantes', 'El módulo de inicio de sesión y registro de estudiantes está en fase de desarrollo.')">
                                    <i class="bi bi-clock-history me-1"></i>
                                    <span>Ingresar como estudiante</span>
                                </button>
                            </div>
                        </div>
                    </div>

                    <!-- Tarjeta 2: EMPRESA -->
                    <div class="col-12 col-md-6 col-lg-4">
                        <div class="role-card role-card-company h-100">
                            <div class="role-icon-box">
                                <i class="bi bi-buildings-fill"></i>
                            </div>
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <h3 class="role-title mb-0">Empresa</h3>
                                <span class="badge bg-warning-subtle text-warning-emphasis border border-warning-subtle px-2 py-1 rounded-pill small">
                                    Próximamente
                                </span>
                            </div>
                            <p class="role-text">
                                Publica oportunidades de prácticas y conecta con estudiantes universitarios que buscan desarrollar su talento.
                            </p>
                            <div class="mt-auto pt-3">
                                <button type="button" 
                                        class="btn btn-outline-info w-100 rounded-pill py-2 fw-semibold"
                                        style="color: #0891b2; border-color: #0891b2;"
                                        onclick="mostrarAvisoDesarrollo('Empresas', 'El módulo para empresas empleadoras se habilitará próximamente.')">
                                    <i class="bi bi-clock-history me-1"></i>
                                    <span>Ingresar como empresa</span>
                                </button>
                            </div>
                        </div>
                    </div>

                    <!-- Tarjeta 3: ADMINISTRADOR (FUNCIONA) -->
                    <div class="col-12 col-md-6 col-lg-4">
                        <div class="role-card role-card-admin h-100 active-role">
                            <div class="role-icon-box">
                                <i class="bi bi-shield-lock-fill"></i>
                            </div>
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <h3 class="role-title mb-0">Administrador</h3>
                                <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1 rounded-pill small">
                                    Disponible
                                </span>
                            </div>
                            <p class="role-text">
                                Gestiona estudiantes, empresas, oportunidades y supervisa el funcionamiento de ProPath.
                            </p>
                            <div class="mt-auto pt-3">
                                <a href="${pageContext.request.contextPath}/login.jsp" 
                                   class="btn btn-propath-primary w-100 rounded-pill py-2 fw-semibold text-white">
                                    <span>Ingresar como administrador</span>
                                    <i class="bi bi-arrow-right ms-2"></i>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- ========================================================
             4. SECCIÓN CÓMO FUNCIONA
             ======================================================== -->
        <section class="landing-section bg-light" id="como-funciona">
            <div class="container">
                <div class="section-header text-center">
                    <span class="section-tag">PASO A PASO</span>
                    <h2 class="section-title">Tu camino profesional comienza aquí</h2>
                    <p class="section-subtitle">
                        Diseñado para que tu inserción al mundo laboral sea sencilla, ágil y transparente.
                    </p>
                </div>

                <div class="row g-4 mt-2">
                    <!-- Paso 1 -->
                    <div class="col-12 col-md-4">
                        <div class="step-card h-100">
                            <div class="step-number">01</div>
                            <div class="step-icon">
                                <i class="bi bi-person-vcard-fill"></i>
                            </div>
                            <h4 class="step-title">1. Crea tu perfil</h4>
                            <p class="step-text">
                                Registra tu información académica, habilidades y experiencia para destacar ante las mejores empresas.
                            </p>
                        </div>
                    </div>

                    <!-- Paso 2 -->
                    <div class="col-12 col-md-4">
                        <div class="step-card h-100">
                            <div class="step-number">02</div>
                            <div class="step-icon">
                                <i class="bi bi-briefcase-fill"></i>
                            </div>
                            <h4 class="step-title">2. Encuentra oportunidades</h4>
                            <p class="step-text">
                                Explora prácticas relacionadas con tu carrera e intereses con filtros por modalidad y vacantes.
                            </p>
                        </div>
                    </div>

                    <!-- Paso 3 -->
                    <div class="col-12 col-md-4">
                        <div class="step-card h-100">
                            <div class="step-number">03</div>
                            <div class="step-icon">
                                <i class="bi bi-send-check-fill"></i>
                            </div>
                            <h4 class="step-title">3. Postula y avanza</h4>
                            <p class="step-text">
                                Envía tu postulación y realiza seguimiento en tiempo real al estado de cada una de tus convocatorias.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- ========================================================
             5. SECCIÓN BENEFICIOS
             ======================================================== -->
        <section class="landing-section bg-white" id="beneficios">
            <div class="container">
                <div class="section-header text-center">
                    <span class="section-tag">¿POR QUÉ PROPATH?</span>
                    <h2 class="section-title">Beneficios pensados para tu crecimiento</h2>
                    <p class="section-subtitle">
                        La herramienta que acorta la distancia entre la universidad y tu primera experiencia laboral.
                    </p>
                </div>

                <div class="row g-4 mt-2">
                    <!-- Beneficio 1 -->
                    <div class="col-12 col-sm-6 col-lg-3">
                        <div class="benefit-card h-100">
                            <div class="benefit-icon bg-primary-subtle text-primary">
                                <i class="bi bi-mortarboard"></i>
                            </div>
                            <h5 class="benefit-title">Oportunidades para universitarios</h5>
                            <p class="benefit-text">
                                Convocatorias diseñadas acorde al plan curricular y disponibilidad horaria de estudiantes.
                            </p>
                        </div>
                    </div>

                    <!-- Beneficio 2 -->
                    <div class="col-12 col-sm-6 col-lg-3">
                        <div class="benefit-card h-100">
                            <div class="benefit-icon bg-info-subtle text-info" style="color: #0891b2 !important;">
                                <i class="bi bi-buildings"></i>
                            </div>
                            <h5 class="benefit-title">Conexión directa con empresas</h5>
                            <p class="benefit-text">
                                Enlace directo entre empresas formales con RUC y talento universitario sin intermediarios confusos.
                            </p>
                        </div>
                    </div>

                    <!-- Beneficio 3 -->
                    <div class="col-12 col-sm-6 col-lg-3">
                        <div class="benefit-card h-100">
                            <div class="benefit-icon bg-teal-subtle text-success" style="background-color: #ccfbf1 !important; color: #0f766e !important;">
                                <i class="bi bi-kanban"></i>
                            </div>
                            <h5 class="benefit-title">Seguimiento de postulaciones</h5>
                            <p class="benefit-text">
                                Monitorea cuándo tu CV ha sido revisado o si fuiste seleccionado para la etapa de entrevistas.
                            </p>
                        </div>
                    </div>

                    <!-- Beneficio 4 -->
                    <div class="col-12 col-sm-6 col-lg-3">
                        <div class="benefit-card h-100">
                            <div class="benefit-icon bg-indigo-subtle text-primary" style="background-color: #e0e7ff !important; color: #4338ca !important;">
                                <i class="bi bi-shield-check"></i>
                            </div>
                            <h5 class="benefit-title">Plataforma fácil y segura</h5>
                            <p class="benefit-text">
                                Navegación intuitiva, protección de datos y entorno digital moderno accesible desde cualquier dispositivo.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- ========================================================
             6. SECCIÓN OPORTUNIDADES DESTACADAS (PREVIEW)
             ======================================================== -->
        <section class="landing-section bg-light" id="oportunidades">
            <div class="container">
                <div class="section-header text-center">
                    <span class="section-tag">CONVOCATORIAS DISPONIBLES</span>
                    <h2 class="section-title">Últimas ofertas publicadas</h2>
                    <p class="section-subtitle">
                        Explora algunas de las prácticas preprofesionales registradas en la plataforma.
                    </p>
                </div>

                <div class="row g-4 mt-2">
                    <!-- Oferta 1 -->
                    <div class="col-12 col-md-6">
                        <div class="opportunity-card p-4 bg-white rounded-4 border shadow-sm h-100">
                            <div class="d-flex justify-content-between align-items-start mb-3">
                                <div>
                                    <span class="badge bg-primary-subtle text-primary mb-2">Ingeniería de Sistemas</span>
                                    <h4 class="h5 fw-bold text-dark mb-1">Practicante de Desarrollo Web</h4>
                                    <div class="text-muted small">
                                        <i class="bi bi-building me-1"></i> Tech Solutions SAC &bull; RUC 20123456789
                                    </div>
                                </div>
                                <span class="badge-status badge-activo">Activa</span>
                            </div>
                            <p class="text-secondary small mb-3">
                                Participación en el desarrollo y mantenimiento de aplicaciones web corporativas usando tecnologías modernas.
                            </p>
                            <div class="d-flex justify-content-between align-items-center pt-3 border-top">
                                <span class="badge-modalidad"><i class="bi bi-laptop me-1"></i> Híbrida &bull; 2 Vacantes</span>
                                <button type="button" 
                                        class="btn btn-sm btn-outline-primary rounded-pill px-3"
                                        onclick="mostrarAvisoDesarrollo('Postulación', 'Para postular a esta vacante debes iniciar sesión como estudiante cuando el módulo esté disponible.')">
                                    Postular
                                </button>
                            </div>
                        </div>
                    </div>

                    <!-- Oferta 2 -->
                    <div class="col-12 col-md-6">
                        <div class="opportunity-card p-4 bg-white rounded-4 border shadow-sm h-100">
                            <div class="d-flex justify-content-between align-items-start mb-3">
                                <div>
                                    <span class="badge bg-info-subtle text-info mb-2" style="color: #0891b2 !important;">Administración</span>
                                    <h4 class="h5 fw-bold text-dark mb-1">Practicante de Recursos Humanos</h4>
                                    <div class="text-muted small">
                                        <i class="bi bi-building me-1"></i> Innovate Perú SAC &bull; RUC 20987654321
                                    </div>
                                </div>
                                <span class="badge-status badge-pendiente">Pendiente</span>
                            </div>
                            <p class="text-secondary small mb-3">
                                Apoyo en procesos de reclutamiento, inducción y gestión de bienestar del talento universitario.
                            </p>
                            <div class="d-flex justify-content-between align-items-center pt-3 border-top">
                                <span class="badge-modalidad"><i class="bi bi-geo-alt me-1"></i> Presencial &bull; 1 Vacante</span>
                                <button type="button" 
                                        class="btn btn-sm btn-outline-primary rounded-pill px-3"
                                        onclick="mostrarAvisoDesarrollo('Postulación', 'Para postular a esta vacante debes iniciar sesión como estudiante cuando el módulo esté disponible.')">
                                    Postular
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- ========================================================
             7. SECCIÓN ESTADÍSTICAS SIMULADAS
             ======================================================== -->
        <section class="landing-stats-section">
            <div class="container">
                <div class="row g-4 text-center">
                    <div class="col-6 col-lg-3">
                        <div class="stat-counter-box">
                            <div class="stat-counter-number">+24</div>
                            <div class="stat-counter-label">Estudiantes</div>
                        </div>
                    </div>
                    <div class="col-6 col-lg-3">
                        <div class="stat-counter-box">
                            <div class="stat-counter-number">+8</div>
                            <div class="stat-counter-label">Empresas</div>
                        </div>
                    </div>
                    <div class="col-6 col-lg-3">
                        <div class="stat-counter-box">
                            <div class="stat-counter-number">+15</div>
                            <div class="stat-counter-label">Oportunidades</div>
                        </div>
                    </div>
                    <div class="col-6 col-lg-3">
                        <div class="stat-counter-box">
                            <div class="stat-counter-number">+42</div>
                            <div class="stat-counter-label">Postulaciones</div>
                        </div>
                    </div>
                </div>
                <div class="text-center mt-3">
                    <span class="badge bg-white bg-opacity-25 text-white rounded-pill px-3 py-1 small">
                        <i class="bi bi-info-circle me-1"></i> Datos demostrativos
                    </span>
                </div>
            </div>
        </section>

        <!-- ========================================================
             8. SECCIÓN LLAMADA A LA ACCIÓN (CTA)
             ======================================================== -->
        <section class="landing-cta-section">
            <div class="container">
                <div class="cta-box text-center">
                    <h2 class="cta-title">Da el primer paso hacia tu futuro profesional</h2>
                    <p class="cta-subtitle">
                        ProPath te ayuda a conectar con nuevas oportunidades y comenzar tu experiencia laboral.
                    </p>
                    <div class="mt-4">
                        <a href="#roles" class="btn btn-cta-primary">
                            <span>Explorar ProPath</span>
                            <i class="bi bi-arrow-right ms-2"></i>
                        </a>
                    </div>
                </div>
            </div>
        </section>

        <!-- ========================================================
             9. FOOTER INSTITUCIONAL
             ======================================================== -->
        <footer class="landing-footer">
            <div class="container">
                <div class="row g-4 align-items-center justify-content-between pb-4 border-bottom border-secondary border-opacity-25">
                    <!-- Marca -->
                    <div class="col-12 col-md-6 text-center text-md-start">
                        <div class="landing-footer-logo-container mb-2">
                            <img src="${pageContext.request.contextPath}/img/logo.png" 
                                 alt="ProPath" 
                                 class="landing-footer-logo">
                        </div>
                        <p class="text-secondary small mb-0">
                            “Tu camino hacia el éxito profesional”
                        </p>
                    </div>

                    <!-- Enlaces rápidos -->
                    <div class="col-12 col-md-6">
                        <ul class="nav justify-content-center justify-content-md-end gap-3">
                            <li class="nav-item"><a href="#inicio" class="text-secondary text-decoration-none small footer-link">Inicio</a></li>
                            <li class="nav-item"><a href="#como-funciona" class="text-secondary text-decoration-none small footer-link">Cómo funciona</a></li>
                            <li class="nav-item"><a href="#beneficios" class="text-secondary text-decoration-none small footer-link">Beneficios</a></li>
                            <li class="nav-item"><a href="#roles" class="text-secondary text-decoration-none small footer-link">Ingresar</a></li>
                        </ul>
                    </div>
                </div>

                <!-- Copyright -->
                <div class="pt-4 text-center text-secondary small">
                    © 2026 ProPath. Todos los derechos reservados.
                </div>
            </div>
        </footer>

        <!-- Modal Informativo: Módulos en desarrollo -->
        <div class="modal fade" id="modalDesarrollo" tabindex="-1" aria-labelledby="modalDesarrolloTitle" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content modal-content-propath">
                    <div class="modal-header modal-header-propath">
                        <h5 class="modal-title" id="modalDesarrolloTitle">
                            <i class="bi bi-tools me-2 text-warning"></i>
                            <span>Módulo en Desarrollo</span>
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body modal-body-propath text-center py-4">
                        <div class="rounded-circle bg-warning-subtle text-warning-emphasis d-inline-flex align-items-center justify-content-center p-3 mb-3" style="width: 70px; height: 70px;">
                            <i class="bi bi-cone-striped fs-1"></i>
                        </div>
                        <h4 class="h5 fw-bold text-dark mb-2" id="modalDesarrolloNombre">Módulo Estudiante</h4>
                        <p class="text-muted" id="modalDesarrolloTexto">
                            Esta funcionalidad se encuentra actualmente en desarrollo y estará disponible en una próxima versión de ProPath.
                        </p>
                        <div class="p-3 bg-light rounded-3 text-start small border">
                            <div class="fw-semibold text-primary mb-1"><i class="bi bi-info-circle me-1"></i> Módulos actualmente habilitados:</div>
                            <span class="text-secondary">El <strong>Módulo Administrador</strong> ya está activo y puedes acceder haciendo clic en “Ingresar como administrador”.</span>
                        </div>
                    </div>
                    <div class="modal-footer bg-light border-0 justify-content-between">
                        <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Entendido</button>
                        <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-primary rounded-pill px-4" style="background: var(--propath-blue); border-color: var(--propath-blue);">
                            Ir al Administrador
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <!-- Bootstrap 5 Bundle JS -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <!-- Lógica JavaScript ProPath -->
        <script src="${pageContext.request.contextPath}/js/script.js"></script>
    </body>
</html>
