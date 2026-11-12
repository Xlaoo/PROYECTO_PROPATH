<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="es" data-bs-theme="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Iniciar Sesión | ProPath - Portal de Prácticas y Empleo</title>

    <!-- Tipografía Google Inter & Bootstrap 5 + Iconos -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    
    <!-- Estilos base globales y estilo profesional dedicado del login -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login-pro.css">
</head>

<body class="login-page-body">
    <div class="login-main-wrapper">
        <div class="login-card-container">

            <!-- =========================================================================
                 PANEL IZQUIERDO: IDENTIDAD INSTITUCIONAL PROPATH
                 ========================================================================= -->
            <div class="login-brand-panel">
                <div class="brand-showcase-header">
                    <div class="brand-logo-badge">
                        <img src="${pageContext.request.contextPath}/img/logo-propath.png" alt="ProPath" class="brand-logo-img">
                    </div>
                    <h1 class="brand-headline">Impulsa tu Futuro Profesional</h1>
                    <p class="brand-subheadline">
                        Conectamos tu talento con las mejores oportunidades de empleo y prácticas profesionales.
                    </p>
                </div>

                <div class="brand-features-group">
                    <div class="brand-feature-card">
                        <div class="brand-feature-icon">
                            <i class="bi bi-mortarboard-fill"></i>
                        </div>
                        <div class="brand-feature-body">
                            <h6>Para Estudiantes y Egresados</h6>
                            <p>Accede a convocatorias verificadas, postula rápidamente y valida tus horas de prácticas.</p>
                        </div>
                    </div>

                    <div class="brand-feature-card">
                        <div class="brand-feature-icon">
                            <i class="bi bi-building-check"></i>
                        </div>
                        <div class="brand-feature-body">
                            <h6>Para Empresas e Instituciones</h6>
                            <p>Publica ofertas laborales, filtra postulantes cualificados y optimiza tus entrevistas.</p>
                        </div>
                    </div>

                    <div class="brand-feature-card">
                        <div class="brand-feature-icon">
                            <i class="bi bi-shield-check"></i>
                        </div>
                        <div class="brand-feature-body">
                            <h6>Seguridad Integral y Protección de Datos</h6>
                            <p>Acceso seguro con cifrado BCrypt, verificación 2FA y validación institucional.</p>
                        </div>
                    </div>
                </div>

                <div class="brand-panel-footer">
                    <div class="brand-badge-pill">
                        <i class="bi bi-patch-check-fill text-info"></i>
                        <span>Red Oficial ProPath</span>
                    </div>
                    <div class="small text-white-50">
                        &copy; <%= java.time.Year.now().getValue() %> ProPath
                    </div>
                </div>
            </div>

            <!-- =========================================================================
                 PANEL DERECHO: FORMULARIO DE ACCESO PROFESIONAL
                 ========================================================================= -->
            <div class="login-form-panel">
                
                <div class="form-panel-top-bar">
                    <span class="security-badge">
                        <i class="bi bi-shield-lock-fill"></i> Portal Seguro
                    </span>
                    <a href="${pageContext.request.contextPath}/index.jsp" class="btn-home-link">
                        <i class="bi bi-house-door me-1"></i> Inicio
                    </a>
                </div>

                <h2 class="form-panel-title">¡Bienvenido a ProPath!</h2>
                <p class="form-panel-subtitle">Ingresa tus credenciales para acceder a tu plataforma.</p>

                <% 
                    String error = (String) request.getAttribute("error"); 
                    if (error == null || error.isEmpty()) {
                        error = request.getParameter("error"); 
                    } 
                    if (error != null && !error.isEmpty()) { 
                %>
                    <div class="alert alert-danger d-flex align-items-center mb-3 py-2 px-3 rounded-3 border-danger-subtle" role="alert">
                        <i class="bi bi-exclamation-triangle-fill fs-5 me-2 text-danger"></i>
                        <div class="small fw-semibold">
                            <% if ("no_autorizado".equals(error)) { %>
                                Debes iniciar sesión con una cuenta autorizada para acceder a esta página.
                            <% } else if ("sesion_cerrada".equals(error)) { %>
                                Tu sesión ha sido finalizada correctamente.
                            <% } else { %>
                                <%= com.propath.conexion.SeguridadUtil.escaparHtml(error) %>
                            <% } %>
                        </div>
                    </div>
                <% } %>

                <% 
                    String exito = request.getParameter("exito"); 
                    if (exito != null && !exito.isEmpty()) { 
                %>
                    <div class="alert alert-success d-flex align-items-center mb-3 py-2 px-3 rounded-3 border-success-subtle" role="alert">
                        <i class="bi bi-check-circle-fill fs-5 me-2 text-success"></i>
                        <div class="small fw-semibold">
                            <% if ("registro_exitoso".equals(exito)) { %>
                                ¡Cuenta creada con éxito! Ya puedes iniciar sesión con tus credenciales.
                            <% } else if ("password_actualizada".equals(exito)) { %>
                                Tu contraseña ha sido actualizada con éxito. Inicia sesión con tu nueva clave.
                            <% } else { %>
                                Operación completada satisfactoriamente.
                            <% } %>
                        </div>
                    </div>
                <% } %>

                <form action="${pageContext.request.contextPath}/login" method="POST" autocomplete="off" id="formLoginPrincipal">
                    
                    <!-- Campo de Correo Electrónico -->
                    <div class="login-input-group">
                        <input type="email" class="form-control" id="correo" name="correo"
                            placeholder="nombre@ejemplo.com"
                            value="<%= request.getAttribute("correoIngresado") != null ? com.propath.conexion.SeguridadUtil.escaparHtml((String) request.getAttribute("correoIngresado")) : "" %>"
                            required autofocus>
                        <i class="bi bi-envelope-fill input-leading-icon"></i>
                    </div>

                    <!-- Campo de Contraseña -->
                    <div class="login-input-group">
                        <input type="password" class="form-control" id="contrasena" name="contrasena"
                            placeholder="Ingresa tu contraseña" required minlength="6">
                        <i class="bi bi-lock-fill input-leading-icon"></i>
                        <button type="button" class="login-toggle-pass-btn" id="togglePasswordBtn"
                            aria-label="Mostrar u ocultar contraseña" tabindex="-1">
                            <i class="bi bi-eye"></i>
                        </button>
                    </div>

                    <!-- Opciones de Sesión -->
                    <div class="login-options-row">
                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" id="recordarme" name="recordarme" value="1">
                            <label class="form-check-label" for="recordarme">
                                Recordar sesión
                            </label>
                        </div>
                        <a href="${pageContext.request.contextPath}/recuperar-contrasena" class="forgot-pass-link">
                            ¿Olvidaste tu contraseña?
                        </a>
                    </div>

                    <!-- Botón de Inicio de Sesión -->
                    <button type="submit" class="btn-login-submit" id="btnIniciarSesion">
                        <span>Iniciar Sesión</span>
                        <i class="bi bi-arrow-right-short fs-4"></i>
                    </button>
                </form>

                <div class="login-section-divider">
                    <span>¿No tienes una cuenta?</span>
                </div>

                <!-- Opciones de Registro -->
                <div class="row g-2">
                    <div class="col-sm-6">
                        <a href="${pageContext.request.contextPath}/registro?tipo=estudiante" class="role-register-btn">
                            <i class="bi bi-mortarboard text-primary fs-5"></i>
                            <span>Soy Estudiante</span>
                        </a>
                    </div>
                    <div class="col-sm-6">
                        <a href="${pageContext.request.contextPath}/registro?tipo=empresa" class="role-register-btn">
                            <i class="bi bi-building text-primary fs-5"></i>
                            <span>Soy Empresa</span>
                        </a>
                    </div>
                </div>

            </div>
        </div>
    </div>

    <!-- =========================================================================
         MODAL DE VERIFICACIÓN 2FA DE INICIO DE SESIÓN
         ========================================================================= -->
    <div class="modal fade modal-seguridad" id="modal2FA" tabindex="-1" aria-labelledby="modal2FALabel" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content border-0 shadow-lg rounded-4">
                <div class="modal-header bg-dark text-white rounded-top-4">
                    <h5 class="modal-title h6 fw-bold mb-0 text-white" id="modal2FALabel">
                        <i class="bi bi-shield-lock-fill me-2 text-info"></i> Verificación de Acceso (2FA)
                    </h5>
                    <button type="button" class="btn-close btn-close-white" id="btnCerrar2FAX" aria-label="Cancelar"></button>
                </div>
                <div class="modal-body p-4 text-center">
                    <div class="mb-3">
                        <div class="avatar-icon-wrapper mx-auto mb-2" style="width: 54px; height: 54px; background: #E0F2FE; border-radius: 50%; display: flex; align-items: center; justify-content: center;">
                            <i class="bi bi-envelope-check-fill text-primary fs-3"></i>
                        </div>
                        <h6 class="fw-bold mb-1">Código de Seguridad</h6>
                        <p class="text-muted small mb-0">Hemos enviado un código alfanumérico de <strong>6 caracteres</strong> a:</p>
                        <p class="fw-bold text-primary small mb-0" id="textoCorreo2FA">
                            <%= request.getAttribute("correoMascara") != null ? com.propath.conexion.SeguridadUtil.escaparHtml((String) request.getAttribute("correoMascara")) : "tu correo registrado" %>
                        </p>
                    </div>

                    <div id="contenedorAlertas2FA"></div>

                    <div class="mb-3">
                        <label for="inputCodigo2FA" class="form-label small fw-semibold text-secondary">Ingresa el código de 6 dígitos:</label>
                        <input type="text" class="form-control text-center fs-4 fw-bold letter-spacing-2" id="inputCodigo2FA"
                               maxlength="6" autocomplete="one-time-code" placeholder="123456" autofocus style="letter-spacing: 4px;">
                        <div class="d-flex justify-content-between align-items-center mt-2 px-1">
                            <span class="small text-muted" id="textoExpiracion2FA">
                                <i class="bi bi-stopwatch me-1 text-primary"></i> Expira en <strong>10:00</strong>
                            </span>
                            <span class="small text-muted" id="textoIntentos2FA">5 intentos permitidos</span>
                        </div>
                    </div>
                </div>
                <div class="modal-footer bg-light border-top d-flex justify-content-between p-3 rounded-bottom-4">
                    <button type="button" class="btn btn-outline-secondary btn-sm rounded-pill px-3" id="btnCancelar2FA">
                        <i class="bi bi-x-circle me-1"></i> Cancelar
                    </button>
                    <div class="d-flex gap-2">
                        <button type="button" class="btn btn-outline-primary btn-sm rounded-pill px-3" id="btnReenviar2FA">
                            <i class="bi bi-arrow-repeat me-1"></i> Reenviar código
                        </button>
                        <button type="button" class="btn btn-primary btn-sm rounded-pill px-4" id="btnConfirmar2FA">
                            <span>Verificar y Entrar</span>
                            <i class="bi bi-arrow-right-short fs-5 ms-1"></i>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Scripts de Bootstrap y Lógica de Verificación -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/verificacion-seguridad.js"></script>

    <%
        boolean autoAbrirModal = Boolean.TRUE.equals(request.getAttribute("mostrarModal2FA"))
                || session.getAttribute("AUTH_2FA_PENDIENTE_USUARIO") != null;
        long segEspera = 60L;
        if (request.getAttribute("segundosEspera") instanceof Number) {
            segEspera = ((Number) request.getAttribute("segundosEspera")).longValue();
        }
        long segExpira = 600L;
        if (request.getAttribute("segundosExpiracion") instanceof Number) {
            segExpira = ((Number) request.getAttribute("segundosExpiracion")).longValue();
        }
    %>
    <div id="data2FAConfig" style="display:none;"
         data-auto-abrir="<%= autoAbrirModal ? "true" : "false" %>"
         data-segundos-espera="<%= segEspera %>"
         data-segundos-expiracion="<%= segExpira %>"
         data-context-path="${pageContext.request.contextPath}"></div>

    <script>
        document.addEventListener('DOMContentLoaded', function () {
            const dataConfig = document.getElementById('data2FAConfig');
            const contextPath = dataConfig ? dataConfig.getAttribute('data-context-path') : '';
            const autoAbrir2FA = dataConfig && dataConfig.getAttribute('data-auto-abrir') === 'true';
            const segundosEspera2FA = dataConfig ? (parseInt(dataConfig.getAttribute('data-segundos-espera'), 10) || 60) : 60;
            const segundosExpiracion2FA = dataConfig ? (parseInt(dataConfig.getAttribute('data-segundos-expiracion'), 10) || 600) : 600;

            // Toggle de contraseña
            const toggleBtn = document.getElementById('togglePasswordBtn');
            const passInput = document.getElementById('contrasena');
            if (toggleBtn && passInput) {
                toggleBtn.addEventListener('click', function () {
                    const isPass = passInput.type === 'password';
                    passInput.type = isPass ? 'text' : 'password';
                    this.innerHTML = isPass ? '<i class="bi bi-eye-slash"></i>' : '<i class="bi bi-eye"></i>';
                });
            }

            const modalElement = document.getElementById('modal2FA');
            const bsModal = new bootstrap.Modal(modalElement);
            const inputCodigo = document.getElementById('inputCodigo2FA');
            const btnConfirmar = document.getElementById('btnConfirmar2FA');
            const btnReenviar = document.getElementById('btnReenviar2FA');
            const btnCancelar = document.getElementById('btnCancelar2FA');
            const btnCerrarX = document.getElementById('btnCerrar2FAX');
            const alertasArea = document.getElementById('contenedorAlertas2FA');
            const textoExpiracion = document.getElementById('textoExpiracion2FA');
            const textoIntentos = document.getElementById('textoIntentos2FA');

            let procesando = false;

            function ejecutarVerificacion2FA() {
                const codigo = inputCodigo.value.trim().toUpperCase();
                if (codigo.length !== 6) {
                    ProPathSeguridad.mostrarAlerta(alertasArea, 'danger', 'El código debe tener exactamente 6 caracteres alfanuméricos.');
                    return;
                }

                if (procesando) return;
                procesando = true;
                btnConfirmar.disabled = true;
                btnConfirmar.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Verificando...';

                const params = new URLSearchParams();
                params.append('accion', 'verificar_2fa');
                params.append('codigo', codigo);

                fetch(contextPath + '/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    body: params.toString()
                })
                    .then(r => r.json())
                    .then(data => {
                        procesando = false;
                        btnConfirmar.disabled = false;
                        btnConfirmar.innerHTML = '<span>Verificar y Entrar</span> <i class="bi bi-arrow-right-short fs-5 ms-1"></i>';

                        if (data.exito) {
                            ProPathSeguridad.mostrarAlerta(alertasArea, 'success', '¡Código verificado! Redirigiendo a tu panel...');
                            const destino = data.redirectUrl || data.redirect || (contextPath + '/estudiante/dashboard.jsp');
                            setTimeout(() => {
                                window.location.href = destino;
                            }, 500);
                        } else {
                            ProPathSeguridad.mostrarAlerta(alertasArea, 'danger', data.mensaje);
                            if (data.intentosRestantes !== undefined) {
                                textoIntentos.innerText = data.intentosRestantes + ' intento(s) restante(s)';
                            }
                            inputCodigo.select();
                        }
                    })
                    .catch(() => {
                        procesando = false;
                        btnConfirmar.disabled = false;
                        btnConfirmar.innerHTML = '<span>Verificar y Entrar</span> <i class="bi bi-arrow-right-short fs-5 ms-1"></i>';
                        ProPathSeguridad.mostrarAlerta(alertasArea, 'danger', 'Error de conexión con el servidor al verificar el código 2FA.');
                    });
            }

            btnConfirmar.addEventListener('click', ejecutarVerificacion2FA);
            ProPathSeguridad.configurarCampoCodigo(inputCodigo, ejecutarVerificacion2FA);

            // Reenviar código 2FA
            btnReenviar.addEventListener('click', function () {
                btnReenviar.disabled = true;
                btnReenviar.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Reenviando...';

                const params = new URLSearchParams();
                params.append('accion', 'reenviar');
                params.append('proposito', 'LOGIN');

                fetch(contextPath + '/verificar-codigo', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                    body: params.toString()
                })
                    .then(r => r.json())
                    .then(data => {
                        if (data.exito) {
                            ProPathSeguridad.mostrarAlerta(alertasArea, 'info', data.mensaje);
                            inputCodigo.value = '';
                            inputCodigo.focus();
                            ProPathSeguridad.iniciarCooldownReenvio(btnReenviar, data.segundosEspera || 60);
                            ProPathSeguridad.iniciarTemporizadorExpiracion(textoExpiracion, data.segundosExpiracion || 600);
                        } else {
                            ProPathSeguridad.mostrarAlerta(alertasArea, 'danger', data.mensaje);
                            btnReenviar.disabled = false;
                            btnReenviar.innerHTML = '<i class="bi bi-arrow-repeat me-1"></i> Reenviar código';
                        }
                    })
                    .catch(() => {
                        btnReenviar.disabled = false;
                        btnReenviar.innerHTML = '<i class="bi bi-arrow-repeat me-1"></i> Reenviar código';
                        ProPathSeguridad.mostrarAlerta(alertasArea, 'danger', 'Error de red al intentar reenviar el código.');
                    });
            });

            // Cancelar y cerrar flujo 2FA
            const cancelarFlujo = function () {
                fetch(contextPath + '/verificar-codigo', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                    body: 'accion=cancelar_2fa'
                }).finally(() => {
                    ProPathSeguridad.limpiarTemporizadores();
                    bsModal.hide();
                    window.location.href = contextPath + '/login.jsp';
                });
            };

            btnCancelar.addEventListener('click', cancelarFlujo);
            btnCerrarX.addEventListener('click', cancelarFlujo);

            if (autoAbrir2FA) {
                bsModal.show();
                setTimeout(() => inputCodigo.focus(), 500);
                ProPathSeguridad.iniciarCooldownReenvio(btnReenviar, segundosEspera2FA);
                ProPathSeguridad.iniciarTemporizadorExpiracion(textoExpiracion, segundosExpiracion2FA);
            }
        });
    </script>
</body>
</html>
