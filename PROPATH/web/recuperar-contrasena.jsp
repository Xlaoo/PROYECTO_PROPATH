<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String csrfToken = (String) session.getAttribute("csrfToken");
    if (csrfToken == null) {
        byte[] bytes = new byte[24];
        new java.security.SecureRandom().nextBytes(bytes);
        csrfToken = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute("csrfToken", csrfToken);
    }

    String paso = (String) request.getAttribute("paso");
    if (paso == null) paso = "1";

    String correo = (String) request.getAttribute("correo");
    if (correo == null) correo = "";

    String correoMascara = (String) request.getAttribute("correoMascara");
    if (correoMascara == null || correoMascara.isEmpty()) {
        correoMascara = correo;
    }

    String tokenReset = (String) request.getAttribute("tokenReset");
    if (tokenReset == null) {
        tokenReset = (String) session.getAttribute("recuperacionTokenReset");
    }

    Boolean abrirModalCodigo = (Boolean) request.getAttribute("abrirModalCodigo");
    Object intentosObj = request.getAttribute("intentosRestantes");
    Integer intentosRestantes = (intentosObj instanceof Number) ? ((Number) intentosObj).intValue() : null;

    Object esperaObj = request.getAttribute("segundosEspera");
    Long segundosEspera = (esperaObj instanceof Number) ? ((Number) esperaObj).longValue() : 60L;

    Object expiraObj = request.getAttribute("segundosExpiracion");
    Long segundosExpiracion = (expiraObj instanceof Number) ? ((Number) expiraObj).longValue() : 600L;

    String error = (String) request.getAttribute("error");
    String info = (String) request.getAttribute("info");
