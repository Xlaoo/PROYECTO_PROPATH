/**
 * PROPATH - PLATAFORMA DE INSERCIÓN LABORAL
 * Lógica JavaScript institucional y módulo Administrador
 */

document.addEventListener('DOMContentLoaded', function () {
    // 1. Alternador de visibilidad de contraseña (Login)
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

    // 2. Botón para auto-completar credenciales de prueba en login
    const autoFillBtn = document.getElementById('btnFillDemo');
    if (autoFillBtn) {
        autoFillBtn.addEventListener('click', function (e) {
            e.preventDefault();
            const correoInput = document.getElementById('correo');
            if (correoInput && passwordInput) {
                correoInput.value = 'admin@propath.com';
                passwordInput.value = 'Admin123';
                mostrarToast('Credenciales de administrador cargadas', 'info');
            }
        });
    }

    // 3. Control de Sidebar Móvil y Backdrop
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

    // 4. Filtrado en tiempo real en tablas administrativas
    const searchInputs = document.querySelectorAll('.table-search-input');
    searchInputs.forEach(input => {
        input.addEventListener('keyup', function () {
            const targetTableId = this.getAttribute('data-table');
            const table = document.getElementById(targetTableId);
            if (!table) return;

            const term = this.value.toLowerCase().trim();
            const rows = table.querySelectorAll('tbody tr');

            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                if (text.includes(term)) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        });
    });

    // 5. Tooltips de Bootstrap
    if (typeof bootstrap !== 'undefined' && bootstrap.Tooltip) {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }
});

/**
 * Muestra una notificación Toast flotante y elegante
 */
function mostrarToast(mensaje, tipo = 'success') {
    const iconClass = tipo === 'success' ? 'bi-check-circle-fill text-success' :
                      tipo === 'danger' ? 'bi-x-circle-fill text-danger' :
                      tipo === 'warning' ? 'bi-exclamation-triangle-fill text-warning' :
                      'bi-info-circle-fill text-info';

    // Eliminar toast previo si existe
    const existingToast = document.querySelector('.toast-propath');
    if (existingToast) {
        existingToast.remove();
    }

    const toast = document.createElement('div');
    toast.className = 'toast-propath';
    toast.innerHTML = `
        <i class="bi ${iconClass} fs-5"></i>
        <div>
            <div class="fw-semibold text-dark" style="font-size: 0.9rem;">${mensaje}</div>
            <div class="text-muted" style="font-size: 0.75rem;">Acción registrada en ProPath</div>
        </div>
        <button type="button" class="btn-close ms-2" style="font-size: 0.75rem;" onclick="this.parentElement.remove()"></button>
    `;

    document.body.appendChild(toast);

    setTimeout(() => {
        if (toast.parentElement) {
            toast.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(15px)';
            setTimeout(() => toast.remove(), 400);
        }
    }, 3500);
}

/**
 * Cambia el estado visual de un estudiante y muestra feedback
 */
function cambiarEstadoEstudiante(id, nuevoEstado, boton) {
    const row = boton.closest('tr');
    if (!row) return;

    const badgeCell = row.querySelector('.badge-status');
    if (badgeCell) {
        if (nuevoEstado === 'Activo') {
            badgeCell.className = 'badge-status badge-activo';
            badgeCell.innerHTML = '<i class="bi bi-check-circle-fill"></i> Activo';
            mostrarToast('Estudiante activado correctamente', 'success');
        } else if (nuevoEstado === 'Bloqueado') {
            badgeCell.className = 'badge-status badge-bloqueado';
            badgeCell.innerHTML = '<i class="bi bi-slash-circle-fill"></i> Bloqueado';
            mostrarToast('Estudiante bloqueado correctamente', 'warning');
        }
    }
}

/**
 * Cambia el estado visual de una empresa y muestra feedback
 */
function cambiarEstadoEmpresa(id, nuevoEstado, boton) {
    const row = boton.closest('tr');
    if (!row) return;

    const badgeCell = row.querySelector('.badge-status');
    if (badgeCell) {
        if (nuevoEstado === 'Aprobada') {
            badgeCell.className = 'badge-status badge-aprobada';
            badgeCell.innerHTML = '<i class="bi bi-shield-check"></i> Aprobada';
            mostrarToast('Empresa aprobada para publicar ofertas', 'success');
        } else if (nuevoEstado === 'Bloqueada') {
            badgeCell.className = 'badge-status badge-bloqueado';
            badgeCell.innerHTML = '<i class="bi bi-slash-circle-fill"></i> Bloqueada';
            mostrarToast('Empresa bloqueada en el sistema', 'warning');
        }
    }
}

