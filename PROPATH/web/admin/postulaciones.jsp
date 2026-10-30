<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.propath.modelo.Postulacion"%>
<%@page import="com.propath.servicio.BackendCliente"%>
<%
    request.setAttribute("activePage", "postulaciones");
    List<Postulacion> listaPostulaciones = BackendCliente.listarPostulaciones();
%>
<!DOCTYPE html>
<html lang="es" data-bs-theme="light">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Gestión de Postulaciones | ProPath</title>

        
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">

        
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
    </head>
    <body>
        <div class="admin-layout">
            
            <jsp:include page="/admin/includes/sidebar.jsp" />

            
            <main class="admin-main">
                
                <jsp:include page="/admin/includes/topbar.jsp" />

                
                <div class="admin-content">
                    
                    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
                        <div>
                            <nav aria-label="breadcrumb">
                                <ol class="breadcrumb mb-1 small">
                                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/dashboard.jsp" class="text-decoration-none">Inicio</a></li>
                                    <li class="breadcrumb-item active" aria-current="page">Postulaciones</li>
                                </ol>
                            </nav>
                            <h1 class="h3 fw-bold text-dark mb-0">Gestión de Postulaciones</h1>
                            <p class="text-muted small mb-0">Visualización centralizada de las postulaciones realizadas por estudiantes a ofertas laborales</p>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            <span class="badge bg-indigo-subtle border px-3 py-2 rounded-pill fw-semibold" style="background-color: #e0e7ff; color: #4338ca; border-color: #c7d2fe !important;">
                                <i class="bi bi-file-earmark-person-fill me-1"></i> <%= (listaPostulaciones != null ? listaPostulaciones.size() : 0) %> Postulaciones monitoreadas
                            </span>
                        </div>
                    </div>

                    
                    <div class="alert alert-primary d-flex align-items-center mb-4 py-3 px-4 rounded-3 border-primary-subtle shadow-sm" role="alert">
                        <i class="bi bi-info-circle-fill fs-4 me-3 text-primary flex-shrink-0"></i>
                        <div>
                            <div class="fw-bold text-primary mb-1">Módulo de Solo Consulta</div>
                            <div class="small text-secondary">
                                Como administrador, tienes acceso de auditoría y supervisión global. La modificación y decisión final sobre cada postulación (<em>Postulado, En revisión, Seleccionado, No seleccionado</em>) es potestad exclusiva de la empresa evaluadora.
                            </div>
                        </div>
                    </div>

                    
                    <div class="propath-card">
                        <div class="propath-card-header">
                            <h2 class="propath-card-title">
                                <i class="bi bi-file-earmark-person-fill"></i>
                                <span>Registro de Postulaciones</span>
                            </h2>
                            
                            <div class="search-box-custom">
                                <i class="bi bi-search search-icon"></i>
                                <input type="text" 
                                       class="form-control table-search-input" 
                                       data-table="tablaPostulaciones" 
                                       placeholder="Buscar por estudiante, oferta o empresa...">
                            </div>
                        </div>

                        <div class="table-responsive">
                            <table class="table table-propath" id="tablaPostulaciones">
                                <thead>
                                    <tr>
                                        <th>Estudiante</th>
                                        <th>Oferta de Prácticas</th>
                                        <th>Empresa</th>
                                        <th>Fecha de Postulación</th>
                                        <th>Estado Actual</th>
                                        <th class="text-center">Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% if (listaPostulaciones == null || listaPostulaciones.isEmpty()) { %>
                                    <tr>
                                        <td colspan="6" class="text-center py-4 text-muted">
                                            <i class="bi bi-inbox fs-3 d-block mb-2 text-secondary"></i>
                                            <span>No hay registros disponibles</span>
                                        </td>
                                    </tr>
                                    <% } else {
                                        for (Postulacion pos : listaPostulaciones) { 
                                            String badgeClass = "badge-postulado";
                                            String iconClass = "bi-send-fill";
                                            
                                            if ("En revisión".equalsIgnoreCase(pos.getEstado())) {
                                                badgeClass = "badge-en-revision";
                                                iconClass = "bi-clock-history";
                                            } else if ("Seleccionado".equalsIgnoreCase(pos.getEstado())) {
                                                badgeClass = "badge-seleccionado";
                                                iconClass = "bi-check2-circle";
                                            } else if ("No seleccionado".equalsIgnoreCase(pos.getEstado())) {
                                                badgeClass = "badge-bloqueado";
                                                iconClass = "bi-x-circle-fill";
                                            }
                                    %>
                                    <tr>
                                        <td>
                                            <div class="d-flex align-items-center gap-2">
                                                <div class="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white shadow-sm"
                                                     style="width: 36px; height: 36px; font-size: 0.82rem; background: linear-gradient(135deg, #1e40af, #3b82f6);">
                                                    <i class="bi bi-person"></i>
                                                </div>
                                                <div>
                                                    <span class="fw-semibold text-dark d-block"><%= pos.getEstudiante() %></span>
                                                    <span class="text-muted" style="font-size: 0.75rem;">Postulante registrado</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <span class="fw-semibold text-primary">
                                                <i class="bi bi-briefcase me-1"></i><%= pos.getOferta() %>
                                            </span>
                                        </td>
                                        <td>
                                            <span class="text-secondary fw-medium">
                                                <i class="bi bi-building me-1 text-muted"></i><%= pos.getEmpresa() %>
                                            </span>
                                        </td>
                                        <td>
                                            <span class="text-muted small">
                                                <i class="bi bi-calendar-event me-1"></i><%= pos.getFechaPostulacion() %>
                                            </span>
                                        </td>
                                        <td>
                                            <span class="badge-status <%= badgeClass %>">
                                                <i class="bi <%= iconClass %>"></i>
                                                <%= pos.getEstado() %>
                                            </span>
                                        </td>
                                        <td class="text-center">
                                            <button type="button" 
                                                    class="btn-action btn-action-view" 
                                                    title="Ver postulación completa"
                                                    data-bs-toggle="tooltip"
                                                    data-id="<%= pos.getId() %>"
                                                    data-estudiante="<%= pos.getEstudiante() %>"
                                                    data-oferta="<%= pos.getOferta() %>"
                                                    data-empresa="<%= pos.getEmpresa() %>"
                                                    data-fecha="<%= pos.getFechaPostulacion() %>"
                                                    data-estado="<%= pos.getEstado() %>"
                                                    onclick="verDetallePostulacionAdmin(this)">
                                                <i class="bi bi-eye"></i>
                                            </button>
                                        </td>
                                    </tr>
                                    <%  }
                                       } %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                    <!-- Modal Detalle Postulación Admin -->
        <div class="modal fade" id="modalDetallePostulacionAdmin" tabindex="-1" aria-labelledby="modalPostulacionAdminTitle" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered modal-lg">
                <div class="modal-content modal-content-propath">
                    <div class="modal-header modal-header-propath">
                        <h2 class="modal-title h5" id="modalPostulacionAdminTitle">
                            <i class="bi bi-file-earmark-person-fill me-2 text-primary"></i>
                            <span>Expediente Completo de Postulación</span>
                        </h2>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body modal-body-propath">
                        <div class="row g-3 mb-4">
                            <div class="col-sm-6">
                                <div class="detail-label">Estudiante Postulante</div>
                                <div class="detail-value fs-5 fw-bold text-dark" id="adminPostulacionEstudiante">—</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Oferta de Prácticas</div>
                                <div class="detail-value fw-semibold text-primary" id="adminPostulacionOferta">—</div>
                            </div>
                            <div class="col-sm-6">
                                <div class="detail-label">Empresa Convocante</div>
                                <div class="detail-value" id="adminPostulacionEmpresa">—</div>
                            </div>
                            <div class="col-sm-3">
                                <div class="detail-label">Fecha de Envío</div>
                                <div class="detail-value" id="adminPostulacionFecha">—</div>
                            </div>
                            <div class="col-sm-3">
                                <div class="detail-label">Estado Actual</div>
                                <span id="adminPostulacionEstado" class="badge-status badge-activo">—</span>
                            </div>
                        </div>

                        <div class="border-top pt-3">
                            <h3 class="h6 fw-bold text-dark mb-3">
                                <i class="bi bi-ui-checks me-1 text-primary"></i>
                                Respuestas al Cuestionario de Postulación (12 Preguntas)
                            </h3>
                            <div id="adminPostulacionRespuestas" class="d-flex flex-column gap-2" style="max-height: 400px; overflow-y: auto;">
                                <div class="text-center py-4 text-muted">
                                    <div class="spinner-border spinner-border-sm me-2" role="status"></div>
                                    <span>Cargando respuestas del postulante...</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer bg-light border-0">
                        <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Cerrar</button>
                    </div>
                </div>
            </div>
        </div>

        <script>
            async function verDetallePostulacionAdmin(btn) {
                const id = btn.getAttribute('data-id');
                const estudiante = btn.getAttribute('data-estudiante') || '—';
                const oferta = btn.getAttribute('data-oferta') || '—';
                const empresa = btn.getAttribute('data-empresa') || '—';
                const fecha = btn.getAttribute('data-fecha') || '—';
                const estado = btn.getAttribute('data-estado') || '—';

                document.getElementById('adminPostulacionEstudiante').textContent = estudiante;
                document.getElementById('adminPostulacionOferta').textContent = oferta;
                document.getElementById('adminPostulacionEmpresa').textContent = empresa;
                document.getElementById('adminPostulacionFecha').textContent = fecha;

                const badge = document.getElementById('adminPostulacionEstado');
                badge.textContent = estado;
                const estNorm = estado.toUpperCase();
                badge.className = 'badge-status ' + (estNorm.includes('SELECCIONADO') && !estNorm.includes('NO') ? 'badge-seleccionado' : (estNorm.includes('REVISIÓN') || estNorm.includes('REVISION') ? 'badge-en-revision' : (estNorm.includes('NO') ? 'badge-bloqueado' : 'badge-postulado')));

                const contenedor = document.getElementById('adminPostulacionRespuestas');
                contenedor.innerHTML = '<div class="text-center py-4 text-muted"><div class="spinner-border spinner-border-sm me-2" role="status"></div><span>Cargando respuestas del postulante...</span></div>';

                const modalEl = document.getElementById('modalDetallePostulacionAdmin');
                const ModalClass = window.BootstrapModalCompat || (window.bootstrap && window.bootstrap.Modal);
                if (ModalClass) {
                    const inst = ModalClass.getOrCreateInstance(modalEl);
                    if (inst) inst.show();
                }

                try {
                    const res = await fetch('${pageContext.request.contextPath}/admin/accion?accion=obtenerPostulacion&id=' + encodeURIComponent(id), {
                        credentials: 'same-origin',
                        cache: 'no-store'
                    });
                    const data = await res.json();
                    contenedor.innerHTML = '';

                    const preguntasBase = [
                        "¿Por qué te interesa esta oportunidad?",
                        "¿Qué conocimientos puedes aportar?",
                        "¿Qué herramientas o tecnologías manejas?",
                        "¿Has desarrollado proyectos relacionados?",
                        "¿Qué esperas aprender?",
                        "¿Cuál es tu disponibilidad horaria?",
                        "¿Cuáles son tus principales fortalezas?",
                        "¿Cómo organizas tus responsabilidades?",
                        "¿Cómo reaccionas ante un problema inesperado?",
                        "¿Cómo trabajas en equipo?",
                        "¿Qué aspecto profesional deseas mejorar?",
                        "¿Cuáles son tus objetivos profesionales?"
                    ];

                    const respuestas = (data && Array.isArray(data.respuestas)) ? data.respuestas : [];
                    if (respuestas.length === 0) {
                        contenedor.innerHTML = '<div class="alert alert-light border text-muted small py-3 text-center">No se encontraron respuestas registradas para esta postulación.</div>';
                        return;
                    }

                    respuestas.sort((a, b) => Number(a.numeroPregunta) - Number(b.numeroPregunta));

                    respuestas.forEach((r) => {
                        const num = Number(r.numeroPregunta);
                        const textoPregunta = r.pregunta || (preguntasBase[num - 1] || ("Pregunta #" + num));
                        const textoRespuesta = r.respuesta || "Sin respuesta registrada.";

                        const card = document.createElement('div');
                        card.className = 'border rounded-3 p-3 bg-light bg-opacity-50';
                        card.innerHTML = '<div class="fw-semibold text-dark small mb-1">' + num + '. ' + textoPregunta + '</div>'
                                       + '<div class="small text-secondary ps-3 border-start border-2 border-primary">' + textoRespuesta + '</div>';
                        contenedor.appendChild(card);
                    });
                } catch (err) {
                    contenedor.innerHTML = '<div class="alert alert-danger small py-2">Error al cargar las respuestas: ' + err.message + '</div>';
                }
            }

            window.verDetallePostulacionAdmin = verDetallePostulacionAdmin;
        </script>
</main>
        </div>

        
        <script type="module" src="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/js/bootstrap.bundle.min.js"></script>
        <script type="module" src="${pageContext.request.contextPath}/js/script.js"></script>
    </body>
</html>
