/**
 * ProPath - Módulo JavaScript de Verificación de Seguridad (6 caracteres alfanuméricos)
 * Gestiona modales de:
 * 1. Verificación de Correo en Registro (Estudiante y Empresa).
 * 2. Verificación de Doble Factor (2FA) en Inicio de Sesión.
 * 3. Recuperación de Contraseña por Correo.
 */

(function () {
    'use strict';

    window.ProPathSeguridad = {
        timers: {},

        /**
         * Inicializa el campo de 6 caracteres con auto-mayúsculas, filtrado y pegado.
         */
        configurarCampoCodigo: function (inputElement, alCompletar) {
            if (!inputElement) return;

            const sanitizar = (val) => {
                if (!val) return '';
                // Permitir mayúsculas y dígitos del alfabeto seguro
                return val.toUpperCase().replace(/[^23456789ABCDEFGHJKMNPQRSTUVWXYZ]/g, '').slice(0, 6);
            };

            inputElement.addEventListener('input', function () {
                const limpio = sanitizar(this.value);
                this.value = limpio;
                if (limpio.length === 6 && typeof alCompletar === 'function') {
                    alCompletar(limpio);
                }
            });

            inputElement.addEventListener('paste', function (e) {
                e.preventDefault();
                const textoPegado = (e.clipboardData || window.clipboardData).getData('text');
                const limpio = sanitizar(textoPegado);
                this.value = limpio;
                if (limpio.length === 6 && typeof alCompletar === 'function') {
                    alCompletar(limpio);
                }
            });

            inputElement.addEventListener('keydown', function (e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    if (this.value.trim().length === 6 && typeof alCompletar === 'function') {
                        alCompletar(this.value.trim());
                    }
                }
            });
        },

        /**
         * Inicia el temporizador de cooldown para reenvío (60 segundos por defecto).
         */
        iniciarCooldownReenvio: function (botonReenvio, segundos) {
            if (!botonReenvio) return;
            clearInterval(this.timers.cooldown);

            let restante = parseInt(segundos, 10);
            if (isNaN(restante) || restante <= 0) restante = 60;

            botonReenvio.disabled = true;
            const textoOriginal = botonReenvio.getAttribute('data-texto-original') || botonReenvio.innerText;
            botonReenvio.setAttribute('data-texto-original', textoOriginal);

            const actualizar = () => {
                if (restante <= 0) {
                    clearInterval(this.timers.cooldown);
                    botonReenvio.disabled = false;
                    botonReenvio.innerHTML = '<i class="bi bi-arrow-repeat me-1"></i> Reenviar código';
                } else {
                    botonReenvio.innerHTML = '<i class="bi bi-clock me-1"></i> Reenviar en ' + restante + 's';
                    restante--;
                }
            };

            actualizar();
            this.timers.cooldown = setInterval(actualizar, 1000);
        },

        /**
         * Inicia el temporizador de vencimiento de 10 minutos.
         */
        iniciarTemporizadorExpiracion: function (elementoTexto, segundos) {
            if (!elementoTexto) return;
            clearInterval(this.timers.expiracion);

            let restante = parseInt(segundos, 10);
            if (isNaN(restante) || restante <= 0) restante = 600; // 10 minutos

            const actualizar = () => {
                if (restante <= 0) {
                    clearInterval(this.timers.expiracion);
                    elementoTexto.innerHTML = '<span class="text-danger fw-bold"><i class="bi bi-exclamation-circle-fill me-1"></i> El código ha vencido. Solicita uno nuevo.</span>';
                } else {
                    const min = Math.floor(restante / 60);
                    const seg = restante % 60;
                    const formateado = (min < 10 ? '0' : '') + min + ':' + (seg < 10 ? '0' : '') + seg;
                    elementoTexto.innerHTML = '<i class="bi bi-stopwatch me-1 text-primary"></i> Expira en <strong>' + formateado + '</strong>';
                    restante--;
                }
            };

            actualizar();
            this.timers.expiracion = setInterval(actualizar, 1000);
        },

        limpiarTemporizadores: function () {
            clearInterval(this.timers.cooldown);
            clearInterval(this.timers.expiracion);
        },

        mostrarAlerta: function (contenedor, tipo, mensaje) {
            if (!contenedor) return;
            const icono = tipo === 'danger' ? 'bi-exclamation-triangle-fill' : (tipo === 'success' ? 'bi-check-circle-fill' : 'bi-info-circle-fill');
            contenedor.innerHTML = '<div class="alert alert-' + tipo + ' d-flex align-items-center py-2 px-3 mb-3 rounded-3 small fw-semibold" role="alert">'
                + '<i class="bi ' + icono + ' fs-5 me-2"></i>'
                + '<div>' + mensaje + '</div>'
                + '</div>';
        },

        limpiarAlerta: function (contenedor) {
            if (contenedor) contenedor.innerHTML = '';
        }
    };
})();