%>
<!DOCTYPE html>
<html lang="es" data-bs-theme="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recuperar Contraseña | ProPath</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
    <div class="login-wrapper py-5">
        <div class="login-card" style="max-width: 480px;">
            <div class="login-header text-center mb-4">
                <a href="${pageContext.request.contextPath}/index.jsp">
                    <img src="${pageContext.request.contextPath}/img/logo-propath.png" alt="ProPath" class="logo-login">
                </a>
                <div>
                    <span class="login-badge-role">
                        <i class="bi bi-shield-lock-fill"></i>
                        Recuperación de Cuenta
                    </span>
                </div>
            </div>

            <% if (error != null && !error.isEmpty()) { %>
                <div class="alert alert-danger d-flex align-items-center mb-4 py-2 px-3 rounded-3 border-danger-subtle" role="alert">
                    <i class="bi bi-exclamation-triangle-fill fs-5 me-2 text-danger"></i>
                    <div class="small fw-semibold">
                        <%= com.propath.conexion.SeguridadUtil.escaparHtml(error) %>
                    </div>
                </div>
            <% } %>

            <% if (info != null && !info.isEmpty()) { %>
                <div class="alert alert-info d-flex align-items-center mb-4 py-2 px-3 rounded-3 border-info-subtle" role="alert">
                    <i class="bi bi-info-circle-fill fs-5 me-2 text-info"></i>
                    <div class="small fw-semibold">
                        <%= com.propath.conexion.SeguridadUtil.escaparHtml(info) %>
                    </div>
                </div>
            <% } %>

            <% if (!"3".equals(paso)) { %>

                <p class="text-muted small text-center mb-4">
                    Ingresa tu correo registrado en ProPath. Si la cuenta existe, te enviaremos un código de seguridad para verificar tu identidad y restablecer tu contraseña.
                </p>

                <form action="${pageContext.request.contextPath}/recuperar-contrasena"
                      method="POST" autocomplete="off" id="formSolicitarCodigo">

                    <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                    <input type="hidden" name="accion" value="solicitar_codigo">

                    <div class="form-floating-custom mb-3">
                        <input type="email" class="form-control"
                               id="correo" name="correo"
                               value="<%= com.propath.conexion.SeguridadUtil.escaparHtml(correo) %>"
                               placeholder="correo@ejemplo.com"
                               required autofocus>
                        <i class="bi bi-envelope-fill input-icon"></i>
                    </div>

                    <div class="d-grid mt-4">
                        <button type="submit" class="btn btn-propath-primary" id="btnEnviarCodigo">
                            <span>Enviar código de recuperación</span>
                            <i class="bi bi-send-fill fs-5 ms-1"></i>
                        </button>
                    </div>
                </form>

            <% } else { %>

                <div class="text-center mb-4">
                    <div class="avatar-icon-wrapper mx-auto mb-2" style="width: 50px; height: 50px; background: #DCFCE7; border-radius: 50%; display: flex; align-items: center; justify-content: center;">
                        <i class="bi bi-shield-check text-success fs-3"></i>
                    </div>
                    <h6 class="fw-bold mb-1">Identidad Verificada</h6>
                    <p class="text-muted small mb-0">Establece tu nueva contraseña para <strong><%= com.propath.conexion.SeguridadUtil.escaparHtml(correoMascara) %></strong>.</p>
                </div>

                <form action="${pageContext.request.contextPath}/recuperar-contrasena"
                      method="POST" autocomplete="off" id="formCambiarContrasena">

                    <input type="hidden" name="csrfToken" value="<%= com.propath.conexion.SeguridadUtil.escaparHtml(csrfToken) %>">
                    <input type="hidden" name="accion" value="cambiar_contrasena">
                    <input type="hidden" name="tokenReset" value="<%= tokenReset != null ? com.propath.conexion.SeguridadUtil.escaparHtml(tokenReset) : "" %>">

                    <div class="form-floating-custom mb-3">
                        <input type="password" class="form-control"
                               id="nuevaContrasena" name="nuevaContrasena"
                               placeholder="Nueva contraseña (mínimo 8 caracteres)"
                               minlength="8" required autofocus>
                        <i class="bi bi-lock-fill input-icon"></i>
                        <button type="button" class="toggle-password-btn"
                                id="toggleNueva" title="Mostrar/ocultar contraseña">
                            <i class="bi bi-eye"></i>
                        </button>
                    </div>

                    <div class="form-floating-custom mb-3">
                        <input type="password" class="form-control"
                               id="confirmarContrasena" name="confirmarContrasena"
                               placeholder="Confirmar contraseña"
                               minlength="8" required>
                        <i class="bi bi-shield-check input-icon"></i>
                        <button type="button" class="toggle-password-btn"
                                id="toggleConfirmar" title="Mostrar/ocultar contraseña">
                            <i class="bi bi-eye"></i>
                        </button>
                    </div>

                    <div class="form-text small mb-3 text-muted">
                        <i class="bi bi-shield-shaded me-1"></i> Mínimo 8 caracteres, combinando letras y números.
                    </div>

                    <div class="d-grid mt-4">
                        <button type="submit" class="btn btn-propath-primary">
                            <span>Guardar nueva contraseña</span>
                            <i class="bi bi-check-lg fs-4 ms-1"></i>
                        </button>
                    </div>
                </form>

            <% } %>

            <hr class="my-4">

            <div class="text-center">
                <a href="${pageContext.request.contextPath}/login.jsp"
                   class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-arrow-left me-1"></i> Volver a Iniciar Sesión
                </a>
            </div>
        </div>
    </div>

    <!-- =========================================================================
         MODAL DE VERIFICACIÓN DE CÓDIGO (RECUPERACIÓN)
         ========================================================================= -->
    <div class="modal fade modal-seguridad" id="modalRecuperacionCodigo" tabindex="-1" aria-labelledby="modalRecupLabel" aria-hidden="true" data-bs-backdrop="static">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title h6 fw-bold mb-0 text-white" id="modalRecupLabel">
                        <i class="bi bi-shield-lock-fill me-2 text-info"></i> Código de Recuperación
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                </div>
                <form action="${pageContext.request.contextPath}/recuperar-contrasena" method="POST" id="formVerificarModal">
                    <div class="modal-body p-4 text-center">
                        <input type="hidden" name="csrfToken" value="<%= com.propath.conexion.SeguridadUtil.escaparHtml(csrfToken) %>">
                        <input type="hidden" name="accion" value="verificar_codigo">
                        <input type="hidden" name="correo" value="<%= com.propath.conexion.SeguridadUtil.escaparHtml(correo) %>">

                        <div class="mb-3">
                            <div class="avatar-icon-wrapper mx-auto mb-2" style="width: 50px; height: 50px; background: #E0F2FE; border-radius: 50%; display: flex; align-items: center; justify-content: center;">
                                <i class="bi bi-envelope-paper-heart text-primary fs-3"></i>
                            </div>
                            <h6 class="fw-bold mb-1">Ingresa el código</h6>
                            <p class="text-muted small mb-0">Hemos enviado un código de seguridad de <strong>6 dígitos</strong> a:</p>
                            <p class="fw-bold text-primary small mb-0" id="textoCorreoModal"><%= com.propath.conexion.SeguridadUtil.escaparHtml(correoMascara) %></p>
                        </div>

                        <div id="contenedorAlertasModal">
                            <% if (error != null && !error.isEmpty() && Boolean.TRUE.equals(abrirModalCodigo)) { %>
                                <div class="alert alert-danger py-2 px-3 small rounded-3 mb-3 text-start">
                                    <i class="bi bi-exclamation-circle-fill me-1"></i> <%= com.propath.conexion.SeguridadUtil.escaparHtml(error) %>
                                </div>
                            <% } %>
                        </div>

                        <div class="mb-3">
                            <label for="inputCodigoRecuperacion" class="form-label small fw-semibold text-secondary">Código de verificación:</label>
                            <input type="text" class="form-control input-codigo-seguridad" id="inputCodigoRecuperacion"
                                   name="codigo" maxlength="6" autocomplete="one-time-code" placeholder="123456" autofocus required>
                            <div class="d-flex justify-content-between align-items-center mt-2 px-1">
                                <span class="small text-muted" id="textoExpiracionModal">
                                    <i class="bi bi-stopwatch me-1 text-primary"></i> Expira en <strong>10:00</strong>
                                </span>
                                <span class="small text-muted" id="textoIntentosModal">
                                    <%= (intentosRestantes != null) ? (intentosRestantes + " intentos restantes") : "5 intentos" %>
                                </span>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer bg-light border-top d-flex justify-content-between p-3">
                        <button type="button" class="btn btn-outline-secondary btn-sm rounded-pill px-3" data-bs-dismiss="modal">
                            Cancelar
                        </button>
                        <div class="d-flex gap-2">
                            <button type="button" class="btn btn-outline-primary btn-sm rounded-pill px-3" id="btnReenviarRecup">
                                <i class="bi bi-arrow-repeat me-1"></i> Reenviar
                            </button>
                            <button type="submit" class="btn btn-primary btn-sm rounded-pill px-4" id="btnConfirmarRecup">
                                <span>Verificar código</span>
                                <i class="bi bi-arrow-right-short fs-5 ms-1"></i>
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Formulario oculto para reenviar código -->
    <form id="formReenviarCodigo" action="${pageContext.request.contextPath}/recuperar-contrasena" method="POST" style="display:none;">
        <input type="hidden" name="csrfToken" value="<%= com.propath.conexion.SeguridadUtil.escaparHtml(csrfToken) %>">
        <input type="hidden" name="accion" value="solicitar_codigo">
        <input type="hidden" name="correo" value="<%= com.propath.conexion.SeguridadUtil.escaparHtml(correo) %>">
    </form>

    <div id="dataRecuperacionConfig" style="display:none;"
         data-abrir-modal="<%= Boolean.TRUE.equals(abrirModalCodigo) ? "true" : "false" %>"
         data-segundos-espera="<%= (segundosEspera != null) ? segundosEspera : 60L %>"
         data-segundos-expiracion="<%= (segundosExpiracion != null) ? segundosExpiracion : 600L %>"></div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/verificacion-seguridad.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            // Leer configuración limpia desde el DOM
            const configEl = document.getElementById('dataRecuperacionConfig');
            const abrirModal = configEl && configEl.getAttribute('data-abrir-modal') === 'true';
            const segEspera = configEl ? (parseInt(configEl.getAttribute('data-segundos-espera'), 10) || 60) : 60;
            const segExpira = configEl ? (parseInt(configEl.getAttribute('data-segundos-expiracion'), 10) || 600) : 600;

            // Configurar botones de ver/ocultar contraseña
            function setupToggle(btnId, inputId) {
                const btn = document.getElementById(btnId);
                const input = document.getElementById(inputId);
                if (btn && input) {
                    btn.addEventListener('click', () => {
                        const visible = input.type === 'password';
                        input.type = visible ? 'text' : 'password';
                        const icon = btn.querySelector('i');
                        if (icon) {
                            icon.classList.toggle('bi-eye');
                            icon.classList.toggle('bi-eye-slash');
                        }
                    });
                }
            }
            setupToggle('toggleNueva', 'nuevaContrasena');
            setupToggle('toggleConfirmar', 'confirmarContrasena');

            // Modal y controles
            const modalElement = document.getElementById('modalRecuperacionCodigo');
            const bsModal = new bootstrap.Modal(modalElement);
            const inputCodigo = document.getElementById('inputCodigoRecuperacion');
            const btnReenviar = document.getElementById('btnReenviarRecup');
            const textoExpiracion = document.getElementById('textoExpiracionModal');
            const formVerificar = document.getElementById('formVerificarModal');

            ProPathSeguridad.configurarCampoCodigo(inputCodigo, () => {
                formVerificar.submit();
            });

            // Si el servidor ordenó abrir el modal (después de solicitar código o error en código)
            if (abrirModal) {
                bsModal.show();
                setTimeout(() => {
                    inputCodigo.focus();
                }, 500);

                ProPathSeguridad.iniciarCooldownReenvio(btnReenviar, segEspera);
                ProPathSeguridad.iniciarTemporizadorExpiracion(textoExpiracion, segExpira);
            }

            btnReenviar.addEventListener('click', function () {
                document.getElementById('formReenviarCodigo').submit();
            });
        });
    </script>
</body>
</html>
