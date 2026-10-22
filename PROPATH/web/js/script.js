import * as bootstrap from 'https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/js/bootstrap.bundle.min.js';


class BootstrapModalCompat {
    constructor(element, options) {
        this.element = typeof element === 'string' ? document.querySelector(element) : element;
        this.options = options || {};
        if (this.element) {
            BootstrapModalCompat._instances.set(this.element, this);
        }
    }

    show() {
        if (!this.element) return;
        document.body.classList.add('modal-open');
        let backdrop = document.querySelector('.modal-backdrop');
        if (!backdrop) {
            backdrop = document.createElement('div');
            backdrop.className = 'modal-backdrop fade';
            document.body.appendChild(backdrop);
            setTimeout(() => backdrop.classList.add('show'), 10);
        }
        this.element.style.display = 'block';
        this.element.removeAttribute('aria-hidden');
        this.element.setAttribute('aria-modal', 'true');
        setTimeout(() => {
            this.element.classList.add('show');
            this.element.dispatchEvent(new Event('shown.bs.modal'));
        }, 10);
    }

    hide() {
        if (!this.element) return;
        this.element.classList.remove('show');
        const backdrops = document.querySelectorAll('.modal-backdrop');
        backdrops.forEach(backdrop => {
            backdrop.classList.remove('show');
            setTimeout(() => {
                if (backdrop.parentNode) {
                    backdrop.parentNode.removeChild(backdrop);
                }
            }, 150);
        });
        setTimeout(() => {
            this.element.style.display = 'none';
            this.element.setAttribute('aria-hidden', 'true');
            this.element.removeAttribute('aria-modal');
            const openModals = document.querySelectorAll('.modal.show');
            if (openModals.length === 0) {
                document.body.classList.remove('modal-open');
                document.body.style.overflow = '';
            }
            this.element.dispatchEvent(new Event('hidden.bs.modal'));
        }, 150);
    }

    static getInstance(el) {
        const elem = typeof el === 'string' ? document.querySelector(el) : el;
        return BootstrapModalCompat._instances.get(elem) || null;
    }

    static getOrCreateInstance(el, options) {
        return BootstrapModalCompat.getInstance(el) || new BootstrapModalCompat(el, options);
    }
}
BootstrapModalCompat._instances = new WeakMap();
window.bootstrap = {
    ...bootstrap,
    Modal: BootstrapModalCompat
};
window.BootstrapModalCompat = BootstrapModalCompat;

document.addEventListener('click', function (e) {
    const dismissBtn = e.target.closest('[data-bs-dismiss="modal"], [data-bs-dismiss="dialog"]');
    if (dismissBtn) {
        const modal = dismissBtn.closest('.modal');
        if (modal) {
            const inst = BootstrapModalCompat.getOrCreateInstance(modal);
            inst.hide();
        }
    }

    const toggleBtn = e.target.closest('[data-bs-toggle="modal"], [data-bs-toggle="dialog"]');
    if (toggleBtn) {
        const targetSel = toggleBtn.getAttribute('data-bs-target') || toggleBtn.getAttribute('href');
        if (targetSel && targetSel.startsWith('#')) {
            const targetEl = document.querySelector(targetSel);
            if (targetEl) {
                const inst = BootstrapModalCompat.getOrCreateInstance(targetEl);
                inst.show();
            }
        }
    }

    const collapseBtn = e.target.closest('[data-bs-toggle="collapse"]');
    if (collapseBtn) {
        e.preventDefault();
        const targetSel = collapseBtn.getAttribute('data-bs-target') || collapseBtn.getAttribute('href');
        if (targetSel && targetSel.startsWith('#')) {
            const targetEl = document.querySelector(targetSel);
            if (targetEl) {
                targetEl.classList.toggle('show');
                const isExpanded = targetEl.classList.contains('show');
                collapseBtn.setAttribute('aria-expanded', isExpanded ? 'true' : 'false');
            }
        }
    }
});

document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
        const openModal = document.querySelector('.modal.show');
        if (openModal) {
            const inst = BootstrapModalCompat.getOrCreateInstance(openModal);
            inst.hide();
        }
    }
});

