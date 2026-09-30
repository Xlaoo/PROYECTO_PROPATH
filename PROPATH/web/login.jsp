<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Iniciar Sesión | ProPath - Portal de Prácticas</title>

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
        <div class="login-wrapper">
            <div class="login-card">
                <!-- Logo Oficial ProPath -->
                <div class="login-header text-center mb-4">
                    <img src="${pageContext.request.contextPath}/img/logo.png"
                         alt="ProPath"
                         class="logo-login">
                    <div>
                        <span class="login-badge-role">
                            <i class="bi bi-shield-lock-fill"></i> Módulo Administrador
                        </span>
                    </div>
                </div>

                <!-- Mensaje de error al fallar credenciales -->
                <% 
                    String error = (String) request.getAttribute("error");
                    if (error != null && !error.isEmpty()) { 
                %>
                    <div class="alert alert-danger d-flex align-items-center mb-4 py-2 px-3 rounded-3 border-danger-subtle" role="alert">
                        <i class="bi bi-exclamation-triangle-fill fs-5 me-2 text-danger"></i>
                        <div class="small fw-semibold">
                            <%= error %>
                        </div>
                    </div>
                <% } %>

                <!-- Notificación de cierre de sesión exitoso -->
                <% 
                    String logoutParam = request.getParameter("logout");
                    if ("1".equals(logoutParam)) { 
                %>
                    <div class="alert alert-info d-flex align-items-center mb-4 py-2 px-3 rounded-3 border-info-subtle" role="alert">
                        <i class="bi bi-info-circle-fill fs-5 me-2 text-info"></i>
                        <div class="small fw-semibold">
                            Has cerrado sesión de forma segura.
                        </div>
                    </div>
                <% } %>

                <!-- Formulario de inicio de sesión -->
                <form action="${pageContext.request.contextPath}/LoginServlet" method="POST" autocomplete="off" novalidate>
                    <!-- Campo Correo -->
                    <div class="form-floating-custom">
                        <input type="email" 
                               class="form-control" 
                               id="correo" 
                               name="correo" 
                               placeholder="admin@propath.com" 
                               value="<%= request.getAttribute("correoIngresado") != null ? request.getAttribute("correoIngresado") : "" %>"
                               required 
                               autofocus>
                        <i class="bi bi-envelope-fill input-icon"></i>
                    </div>

                    <!-- Campo Contraseña -->
                    <div class="form-floating-custom">
                        <input type="password" 
                               class="form-control" 
                               id="contrasena" 
                               name="contrasena" 
                               placeholder="Contraseña" 
                               required>
                        <i class="bi bi-lock-fill input-icon"></i>
                        <button type="button" class="toggle-password-btn" id="togglePassword" title="Mostrar/ocultar contraseña" tabindex="-1">
                            <i class="bi bi-eye"></i>
                        </button>
                    </div>

                    <!-- Botón Iniciar Sesión -->
                    <div class="d-grid mt-4">
                        <button type="submit" class="btn btn-propath-primary">
                            <span>Iniciar sesión</span>
                            <i class="bi bi-arrow-right-short fs-4"></i>
                        </button>
                    </div>
                </form>

                <!-- Tarjeta con credenciales de prueba temporal -->
                <div class="demo-credentials-badge">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <div class="demo-title">
                            <i class="bi bi-key-fill text-warning"></i>
                            <span>Credenciales temporales (Demo)</span>
                        </div>
                        <button type="button" id="btnFillDemo" class="btn btn-sm btn-outline-primary py-0 px-2 rounded-pill" style="font-size: 0.72rem;">
                            Auto-completar
                        </button>
                    </div>
                    <div class="small text-secondary">
                        <strong>Correo:</strong> <code>admin@propath.com</code><br>
                        <strong>Contraseña:</strong> <code>Admin123</code>
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
