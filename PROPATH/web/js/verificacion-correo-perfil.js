/**
 * ProPath - Módulo Centralizado de Verificación de Correo Electrónico al Editar Perfil
 * Aplica para los 3 roles: Administrador, Empresa y Estudiante.
 * Vigencia estricta del código: 60 segundos (1 minuto).
 */
(function () {
    'use strict';

    window.ProPathCambioCorreo = {
        timerExpiracion: null,
        timerCooldown: null,
        segundosRestantes: 60,
        correoOriginal: '',
        nuevoCorreoDestino: '',
        contextPath: '',
        callbacks: {},
        modalInstance: null,

        /**
         * Inicializa los listeners y prepara el modal de verificación de correo.
         */
        inicializar: function (contextPath) {
            this.contextPath = contextPath || '';
            const modalEl = document.getElementById('modalVerificacionCambioCorreo');
            if (!modalEl) return;

            if (typeof bootstrap !== 'undefined' && bootstrap.Modal) {
                this.modalInstance = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
            }

            const inputCodigo = document.getElementById('inputCodigoCambioCorreo');
            const btnConfirmar = document.getElementById('btnConfirmarCambioCorreo');
            const btnReenviar = document.getElementById('btnReenviarCambioCorreo');
            const btnCancelar = document.getElementById('btnCancelarCambioCorreo');
            const btnCerrarX = document.getElementById('btnCerrarModalCambioCorreoX');

            // Filtrado estricto a 6 dígitos numéricos sin borrar '0' ni '1'
            if (inputCodigo) {
                inputCodigo.addEventListener('input', function () {
                    const limpio = this.value.replace(/[^0-9]/g, '').slice(0, 6);
                    this.value = limpio;
                    if (limpio.length === 6 && btnConfirmar && !btnConfirmar.disabled) {
                        btnConfirmar.focus();
                    }
                });

                inputCodigo.addEventListener('keydown', function (e) {
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        if (btnConfirmar && !btnConfirmar.disabled) {
                            btnConfirmar.click();
                        }
                    }
                });
            }

            if (btnConfirmar) {
                btnConfirmar.addEventListener('click', () => this.confirmarCodigo());
            }

            if (btnReenviar) {
                btnReenviar.addEventListener('click', () => this.reenviarCodigo());
            }

            if (btnCancelar) {
                btnCancelar.addEventListener('click', () => this.cancelar());
            }

            if (btnCerrarX) {
                btnCerrarX.addEventListener('click', () => this.cancelar());
            }
        },

        /**
         * Vincula un campo de correo, botón VALIDAR CORREO, contenedor de estado y formulario.
         * Aplica idéntico comportamiento en Admin, Empresa y Estudiante.
         */
        vincularCampoCorreo: function (opciones) {
            const inputCorreo = document.getElementById(opciones.inputId);
            const btnValidar = document.getElementById(opciones.btnValidarId);
            const estadoEl = document.getElementById(opciones.estadoId);
            const formEl = document.getElementById(opciones.formId);
            const contextPath = opciones.contextPath || this.contextPath || '';

            if (!inputCorreo) return;

            const correoBase = (inputCorreo.getAttribute('data-correo-actual') || inputCorreo.value || '').trim();
            inputCorreo.setAttribute('data-correo-actual', correoBase);
            inputCorreo.setAttribute('data-verificado', 'true');

            // 1. Al modificar el correo electrónico:
            inputCorreo.addEventListener('input', () => {
                const actual = (inputCorreo.getAttribute('data-correo-actual') || '').trim().toLowerCase();
                const nuevo = inputCorreo.value.trim().toLowerCase();

                if (nuevo !== '' && nuevo !== actual) {
                    inputCorreo.setAttribute('data-verificado', 'false');
                    if (btnValidar) {
                        btnValidar.classList.remove('d-none');
                        btnValidar.disabled = false;
                        btnValidar.className = 'btn btn-primary fw-semibold';
                        btnValidar.innerHTML = '<i class="bi bi-patch-check me-1"></i> VALIDAR CORREO';
                    }
                    if (estadoEl) {
                        estadoEl.classList.remove('d-none');
                        estadoEl.className = 'mt-1 small text-warning fw-semibold';
                        estadoEl.innerHTML = '<i class="bi bi-exclamation-triangle-fill me-1"></i> Correo modificado: pulsa VALIDAR CORREO';
                    }
                } else {
                    inputCorreo.setAttribute('data-verificado', 'true');
                    if (btnValidar) btnValidar.classList.add('d-none');
                    if (estadoEl) estadoEl.classList.add('d-none');
                }
            });

            // 2. Al pulsar VALIDAR CORREO:
            if (btnValidar) {
                btnValidar.addEventListener('click', (e) => {
                    e.preventDefault();
                    const actual = (inputCorreo.getAttribute('data-correo-actual') || '').trim().toLowerCase();
                    const nuevo = inputCorreo.value.trim().toLowerCase();

                    if (!nuevo || nuevo === actual) {
                        alert('El correo no ha sido modificado.');
                        return;
                    }

                    if (!nuevo.match(/^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/)) {
                        alert('Por favor ingresa un correo electrónico válido con formato nombre@dominio.com');
                        inputCorreo.focus();
                        return;
                    }

                    btnValidar.disabled = true;
                    btnValidar.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Enviando...';

                    this.contextPath = contextPath;
                    this.correoOriginal = actual;
                    this.nuevoCorreoDestino = nuevo;
                    this.callbacks = {
                        onExito: (nuevoCorreoVerificado) => {
                            inputCorreo.value = nuevoCorreoVerificado;
                            inputCorreo.setAttribute('data-correo-actual', nuevoCorreoVerificado);
                            inputCorreo.setAttribute('data-verificado', 'true');

                            if (btnValidar) {
                                btnValidar.classList.remove('d-none');
                                btnValidar.className = 'btn btn-outline-success fw-semibold';
                                btnValidar.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i> VERIFICADO';
                                btnValidar.disabled = true;
                            }
                            if (estadoEl) {
                                estadoEl.classList.remove('d-none');
                                estadoEl.className = 'mt-1 small text-success fw-semibold';
                                estadoEl.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i> Correo verificado correctamente';
                            }
                        },
                        onCancelar: (correoAnterior) => {
                            inputCorreo.value = correoAnterior;
                            inputCorreo.setAttribute('data-verificado', 'true');
                            if (btnValidar) {
                                btnValidar.classList.add('d-none');
                                btnValidar.disabled = false;
                                btnValidar.className = 'btn btn-primary fw-semibold';
                                btnValidar.innerHTML = '<i class="bi bi-patch-check me-1"></i> VALIDAR CORREO';
                            }
                            if (estadoEl) estadoEl.classList.add('d-none');
                        },
                        onError: (msg) => {
                            if (btnValidar) {
                                btnValidar.disabled = false;
                                btnValidar.className = 'btn btn-primary fw-semibold';
                                btnValidar.innerHTML = '<i class="bi bi-patch-check me-1"></i> VALIDAR CORREO';
                            }
                            alert(msg);
                        }
                    };

                    this.solicitarCodigoAlServidor(nuevo, btnValidar);
                });
            }

            // 3. Interceptar Guardar cambios:
            if (formEl) {
                formEl.addEventListener('submit', (e) => {
                    const actual = (inputCorreo.getAttribute('data-correo-actual') || '').trim().toLowerCase();
                    const nuevo = inputCorreo.value.trim().toLowerCase();
                    const verificado = inputCorreo.getAttribute('data-verificado');

                    if (nuevo !== actual && verificado !== 'true') {
                        e.preventDefault();
                        alert('Debes validar tu nuevo correo electrónico haciendo clic en "VALIDAR CORREO" antes de guardar los cambios.');
                        if (btnValidar) {
                            btnValidar.classList.remove('d-none');
                            btnValidar.focus();
                        }
                        return false;
                    }

                    if (typeof opciones.onGuardar === 'function') {
                        e.preventDefault();
                        opciones.onGuardar(e);
                        return false;
                    }
                });
            }
        },

        /**
         * Intercepta la edición del formulario de perfil.
         * Retorna true si el correo cambió y se inicia la verificación por código.
         * Retorna false si el correo no cambió y el formulario puede guardarse directamente.
         */
        verificarSiCambioCorreo: function (opciones) {
            const inputCorreo = opciones.inputCorreo;
            const correoActual = (opciones.correoActual || '').trim().toLowerCase();
            const nuevoCorreo = (inputCorreo ? inputCorreo.value : '').trim().toLowerCase();
            this.contextPath = opciones.contextPath || this.contextPath || '';
            this.callbacks = {
                onExito: opciones.onExito,
                onCancelar: opciones.onCancelar,
                onError: opciones.onError
            };
            this.correoOriginal = correoActual;
            this.nuevoCorreoDestino = nuevoCorreo;

            // Si el correo no fue modificado, no requiere verificación
            if (!nuevoCorreo || nuevoCorreo === correoActual) {
                return false;
            }

            // Validar formato de correo antes de contactar al servidor
            if (!nuevoCorreo.match(/^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/)) {
                if (typeof this.callbacks.onError === 'function') {
                    this.callbacks.onError('Por favor ingresa un correo electrónico válido con formato nombre@dominio.com');
                } else {
                    alert('Por favor ingresa un correo electrónico válido.');
                }
                if (inputCorreo) inputCorreo.focus();
                return true;
            }

            // Despachar solicitud de código al servidor
            this.solicitarCodigoAlServidor(nuevoCorreo, opciones.btnGuardar);
            return true;
        },

        solicitarCodigoAlServidor: function (nuevoCorreo, btnGuardar) {
            let textoOriginalBtn = '';
            if (btnGuardar) {
                btnGuardar.disabled = true;
                textoOriginalBtn = btnGuardar.innerHTML;
                btnGuardar.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Enviando código...';
            }

            const params = new URLSearchParams();
            params.append('accion', 'solicitar_cambio_correo');
            params.append('nuevoCorreo', nuevoCorreo);

            fetch(this.contextPath + '/verificar-codigo', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                body: params.toString()
            })
            .then(res => res.json())
            .then(data => {
                if (btnGuardar) {
                    btnGuardar.disabled = false;
                    btnGuardar.innerHTML = textoOriginalBtn;
                }

                if (!data.exito) {
                    // SMTP falló o correo duplicado: no abrir modal
                    if (typeof this.callbacks.onError === 'function') {
                        this.callbacks.onError(data.mensaje || 'No fue posible enviar el código de verificación.');
                    } else {
                        alert(data.mensaje || 'Error al enviar código de verificación.');
                    }
                    return;
                }

                // Éxito: Abrir modal e iniciar cuenta regresiva estricta de 60 segundos
                this.abrirModal(data.correoMascara || nuevoCorreo, data.segundosExpiracion || 60, data.segundosEspera || 30);
            })
            .catch(err => {
                if (btnGuardar) {
                    btnGuardar.disabled = false;
                    btnGuardar.innerHTML = textoOriginalBtn;
                }
                const msg = 'Error de conexión con el servidor al solicitar verificación de correo.';
                if (typeof this.callbacks.onError === 'function') {
                    this.callbacks.onError(msg);
                } else {
                    alert(msg);
                }
            });
        },

        abrirModal: function (correoMascara, segExpiracion, segEspera) {
            const modalEl = document.getElementById('modalVerificacionCambioCorreo');
            if (!this.modalInstance && modalEl) {
                this.modalInstance = new bootstrap.Modal(modalEl);
            }

            const textoCorreo = document.getElementById('textoNuevoCorreoModal');
            if (textoCorreo) textoCorreo.innerText = correoMascara;

            const inputCodigo = document.getElementById('inputCodigoCambioCorreo');
            if (inputCodigo) {
                inputCodigo.value = '';
                inputCodigo.disabled = false;
            }

            const btnConfirmar = document.getElementById('btnConfirmarCambioCorreo');
            if (btnConfirmar) {
                btnConfirmar.disabled = false;
                btnConfirmar.innerHTML = '<span>Verificar código</span> <i class="bi bi-arrow-right-short fs-5 ms-1"></i>';
            }

            const alertasArea = document.getElementById('contenedorAlertasCambioCorreo');
            if (alertasArea) alertasArea.innerHTML = '';

            const textoIntentos = document.getElementById('textoIntentosCambioCorreo');
            if (textoIntentos) textoIntentos.innerText = '5 intentos';

            if (this.modalInstance) {
                this.modalInstance.show();
                setTimeout(() => {
                    if (inputCodigo) inputCodigo.focus();
                }, 500);
            }

            // Iniciar cuenta regresiva visible de 60 segundos (01:00)
            this.iniciarCuentaRegresiva(segExpiracion || 60);

            // Iniciar cooldown de reenvío
            const btnReenviar = document.getElementById('btnReenviarCambioCorreo');
            this.iniciarCooldownReenvio(btnReenviar, segEspera || 30);
        },

        iniciarCuentaRegresiva: function (segundosTotales) {
            clearInterval(this.timerExpiracion);
            this.segundosRestantes = parseInt(segundosTotales, 10) || 60;

            const relojEl = document.getElementById('relojExpiracionCambioCorreo');
            const inputCodigo = document.getElementById('inputCodigoCambioCorreo');
            const btnConfirmar = document.getElementById('btnConfirmarCambioCorreo');
            const alertasArea = document.getElementById('contenedorAlertasCambioCorreo');

            const actualizar = () => {
                if (this.segundosRestantes <= 0) {
                    clearInterval(this.timerExpiracion);
                    if (relojEl) relojEl.innerText = '00:00';
                    if (btnConfirmar) btnConfirmar.disabled = true;
                    if (inputCodigo) inputCodigo.disabled = true;

                    if (alertasArea) {
                        alertasArea.innerHTML = '<div class="alert alert-danger py-2 px-3 small rounded-3 mb-3 text-start">'
                            + '<i class="bi bi-exclamation-triangle-fill me-1"></i> '
                            + '<strong>El código ha expirado</strong> (validez estricta de 1 minuto). Haz clic en "Reenviar código" para recibir uno nuevo.'
                            + '</div>';
                    }
                } else {
                    const min = Math.floor(this.segundosRestantes / 60);
                    const seg = this.segundosRestantes % 60;
                    const formateado = (min < 10 ? '0' : '') + min + ':' + (seg < 10 ? '0' : '') + seg;
                    if (relojEl) {
                        relojEl.innerText = formateado;
                        if (this.segundosRestantes <= 15) {
                            relojEl.classList.add('text-danger', 'fw-bold');
                        } else {
                            relojEl.classList.remove('text-danger');
                        }
                    }
                    this.segundosRestantes--;
                }
            };

            actualizar();
            this.timerExpiracion = setInterval(actualizar, 1000);
        },

        iniciarCooldownReenvio: function (boton, segundos) {
            if (!boton) return;
            clearInterval(this.timerCooldown);
            let restante = parseInt(segundos, 10) || 30;
            boton.disabled = true;

            const actualizar = () => {
                if (restante <= 0) {
                    clearInterval(this.timerCooldown);
                    boton.disabled = false;
                    boton.innerHTML = '<i class="bi bi-arrow-repeat me-1"></i> Reenviar código';
                } else {
                    boton.innerHTML = '<i class="bi bi-clock me-1"></i> Reenviar en ' + restante + 's';
                    restante--;
                }
            };

            actualizar();
            this.timerCooldown = setInterval(actualizar, 1000);
        },

        reenviarCodigo: function () {
            const btnReenviar = document.getElementById('btnReenviarCambioCorreo');
            if (btnReenviar) btnReenviar.disabled = true;

            const params = new URLSearchParams();
            params.append('accion', 'solicitar_cambio_correo');
            params.append('nuevoCorreo', this.nuevoCorreoDestino);

            fetch(this.contextPath + '/verificar-codigo', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                body: params.toString()
            })
            .then(res => res.json())
            .then(data => {
                if (data.exito) {
                    const inputCodigo = document.getElementById('inputCodigoCambioCorreo');
                    if (inputCodigo) {
                        inputCodigo.disabled = false;
                        inputCodigo.value = '';
                        inputCodigo.focus();
                    }
                    const btnConfirmar = document.getElementById('btnConfirmarCambioCorreo');
                    if (btnConfirmar) btnConfirmar.disabled = false;

                    const alertasArea = document.getElementById('contenedorAlertasCambioCorreo');
                    if (alertasArea) {
                        alertasArea.innerHTML = '<div class="alert alert-info py-2 px-3 small rounded-3 mb-3 text-start">'
                            + '<i class="bi bi-info-circle-fill me-1"></i> Hemos enviado un nuevo código de 6 dígitos con vigencia de 1 minuto.'
                            + '</div>';
                    }

                    // Reiniciar cuenta regresiva a 01:00
                    this.iniciarCuentaRegresiva(data.segundosExpiracion || 60);
                    this.iniciarCooldownReenvio(btnReenviar, data.segundosEspera || 30);
                } else {
                    const alertasArea = document.getElementById('contenedorAlertasCambioCorreo');
                    if (alertasArea) {
                        alertasArea.innerHTML = '<div class="alert alert-danger py-2 px-3 small rounded-3 mb-3 text-start">'
                            + '<i class="bi bi-exclamation-circle-fill me-1"></i> ' + (data.mensaje || 'No se pudo reenviar el código.')
                            + '</div>';
                    }
                    if (btnReenviar) btnReenviar.disabled = false;
                }
            })
            .catch(() => {
                if (btnReenviar) btnReenviar.disabled = false;
            });
        },

        confirmarCodigo: function () {
            const inputCodigo = document.getElementById('inputCodigoCambioCorreo');
            const codigo = inputCodigo ? inputCodigo.value.trim() : '';

            if (codigo.length !== 6) {
                const alertasArea = document.getElementById('contenedorAlertasCambioCorreo');
                if (alertasArea) {
                    alertasArea.innerHTML = '<div class="alert alert-warning py-2 px-3 small rounded-3 mb-3 text-start">'
                        + '<i class="bi bi-exclamation-triangle-fill me-1"></i> Ingresa el código completo de 6 dígitos.'
                        + '</div>';
                }
                if (inputCodigo) inputCodigo.focus();
                return;
            }

            const btnConfirmar = document.getElementById('btnConfirmarCambioCorreo');
            if (btnConfirmar) {
                btnConfirmar.disabled = true;
                btnConfirmar.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Verificando...';
            }

            const params = new URLSearchParams();
            params.append('accion', 'verificar_cambio_correo');
            params.append('nuevoCorreo', this.nuevoCorreoDestino);
            params.append('codigo', codigo);

            fetch(this.contextPath + '/verificar-codigo', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                body: params.toString()
            })
            .then(res => res.json())
            .then(data => {
                if (btnConfirmar) {
                    btnConfirmar.disabled = false;
                    btnConfirmar.innerHTML = '<span>Verificar código</span> <i class="bi bi-arrow-right-short fs-5 ms-1"></i>';
                }

                const alertasArea = document.getElementById('contenedorAlertasCambioCorreo');

                if (data.exito) {
                    clearInterval(this.timerExpiracion);
                    clearInterval(this.timerCooldown);

                    if (alertasArea) {
                        alertasArea.innerHTML = '<div class="alert alert-success py-2 px-3 small rounded-3 mb-3 text-start">'
                            + '<i class="bi bi-check-circle-fill me-1"></i> '
                            + '<strong>¡Verificación exitosa!</strong> Tu correo electrónico ha sido actualizado correctamente.'
                            + '</div>';
                    }

                    if (inputCodigo) inputCodigo.disabled = true;

                    // Cerrar modal y ejecutar callback tras confirmación visual
                    setTimeout(() => {
                        if (this.modalInstance) {
                            this.modalInstance.hide();
                        }
                        if (typeof this.callbacks.onExito === 'function') {
                            this.callbacks.onExito(data.nuevoCorreo || this.nuevoCorreoDestino);
                        }
                    }, 1000);

                } else {
                    if (alertasArea) {
                        alertasArea.innerHTML = '<div class="alert alert-danger py-2 px-3 small rounded-3 mb-3 text-start">'
                            + '<i class="bi bi-exclamation-circle-fill me-1"></i> '
                            + (data.mensaje || 'El código es incorrecto o ya ha expirado.')
                            + '</div>';
                    }
                    if (inputCodigo) {
                        inputCodigo.select();
                    }
                }
            })
            .catch(() => {
                if (btnConfirmar) {
                    btnConfirmar.disabled = false;
                    btnConfirmar.innerHTML = '<span>Verificar código</span> <i class="bi bi-arrow-right-short fs-5 ms-1"></i>';
                }
                const alertasArea = document.getElementById('contenedorAlertasCambioCorreo');
                if (alertasArea) {
                    alertasArea.innerHTML = '<div class="alert alert-danger py-2 px-3 small rounded-3 mb-3 text-start">'
                        + '<i class="bi bi-wifi-off me-1"></i> Error de red al verificar el código.'
                        + '</div>';
                }
            });
        },

        cancelar: function () {
            clearInterval(this.timerExpiracion);
            clearInterval(this.timerCooldown);

            if (this.modalInstance) {
                this.modalInstance.hide();
            }

            if (typeof this.callbacks.onCancelar === 'function') {
                this.callbacks.onCancelar(this.correoOriginal);
            }
        }
    };
})();