document.addEventListener('DOMContentLoaded', function () {
    const togglePasswordBtn = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('contrasena');
    if (togglePasswordBtn && passwordInput) {
        togglePasswordBtn.addEventListener('click', function () {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            const icon = this.querySelector('i');
            if (icon) {
                icon.classList.toggle('bi-eye');
                icon.classList.toggle('bi-eye-slash');
            }
        });
    }

    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebar = document.querySelector('.admin-sidebar');
    let backdrop = document.querySelector('.sidebar-backdrop');

    if (sidebarToggle && sidebar) {
        if (!backdrop) {
            backdrop = document.createElement('div');
            backdrop.className = 'sidebar-backdrop';
            document.body.appendChild(backdrop);
        }

        sidebarToggle.addEventListener('click', function () {
            sidebar.classList.toggle('show');
            backdrop.classList.toggle('show');
        });

        backdrop.addEventListener('click', function () {
            sidebar.classList.remove('show');
            backdrop.classList.remove('show');
        });
    }

    const searchInputs = document.querySelectorAll('.table-search-input');

    searchInputs.forEach(input => {
        input.addEventListener('input', function () {
            const table = document.getElementById(this.dataset.table);
            if (!table) return;

            const termino = this.value.trim().toLocaleLowerCase('es');

            table.querySelectorAll('tbody tr').forEach(fila => {
                const contenido = fila.textContent.toLocaleLowerCase('es');
                fila.style.display = contenido.includes(termino) ? '' : 'none';
            });
        });
    });


    if (typeof bootstrap !== 'undefined' && bootstrap.Tooltip) {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }
});

function mostrarToast(mensaje, tipo = 'success') {
    const iconClass = tipo === 'success' ? 'bi-check-circle-fill text-success' :
        tipo === 'danger' ? 'bi-x-circle-fill text-danger' :
            tipo === 'warning' ? 'bi-exclamation-triangle-fill text-warning' :
                'bi-info-circle-fill text-info';

    const existingToast = document.querySelector('.toast-propath');
    if (existingToast) {
        existingToast.remove();
    }

    const toast = document.createElement('div');
    toast.className = `toast-propath toast-${tipo}`;
    toast.innerHTML = `
        <i class="bi ${iconClass} fs-5"></i>
        <div class="toast-content">${mensaje}</div>
        <button type="button" class="btn-close ms-auto" aria-label="Cerrar"></button>
    `;

    document.body.appendChild(toast);

    toast.querySelector('.btn-close').addEventListener('click', () => {
        toast.style.animation = 'slideOut 0.3s forwards';
        setTimeout(() => toast.remove(), 300);
    });

    setTimeout(() => {
        if (document.body.contains(toast)) {
            toast.style.animation = 'slideOut 0.3s forwards';
            setTimeout(() => toast.remove(), 300);
        }
    }, 4000);
}

function inicializarGraficos() {
    return;
}

function filtrarCards(categoria, btn) {
    const buttons = btn.parentElement.querySelectorAll('.btn-filter');
    buttons.forEach(b => b.classList.remove('active'));
    btn.classList.add('active');

    const cards = document.querySelectorAll('.opportunity-card-wrapper');
    cards.forEach(card => {
        if (categoria === 'all') {
            card.style.display = 'block';
        } else {
            const cardCat = card.getAttribute('data-category');
            if (cardCat === categoria) {
                card.style.display = 'block';
            } else {
                card.style.display = 'none';
            }
        }
    });
}

function initDashboardCharts() {
    return;
}

function confirmarEliminarEstudiante(id, nombre) {
    mostrarModalConfirmacion(
        'Eliminar Estudiante',
        '¿Está seguro de eliminar definitivamente a ' + nombre + '? Esta acción no se puede deshacer.',
        async function () {
            const body = new URLSearchParams({
                accion: 'eliminar',
                recurso: 'estudiantes',
                id: id
            });
            try {
                const res = await fetch(window.location.pathname.includes('/admin/') ? 'accion' : 'admin/accion', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                    body: body.toString()
                });
                const data = await res.json();
                if (data.exito) {
                    mostrarModalMensaje('Estudiante Eliminado', data.mensaje || 'Estudiante eliminado con éxito.', 'success', () => window.location.reload());
                } else {
                    mostrarModalMensaje('Error al Eliminar', data.mensaje || 'No se pudo eliminar el estudiante.', 'danger');
                }
            } catch (err) {
                mostrarModalMensaje('Error de Conexión', 'No se pudo conectar con el servidor: ' + err.message, 'danger');
            }
        }
    );
}

function verDetalleEstudianteBtn(btn) {
    verDetalleEstudiante(
        btn.getAttribute('data-nombre'),
        btn.getAttribute('data-apellidos'),
        btn.getAttribute('data-correo'),
        btn.getAttribute('data-universidad'),
        btn.getAttribute('data-carrera'),
        btn.getAttribute('data-ciclo'),
        btn.getAttribute('data-telefono'),
        btn.getAttribute('data-cv')
    );
}

