<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es" data-bs-theme="light">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Registro Empresa | ProPath</title>

        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
    </head>
    <body>
        <div class="login-wrapper py-5">
            <div class="login-card" style="max-width: 540px;">
                
                <div class="login-header text-center mb-4">
                    <a href="${pageContext.request.contextPath}/index.jsp">
                        <img src="${pageContext.request.contextPath}/img/logo-propath.png" alt="ProPath" class="logo-login">
                    </a>
                    <div>
                        <span class="login-badge-role">
                            <i class="bi bi-building-fill"></i> Registro de Empresa
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

                <form action="${pageContext.request.contextPath}/registro" method="POST" autocomplete="off" id="formRegistroEmpresa">
                    <input type="hidden" name="tipo" value="empresa">

                    <div class="row g-2 mb-3">
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">Razón Social *</label>
                            <input type="text" class="form-control" id="razonSocialInput" name="razonSocial" placeholder="Ej. Tech Solutions SAC" 
                                   value="<%= request.getAttribute("razonSocial") != null ? request.getAttribute("razonSocial") : "" %>" required>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">RUC (11 dígitos) *</label>
                            <input type="text" class="form-control" name="ruc" pattern="[0-9]{11}" maxlength="11" placeholder="20123456789" 
                                   value="<%= request.getAttribute("ruc") != null ? request.getAttribute("ruc") : "" %>" required>
                        </div>
                    </div>

                    <div class="row g-2 mb-3">
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">Nombres del Representante *</label>
                            <input type="text" class="form-control" name="nombre"
                                   placeholder="Ej. Rosalinda"
                                   value="<%= request.getAttribute("nombre") != null ? request.getAttribute("nombre") : "" %>"
                                   required>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">Apellidos del Representante *</label>
                            <input type="text" class="form-control" name="apellido"
                                   placeholder="Ej. Papuico"
                                   value="<%= request.getAttribute("apellido") != null ? request.getAttribute("apellido") : "" %>"
                                   required>
                        </div>
                    </div>

                    <!-- Campo de Correo Electrónico Corporativo con Botón y Badge de Verificación -->
                    <div class="mb-3">
                        <div class="d-flex justify-content-between align-items-center mb-1">
                            <label for="correoRegistro" class="form-label small fw-semibold text-secondary mb-0">Correo Corporativo *</label>
                            <span id="badgeEstadoCorreo" class="badge-correo-pendiente">
                                <i class="bi bi-shield-exclamation me-1"></i> No verificado
                            </span>
                        </div>
                        <div class="input-group">
                            <input type="email" class="form-control" id="correoRegistro" name="correo" placeholder="contacto@empresa.com" 
                                   value="<%= request.getAttribute("correo") != null ? request.getAttribute("correo") : "" %>" required>
                            <button class="btn btn-outline-primary" type="button" id="btnVerificarCorreo">
                                <i class="bi bi-patch-check me-1"></i> Verificar correo
                            </button>
                        </div>
                        <div class="form-text small" id="ayudaCorreo">
                            Debes verificar tu correo corporativo antes de crear la cuenta de la empresa.
                        </div>
                    </div>

                    <div class="row g-2 mb-3">
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">Contraseña *</label>
                            <input type="password" class="form-control" name="contrasena" placeholder="Mínimo 8 caracteres (letras y números)" minlength="8" required>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">Teléfono (7 a 9 dígitos)</label>
                            <input type="tel" class="form-control" name="telefono" placeholder="987654321" pattern="[0-9]{7,9}" maxlength="9"
                                   value="<%= request.getAttribute("telefono") != null ? request.getAttribute("telefono") : "" %>">
                        </div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-semibold text-secondary">Dirección / Sede</label>
                        <input type="text" class="form-control" name="direccion" placeholder="Av. Javier Prado 1234, Lima" 
                               value="<%= request.getAttribute("direccion") != null ? request.getAttribute("direccion") : "" %>">
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-semibold text-secondary">Descripción de la Empresa</label>
                        <textarea class="form-control" name="descripcion" rows="2" placeholder="Breve reseña del rubro o sector..."><%= request.getAttribute("descripcion") != null ? request.getAttribute("descripcion") : "" %></textarea>
                    </div>

                    <div class="d-grid mt-4">
                        <button type="submit" class="btn btn-propath-primary" id="btnCrearCuenta">
                            <span>Crear Cuenta de Empresa</span>
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

        <!-- =========================================================================
             MODAL DE VERIFICACIÓN DE CORREO (REGISTRO EMPRESA)
             ========================================================================= -->
        <div class="modal fade modal-seguridad" id="modalVerificacionCorreo" tabindex="-1" aria-labelledby="modalVerifLabel" aria-hidden="true" data-bs-backdrop="static">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title h6 fw-bold mb-0 text-white" id="modalVerifLabel">
                            <i class="bi bi-envelope-check-fill me-2 text-info"></i> Verificar Correo Corporativo
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body p-4 text-center">
                        <div class="mb-3">
                            <div class="avatar-icon-wrapper mx-auto mb-2" style="width: 50px; height: 50px; background: #E0F2FE; border-radius: 50%; display: flex; align-items: center; justify-content: center;">
                                <i class="bi bi-building-check text-primary fs-3"></i>
                            </div>
                            <h6 class="fw-bold mb-1">Verificación de Correo</h6>
                            <p class="text-muted small mb-0">Hemos enviado un código alfanumérico de <strong>6 caracteres</strong> a:</p>
                            <p class="fw-bold text-primary small mb-0" id="textoCorreoModal">empresa@ejemplo.com</p>
                        </div>

                        <div id="contenedorAlertasModal"></div>

                        <div class="mb-3">
                            <label for="inputCodigoRegistro" class="form-label small fw-semibold text-secondary">Código de 6 caracteres:</label>
                            <input type="text" class="form-control input-codigo-seguridad" id="inputCodigoRegistro"
                                   maxlength="6" autocomplete="one-time-code" placeholder="ABC123" autofocus>
                            <div class="d-flex justify-content-between align-items-center mt-2 px-1">
                                <span class="small text-muted" id="textoExpiracionModal">
                                    <i class="bi bi-stopwatch me-1 text-primary"></i> Expira en <strong>10:00</strong>
                                </span>
                                <span class="small text-muted" id="textoIntentosModal">5 intentos</span>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer bg-light border-top d-flex justify-content-between p-3">
                        <button type="button" class="btn btn-outline-secondary btn-sm rounded-pill px-3" data-bs-dismiss="modal">
                            Cancelar
                        </button>
                        <div class="d-flex gap-2">
                            <button type="button" class="btn btn-outline-primary btn-sm rounded-pill px-3" id="btnReenviarModal">
                                <i class="bi bi-arrow-repeat me-1"></i> Reenviar código
                            </button>
                            <button type="button" class="btn btn-primary btn-sm rounded-pill px-4" id="btnConfirmarModal">
                                <span>Verificar código</span>
                                <i class="bi bi-check2 fs-6 ms-1"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <script src="${pageContext.request.contextPath}/js/verificacion-seguridad.js"></script>
        <script>
            document.addEventListener('DOMContentLoaded', function () {
                const correoInput = document.getElementById('correoRegistro');
                const razonSocialInput = document.getElementById('razonSocialInput');
                const btnVerificar = document.getElementById('btnVerificarCorreo');
                const badgeEstado = document.getElementById('badgeEstadoCorreo');
                const ayudaCorreo = document.getElementById('ayudaCorreo');
                const formRegistro = document.getElementById('formRegistroEmpresa');

                const modalElement = document.getElementById('modalVerificacionCorreo');
                const bsModal = new bootstrap.Modal(modalElement);
                const inputCodigo = document.getElementById('inputCodigoRegistro');
                const btnConfirmar = document.getElementById('btnConfirmarModal');
                const btnReenviar = document.getElementById('btnReenviarModal');
                const textoCorreoModal = document.getElementById('textoCorreoModal');
                const alertasArea = document.getElementById('contenedorAlertasModal');
                const textoExpiracion = document.getElementById('textoExpiracionModal');
                const textoIntentos = document.getElementById('textoIntentosModal');

                let correoVerificadoExitoso = null;
                let procesando = false;

                // Si el usuario cambia el correo, invalidar la verificación previa
                correoInput.addEventListener('input', function () {
                    if (correoVerificadoExitoso && this.value.trim().toLowerCase() !== correoVerificadoExitoso) {
                        correoVerificadoExitoso = null;
                        badgeEstado.className = 'badge-correo-pendiente';
                        badgeEstado.innerHTML = '<i class="bi bi-shield-exclamation me-1"></i> No verificado';
                        ayudaCorreo.className = 'form-text small text-warning';
                        ayudaCorreo.innerHTML = 'Has modificado el correo corporativo. Debes volver a verificarlo antes de registrar.';
                    }
                });

                // Solicitar código de verificación
                btnVerificar.addEventListener('click', function () {
                    const correo = correoInput.value.trim();
                    if (!correo || !correo.match(/^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/)) {
                        alert('Por favor ingresa un correo electrónico válido antes de verificar.');
                        correoInput.focus();
                        return;
                    }

                    btnVerificar.disabled = true;
                    btnVerificar.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Enviando...';

                    const params = new URLSearchParams();
                    params.append('accion', 'solicitar_registro');
                    params.append('correo', correo);
                    params.append('nombre', (razonSocialInput ? razonSocialInput.value.trim() : 'Empresa'));

                    fetch('${pageContext.request.contextPath}/verificar-codigo', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                        body: params.toString()
                    })
                    .then(r => r.json())
                    .then(data => {
                        btnVerificar.disabled = false;
                        btnVerificar.innerHTML = '<i class="bi bi-patch-check me-1"></i> Verificar correo';

                        if (data.exito) {
                            textoCorreoModal.innerText = data.correoMascara || correo;
                            ProPathSeguridad.limpiarAlerta(alertasArea);
                            inputCodigo.value = '';
                            inputCodigo.disabled = false;
                            btnConfirmar.disabled = false;
                            bsModal.show();
                            setTimeout(() => inputCodigo.focus(), 500);

                            ProPathSeguridad.iniciarCooldownReenvio(btnReenviar, data.segundosEspera || 30);
                            ProPathSeguridad.iniciarTemporizadorExpiracion(textoExpiracion, data.segundosExpiracion || 60, function () {
                                inputCodigo.disabled = true;
                                btnConfirmar.disabled = true;
                            });
                        } else {
                            alert(data.mensaje);
                        }
                    })
                    .catch(() => {
                        btnVerificar.disabled = false;
                        btnVerificar.innerHTML = '<i class="bi bi-patch-check me-1"></i> Verificar correo';
                        alert('Error de red al solicitar el código de verificación.');
                    });
                });

                // Enviar código para verificar
                function ejecutarVerificacion() {
                    const codigo = inputCodigo.value.trim().toUpperCase();
                    if (codigo.length !== 6) {
                        ProPathSeguridad.mostrarAlerta(alertasArea, 'warning', 'Ingresa el código completo de 6 caracteres.');
                        return;
                    }

                    if (procesando) return;
                    procesando = true;
                    btnConfirmar.disabled = true;
                    btnConfirmar.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Verificando...';

                    const correo = correoInput.value.trim();
                    const params = new URLSearchParams();
                    params.append('accion', 'verificar_registro');
                    params.append('correo', correo);
                    params.append('codigo', codigo);

                    fetch('${pageContext.request.contextPath}/verificar-codigo', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                        body: params.toString()
                    })
                    .then(r => r.json())
                    .then(data => {
                        procesando = false;
                        btnConfirmar.disabled = false;
                        btnConfirmar.innerHTML = '<span>Verificar código</span> <i class="bi bi-check2 fs-6 ms-1"></i>';

                        if (data.exito) {
                            correoVerificadoExitoso = correo.toLowerCase();
                            badgeEstado.className = 'badge-correo-verificado';
                            badgeEstado.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i> Correo verificado';
                            ayudaCorreo.className = 'form-text small text-success';
                            ayudaCorreo.innerHTML = '¡Correo verificado con éxito! Ya puedes completar el registro.';
                            bsModal.hide();
                        } else {
                            ProPathSeguridad.mostrarAlerta(alertasArea, 'danger', data.mensaje);
                            if (data.intentosRestantes !== undefined) {
                                textoIntentos.innerText = data.intentosRestantes + ' intentos restantes';
                            }
                            inputCodigo.select();
                        }
                    })
                    .catch(() => {
                        procesando = false;
                        btnConfirmar.disabled = false;
                        btnConfirmar.innerHTML = '<span>Verificar código</span> <i class="bi bi-check2 fs-6 ms-1"></i>';
                        ProPathSeguridad.mostrarAlerta(alertasArea, 'danger', 'Ocurrió un error de conexión al verificar el código.');
                    });
                }

                btnConfirmar.addEventListener('click', ejecutarVerificacion);

                // Reenviar código
                btnReenviar.addEventListener('click', function () {
                    const correo = correoInput.value.trim();
                    btnReenviar.disabled = true;
                    btnReenviar.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Reenviando...';

                    const params = new URLSearchParams();
                    params.append('accion', 'reenviar');
                    params.append('correo', correo);
                    params.append('proposito', 'REGISTRO');

                    fetch('${pageContext.request.contextPath}/verificar-codigo', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                        body: params.toString()
                    })
                    .then(r => r.json())
                    .then(data => {
                        if (data.exito) {
                            ProPathSeguridad.mostrarAlerta(alertasArea, 'info', data.mensaje);
                            inputCodigo.value = '';
                            inputCodigo.disabled = false;
                            btnConfirmar.disabled = false;
                            inputCodigo.focus();
                            ProPathSeguridad.iniciarCooldownReenvio(btnReenviar, data.segundosEspera || 30);
                            ProPathSeguridad.iniciarTemporizadorExpiracion(textoExpiracion, data.segundosExpiracion || 60, function () {
                                inputCodigo.disabled = true;
                                btnConfirmar.disabled = true;
                            });
                        } else {
                            ProPathSeguridad.mostrarAlerta(alertasArea, 'danger', data.mensaje);
                            btnReenviar.disabled = false;
                            btnReenviar.innerHTML = '<i class="bi bi-arrow-repeat me-1"></i> Reenviar código';
                        }
                    })
                    .catch(() => {
                        btnReenviar.disabled = false;
                        btnReenviar.innerHTML = '<i class="bi bi-arrow-repeat me-1"></i> Reenviar código';
                        ProPathSeguridad.mostrarAlerta(alertasArea, 'danger', 'Error de red al reenviar el código.');
                    });
                });

                ProPathSeguridad.configurarCampoCodigo(inputCodigo, ejecutarVerificacion);

                // Validar antes de enviar el formulario
                formRegistro.addEventListener('submit', function (e) {
                    const correo = correoInput.value.trim().toLowerCase();
                    if (!correoVerificadoExitoso || correo !== correoVerificadoExitoso) {
                        e.preventDefault();
                        alert('Debes verificar tu correo corporativo antes de crear tu cuenta de empresa.');
                        correoInput.focus();
                    }
                });
            });
        </script>
    </body>
</html>
