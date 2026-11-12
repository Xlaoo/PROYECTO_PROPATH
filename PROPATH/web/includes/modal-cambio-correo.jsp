<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!-- =========================================================================
     MODAL UNIFORME DE VERIFICACIÓN DE CORREO ELECTRÓNICO (3 ROLES)
     Vigencia estricta: 60 segundos (1 minuto)
     ========================================================================= -->
<div class="modal fade modal-seguridad" id="modalVerificacionCambioCorreo" tabindex="-1"
     aria-labelledby="modalVerifCambioCorreoLabel" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg rounded-4 overflow-hidden">
            <div class="modal-header bg-dark text-white py-3 px-4">
                <h5 class="modal-title h6 fw-bold mb-0 text-white" id="modalVerifCambioCorreoLabel">
                    <i class="bi bi-shield-check me-2 text-info"></i> Verificación de correo electrónico
                </h5>
                <button type="button" class="btn-close btn-close-white" id="btnCerrarModalCambioCorreoX" aria-label="Cancelar"></button>
            </div>
            <div class="modal-body p-4 text-center">
                <div class="mb-3">
                    <div class="avatar-icon-wrapper mx-auto mb-2" style="width: 52px; height: 52px; background: #E0F2FE; border-radius: 50%; display: flex; align-items: center; justify-content: center;">
                        <i class="bi bi-envelope-check text-primary fs-3"></i>
                    </div>
                    <h6 class="fw-bold mb-1">Confirma tu nuevo correo</h6>
                    <p class="text-muted small mb-1">Hemos enviado un código de seguridad a tu nuevo correo electrónico:</p>
                    <p class="fw-bold text-primary small mb-0 px-2 py-1 bg-light rounded-pill d-inline-block" id="textoNuevoCorreoModal">correo@ejemplo.com</p>
                </div>

                <div id="contenedorAlertasCambioCorreo"></div>

                <div class="mb-3 text-start">
                    <label for="inputCodigoCambioCorreo" class="form-label small fw-semibold text-secondary">Código de 6 dígitos:</label>
                    <input type="text" class="form-control text-center fs-4 fw-bold letter-spacing-2"
                           id="inputCodigoCambioCorreo" maxlength="6" autocomplete="one-time-code"
                           placeholder="123456" autofocus style="letter-spacing: 6px;">
                    <div class="d-flex justify-content-between align-items-center mt-2 px-1">
                        <span class="small text-muted" id="textoExpiracionCambioCorreo">
                            <i class="bi bi-stopwatch me-1 text-primary"></i> Expira en <strong id="relojExpiracionCambioCorreo" class="text-danger">01:00</strong>
                        </span>
                        <span class="small text-muted" id="textoIntentosCambioCorreo">5 intentos</span>
                    </div>
                </div>
            </div>
            <div class="modal-footer bg-light border-top d-flex justify-content-between px-4 py-3">
                <button type="button" class="btn btn-outline-secondary btn-sm rounded-pill px-3" id="btnCancelarCambioCorreo">
                    Cancelar
                </button>
                <div class="d-flex gap-2">
                    <button type="button" class="btn btn-outline-primary btn-sm rounded-pill px-3" id="btnReenviarCambioCorreo">
                        <i class="bi bi-arrow-repeat me-1"></i> Reenviar código
                    </button>
                    <button type="button" class="btn btn-primary btn-sm rounded-pill px-4" id="btnConfirmarCambioCorreo">
                        <span>Verificar código</span>
                        <i class="bi bi-arrow-right-short fs-5 ms-1"></i>
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