function verDetalleEstudiante(nombre, apellidos, correo, universidad, carrera, ciclo, telefono, cv) {
    let nomCompleto = nombre || '';
    if (apellidos && apellidos !== 'null' && apellidos !== 'undefined') {
        nomCompleto += ' ' + apellidos;
    }
    document.getElementById('modalEstudianteNombre').textContent = nomCompleto.trim() || '—';
    document.getElementById('modalEstudianteCorreo').textContent = correo || '—';
    document.getElementById('modalEstudianteUniversidad').textContent = universidad || 'No especificada';
    document.getElementById('modalEstudianteCarrera').textContent = carrera || 'No especificada';
    document.getElementById('modalEstudianteCiclo').textContent = ciclo ? ciclo + '° Ciclo' : 'No especificado';
    document.getElementById('modalEstudianteTelefono').textContent = telefono || 'No registrado';

    const cvLink = document.getElementById('modalEstudianteCv');
    if (cv && cv !== 'null' && cv !== '') {
        cvLink.href = cv;
        cvLink.textContent = 'Ver Curriculum Vitae (PDF)';
        cvLink.classList.remove('text-muted');
        cvLink.classList.add('text-primary');
    } else {
        cvLink.removeAttribute('href');
        cvLink.textContent = 'No adjuntado';
        cvLink.classList.remove('text-primary');
        cvLink.classList.add('text-muted');
    }

    const modal = BootstrapModalCompat.getOrCreateInstance(document.getElementById('modalDetalleEstudiante'));
    modal.show();
}

function verDetalleEmpresaBtn(btn) {
    verDetalleEmpresa(
        btn.getAttribute('data-razon'),
        btn.getAttribute('data-ruc'),
        btn.getAttribute('data-correo'),
        btn.getAttribute('data-telefono'),
        btn.getAttribute('data-direccion'),
        btn.getAttribute('data-descripcion'),
        btn.getAttribute('data-representante'),
        btn.getAttribute('data-estado'),
        btn.getAttribute('data-estado-cuenta'),
        btn.getAttribute('data-id')
    );
}

function verDetalleEmpresa(razonSocial, ruc, correo, telefono, direccion, descripcion, representante, estado, estadoCuenta, idEmpresa) {
    document.getElementById('modalEmpresaRazon').textContent = razonSocial || '—';
    document.getElementById('modalEmpresaRuc').textContent = ruc || '—';
    document.getElementById('modalEmpresaCorreo').textContent = correo || '—';
    document.getElementById('modalEmpresaTelefono').textContent = telefono || 'No registrado';
    const repEl = document.getElementById('modalEmpresaRepresentante');
    if (repEl) repEl.textContent = representante || 'No especificado';
    const dirEl = document.getElementById('modalEmpresaDireccion');
    if (dirEl) dirEl.textContent = direccion || 'No especificada';
    const descEl = document.getElementById('modalEmpresaDescripcion');
    if (descEl) descEl.textContent = descripcion || 'Sin descripción';

    const cuentaEl = document.getElementById('modalEmpresaEstadoCuenta');
    if (cuentaEl) {
        const estC = (estadoCuenta || 'ACTIVO').toUpperCase();
        cuentaEl.textContent = estC;
        cuentaEl.className = 'badge ' + (estC === 'ACTIVO' ? 'bg-success' : 'bg-secondary');
    }

    const badge = document.getElementById('modalEmpresaEstado');
    if (badge) {
        const estA = (estado || 'PENDIENTE').toUpperCase();
        badge.textContent = estA;
        badge.className = 'badge ' + (estA === 'APROBADA' ? 'bg-primary' : (estA === 'PENDIENTE' ? 'bg-warning text-dark' : 'bg-danger'));
    }

    const btnAprobar = document.getElementById('btnModalAprobarEmpresa');
    if (btnAprobar) {
        const estA = (estado || 'PENDIENTE').toUpperCase();
        if (estA === 'PENDIENTE') {
            btnAprobar.classList.remove('d-none');
            btnAprobar.style.display = 'inline-flex';
            btnAprobar.setAttribute('data-id', idEmpresa || '');
            btnAprobar.setAttribute('data-razon', razonSocial || '');
        } else {
            btnAprobar.classList.add('d-none');
            btnAprobar.style.display = 'none';
        }
    }

    const modal = BootstrapModalCompat.getOrCreateInstance(document.getElementById('modalDetalleEmpresa'));
    modal.show();
}

