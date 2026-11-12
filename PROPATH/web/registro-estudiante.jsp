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

                <form action="${pageContext.request.contextPath}/registro" method="POST" autocomplete="off" id="formRegistroEstudiante">
                    <input type="hidden" name="tipo" value="estudiante">

                    <div class="row g-2 mb-3">
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">DNI (8 dígitos) *</label>
                            <input type="text" class="form-control" id="dniInput" name="dni" placeholder="12345678"
                                   pattern="[0-9]{8}" maxlength="8"
                                   value="<%= request.getAttribute("dni") != null ? request.getAttribute("dni") : "" %>" required>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">Teléfono / Celular (9 dígitos)</label>
                            <input type="tel" class="form-control" name="telefono" placeholder="987654321"
                                   pattern="[0-9]{9}" maxlength="9"
                                   value="<%= request.getAttribute("telefono") != null ? request.getAttribute("telefono") : "" %>">
                        </div>
                    </div>

                    <div class="row g-2 mb-3">
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">Nombres *</label>
                            <input type="text" class="form-control" id="nombreInput" name="nombre" placeholder="Ej. Carlos" 
                                   value="<%= request.getAttribute("nombre") != null ? request.getAttribute("nombre") : "" %>" required>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">Apellidos *</label>
                            <input type="text" class="form-control" name="apellidos" placeholder="Ej. Mendoza Vega" 
                                   value="<%= request.getAttribute("apellidos") != null ? request.getAttribute("apellidos") : "" %>" required>
                        </div>
                    </div>

                    <!-- Campo de Correo Electrónico con Botón y Badge de Verificación -->
                    <div class="mb-3">
                        <div class="d-flex justify-content-between align-items-center mb-1">
                            <label for="correoRegistro" class="form-label small fw-semibold text-secondary mb-0">Correo Electrónico *</label>
                            <span id="badgeEstadoCorreo" class="badge-correo-pendiente">
                                <i class="bi bi-shield-exclamation me-1"></i> No verificado
                            </span>
                        </div>
                        <div class="input-group">
                            <input type="email" class="form-control" id="correoRegistro" name="correo" placeholder="carlos@universidad.edu.pe" 
                                   value="<%= request.getAttribute("correo") != null ? request.getAttribute("correo") : "" %>" required>
                            <button class="btn btn-outline-primary" type="button" id="btnVerificarCorreo">
                                <i class="bi bi-patch-check me-1"></i> Verificar correo
                            </button>
                        </div>
                        <div class="form-text small" id="ayudaCorreo">
                            Debes verificar tu correo institucional o personal para poder crear tu cuenta.
                        </div>
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
                            <label class="form-label small fw-semibold text-secondary">Contraseña *</label>
                            <input type="password" class="form-control" name="contrasena" placeholder="Mínimo 8 caracteres (letras y números)" minlength="8" required>
                        </div>
                        <div class="col-sm-6">
                            <label class="form-label small fw-semibold text-secondary">Ciclo Actual *</label>
                            <input type="number" class="form-control" name="ciclo" min="1" max="14" placeholder="7" 
                                   value="<%= request.getAttribute("ciclo") != null ? request.getAttribute("ciclo") : "7" %>" required>
                        </div>
                    </div>

                    <div class="d-grid mt-4">
                        <button type="submit" class="btn btn-propath-primary" id="btnCrearCuenta">
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

        <!-- =========================================================================
             MODAL DE VERIFICACIÓN DE CORREO (REGISTRO)
             ========================================================================= -->
        <div class="modal fade modal-seguridad" id="modalVerificacionCorreo" tabindex="-1" aria-labelledby="modalVerifLabel" aria-hidden="true" data-bs-backdrop="static">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title h6 fw-bold mb-0 text-white" id="modalVerifLabel">
                            <i class="bi bi-envelope-check-fill me-2 text-info"></i> Verificar Correo Electrónico
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body p-4 text-center">
                        <div class="mb-3">
                            <div class="avatar-icon-wrapper mx-auto mb-2" style="width: 50px; height: 50px; background: #E0F2FE; border-radius: 50%; display: flex; align-items: center; justify-content: center;">
                                <i class="bi bi-patch-check-fill text-primary fs-3"></i>
                            </div>
                            <h6 class="fw-bold mb-1">Verificación de Correo</h6>
                            <p class="text-muted small mb-0">Hemos enviado un código alfanumérico de <strong>6 caracteres</strong> a:</p>
                            <p class="fw-bold text-primary small mb-0" id="textoCorreoModal">correo@ejemplo.com</p>
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
                const nombreInput = document.getElementById('nombreInput');
                const btnVerificar = document.getElementById('btnVerificarCorreo');
                const badgeEstado = document.getElementById('badgeEstadoCorreo');
                const ayudaCorreo = document.getElementById('ayudaCorreo');
                const formRegistro = document.getElementById('formRegistroEstudiante');

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
                        ayudaCorreo.innerHTML = 'Has modificado el correo. Debes volver a verificarlo antes de crear tu cuenta.';
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
                    params.append('nombre', (nombreInput ? nombreInput.value.trim() : 'Estudiante'));

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

                // Validar código ingresado
                const ejecutarConfirmacion = function (codigo) {
                    if (procesando) return;
                    const cod = (codigo || inputCodigo.value || '').trim().toUpperCase();
                    if (cod.length !== 6) {
                        ProPathSeguridad.mostrarAlerta(alertasArea, 'danger', 'El código debe tener exactamente 6 caracteres alfanuméricos.');
                        inputCodigo.focus();
                        return;
                    }

                    procesando = true;
                    btnConfirmar.disabled = true;
                    btnConfirmar.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Verificando...';
                    ProPathSeguridad.limpiarAlerta(alertasArea);

                    const params = new URLSearchParams();
                    params.append('accion', 'verificar_registro');
                    params.append('correo', correoInput.value.trim());
                    params.append('codigo', cod);

                    fetch('${pageContext.request.contextPath}/verificar-codigo', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                        body: params.toString()
                    })
                    .then(r => r.json())
                    .then(data => {
                        procesando = false;
                        btnConfirmar.disabled = false;
                        btnConfirmar.innerHTML = 'Verificar código <i class="bi bi-check2 fs-6 ms-1"></i>';

                        if (data.exito) {
                            correoVerificadoExitoso = (data.correo || correoInput.value.trim()).toLowerCase();
                            badgeEstado.className = 'badge-correo-verificado';
                            badgeEstado.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i> Verificado';
                            ayudaCorreo.className = 'form-text small text-success';
                            ayudaCorreo.innerHTML = 'Correo confirmado exitosamente. Puedes completar tu registro.';

                            ProPathSeguridad.mostrarAlerta(alertasArea, 'success', '¡Correo verificado con éxito!');
                            setTimeout(() => {
                                bsModal.hide();
                            }, 800);
                        } else {
                            ProPathSeguridad.mostrarAlerta(alertasArea, 'danger', data.mensaje);
                            if (data.intentosRestantes !== undefined) {
                                textoIntentosModal.innerText = data.intentosRestantes + ' intentos restantes';
                            }
                            inputCodigo.value = '';
                            inputCodigo.focus();
                        }
                    })
                    .catch(() => {
                        procesando = false;
                        btnConfirmar.disabled = false;
                        btnConfirmar.innerHTML = 'Verificar código <i class="bi bi-check2 fs-6 ms-1"></i>';
                        ProPathSeguridad.mostrarAlerta(alertasArea, 'danger', 'Error de conexión al validar el código.');
                    });
                };

                ProPathSeguridad.configurarCampoCodigo(inputCodigo, function (cod) {
                    if (cod.length === 6) {
                        ejecutarConfirmacion(cod);
                    }
                });

                btnConfirmar.addEventListener('click', function () {
                    ejecutarConfirmacion();
                });

                // Reenviar código
                btnReenviar.addEventListener('click', function () {
                    if (btnReenviar.disabled) return;
                    btnReenviar.disabled = true;
                    btnReenviar.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Enviando...';

                    const params = new URLSearchParams();
                    params.append('accion', 'reenviar');
                    params.append('proposito', 'REGISTRO');
                    params.append('correo', correoInput.value.trim());

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
                            if (data.segundosEspera) {
                                ProPathSeguridad.iniciarCooldownReenvio(btnReenviar, data.segundosEspera);
                            } else {
                                btnReenviar.disabled = false;
                                btnReenviar.innerHTML = '<i class="bi bi-arrow-repeat me-1"></i> Reenviar código';
                            }
                        }
                    })
                    .catch(() => {
                        btnReenviar.disabled = false;
                        btnReenviar.innerHTML = '<i class="bi bi-arrow-repeat me-1"></i> Reenviar código';
                        ProPathSeguridad.mostrarAlerta(alertasArea, 'danger', 'Error al reenviar el código.');
                    });
                });

                // Validación previa al envío del formulario completo
                formRegistro.addEventListener('submit', function (e) {
                    const correoActual = correoInput.value.trim().toLowerCase();
                    if (!correoVerificadoExitoso || correoVerificadoExitoso !== correoActual) {
                        e.preventDefault();
                        alert('Debes verificar tu correo electrónico antes de crear tu cuenta. Haz clic en el botón "Verificar correo".');
                        correoInput.focus();
                        return false;
                    }
                });
            });
        </script>
    </body>
</html>
