<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es" data-bs-theme="light">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Registro Estudiante | ProPath</title>

        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

        <link href="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
    </head>
    <body>
        <div class="login-wrapper py-5">
            <div class="login-card" style="max-width: 520px;">
                
                <div class="login-header text-center mb-4">
                    <a href="${pageContext.request.contextPath}/index.jsp">
                        <img src="${pageContext.request.contextPath}/img/logo.png" alt="ProPath" class="logo-login">
                    </a>
                    <div>
                        <span class="login-badge-role">
                            <i class="bi bi-mortarboard-fill"></i> Registro de Estudiante
                        </span>
                    </div>
                </div>

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

                <form action="${pageContext.request.contextPath}/registro" method="POST" autocomplete="off">
                    <input type="hidden" name="tipo" value="estudiante">

                    <div class="row g-2 mb-3">
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">Nombres *</label>
                            <input type="text" class="form-control" name="nombre" placeholder="Ej. Carlos" 
                                   value="<%= request.getAttribute("nombre") != null ? request.getAttribute("nombre") : "" %>" required>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">Apellidos *</label>
                            <input type="text" class="form-control" name="apellidos" placeholder="Ej. Mendoza Vega" 
                                   value="<%= request.getAttribute("apellidos") != null ? request.getAttribute("apellidos") : "" %>" required>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-semibold text-secondary">Correo Electrónico *</label>
                        <input type="email" class="form-control" name="correo" placeholder="carlos@universidad.edu.pe" 
                               value="<%= request.getAttribute("correo") != null ? request.getAttribute("correo") : "" %>" required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-semibold text-secondary">Contraseña *</label>
                        <input type="password" class="form-control" name="contrasena" placeholder="Mínimo 6 caracteres" minlength="6" required>
                    </div>

                    <div class="row g-2 mb-3">
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">Universidad / Instituto *</label>
                            <input type="text" class="form-control" name="universidad" placeholder="Ej. UNMSM, UNI, PUCP" 
                                   value="<%= request.getAttribute("universidad") != null ? request.getAttribute("universidad") : "" %>" required>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">Carrera *</label>
                            <input type="text" class="form-control" name="carrera" placeholder="Ej. Ingeniería de Sistemas" 
                                   value="<%= request.getAttribute("carrera") != null ? request.getAttribute("carrera") : "" %>" required>
                        </div>
                    </div>

                    <div class="row g-2 mb-3">
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">Ciclo Actual *</label>
                            <input type="number" class="form-control" name="ciclo" min="1" max="14" placeholder="7" 
                                   value="<%= request.getAttribute("ciclo") != null ? request.getAttribute("ciclo") : "7" %>" required>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">Teléfono / Celular</label>
                            <input type="tel" class="form-control" name="telefono" placeholder="987654321" 
                                   value="<%= request.getAttribute("telefono") != null ? request.getAttribute("telefono") : "" %>">
                        </div>
                    </div>

                    <div class="d-grid mt-4">
                        <button type="submit" class="btn btn-propath-primary">
                            <span>Crear Cuenta de Estudiante</span>
                            <i class="bi bi-arrow-right-short fs-4"></i>
                        </button>
                    </div>
                </form>

                <div class="mt-4 pt-3 border-top text-center">
                    <p class="small text-muted mb-2">¿Ya tienes una cuenta registrada?</p>
                    <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-sm btn-outline-secondary">
                        <i class="bi bi-box-arrow-in-right me-1"></i> Iniciar Sesión
                    </a>
                </div>

            </div>
        </div>

        <script type="module" src="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/js/bootstrap.bundle.min.js"></script>
        <script type="module" src="${pageContext.request.contextPath}/js/script.js"></script>
    </body>
</html>