function verDetalleOfertaBtn(btn) {
    verDetalleOferta(
        btn.getAttribute('data-titulo'),
        btn.getAttribute('data-empresa'),
        btn.getAttribute('data-carrera'),
        btn.getAttribute('data-modalidad'),
        btn.getAttribute('data-vacantes'),
        btn.getAttribute('data-fecha'),
        btn.getAttribute('data-estado')
    );
}

function verDetalleOferta(puesto, empresa, carrera, modalidad, vacantes, fecha, estado) {
    document.getElementById('modalOfertaPuesto').textContent = puesto;
    document.getElementById('modalOfertaEmpresa').textContent = empresa;
    document.getElementById('modalOfertaCarrera').textContent = carrera;
    document.getElementById('modalOfertaModalidad').textContent = modalidad;
    document.getElementById('modalOfertaVacantes').textContent = vacantes + (vacantes > 1 ? ' vacantes' : ' vacante');
    document.getElementById('modalOfertaFecha').textContent = fecha;

    const badge = document.getElementById('modalOfertaEstado');
    badge.textContent = estado;
    badge.className = 'badge-status ' + (estado === 'Activa' ? 'badge-activo' : (estado === 'Pendiente' ? 'badge-pendiente' : 'badge-bloqueado'));

    const modal = BootstrapModalCompat.getOrCreateInstance(document.getElementById('modalDetalleOferta'));
    modal.show();
}

function cambiarEstadoOfertaBtn(btn) {
    const id = btn.getAttribute('data-id');
    const nuevoEstado = btn.getAttribute('data-estado');
    const accionTexto = nuevoEstado === 'Activa' ? 'aprobar y activar' : (nuevoEstado === 'Rechazada' ? 'rechazar' : 'desactivar');
    mostrarModalConfirmacion(
        'Cambiar Estado de Oferta',
        '¿Desea ' + accionTexto + ' esta oferta de práctica?',
        async function () {
            const body = new URLSearchParams({
                accion: 'cambiarEstadoOferta',
                id: id,
                estado: nuevoEstado
            });
            try {
                const res = await fetch(window.location.pathname.includes('/admin/') ? 'accion' : 'admin/accion', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                    body: body.toString()
                });
                const data = await res.json();
                if (data.exito) {
                    mostrarModalMensaje('Estado Actualizado', data.mensaje || 'Oferta actualizada exitosamente.', 'success', () => window.location.reload());
                } else {
                    mostrarModalMensaje('Error al Actualizar', data.mensaje || 'No se pudo actualizar la oferta.', 'danger');
                }
            } catch (err) {
                mostrarModalMensaje('Error de Conexión', 'No se pudo conectar con el servidor: ' + err.message, 'danger');
            }
        }
    );
}

function mostrarAvisoDesarrollo(modulo, mensaje) {
    const modalEl = document.getElementById('modalDesarrollo');
    if (modalEl && typeof bootstrap !== 'undefined' && bootstrap.Modal) {
        const nombreEl = document.getElementById('modalDesarrolloNombre');
        const textoEl = document.getElementById('modalDesarrolloTexto');
        if (nombreEl) nombreEl.textContent = 'Módulo ' + modulo;
        if (textoEl) textoEl.textContent = mensaje;

        const modal = BootstrapModalCompat.getOrCreateInstance(modalEl);
        modal.show();
    } else {
        mostrarToast('Módulo ' + modulo + ': ' + mensaje, 'warning');
    }
}