/**
 * Cambia el estado visual de una oferta y muestra feedback
 */
function cambiarEstadoOferta(id, nuevoEstado, boton) {
    const row = boton.closest('tr');
    if (!row) return;

    const badgeCell = row.querySelector('.badge-status');
    if (badgeCell) {
        if (nuevoEstado === 'Activa') {
            badgeCell.className = 'badge-status badge-activo';
            badgeCell.innerHTML = '<i class="bi bi-check-circle-fill"></i> Activa';
            mostrarToast('Oferta de práctica aprobada y activa', 'success');
        } else if (nuevoEstado === 'Rechazada') {
            badgeCell.className = 'badge-status badge-bloqueado';
            badgeCell.innerHTML = '<i class="bi bi-x-circle-fill"></i> Rechazada';
            mostrarToast('Oferta de práctica rechazada', 'danger');
        } else if (nuevoEstado === 'Desactivada') {
            badgeCell.className = 'badge-status badge-bloqueado';
            badgeCell.innerHTML = '<i class="bi bi-pause-circle-fill"></i> Desactivada';
            mostrarToast('Oferta de práctica desactivada', 'warning');
        }
    }
}

/**
 * Carga datos en el modal de detalle del estudiante
 */
function verDetalleEstudiante(nombre, correo, universidad, carrera, ciclo, estado) {
    document.getElementById('modalEstudianteNombre').textContent = nombre;
    document.getElementById('modalEstudianteCorreo').textContent = correo;
    document.getElementById('modalEstudianteUniversidad').textContent = universidad;
    document.getElementById('modalEstudianteCarrera').textContent = carrera;
    document.getElementById('modalEstudianteCiclo').textContent = 'Ciclo ' + ciclo;
    
    const badge = document.getElementById('modalEstudianteEstado');
    badge.textContent = estado;
    badge.className = 'badge-status ' + (estado === 'Activo' ? 'badge-activo' : 'badge-bloqueado');

    const modal = new bootstrap.Modal(document.getElementById('modalDetalleEstudiante'));
    modal.show();
}

/**
 * Carga datos en el modal de detalle de la empresa
 */
function verDetalleEmpresa(razonSocial, ruc, correo, telefono, estado) {
    document.getElementById('modalEmpresaRazon').textContent = razonSocial;
    document.getElementById('modalEmpresaRuc').textContent = ruc;
    document.getElementById('modalEmpresaCorreo').textContent = correo;
    document.getElementById('modalEmpresaTelefono').textContent = telefono;

    const badge = document.getElementById('modalEmpresaEstado');
    badge.textContent = estado;
    badge.className = 'badge-status ' + (estado === 'Aprobada' ? 'badge-aprobada' : (estado === 'Pendiente' ? 'badge-pendiente' : 'badge-bloqueado'));

    const modal = new bootstrap.Modal(document.getElementById('modalDetalleEmpresa'));
    modal.show();
}

/**
 * Carga datos en el modal de detalle de la oferta
 */
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

    const modal = new bootstrap.Modal(document.getElementById('modalDetalleOferta'));
    modal.show();
}

/**
 * Muestra el modal informativo o alerta para módulos en desarrollo (Estudiante, Empresa)
 */
function mostrarAvisoDesarrollo(modulo, mensaje) {
    const modalEl = document.getElementById('modalDesarrollo');
    if (modalEl && typeof bootstrap !== 'undefined' && bootstrap.Modal) {
        const nombreEl = document.getElementById('modalDesarrolloNombre');
        const textoEl = document.getElementById('modalDesarrolloTexto');
        if (nombreEl) nombreEl.textContent = 'Módulo ' + modulo;
        if (textoEl) textoEl.textContent = mensaje;

        const modal = new bootstrap.Modal(modalEl);
        modal.show();
    } else {
        mostrarToast('Módulo ' + modulo + ': ' + mensaje, 'warning');
    }
}
