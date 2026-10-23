<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html lang="es" data-bs-theme="light">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Iniciar Sesión | ProPath - Portal de Prácticas</title>

        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
            rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
    </head>

    <body>
        <div class="login-wrapper">
            <div class="login-card">
                <div class="login-header text-center mb-4">
                    <img src="${pageContext.request.contextPath}/img/logo.png" alt="ProPath" class="logo-login">
                    <div>
                        <span class="login-badge-role">
                            <i class="bi bi-person-circle"></i> Acceso a ProPath
                        </span>
                    </div>
                </div>
                <% String error=(String) request.getAttribute("error"); if (error==null || error.isEmpty()) {
                    error=request.getParameter("error"); } if (error !=null && !error.isEmpty()) { %>
                    <div class="alert alert-danger d-flex align-items-center mb-4 py-2 px-3 rounded-3 border-danger-subtle"
                        role="alert">
                        <i class="bi bi-exclamation-triangle-fill fs-5 me-2 text-danger"></i>
                        <div class="small fw-semibold">
                            <%= error %>
                        </div>
                    </div>
                    <% } %>
                        <% String logoutParam=request.getParameter("logout"); if ("1".equals(logoutParam)) { %>
                            <div class="alert alert-info d-flex align-items-center mb-4 py-2 px-3 rounded-3 border-info-subtle"
                                role="alert">
                                <i class="bi bi-info-circle-fill fs-5 me-2 text-info"></i>
                                <div class="small fw-semibold">
                                    Has cerrado sesión de forma segura.
                                </div>
                            </div>
                            <% } %>
                                <% String exitoParam=request.getParameter("exito"); if (exitoParam !=null &&
                                    !exitoParam.isEmpty()) { %>
                                    <div class="alert alert-success d-flex align-items-center mb-4 py-2 px-3 rounded-3 border-success-subtle"
                                        role="alert">
                                        <i class="bi bi-check-circle-fill fs-5 me-2 text-success"></i>
                                        <div class="small fw-semibold">
                                            <%= "cuenta_desactivada" .equals(exitoParam)
                                                ? "Tu cuenta ha sido dada de baja correctamente."
                                                : ("password_actualizada".equals(exitoParam)
                                                    ? "Tu contraseña ha sido restablecida exitosamente. Inicia sesión con tu nueva contraseña."
                                                    : "Operación realizada exitosamente.") %>
                                        </div>
                                    </div>
                                    <% } %>
                                        <form action="${pageContext.request.contextPath}/LoginServlet" method="POST"
                                            autocomplete="off" novalidate>
                                            <div class="form-floating-custom">
                                                <input type="email" class="form-control" id="correo" name="correo"
                                                    placeholder="admin@propath.com" required autofocus>
                                                <i class="bi bi-envelope-fill input-icon"></i>
                                            </div>
                                            <div class="form-floating-custom">
                                                <input type="password" class="form-control" id="contrasena"
                                                    name="contrasena" placeholder="Contraseña" required>
                                                <i class="bi bi-lock-fill input-icon"></i>
                                                <button type="button" class="toggle-password-btn" id="togglePassword"
                                                    title="Mostrar/ocultar contraseña" tabindex="-1">
                                                    <i class="bi bi-eye"></i>
                                                </button>
                                            </div>

                                            <div class="text-end mt-2 mb-3">
                                                <a href="${pageContext.request.contextPath}/recuperar-contrasena.jsp"
                                                    class="small text-decoration-none fw-semibold">
                                                    ¿Olvidaste tu contraseña?
                                                </a>
                                            </div>

                                            <div class="d-grid mt-4">
                                                <button type="submit" class="btn btn-propath-primary">
                                                    <span>Iniciar sesión</span>
                                                    <i class="bi bi-arrow-right-short fs-4"></i>
                                                </button>
                                            </div>
                                        </form>
                                        <div class="text-center mt-4">

                                            <p class="text-muted small mb-3">
                                                ¿Todavía no tienes una cuenta?
                                            </p>

                                            <div class="d-grid gap-2">

                                                <a href="${pageContext.request.contextPath}/registro-estudiante.jsp"
                                                    class="btn btn-outline-primary">
                                                    <i class="bi bi-mortarboard-fill me-2"></i>
                                                    Registrarme como Estudiante
                                                </a>

                                                <a href="${pageContext.request.contextPath}/registro-empresa.jsp"
                                                    class="btn btn-outline-primary">
                                                    <i class="bi bi-building me-2"></i>
                                                    Registrarme como Empresa
                                                </a>

                                            </div>

                                            <hr class="my-4">

                                            <a href="${pageContext.request.contextPath}/index.jsp"
                                                class="text-decoration-none fw-semibold">
                                                <i class="bi bi-arrow-left me-2"></i>
                                                Volver a la página principal
                                            </a>

                                        </div>

            </div>
        </div>

        <script type="module" src="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/js/bootstrap.bundle.min.js"></script>
        <script type="module" src="${pageContext.request.contextPath}/js/script.js"></script>
    </body>

    </html>