function asegurarModalesSistema() {
    if (!document.getElementById('modalMensajeSistema')) {
        const divAviso = document.createElement('div');
        divAviso.className = 'modal fade';
        divAviso.id = 'modalMensajeSistema';
        divAviso.tabIndex = -1;
        divAviso.setAttribute('aria-hidden', 'true');
        divAviso.innerHTML = `
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content rounded-4 border-0 shadow">
                    <div class="modal-header border-0 pb-0">
                        <h5 class="modal-title fw-bold" id="modalMensajeTitulo">Notificación</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body py-4 fs-6 text-secondary" id="modalMensajeCuerpo"></div>
                    <div class="modal-footer border-0 pt-0">
                        <button type="button" class="btn btn-primary rounded-pill px-4" data-bs-dismiss="modal" id="modalMensajeBtnOk">Entendido</button>
                    </div>
                </div>
            </div>`;
        document.body.appendChild(divAviso);
    }

    if (!document.getElementById('modalConfirmacionSistema')) {
        const divConf = document.createElement('div');
        divConf.className = 'modal fade';
        divConf.id = 'modalConfirmacionSistema';
        divConf.tabIndex = -1;
        divConf.setAttribute('aria-hidden', 'true');
        divConf.innerHTML = `
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content rounded-4 border-0 shadow">
                    <div class="modal-header border-0 pb-0">
                        <h5 class="modal-title fw-bold text-danger" id="modalConfirmTitulo"><i class="bi bi-exclamation-triangle-fill me-2"></i>Confirmación</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body py-4 fs-6 text-dark" id="modalConfirmCuerpo"></div>
                    <div class="modal-footer border-0 pt-0">
                        <button type="button" class="btn btn-outline-secondary rounded-pill px-3" data-bs-dismiss="modal">Cancelar</button>
                        <button type="button" class="btn btn-danger rounded-pill px-4" id="modalConfirmBtnAccion">Confirmar</button>
                    </div>
                </div>
            </div>`;
        document.body.appendChild(divConf);
    }
}

function mostrarModalMensaje(titulo, mensaje, tipo, onCerrar) {
    asegurarModalesSistema();
    const modalEl = document.getElementById('modalMensajeSistema');
    const titleEl = document.getElementById('modalMensajeTitulo');
    const bodyEl = document.getElementById('modalMensajeCuerpo');
    const okBtn = document.getElementById('modalMensajeBtnOk');

    titleEl.textContent = titulo;
    bodyEl.textContent = mensaje;

    if (tipo === 'danger' || tipo === 'error' || (titulo && titulo.toLowerCase().includes('prohibid'))) {
        titleEl.className = 'modal-title fw-bold text-danger';
        okBtn.className = 'btn btn-danger rounded-pill px-4';
    } else if (tipo === 'success') {
        titleEl.className = 'modal-title fw-bold text-success';
        okBtn.className = 'btn btn-success rounded-pill px-4';
    } else {
        titleEl.className = 'modal-title fw-bold text-primary';
        okBtn.className = 'btn btn-primary rounded-pill px-4';
    }

    const modal = BootstrapModalCompat.getOrCreateInstance(modalEl);
    if (onCerrar) {
        const handler = function () {
            modalEl.removeEventListener('hidden.bs.modal', handler);
            onCerrar();
        };
        modalEl.addEventListener('hidden.bs.modal', handler);
    }
    modal.show();
}

function mostrarModalConfirmacion(titulo, mensaje, onConfirmar) {
    asegurarModalesSistema();
    const modalEl = document.getElementById('modalConfirmacionSistema');
    const titleEl = document.getElementById('modalConfirmTitulo');
    const bodyEl = document.getElementById('modalConfirmCuerpo');
    const btnAccion = document.getElementById('modalConfirmBtnAccion');

    titleEl.innerHTML = '<i class="bi bi-exclamation-triangle-fill me-2"></i>' + titulo;
    bodyEl.textContent = mensaje;

    const modal = BootstrapModalCompat.getOrCreateInstance(modalEl);
    btnAccion.onclick = function () {
        btnAccion.onclick = null;
        modal.hide();
        if (typeof onConfirmar === 'function') {
            onConfirmar();
        }
    };
    modal.show();
}

function mostrarModalProhibido(mensaje) {
    mostrarModalMensaje(
        'Acción prohibida',
        mensaje || 'No está permitido eliminar la cuenta del administrador principal del sistema.',
        'danger'
    );
}

window.mostrarToast = mostrarToast;
window.filtrarCards = filtrarCards;
window.confirmarEliminarEstudiante = confirmarEliminarEstudiante;
window.verDetalleEstudianteBtn = verDetalleEstudianteBtn;
window.verDetalleEstudiante = verDetalleEstudiante;
window.verDetalleEmpresaBtn = verDetalleEmpresaBtn;
window.verDetalleEmpresa = verDetalleEmpresa;
window.verDetalleOfertaBtn = verDetalleOfertaBtn;
window.verDetalleOferta = verDetalleOferta;
window.mostrarAvisoDesarrollo = mostrarAvisoDesarrollo;
window.asegurarModalesSistema = asegurarModalesSistema;
window.mostrarModalMensaje = mostrarModalMensaje;
window.mostrarModalConfirmacion = mostrarModalConfirmacion;
window.mostrarModalProhibido = mostrarModalProhibido;
window.cambiarEstadoOfertaBtn = cambiarEstadoOfertaBtn;
window.BootstrapModalCompat = BootstrapModalCompat;
