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

    String error = (String) request.getAttribute("error");
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
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
    <div class="login-wrapper">
        <div class="login-card">
            <div class="login-header text-center mb-4">
                <img src="${pageContext.request.contextPath}/img/logo.png" alt="ProPath" class="logo-login">
                <div>
                    <span class="login-badge-role">
                        <i class="bi bi-shield-lock-fill"></i>
                        Recuperación de Cuenta
                    </span>
                </div>
            </div>

            <% if (error != null && !error.isEmpty()) { %>
                <div class="alert alert-danger rounded-3 mb-4">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>
                    <%= error %>
                </div>
            <% } %>

            <% if ("1".equals(paso)) { %>

                <p class="text-muted small text-center mb-4">
                    Ingresa tu correo registrado en ProPath para verificar tu cuenta.
                </p>

                <form action="${pageContext.request.contextPath}/recuperar-contrasena"
                      method="POST" autocomplete="off">

                    <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                    <input type="hidden" name="accion" value="verificar_correo">

                    <div class="form-floating-custom mb-3">
                        <input type="email" class="form-control"
                               id="correo" name="correo"
                               placeholder="correo@ejemplo.com"
                               required autofocus>
                        <i class="bi bi-envelope-fill input-icon"></i>
                    </div>

                    <div class="d-grid mt-4">
                        <button type="submit" class="btn btn-propath-primary">
                            <span>Verificar correo</span>
                            <i class="bi bi-arrow-right-short fs-4"></i>
                        </button>
                    </div>
                </form>

            <% } else if ("2".equals(paso)) { %>

                <p class="text-muted small text-center mb-4">
                    Confirma tu identidad para continuar con el cambio de contraseña.
                </p>

                <form action="${pageContext.request.contextPath}/recuperar-contrasena"
                      method="POST" autocomplete="off">

                    <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                    <input type="hidden" name="accion" value="verificar_identidad">

                    <div class="form-floating-custom mb-3">
                        <input type="password" class="form-control"
                               id="contrasenaActual" name="contrasenaActual"
                               placeholder="Contraseña actual"
                               required autofocus>
                        <i class="bi bi-lock-fill input-icon"></i>
                        <button type="button" class="toggle-password-btn"
                                id="toggleActual" title="Mostrar/ocultar contraseña">
                            <i class="bi bi-eye"></i>
                        </button>
                    </div>

                    <div class="d-grid mt-4">
                        <button type="submit" class="btn btn-propath-primary">
                            <span>Verificar identidad</span>
                            <i class="bi bi-shield-check fs-4"></i>
                        </button>
                    </div>
                </form>

            <% } else if ("3".equals(paso)) { %>

                <p class="text-muted small text-center mb-4">
                    Establece tu nueva contraseña de acceso.
                </p>

                <form action="${pageContext.request.contextPath}/recuperar-contrasena"
                      method="POST" autocomplete="off">

                    <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                    <input type="hidden" name="accion" value="cambiar_contrasena">

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

                    <div class="d-grid mt-4">
                        <button type="submit" class="btn btn-propath-primary">
                            <span>Guardar nueva contraseña</span>
                            <i class="bi bi-check-lg fs-4"></i>
                        </button>
                    </div>
                </form>

            <% } %>

            <hr class="my-4">

            <div class="text-center">
                <a href="${pageContext.request.contextPath}/login.jsp"
                   class="text-decoration-none fw-semibold small">
                    <i class="bi bi-arrow-left me-1"></i>
                    Volver a Iniciar sesión
                </a>
            </div>
        </div>
    </div>

    <script>
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

        setupToggle('toggleActual', 'contrasenaActual');
        setupToggle('toggleNueva', 'nuevaContrasena');
        setupToggle('toggleConfirmar', 'confirmarContrasena');
    </script>
</body>
</html>