<%! private String entrevistaCampo(String objeto, String campo) { java.util.regex.Matcher m=java.util.regex.Pattern
    .compile(java.util.regex.Pattern.quote("\"" + campo + "\"")
           + "\\s*:\\s*(?:\"([^\"]*)\"|([0-9]+))") .matcher(objeto); return m.find() ? (m.group(1) !=null ?
    m.group(1) : m.group(2)) : "" ; } private java.util.List<String> entrevistasJson(String json) {
    java.util.List<String> lista = new java.util.ArrayList<>();
            if (json == null) return lista;
            java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("\\{[^{}]*\\}")
            .matcher(json);
            while (m.find()) {
            lista.add(m.group());
            }
            return lista;
            }

            private String escaparHtml(String s) {
            if (s == null) return "";
            return s.replace("&", "&amp;")
            .replace("<", "&lt;" ) .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
                }
                %>
                <%@page contentType="text/html" pageEncoding="UTF-8" %>
                    <%@page import="com.propath.modelo.Usuario" %>
                        <%@page import="com.propath.modelo.Estudiante" %>
                            <%@page import="com.propath.modelo.OfertaPractica" %>
                                <%@page import="com.propath.modelo.Postulacion" %>
                                    <%@page import="com.propath.modelo.DocumentoEstudiante" %>
                                        <%@page import="com.propath.servicio.BackendCliente" %>
                                            <%@page import="java.util.List" %>
                                                <%@page import="java.util.ArrayList" %>
                                                    <% Usuario usuario=(Usuario)
                                                        session.getAttribute("usuarioLogueado"); if (usuario==null) {
                                                        response.sendRedirect(request.getContextPath() + "/login.jsp" );
                                                        return; } List<Estudiante> estudiantes =
                                                        BackendCliente.listarEstudiantes();
                                                        Estudiante miEstudiante = null;
                                                        for (Estudiante e : estudiantes) {
                                                        if (e.getCorreo() != null &&
                                                        e.getCorreo().equalsIgnoreCase(usuario.getCorreo())) {
                                                        miEstudiante = e;
                                                        usuario.setIdEstudiante(e.getId());
                                                        break;
                                                        }
                                                        }

                                                        int idEstudiante = usuario.getIdEstudiante() != null ?
                                                        usuario.getIdEstudiante()
                                                        : (miEstudiante != null ? miEstudiante.getId() : 0);
                                                        DocumentoEstudiante miCv = null;
                                                        if (idEstudiante > 0) {
                                                        miCv = BackendCliente.obtenerCvEstudiante(idEstudiante);
                                                        }

                                                        List<OfertaPractica> todasOfertas =
                                                            BackendCliente.listarOfertas();
                                                            List<Postulacion> todasPostulaciones =
                                                                BackendCliente.listarPostulaciones();

                                                                List<Postulacion> misPostulaciones = new ArrayList<>();
                                                                        for (Postulacion p : todasPostulaciones) {
                                                                        if (p.getEstudiante() != null &&
                                                                        usuario.getNombre() != null &&
                                                                        (p.getEstudiante().toLowerCase().contains(usuario.getNombre().toLowerCase())
                                                                        ||
                                                                        usuario.getNombre().toLowerCase().contains(p.getEstudiante().toLowerCase())))
                                                                        {
                                                                        misPostulaciones.add(p);
                                                                        }
                                                                        }

                                                                        String entrevistasEstudianteJson = "[]";
                                                                        try { if (idEstudiante > 0)
                                                                        entrevistasEstudianteJson =
                                                                        BackendCliente.consultarEntrevistasEstudiante(idEstudiante);
                                                                        } catch (Exception ignored) { }
                                                                        java.util.List<String> entrevistasEstudiante =
                                                                            entrevistasJson(entrevistasEstudianteJson);
                                                                            String exito =
                                                                            request.getParameter("exito");
                                                                            String error =
                                                                            request.getParameter("error");
                                                                            %>
                                                                            <!DOCTYPE html>
                                                                            <html lang="es" data-bs-theme="light">

                                                                            <head>
                                                                                <meta charset="UTF-8">
                                                                                <meta name="viewport"
                                                                                    content="width=device-width, initial-scale=1.0">
                                                                                <title>Panel del Estudiante | ProPath
                                                                                </title>
                                                                                <link rel="preconnect"
                                                                                    href="https://fonts.googleapis.com">
                                                                                <link rel="preconnect"
                                                                                    href="https://fonts.gstatic.com"
                                                                                    crossorigin>
                                                                                <link
                                                                                    href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
                                                                                    rel="stylesheet">
                                                                                <link
                                                                                    href="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/css/bootstrap.min.css"
                                                                                    rel="stylesheet">
                                                                                <link rel="stylesheet"
                                                                                    href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
                                                                                <link rel="stylesheet"
                                                                                    href="${pageContext.request.contextPath}/css/estilos.css">
                                                                            </head>

                                                                            <body class="bg-light">
                                                                                <nav
                                                                                    class="navbar navbar-expand-lg bg-white border-bottom sticky-top shadow-sm py-2">
                                                                                    <div class="container">
                                                                                        <a class="navbar-brand d-flex align-items-center"
                                                                                            href="${pageContext.request.contextPath}/estudiante/dashboard.jsp">
                                                                                            <img src="${pageContext.request.contextPath}/img/logo.png"
                                                                                                alt="ProPath"
                                                                                                height="38">
                                                                                        </a>
                                                                                        <div
                                                                                            class="d-flex align-items-center gap-3 ms-auto">
                                                                                            <span
                                                                                                class="badge bg-primary-subtle text-primary border border-primary-subtle px-3 py-2 rounded-pill small">
                                                                                                <i
                                                                                                    class="bi bi-mortarboard-fill me-1"></i>
                                                                                                <%= usuario.getNombre()
                                                                                                    %> (Estudiante)
                                                                                            </span>
                                                                                            <a href="${pageContext.request.contextPath}/logout"
                                                                                                class="btn btn-sm btn-outline-danger rounded-pill px-3">
                                                                                                <i
                                                                                                    class="bi bi-box-arrow-right me-1"></i>
                                                                                                Cerrar sesión
                                                                                            </a>
                                                                                        </div>
                                                                                    </div>
                                                                                </nav>

                                                                                <div class="container py-4">
                                                                                    <% if (exito !=null &&
                                                                                        !exito.isEmpty()) { String
                                                                                        msgExito="¡Operación completada exitosamente!"
                                                                                        ; if
                                                                                        ("cv_guardado".equals(exito)) {
                                                                                        msgExito="¡Currículum Vitae subido exitosamente en formato PDF!"
                                                                                        ; } else if
                                                                                        ("cv_reemplazado".equals(exito))
                                                                                        {
                                                                                        msgExito="¡Currículum Vitae reemplazado exitosamente!"
                                                                                        ; } else if
                                                                                        ("cv_eliminado".equals(exito)) {
                                                                                        msgExito="¡Currículum Vitae eliminado correctamente!"
                                                                                        ; } else if
                                                                                        ("postulacion".equals(exito)
                                                                                        || "1" .equals(exito) || "true"
                                                                                        .equals(exito)) {
                                                                                        msgExito="¡Postulación enviada exitosamente! Puedes darle seguimiento aquí."
                                                                                        ; } else if
                                                                                        ("postulacion_editada".equals(exito))
                                                                                        {
                                                                                        msgExito="¡Respuestas de la postulación actualizadas correctamente!"
                                                                                        ; } %>
                                                                                        <div class="alert alert-success alert-dismissible fade show rounded-3 mb-4"
                                                                                            role="alert">
                                                                                            <i
                                                                                                class="bi bi-check-circle-fill me-2"></i>
                                                                                            <%= msgExito %>
                                                                                                <button type="button"
                                                                                                    class="btn-close"
                                                                                                    data-bs-dismiss="alert"></button>
                                                                                        </div>
                                                                                        <% } %>

                                                                                            <% if (error !=null &&
                                                                                                !error.isEmpty()) { %>
                                                                                                <div class="alert alert-danger alert-dismissible fade show rounded-3 mb-4"
                                                                                                    role="alert">
                                                                                                    <i
                                                                                                        class="bi bi-exclamation-triangle-fill me-2"></i>
                                                                                                    <%= error %>
                                                                                                        <button
                                                                                                            type="button"
                                                                                                            class="btn-close"
                                                                                                            data-bs-dismiss="alert"></button>
                                                                                                </div>
                                                                                                <% } %>

                                                                                                    <div
                                                                                                        class="row g-4 mb-4">
                                                                                                        <div
                                                                                                            class="col-12 col-md-6 col-xl-4">
                                                                                                            <div
                                                                                                                class="stat-card stat-card-blue bg-white p-4 rounded-4 border shadow-sm">
                                                                                                                <div
                                                                                                                    class="d-flex justify-content-between align-items-start">
                                                                                                                    <div>
                                                                                                                        <div
                                                                                                                            class="stat-value fs-2 fw-bold text-dark">
                                                                                                                            <%= todasOfertas.size()
                                                                                                                                %>
                                                                                                                        </div>
                                                                                                                        <h3
                                                                                                                            class="stat-label small text-secondary mb-0">
                                                                                                                            Ofertas
                                                                                                                            de
                                                                                                                            Prácticas
                                                                                                                        </h3>
                                                                                                                    </div>
                                                                                                                    <div
                                                                                                                        class="stat-icon-wrapper rounded-circle p-3 bg-primary-subtle text-primary">
                                                                                                                        <i
                                                                                                                            class="bi bi-briefcase-fill fs-4"></i>
                                                                                                                    </div>
                                                                                                                </div>
                                                                                                            </div>
                                                                                                        </div>
                                                                                                        <div
                                                                                                            class="col-12 col-md-6 col-xl-4">
                                                                                                            <div
                                                                                                                class="stat-card stat-card-turquoise bg-white p-4 rounded-4 border shadow-sm">
                                                                                                                <div
                                                                                                                    class="d-flex justify-content-between align-items-start">
                                                                                                                    <div>
                                                                                                                        <div
                                                                                                                            class="stat-value fs-2 fw-bold text-dark">
                                                                                                                            <%= misPostulaciones.size()
                                                                                                                                %>
                                                                                                                        </div>
                                                                                                                        <h3
                                                                                                                            class="stat-label small text-secondary mb-0">
                                                                                                                            Mis
                                                                                                                            Postulaciones
                                                                                                                        </h3>
                                                                                                                    </div>
                                                                                                                    <div
                                                                                                                        class="stat-icon-wrapper rounded-circle p-3 bg-info-subtle text-info">
                                                                                                                        <i
                                                                                                                            class="bi bi-send-check-fill fs-4"></i>
                                                                                                                    </div>
                                                                                                                </div>
                                                                                                            </div>
                                                                                                        </div>
                                                                                                        <div
                                                                                                            class="col-12 col-md-12 col-xl-4">
                                                                                                            <div
                                                                                                                class="bg-white p-4 rounded-4 border shadow-sm h-100 d-flex flex-column justify-content-between">
                                                                                                                <div>
                                                                                                                    <div
                                                                                                                        class="d-flex justify-content-between align-items-center mb-2">
                                                                                                                        <span
                                                                                                                            class="small fw-bold text-muted text-uppercase tracking-wider">Perfil
                                                                                                                            Académico</span>
                                                                                                                        <span
                                                                                                                            class="badge bg-success-subtle text-success border border-success-subtle rounded-pill">
                                                                                                                            <%= usuario.getEstado()
                                                                                                                                %>
                                                                                                                        </span>
                                                                                                                    </div>
                                                                                                                    <div
                                                                                                                        class="fw-bold text-dark fs-5">
                                                                                                                        <%= miEstudiante
                                                                                                                            !=null
                                                                                                                            ?
                                                                                                                            miEstudiante.getNombreCompleto()
                                                                                                                            :
                                                                                                                            usuario.getNombre()
                                                                                                                            %>
                                                                                                                    </div>
                                                                                                                    <div
                                                                                                                        class="small text-muted mb-2">
                                                                                                                        <i
                                                                                                                            class="bi bi-envelope me-1"></i>
                                                                                                                        <%= usuario.getCorreo()
                                                                                                                            %>
                                                                                                                    </div>
                                                                                                                    <div
                                                                                                                        class="small text-secondary mb-1">
                                                                                                                        <i
                                                                                                                            class="bi bi-bank me-1 text-primary"></i>
                                                                                                                        <strong>Univ:</strong>
                                                                                                                        <%= (miEstudiante
                                                                                                                            !=null
                                                                                                                            &&
                                                                                                                            miEstudiante.getUniversidad()
                                                                                                                            !=null
                                                                                                                            &&
                                                                                                                            !miEstudiante.getUniversidad().isEmpty())
                                                                                                                            ?
                                                                                                                            miEstudiante.getUniversidad()
                                                                                                                            : "No especificada"
                                                                                                                            %>
                                                                                                                    </div>
                                                                                                                    <div
                                                                                                                        class="small text-secondary mb-1">
                                                                                                                        <i
                                                                                                                            class="bi bi-mortarboard me-1 text-primary"></i>
                                                                                                                        <strong>Carrera:</strong>
                                                                                                                        <%= (miEstudiante
                                                                                                                            !=null
                                                                                                                            &&
                                                                                                                            miEstudiante.getCarrera()
                                                                                                                            !=null
                                                                                                                            &&
                                                                                                                            !miEstudiante.getCarrera().isEmpty())
                                                                                                                            ?
                                                                                                                            miEstudiante.getCarrera()
                                                                                                                            : "No especificada"
                                                                                                                            %>
                                                                                                                    </div>
                                                                                                                    <div
                                                                                                                        class="small text-secondary">
                                                                                                                        <i
                                                                                                                            class="bi bi-calendar3 me-1 text-primary"></i>
                                                                                                                        <strong>Ciclo:</strong>
                                                                                                                        <%= (miEstudiante
                                                                                                                            !=null
                                                                                                                            &&
                                                                                                                            miEstudiante.getCiclo()>
                                                                                                                            0)
                                                                                                                            ?
                                                                                                                            miEstudiante.getCiclo()
                                                                                                                            :
                                                                                                                            "1"
                                                                                                                            %>
                                                                                                                            &bull;
                                                                                                                            <i
                                                                                                                                class="bi bi-telephone ms-1 me-1 text-primary"></i>
                                                                                                                            <%= (miEstudiante
                                                                                                                                !=null
                                                                                                                                &&
                                                                                                                                miEstudiante.getTelefono()
                                                                                                                                !=null
                                                                                                                                &&
                                                                                                                                !miEstudiante.getTelefono().isEmpty())
                                                                                                                                ?
                                                                                                                                miEstudiante.getTelefono()
                                                                                                                                : "—"
                                                                                                                                %>
                                                                                                                    </div>
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    class="pt-3 border-top mt-3">
                                                                                                                    <button
                                                                                                                        type="button"
                                                                                                                        class="btn btn-sm btn-outline-primary w-100 rounded-pill fw-semibold"
                                                                                                                        data-bs-toggle="modal"
                                                                                                                        data-bs-target="#modalEditarPerfilEstudiante">
                                                                                                                        <i
                                                                                                                            class="bi bi-pencil-square me-1"></i>
                                                                                                                        Editar
                                                                                                                        Mi
                                                                                                                        Perfil
                                                                                                                    </button>
                                                                                                                </div>
                                                                                                            </div>
                                                                                                        </div>
                                                                                                    </div>

                                                                                                    <div
                                                                                                        class="row g-4">
                                                                                                        <div
                                                                                                            class="col-12 col-lg-7">
                                                                                                            <div
                                                                                                                class="bg-white p-4 rounded-4 border shadow-sm mb-4">
                                                                                                                <div
                                                                                                                    class="d-flex justify-content-between align-items-center mb-3">
                                                                                                                    <h2
                                                                                                                        class="h5 fw-bold text-dark mb-0">
                                                                                                                        <i
                                                                                                                            class="bi bi-briefcase me-2 text-primary"></i>Convocatorias
                                                                                                                        de
                                                                                                                        Prácticas
                                                                                                                    </h2>
                                                                                                                    <span
                                                                                                                        class="badge bg-light text-secondary border">
                                                                                                                        <%= todasOfertas.size()
                                                                                                                            %>
                                                                                                                            disponibles
                                                                                                                    </span>
                                                                                                                </div>

                                                                                                                <% if
                                                                                                                    (todasOfertas.isEmpty())
                                                                                                                    { %>
                                                                                                                    <div
                                                                                                                        class="text-center py-5 text-muted">
                                                                                                                        <i
                                                                                                                            class="bi bi-inbox fs-1 d-block mb-2"></i>
                                                                                                                        No
                                                                                                                        hay
                                                                                                                        ofertas
                                                                                                                        de
                                                                                                                        prácticas
                                                                                                                        disponibles
                                                                                                                        en
                                                                                                                        este
                                                                                                                        momento.
                                                                                                                    </div>
                                                                                                                    <% } else
                                                                                                                        {
                                                                                                                        %>
                                                                                                                        <div
                                                                                                                            class="d-flex flex-column gap-3">
                                                                                                                            <% for
                                                                                                                                (OfertaPractica
                                                                                                                                ofr
                                                                                                                                :
                                                                                                                                todasOfertas)
                                                                                                                                {
                                                                                                                                boolean
                                                                                                                                yaPostulado=false;
                                                                                                                                for
                                                                                                                                (Postulacion
                                                                                                                                p
                                                                                                                                :
                                                                                                                                misPostulaciones)
                                                                                                                                {
                                                                                                                                if
                                                                                                                                (p.getOferta()
                                                                                                                                !=null
                                                                                                                                &&
                                                                                                                                p.getOferta().equalsIgnoreCase(ofr.getTitulo()))
                                                                                                                                {
                                                                                                                                yaPostulado=true;
                                                                                                                                break;
                                                                                                                                }
                                                                                                                                }
                                                                                                                                %>
                                                                                                                                <div
                                                                                                                                    class="p-3 border rounded-3 bg-light bg-opacity-50">
                                                                                                                                    <div
                                                                                                                                        class="d-flex justify-content-between align-items-start mb-2">
                                                                                                                                        <div>
                                                                                                                                            <span
                                                                                                                                                class="badge bg-primary-subtle text-primary mb-1">
                                                                                                                                                <%= ofr.getCarrera()
                                                                                                                                                    %>
                                                                                                                                            </span>
                                                                                                                                            <h3
                                                                                                                                                class="h6 fw-bold text-dark mb-1">
                                                                                                                                                <%= ofr.getTitulo()
                                                                                                                                                    %>
                                                                                                                                            </h3>
                                                                                                                                            <div
                                                                                                                                                class="small text-muted">
                                                                                                                                                <i
                                                                                                                                                    class="bi bi-building me-1"></i>
                                                                                                                                                <%= ofr.getEmpresa()
                                                                                                                                                    !=null
                                                                                                                                                    ?
                                                                                                                                                    ofr.getEmpresa()
                                                                                                                                                    : "Empresa"
                                                                                                                                                    %>
                                                                                                                                            </div>
                                                                                                                                        </div>
                                                                                                                                        <span
                                                                                                                                            class="badge-status badge-activo">
                                                                                                                                            <%= ofr.getEstado()
                                                                                                                                                %>
                                                                                                                                        </span>
                                                                                                                                    </div>
                                                                                                                                    <div
                                                                                                                                        class="d-flex justify-content-between align-items-center pt-2 border-top mt-2">
                                                                                                                                        <span
                                                                                                                                            class="small text-secondary">
                                                                                                                                            <i
                                                                                                                                                class="bi bi-laptop me-1"></i>
                                                                                                                                            <%= ofr.getModalidad()
                                                                                                                                                %>
                                                                                                                                                &bull;
                                                                                                                                                <%= ofr.getVacantes()
                                                                                                                                                    %>
                                                                                                                                                    vacantes
                                                                                                                                                    &bull;
                                                                                                                                                    Publicado:
                                                                                                                                                    <%= ofr.getFechaPublicacion()
                                                                                                                                                        %>
                                                                                                                                        </span>
                                                                                                                                        <% if
                                                                                                                                            (yaPostulado)
                                                                                                                                            {
                                                                                                                                            %>
                                                                                                                                            <span
                                                                                                                                                class="badge bg-secondary-subtle text-secondary py-2 px-3 rounded-pill">
                                                                                                                                                <i
                                                                                                                                                    class="bi bi-check-all me-1"></i>
                                                                                                                                                Ya
                                                                                                                                                postulado
                                                                                                                                            </span>
                                                                                                                                            <% } else
                                                                                                                                                if
                                                                                                                                                (!"ACTIVA".equalsIgnoreCase(ofr.getEstado()))
                                                                                                                                                {
                                                                                                                                                %>
                                                                                                                                                <span
                                                                                                                                                    class="badge bg-warning-subtle text-warning border rounded-pill py-2 px-3">
                                                                                                                                                    <i
                                                                                                                                                        class="bi bi-lock-fill me-1"></i>
                                                                                                                                                    Postulación
                                                                                                                                                    bloqueada
                                                                                                                                                </span>
                                                                                                                                                <% } else
                                                                                                                                                    {
                                                                                                                                                    %>
                                                                                                                                                    <button
                                                                                                                                                        type="button"
                                                                                                                                                        class="btn btn-sm btn-primary rounded-pill px-3"
                                                                                                                                                        data-bs-toggle="modal"
                                                                                                                                                        data-bs-target="#modalPostular"
                                                                                                                                                        data-id-oferta="<%= ofr.getId() %>"
                                                                                                                                                        data-titulo="<%= ofr.getTitulo() != null ? ofr.getTitulo() : "" %>"
                                                                                                                                                        onclick="seleccionarOfertaPostular(this)">
                                                                                                                                                        <i
                                                                                                                                                            class="bi bi-send me-1"></i>
                                                                                                                                                        Postular
                                                                                                                                                    </button>
                                                                                                                                                    <% }
                                                                                                                                                        %>
                                                                                                                                    </div>
                                                                                                                                </div>
                                                                                                                                <% }
                                                                                                                                    %>
                                                                                                                        </div>
                                                                                                                        <% }
                                                                                                                            %>
                                                                                                            </div>
                                                                                                        </div>

                                                                                                        <div
                                                                                                            class="col-12 col-lg-5">
                                                                                                            <div
                                                                                                                class="bg-white p-4 rounded-4 border shadow-sm mb-4">
                                                                                                                <div
                                                                                                                    class="d-flex justify-content-between align-items-center mb-3">
                                                                                                                    <h2
                                                                                                                        class="h5 fw-bold text-dark mb-0">
                                                                                                                        <i
                                                                                                                            class="bi bi-folder2-open me-2 text-primary"></i>Mis
                                                                                                                        Documentos
                                                                                                                    </h2>
                                                                                                                    <span
                                                                                                                        class="badge bg-light text-secondary border">CRUD
                                                                                                                        Documentos</span>
                                                                                                                </div>

                                                                                                                <div
                                                                                                                    class="card border rounded-3 p-3 bg-light bg-opacity-25">
                                                                                                                    <div
                                                                                                                        class="d-flex align-items-start gap-3">
                                                                                                                        <div
                                                                                                                            class="rounded-3 p-3 bg-danger-subtle text-danger d-flex align-items-center justify-content-center">
                                                                                                                            <i
                                                                                                                                class="bi bi-file-earmark-pdf-fill fs-2"></i>
                                                                                                                        </div>
                                                                                                                        <div
                                                                                                                            class="flex-grow-1">
                                                                                                                            <h3
                                                                                                                                class="h6 fw-bold text-dark mb-1">
                                                                                                                                Currículum
                                                                                                                                Vitae
                                                                                                                                (CV)
                                                                                                                            </h3>
                                                                                                                            <% if
                                                                                                                                (miCv==null)
                                                                                                                                {
                                                                                                                                %>
                                                                                                                                <p
                                                                                                                                    class="text-muted small mb-3">
                                                                                                                                    Aún
                                                                                                                                    no
                                                                                                                                    has
                                                                                                                                    subido
                                                                                                                                    tu
                                                                                                                                    currículum.
                                                                                                                                </p>
                                                                                                                                <button
                                                                                                                                    type="button"
                                                                                                                                    class="btn btn-sm btn-primary rounded-pill px-3"
                                                                                                                                    data-bs-toggle="modal"
                                                                                                                                    data-bs-target="#modalSubirCv">
                                                                                                                                    <i
                                                                                                                                        class="bi bi-upload me-1"></i>
                                                                                                                                    Subir
                                                                                                                                    CV
                                                                                                                                </button>
                                                                                                                                <% } else
                                                                                                                                    {
                                                                                                                                    %>
                                                                                                                                    <div
                                                                                                                                        class="small fw-semibold text-dark text-break mb-1">
                                                                                                                                        <i
                                                                                                                                            class="bi bi-file-text me-1 text-danger"></i>
                                                                                                                                        <%= miCv.getNombreArchivo()
                                                                                                                                            %>
                                                                                                                                    </div>
                                                                                                                                    <div
                                                                                                                                        class="small text-muted mb-3">
                                                                                                                                        <span><i
                                                                                                                                                class="bi bi-calendar3 me-1"></i>
                                                                                                                                            <%= miCv.getFechaSubida()
                                                                                                                                                %>
                                                                                                                                        </span>
                                                                                                                                        <% if
                                                                                                                                            (miCv.getTamanioFormateado()
                                                                                                                                            !=null
                                                                                                                                            &&
                                                                                                                                            !miCv.getTamanioFormateado().isEmpty())
                                                                                                                                            {
                                                                                                                                            %>
                                                                                                                                            <span
                                                                                                                                                class="ms-2">&bull;
                                                                                                                                                <i
                                                                                                                                                    class="bi bi-hdd me-1"></i>
                                                                                                                                                <%= miCv.getTamanioFormateado()
                                                                                                                                                    %>
                                                                                                                                            </span>
                                                                                                                                            <% }
                                                                                                                                                %>
                                                                                                                                    </div>
                                                                                                                                    <div
                                                                                                                                        class="d-flex flex-wrap gap-2">
                                                                                                                                        <a href="${pageContext.request.contextPath}/estudiante/documentos?accion=ver"
                                                                                                                                            target="_blank"
                                                                                                                                            class="btn btn-sm btn-outline-primary rounded-pill px-3">
                                                                                                                                            <i
                                                                                                                                                class="bi bi-eye me-1"></i>
                                                                                                                                            Ver
                                                                                                                                            PDF
                                                                                                                                        </a>
                                                                                                                                        <a href="${pageContext.request.contextPath}/estudiante/documentos?accion=descargar"
                                                                                                                                            class="btn btn-sm btn-outline-secondary rounded-pill px-3">
                                                                                                                                            <i
                                                                                                                                                class="bi bi-download me-1"></i>
                                                                                                                                            Descargar
                                                                                                                                        </a>
                                                                                                                                        <button
                                                                                                                                            type="button"
                                                                                                                                            class="btn btn-sm btn-outline-warning rounded-pill px-3 text-dark"
                                                                                                                                            data-bs-toggle="modal"
                                                                                                                                            data-bs-target="#modalReemplazarCv">
                                                                                                                                            <i
                                                                                                                                                class="bi bi-arrow-repeat me-1"></i>
                                                                                                                                            Reemplazar
                                                                                                                                        </button>
                                                                                                                                        <button
                                                                                                                                            type="button"
                                                                                                                                            class="btn btn-sm btn-outline-danger rounded-pill px-3"
                                                                                                                                            onclick="confirmarEliminarCv()">
                                                                                                                                            <i
                                                                                                                                                class="bi bi-trash me-1"></i>
                                                                                                                                            Eliminar
                                                                                                                                        </button>
                                                                                                                                    </div>
                                                                                                                                    <% }
                                                                                                                                        %>
                                                                                                                        </div>
                                                                                                                    </div>
                                                                                                                </div>
                                                                                                            </div>

                                                                                                            <div
                                                                                                                class="bg-white p-4 rounded-4 border shadow-sm">
                                                                                                                <div
                                                                                                                    class="d-flex justify-content-between align-items-center mb-3">
                                                                                                                    <h2
                                                                                                                        class="h5 fw-bold text-dark mb-0">
                                                                                                                        <i
                                                                                                                            class="bi bi-clock-history me-2 text-info"></i>Mis
                                                                                                                        Postulaciones
                                                                                                                    </h2>
                                                                                                                    <span
                                                                                                                        class="badge bg-light text-secondary border">
                                                                                                                        <%= misPostulaciones.size()
                                                                                                                            %>
                                                                                                                            enviadas
                                                                                                                    </span>
                                                                                                                </div>

                                                                                                                <% if
                                                                                                                    (misPostulaciones.isEmpty())
                                                                                                                    { %>
                                                                                                                    <div
                                                                                                                        class="text-center py-5 text-muted">
                                                                                                                        <i
                                                                                                                            class="bi bi-file-earmark-text fs-1 d-block mb-2"></i>
                                                                                                                        Aún
                                                                                                                        no
                                                                                                                        te
                                                                                                                        has
                                                                                                                        postulado
                                                                                                                        a
                                                                                                                        ninguna
                                                                                                                        vacante.
                                                                                                                        Explora
                                                                                                                        las
                                                                                                                        ofertas
                                                                                                                        y
                                                                                                                        postula
                                                                                                                        con
                                                                                                                        un
                                                                                                                        clic.
                                                                                                                    </div>
                                                                                                                    <% } else
                                                                                                                        {
                                                                                                                        %>
                                                                                                                        <div
                                                                                                                            class="d-flex flex-column gap-3">
                                                                                                                            <% for
                                                                                                                                (Postulacion
                                                                                                                                post
                                                                                                                                :
                                                                                                                                misPostulaciones)
                                                                                                                                {
                                                                                                                                %>
                                                                                                                                <div
                                                                                                                                    class="p-3 border rounded-3 bg-light bg-opacity-25">
                                                                                                                                    <div
                                                                                                                                        class="d-flex justify-content-between align-items-start mb-1">
                                                                                                                                        <h3
                                                                                                                                            class="h6 fw-bold text-dark mb-0">
                                                                                                                                            <%= post.getOferta()
                                                                                                                                                %>
                                                                                                                                        </h3>
                                                                                                                                        <span
                                                                                                                                            class="badge bg-info-subtle text-info border border-info-subtle rounded-pill small">
                                                                                                                                            <%= "NO_SELECCIONADO"
                                                                                                                                                .equalsIgnoreCase(post.getEstado())
                                                                                                                                                ? "RECHAZADO"
                                                                                                                                                :
                                                                                                                                                post.getEstado()
                                                                                                                                                %>
                                                                                                                                        </span>
                                                                                                                                    </div>
                                                                                                                                    <div
                                                                                                                                        class="small text-muted mb-2">
                                                                                                                                        <i
                                                                                                                                            class="bi bi-building me-1"></i>
                                                                                                                                        <%= post.getEmpresa()
                                                                                                                                            %>
                                                                                                                                    </div>
                                                                                                                                    <div
                                                                                                                                        class="small text-secondary pt-2 border-top d-flex justify-content-between align-items-center">
                                                                                                                                        <span>Fecha:
                                                                                                                                            <%= post.getFechaPostulacion()
                                                                                                                                                %>
                                                                                                                                        </span>

                                                                                                                                        <% if
                                                                                                                                            ("POSTULADO".equalsIgnoreCase(post.getEstado()))
                                                                                                                                            {
                                                                                                                                            %>
                                                                                                                                            <button
                                                                                                                                                type="button"
                                                                                                                                                class="btn btn-sm btn-outline-primary rounded-pill"
                                                                                                                                                data-bs-toggle="modal"
                                                                                                                                                data-bs-target="#modalPostular"
                                                                                                                                                data-id-postulacion="<%= post.getId() %>"
                                                                                                                                                onclick="cargarRespuestasPostulacion(this)"
                                                                                                                                                data-titulo="<%= post.getOferta() != null ? post.getOferta() : "" %>">
                                                                                                                                                <i
                                                                                                                                                    class="bi bi-pencil-square me-1"></i>Editar
                                                                                                                                            </button>
                                                                                                                                            <% }
                                                                                                                                                %>
                                                                                                                                                <% if
                                                                                                                                                    ("POSTULADO".equalsIgnoreCase(post.getEstado()))
                                                                                                                                                    {
                                                                                                                                                    %>
                                                                                                                                                    <form
                                                                                                                                                        action="${pageContext.request.contextPath}/estudiante/postular"
                                                                                                                                                        method="POST"
                                                                                                                                                        class="m-0"
                                                                                                                                                        onsubmit="event.preventDefault(); mostrarModalConfirmacion('Retirar postulación', '¿Seguro que deseas retirar esta postulación?', () => this.submit());">
                                                                                                                                                        <input
                                                                                                                                                            type="hidden"
                                                                                                                                                            name="accion"
                                                                                                                                                            value="retirar">
                                                                                                                                                        <input
                                                                                                                                                            type="hidden"
                                                                                                                                                            name="idPostulacion"
                                                                                                                                                            value="<%= post.getId() %>">
                                                                                                                                                        <button
                                                                                                                                                            type="submit"
                                                                                                                                                            class="btn btn-sm btn-outline-danger rounded-pill">
                                                                                                                                                            <i
                                                                                                                                                                class="bi bi-x-circle me-1"></i>Retirar
                                                                                                                                                        </button>
                                                                                                                                                    </form>
                                                                                                                                                    <% }
                                                                                                                                                        %>

                                                                                                                                                        <% if
                                                                                                                                                            ("NO_SELECCIONADO".equalsIgnoreCase(post.getEstado()))
                                                                                                                                                            {
                                                                                                                                                            %>
                                                                                                                                                            <button
                                                                                                                                                                type="button"
                                                                                                                                                                class="btn btn-sm btn-outline-primary rounded-pill"
                                                                                                                                                                data-bs-toggle="modal"
                                                                                                                                                                data-bs-target="#modalMotivoRechazo"
                                                                                                                                                                data-motivo="<%= escaparHtml(post.getMotivoRechazo()) %>"
                                                                                                                                                                onclick="mostrarMotivoRechazo(this)">
                                                                                                                                                                <i
                                                                                                                                                                    class="bi bi-eye me-1"></i>Ver
                                                                                                                                                                motivo
                                                                                                                                                            </button>

                                                                                                                                                            <form
                                                                                                                                                                action="${pageContext.request.contextPath}/estudiante/postular"
                                                                                                                                                                method="POST"
                                                                                                                                                                class="m-0"
                                                                                                                                                                onsubmit="event.preventDefault(); mostrarModalConfirmacion('Eliminar postulación', '¿Seguro que deseas eliminar definitivamente esta postulación?', () => this.submit());">
                                                                                                                                                                <input
                                                                                                                                                                    type="hidden"
                                                                                                                                                                    name="accion"
                                                                                                                                                                    value="eliminar">
                                                                                                                                                                <input
                                                                                                                                                                    type="hidden"
                                                                                                                                                                    name="idPostulacion"
                                                                                                                                                                    value="<%= post.getId() %>">
                                                                                                                                                                <button
                                                                                                                                                                    type="submit"
                                                                                                                                                                    class="btn btn-sm btn-outline-danger rounded-pill">
                                                                                                                                                                    <i
                                                                                                                                                                        class="bi bi-trash me-1"></i>Eliminar
                                                                                                                                                                </button>
                                                                                                                                                            </form>
                                                                                                                                                            <% }
                                                                                                                                                                %>
                                                                                                                                                                <% if
                                                                                                                                                                    ("SELECCIONADO".equalsIgnoreCase(post.getEstado()))
                                                                                                                                                                    {
                                                                                                                                                                    boolean
                                                                                                                                                                    tieneEntrevista=false;
                                                                                                                                                                    for
                                                                                                                                                                    (String
                                                                                                                                                                    item
                                                                                                                                                                    :
                                                                                                                                                                    entrevistasEstudiante)
                                                                                                                                                                    {
                                                                                                                                                                    if
                                                                                                                                                                    (!String.valueOf(post.getId()).equals(entrevistaCampo(item, "idPostulacion"
                                                                                                                                                                    )))
                                                                                                                                                                    continue;
                                                                                                                                                                    tieneEntrevista=true;
                                                                                                                                                                    String
                                                                                                                                                                    fechaEnt=entrevistaCampo(item, "fecha"
                                                                                                                                                                    );
                                                                                                                                                                    String
                                                                                                                                                                    horaEnt=entrevistaCampo(item, "hora"
                                                                                                                                                                    );
                                                                                                                                                                    String
                                                                                                                                                                    modalidadEnt=entrevistaCampo(item, "modalidad"
                                                                                                                                                                    );
                                                                                                                                                                    String
                                                                                                                                                                    lugarEnt=entrevistaCampo(item, "enlaceLugar"
                                                                                                                                                                    );
                                                                                                                                                                    String
                                                                                                                                                                    estadoEnt=entrevistaCampo(item, "estado"
                                                                                                                                                                    );
                                                                                                                                                                    %>
                                                                                                                                                                    <div
                                                                                                                                                                        class="border rounded-3 p-3 mt-2 bg-primary-subtle w-100">
                                                                                                                                                                        <div
                                                                                                                                                                            class="fw-bold small mb-2">
                                                                                                                                                                            <i
                                                                                                                                                                                class="bi bi-calendar-check me-1"></i>
                                                                                                                                                                            Entrevista
                                                                                                                                                                            <%= escaparHtml(estadoEnt)
                                                                                                                                                                                %>
                                                                                                                                                                        </div>
                                                                                                                                                                        <div
                                                                                                                                                                            class="small">
                                                                                                                                                                            Fecha:
                                                                                                                                                                            <%= escaparHtml(fechaEnt)
                                                                                                                                                                                %>
                                                                                                                                                                                ·
                                                                                                                                                                                Hora:
                                                                                                                                                                                <%= escaparHtml(horaEnt)
                                                                                                                                                                                    %>
                                                                                                                                                                        </div>
                                                                                                                                                                        <div
                                                                                                                                                                            class="small">
                                                                                                                                                                            Modalidad:
                                                                                                                                                                            <%= escaparHtml(modalidadEnt)
                                                                                                                                                                                %>
                                                                                                                                                                        </div>
                                                                                                                                                                        <% if
                                                                                                                                                                            ("VIRTUAL".equalsIgnoreCase(modalidadEnt)
                                                                                                                                                                            &&
                                                                                                                                                                            lugarEnt.startsWith("https://"))
                                                                                                                                                                            {
                                                                                                                                                                            %>
                                                                                                                                                                            <a class="btn btn-sm btn-primary rounded-pill mt-2"
                                                                                                                                                                                href="<%= escaparHtml(lugarEnt) %>"
                                                                                                                                                                                target="_blank"
                                                                                                                                                                                rel="noopener noreferrer"><i
                                                                                                                                                                                    class="bi bi-camera-video me-1"></i>Entrar
                                                                                                                                                                                a
                                                                                                                                                                                la
                                                                                                                                                                                entrevista</a>
                                                                                                                                                                            <% } else
                                                                                                                                                                                {
                                                                                                                                                                                %>
                                                                                                                                                                                <div
                                                                                                                                                                                    class="small">
                                                                                                                                                                                    Lugar:
                                                                                                                                                                                    <%= escaparHtml(lugarEnt)
                                                                                                                                                                                        %>
                                                                                                                                                                                </div>
                                                                                                                                                                                <% }
                                                                                                                                                                                    %>
                                                                                                                                                                    </div>
                                                                                                                                                                    <% } if
                                                                                                                                                                        (!tieneEntrevista)
                                                                                                                                                                        {
                                                                                                                                                                        %>
                                                                                                                                                                        <div
                                                                                                                                                                            class="small text-primary mt-2">
                                                                                                                                                                            Seleccionado.
                                                                                                                                                                            La
                                                                                                                                                                            empresa
                                                                                                                                                                            aún
                                                                                                                                                                            no
                                                                                                                                                                            ha
                                                                                                                                                                            programado
                                                                                                                                                                            la
                                                                                                                                                                            entrevista.
                                                                                                                                                                        </div>
                                                                                                                                                                        <% } }
                                                                                                                                                                            %>

                                                                                                                                    </div>
                                                                                                                                </div>
                                                                                                                                <% }
                                                                                                                                    %>
                                                                                                                        </div>
                                                                                                                        <% }
                                                                                                                            %>
                                                                                                            </div>
                                                                                                        </div>
                                                                                                    </div>
                                                                                </div>
                                                                                <div class="modal fade"
                                                                                    id="modalPostular" tabindex="-1">
                                                                                    <div
                                                                                        class="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
                                                                                        <div class="modal-content">
                                                                                            <form id="formPostular"
                                                                                                action="${pageContext.request.contextPath}/estudiante/postular"
                                                                                                method="POST">

                                                                                                <div
                                                                                                    class="modal-header">
                                                                                                    <h5
                                                                                                        class="modal-title">
                                                                                                        Postulación a
                                                                                                        prácticas</h5>
                                                                                                    <button
                                                                                                        type="button"
                                                                                                        class="btn-close"
                                                                                                        data-bs-dismiss="modal"></button>
                                                                                                </div>

                                                                                                <div class="modal-body">
                                                                                                    <input type="hidden"
                                                                                                        name="idOferta"
                                                                                                        id="postularIdOferta">
                                                                                                    <input type="hidden"
                                                                                                        name="accion"
                                                                                                        id="postularAccion"
                                                                                                        value="">
                                                                                                    <input type="hidden"
                                                                                                        name="idPostulacion"
                                                                                                        id="postularIdPostulacion">
                                                                                                    <h6 id="postularTituloOferta"
                                                                                                        class="text-primary mb-3">
                                                                                                    </h6>

                                                                                                    <div
                                                                                                        id="pasoPostular1">
                                                                                                        <h6>Paso 1:
                                                                                                            Verificar CV
                                                                                                        </h6>
                                                                                                        <% if (miCv
                                                                                                            !=null) { %>
                                                                                                            <div
                                                                                                                class="alert alert-success">
                                                                                                                CV PDF
                                                                                                                registrado:
                                                                                                                <strong>
                                                                                                                    <%= miCv.getNombreArchivo()
                                                                                                                        %>
                                                                                                                </strong>
                                                                                                            </div>
                                                                                                            <% } else {
                                                                                                                %>
                                                                                                                <div
                                                                                                                    class="alert alert-warning">
                                                                                                                    Primero
                                                                                                                    debes
                                                                                                                    subir
                                                                                                                    tu
                                                                                                                    CV
                                                                                                                    en
                                                                                                                    PDF.
                                                                                                                </div>
                                                                                                                <button
                                                                                                                    type="button"
                                                                                                                    class="btn btn-outline-primary"
                                                                                                                    onclick="abrirSubidaCv()">
                                                                                                                    Subir
                                                                                                                    CV
                                                                                                                </button>
                                                                                                                <% } %>
                                                                                                    </div>

                                                                                                    <% String[]
                                                                                                        preguntas={ "¿Por qué te interesa esta oportunidad?"
                                                                                                        , "¿Qué conocimientos puedes aportar?"
                                                                                                        , "¿Qué herramientas o tecnologías manejas?"
                                                                                                        , "¿Has desarrollado proyectos relacionados?"
                                                                                                        , "¿Qué esperas aprender?"
                                                                                                        , "¿Cuál es tu disponibilidad horaria?"
                                                                                                        , "¿Cuáles son tus principales fortalezas?"
                                                                                                        , "¿Cómo organizas tus responsabilidades?"
                                                                                                        , "¿Cómo reaccionas ante un problema inesperado?"
                                                                                                        , "¿Cómo trabajas en equipo?"
                                                                                                        , "¿Qué aspecto profesional deseas mejorar?"
                                                                                                        , "¿Cuáles son tus objetivos profesionales?"
                                                                                                        }; %>

                                                                                                        <div id="pasoPostular2"
                                                                                                            style="display:none">
                                                                                                            <h6>Paso 2:
                                                                                                                Preguntas
                                                                                                                laborales
                                                                                                            </h6>
                                                                                                            <% for (int
                                                                                                                i=0; i <
                                                                                                                6; i++)
                                                                                                                { %>
                                                                                                                <div
                                                                                                                    class="mb-3">
                                                                                                                    <label
                                                                                                                        class="form-label">
                                                                                                                        <%= i
                                                                                                                            +
                                                                                                                            1
                                                                                                                            %>
                                                                                                                            .
                                                                                                                            <%= preguntas[i]
                                                                                                                                %>
                                                                                                                    </label>
                                                                                                                    <textarea
                                                                                                                        name="respuesta<%= i + 1 %>"
                                                                                                                        class="form-control"
                                                                                                                        rows="2"
                                                                                                                        maxlength="1000"
                                                                                                                        required></textarea>
                                                                                                                </div>
                                                                                                                <% } %>
                                                                                                        </div>

                                                                                                        <div id="pasoPostular3"
                                                                                                            style="display:none">
                                                                                                            <h6>Paso 3:
                                                                                                                Perfil
                                                                                                                personal
                                                                                                                y
                                                                                                                profesional
                                                                                                            </h6>
                                                                                                            <% for (int
                                                                                                                i=6; i <
                                                                                                                12; i++)
                                                                                                                { %>
                                                                                                                <div
                                                                                                                    class="mb-3">
                                                                                                                    <label
                                                                                                                        class="form-label">
                                                                                                                        <%= i
                                                                                                                            +
                                                                                                                            1
                                                                                                                            %>
                                                                                                                            .
                                                                                                                            <%= preguntas[i]
                                                                                                                                %>
                                                                                                                    </label>
                                                                                                                    <textarea
                                                                                                                        name="respuesta<%= i + 1 %>"
                                                                                                                        class="form-control"
                                                                                                                        rows="2"
                                                                                                                        maxlength="1000"
                                                                                                                        required></textarea>
                                                                                                                </div>
                                                                                                                <% } %>
                                                                                                        </div>

                                                                                                        <div id="pasoPostular4"
                                                                                                            style="display:none">
                                                                                                            <h6>Paso 4:
                                                                                                                Confirmación
                                                                                                            </h6>
                                                                                                            <p>Revisa
                                                                                                                tus
                                                                                                                respuestas
                                                                                                                antes
                                                                                                                de
                                                                                                                enviar.
                                                                                                            </p>
                                                                                                            <div
                                                                                                                id="resumenPostulacion">
                                                                                                            </div>
                                                                                                        </div>
                                                                                                </div>

                                                                                                <div
                                                                                                    class="modal-footer">
                                                                                                    <button
                                                                                                        type="button"
                                                                                                        class="btn btn-outline-secondary"
                                                                                                        id="btnAnteriorPostular"
                                                                                                        onclick="cambiarPasoPostular(-1)"
                                                                                                        style="display:none">
                                                                                                        Anterior
                                                                                                    </button>

                                                                                                    <button
                                                                                                        type="button"
                                                                                                        class="btn btn-primary"
                                                                                                        id="btnSiguientePostular"
                                                                                                        onclick="cambiarPasoPostular(1)"
                                                                                                        <%=miCv==null
                                                                                                        ? "disabled"
                                                                                                        : "" %>>
                                                                                                        Siguiente
                                                                                                    </button>

                                                                                                    <button
                                                                                                        type="submit"
                                                                                                        class="btn btn-success"
                                                                                                        id="btnConfirmarPostular"
                                                                                                        style="display:none">
                                                                                                        Confirmar
                                                                                                        postulación
                                                                                                    </button>
                                                                                                </div>
                                                                                            </form>
                                                                                        </div>
                                                                                    </div>
                                                                                </div>
                                                                                <div class="modal fade"
                                                                                    id="modalSubirCv" tabindex="-1"
                                                                                    aria-labelledby="modalSubirCvLabel"
                                                                                    aria-hidden="true">
                                                                                    <div
                                                                                        class="modal-dialog modal-dialog-centered">
                                                                                        <div
                                                                                            class="modal-content modal-content-propath">
                                                                                            <div
                                                                                                class="modal-header modal-header-propath">
                                                                                                <h2 class="modal-title h5"
                                                                                                    id="modalSubirCvLabel">
                                                                                                    <i
                                                                                                        class="bi bi-file-earmark-arrow-up me-2 text-primary"></i>
                                                                                                    Subir Currículum
                                                                                                    Vitae (CV)
                                                                                                </h2>
                                                                                                <button type="button"
                                                                                                    class="btn-close"
                                                                                                    data-bs-dismiss="modal"
                                                                                                    aria-label="Cerrar"></button>
                                                                                            </div>
                                                                                            <form
                                                                                                action="${pageContext.request.contextPath}/estudiante/documentos"
                                                                                                method="POST"
                                                                                                enctype="multipart/form-data"
                                                                                                onsubmit="return validarArchivoPdf(this.querySelector('input[type=file]'))">
                                                                                                <input type="hidden"
                                                                                                    name="accion"
                                                                                                    value="subir">
                                                                                                <div
                                                                                                    class="modal-body modal-body-propath">
                                                                                                    <div class="mb-3">
                                                                                                        <label
                                                                                                            class="form-label small fw-semibold">Seleccionar
                                                                                                            documento
                                                                                                            PDF
                                                                                                            *</label>
                                                                                                        <input
                                                                                                            type="file"
                                                                                                            class="form-control"
                                                                                                            name="archivoPdf"
                                                                                                            accept=".pdf,application/pdf"
                                                                                                            required>
                                                                                                        <div
                                                                                                            class="form-text small text-muted">
                                                                                                            <i
                                                                                                                class="bi bi-info-circle me-1"></i>
                                                                                                            Formato PDF
                                                                                                            real. Tamaño
                                                                                                            máximo
                                                                                                            permitido: 5
                                                                                                            MB.
                                                                                                        </div>
                                                                                                    </div>
                                                                                                </div>
                                                                                                <div
                                                                                                    class="modal-footer bg-light border-0">
                                                                                                    <button
                                                                                                        type="button"
                                                                                                        class="btn btn-secondary rounded-pill px-4"
                                                                                                        data-bs-dismiss="modal">Cancelar</button>
                                                                                                    <button
                                                                                                        type="submit"
                                                                                                        class="btn btn-primary rounded-pill px-4">
                                                                                                        <i
                                                                                                            class="bi bi-upload me-1"></i>
                                                                                                        Subir CV
                                                                                                    </button>
                                                                                                </div>
                                                                                            </form>
                                                                                        </div>
                                                                                    </div>
                                                                                </div>

                                                                                <div class="modal fade"
                                                                                    id="modalReemplazarCv" tabindex="-1"
                                                                                    aria-labelledby="modalReemplazarCvLabel"
                                                                                    aria-hidden="true">
                                                                                    <div
                                                                                        class="modal-dialog modal-dialog-centered">
                                                                                        <div
                                                                                            class="modal-content modal-content-propath">
                                                                                            <div
                                                                                                class="modal-header modal-header-propath">
                                                                                                <h2 class="modal-title h5"
                                                                                                    id="modalReemplazarCvLabel">
                                                                                                    <i
                                                                                                        class="bi bi-arrow-repeat me-2 text-warning"></i>
                                                                                                    Reemplazar
                                                                                                    Currículum Vitae
                                                                                                    (CV)
                                                                                                </h2>
                                                                                                <button type="button"
                                                                                                    class="btn-close"
                                                                                                    data-bs-dismiss="modal"
                                                                                                    aria-label="Cerrar"></button>
                                                                                            </div>
                                                                                            <form
                                                                                                action="${pageContext.request.contextPath}/estudiante/documentos"
                                                                                                method="POST"
                                                                                                enctype="multipart/form-data"
                                                                                                onsubmit="return validarArchivoPdf(this.querySelector('input[type=file]'))">
                                                                                                <input type="hidden"
                                                                                                    name="accion"
                                                                                                    value="reemplazar">
                                                                                                <div
                                                                                                    class="modal-body modal-body-propath">
                                                                                                    <div
                                                                                                        class="alert alert-info py-2 small mb-3">
                                                                                                        <i
                                                                                                            class="bi bi-shield-check me-1"></i>
                                                                                                        El CV anterior
                                                                                                        no se eliminará
                                                                                                        hasta
                                                                                                        comprobar que el
                                                                                                        nuevo archivo se
                                                                                                        haya guardado
                                                                                                        correctamente.
                                                                                                    </div>
                                                                                                    <div class="mb-3">
                                                                                                        <label
                                                                                                            class="form-label small fw-semibold">Seleccionar
                                                                                                            nuevo
                                                                                                            documento
                                                                                                            PDF
                                                                                                            *</label>
                                                                                                        <input
                                                                                                            type="file"
                                                                                                            class="form-control"
                                                                                                            name="archivoPdf"
                                                                                                            accept=".pdf,application/pdf"
                                                                                                            required>
                                                                                                        <div
                                                                                                            class="form-text small text-muted">
                                                                                                            <i
                                                                                                                class="bi bi-info-circle me-1"></i>
                                                                                                            Formato PDF
                                                                                                            real. Tamaño
                                                                                                            máximo
                                                                                                            permitido: 5
                                                                                                            MB.
                                                                                                        </div>
                                                                                                    </div>
                                                                                                </div>
                                                                                                <div
                                                                                                    class="modal-footer bg-light border-0">
                                                                                                    <button
                                                                                                        type="button"
                                                                                                        class="btn btn-secondary rounded-pill px-4"
                                                                                                        data-bs-dismiss="modal">Cancelar</button>
                                                                                                    <button
                                                                                                        type="submit"
                                                                                                        class="btn btn-warning rounded-pill px-4 text-dark">
                                                                                                        <i
                                                                                                            class="bi bi-check2 me-1"></i>
                                                                                                        Reemplazar CV
                                                                                                    </button>
                                                                                                </div>
                                                                                            </form>
                                                                                        </div>
                                                                                    </div>
                                                                                </div>

                                                                                <div class="modal fade"
                                                                                    id="modalEliminarCv" tabindex="-1"
                                                                                    aria-labelledby="modalEliminarCvLabel"
                                                                                    aria-hidden="true">
                                                                                    <div
                                                                                        class="modal-dialog modal-dialog-centered">
                                                                                        <div
                                                                                            class="modal-content modal-content-propath">
                                                                                            <div
                                                                                                class="modal-header modal-header-propath">
                                                                                                <h2 class="modal-title h5 text-danger"
                                                                                                    id="modalEliminarCvLabel">
                                                                                                    <i
                                                                                                        class="bi bi-trash-fill me-2"></i>
                                                                                                    Eliminar currículum
                                                                                                </h2>
                                                                                                <button type="button"
                                                                                                    class="btn-close"
                                                                                                    data-bs-dismiss="modal"
                                                                                                    aria-label="Cerrar"></button>
                                                                                            </div>
                                                                                            <div
                                                                                                class="modal-body modal-body-propath py-4">
                                                                                                <p
                                                                                                    class="fs-6 text-dark mb-0">
                                                                                                    ¿Seguro que
                                                                                                    deseas eliminar tu
                                                                                                    CV? Esta acción no
                                                                                                    se
                                                                                                    puede deshacer.</p>
                                                                                            </div>
                                                                                            <div
                                                                                                class="modal-footer bg-light border-0">
                                                                                                <button type="button"
                                                                                                    class="btn btn-outline-secondary rounded-pill px-4"
                                                                                                    data-bs-dismiss="modal">Cancelar</button>
                                                                                                <button type="button"
                                                                                                    class="btn btn-danger rounded-pill px-4"
                                                                                                    onclick="document.getElementById('formEliminarCv').submit()">
                                                                                                    <i
                                                                                                        class="bi bi-trash me-1"></i>
                                                                                                    Sí,
                                                                                                    eliminar
                                                                                                </button>
                                                                                            </div>
                                                                                        </div>
                                                                                    </div>
                                                                                </div>
                                                                                <form id="formEliminarCv"
                                                                                    action="${pageContext.request.contextPath}/estudiante/documentos"
                                                                                    method="POST"
                                                                                    style="display: none;">
                                                                                    <input type="hidden" name="accion"
                                                                                        value="eliminar">
                                                                                </form>

                                                                                <div class="modal fade"
                                                                                    id="modalEditarPerfilEstudiante"
                                                                                    tabindex="-1"
                                                                                    aria-labelledby="modalEditarPerfilLabel"
                                                                                    aria-hidden="true">
                                                                                    <div
                                                                                        class="modal-dialog modal-dialog-centered">
                                                                                        <div
                                                                                            class="modal-content modal-content-propath">
                                                                                            <div
                                                                                                class="modal-header modal-header-propath">
                                                                                                <h2 class="modal-title h5"
                                                                                                    id="modalEditarPerfilLabel">
                                                                                                    <i
                                                                                                        class="bi bi-person-gear me-2"></i>
                                                                                                    Actualizar Mi Perfil
                                                                                                </h2>
                                                                                                <button type="button"
                                                                                                    class="btn-close"
                                                                                                    data-bs-dismiss="modal"
                                                                                                    aria-label="Cerrar"></button>
                                                                                            </div>
                                                                                            <form
                                                                                                action="${pageContext.request.contextPath}/estudiante/postular"
                                                                                                method="POST">
                                                                                                <input type="hidden"
                                                                                                    name="accion"
                                                                                                    value="actualizarPerfil">
                                                                                                <div
                                                                                                    class="modal-body modal-body-propath">
                                                                                                    <div
                                                                                                        class="row g-3">
                                                                                                        <div
                                                                                                            class="col-sm-6">
                                                                                                            <label
                                                                                                                class="form-label small fw-semibold">Nombres
                                                                                                                *</label>
                                                                                                            <input
                                                                                                                type="text"
                                                                                                                class="form-control"
                                                                                                                name="nombre"
                                                                                                                value="<%= usuario.getNombre() != null ? usuario.getNombre() : "" %>"
                                                                                                                required>
                                                                                                        </div>
                                                                                                        <div
                                                                                                            class="col-sm-6">
                                                                                                            <label
                                                                                                                class="form-label small fw-semibold">Apellidos
                                                                                                                *</label>
                                                                                                            <input
                                                                                                                type="text"
                                                                                                                class="form-control"
                                                                                                                name="apellidos"
                                                                                                                value="<%= (miEstudiante != null && miEstudiante.getApellido() != null && !miEstudiante.getApellido().isEmpty()) ? miEstudiante.getApellido() : (usuario.getApellidos() != null ? usuario.getApellidos() : "") %>"
                                                                                                                required>
                                                                                                        </div>
                                                                                                        <div
                                                                                                            class="col-12">
                                                                                                            <label
                                                                                                                class="form-label small fw-semibold">Universidad
                                                                                                                *</label>
                                                                                                            <input
                                                                                                                type="text"
                                                                                                                class="form-control"
                                                                                                                name="universidad"
                                                                                                                value="<%= (miEstudiante != null && miEstudiante.getUniversidad() != null) ? miEstudiante.getUniversidad() : "" %>"
                                                                                                                required>
                                                                                                        </div>
                                                                                                        <div
                                                                                                            class="col-sm-8">
                                                                                                            <label
                                                                                                                class="form-label small fw-semibold">Carrera
                                                                                                                Profesional
                                                                                                                *</label>
                                                                                                            <input
                                                                                                                type="text"
                                                                                                                class="form-control"
                                                                                                                name="carrera"
                                                                                                                value="<%= (miEstudiante != null && miEstudiante.getCarrera() != null) ? miEstudiante.getCarrera() : "" %>"
                                                                                                                required>
                                                                                                        </div>
                                                                                                        <div
                                                                                                            class="col-sm-4">
                                                                                                            <label
                                                                                                                class="form-label small fw-semibold">Ciclo
                                                                                                                Actual
                                                                                                                *</label>
                                                                                                            <input
                                                                                                                type="number"
                                                                                                                class="form-control"
                                                                                                                name="ciclo"
                                                                                                                min="1"
                                                                                                                max="14"
                                                                                                                value="<%= (miEstudiante != null && miEstudiante.getCiclo() > 0) ? miEstudiante.getCiclo() : 1 %>"
                                                                                                                required>
                                                                                                        </div>
                                                                                                        <div
                                                                                                            class="col-12">
                                                                                                            <label
                                                                                                                class="form-label small fw-semibold">Teléfono
                                                                                                                de
                                                                                                                Contacto</label>
                                                                                                            <input
                                                                                                                type="text"
                                                                                                                class="form-control"
                                                                                                                name="telefono"
                                                                                                                value="<%= (miEstudiante != null && miEstudiante.getTelefono() != null) ? miEstudiante.getTelefono() : "" %>">
                                                                                                        </div>
                                                                                                        <div
                                                                                                            class="col-sm-6">
                                                                                                            <label
                                                                                                                class="form-label small fw-semibold">Correo
                                                                                                                Electrónico
                                                                                                                *</label>
                                                                                                            <input
                                                                                                                type="email"
                                                                                                                class="form-control"
                                                                                                                name="correo"
                                                                                                                value="<%= usuario.getCorreo() != null ? usuario.getCorreo() : "" %>"
                                                                                                                required>
                                                                                                        </div>
                                                                                                        <div
                                                                                                            class="col-sm-6">
                                                                                                            <label
                                                                                                                class="form-label small fw-semibold">Nueva
                                                                                                                Contraseña
                                                                                                                (opcional)</label>
                                                                                                            <input
                                                                                                                type="password"
                                                                                                                class="form-control"
                                                                                                                name="contrasena"
                                                                                                                placeholder="Dejar en blanco para conservar"
                                                                                                                minlength="6">
                                                                                                        </div>
                                                                                                    </div>
                                                                                                </div>
                                                                                                <div
                                                                                                    class="modal-footer bg-light border-0 d-flex justify-content-between">
                                                                                                    <button
                                                                                                        type="button"
                                                                                                        class="btn btn-outline-danger rounded-pill px-3"
                                                                                                        onclick="mostrarModalConfirmacion('Dar de baja perfil', '¿Seguro que deseas solicitar la baja de tu cuenta de estudiante?', function() { const f = document.getElementById('formBajaEstudiante'); f.submit(); });">
                                                                                                        <i
                                                                                                            class="bi bi-person-x me-1"></i>
                                                                                                        Dar de baja
                                                                                                    </button>
                                                                                                    <div
                                                                                                        class="d-flex gap-2">
                                                                                                        <button
                                                                                                            type="button"
                                                                                                            class="btn btn-secondary rounded-pill px-4"
                                                                                                            data-bs-dismiss="modal">Cancelar</button>
                                                                                                        <button
                                                                                                            type="submit"
                                                                                                            class="btn btn-primary rounded-pill px-4">
                                                                                                            <i
                                                                                                                class="bi bi-check2 me-1"></i>
                                                                                                            Guardar
                                                                                                            Cambios
                                                                                                        </button>
                                                                                                    </div>
                                                                                                </div>
                                                                                            </form>
                                                                                            <form
                                                                                                id="formBajaEstudiante"
                                                                                                action="${pageContext.request.contextPath}/estudiante/postular"
                                                                                                method="POST"
                                                                                                style="display: none;">
                                                                                                <input type="hidden"
                                                                                                    name="accion"
                                                                                                    value="darDeBaja">
                                                                                            </form>
                                                                                        </div>
                                                                                    </div>
                                                                                </div>
                                                                                <div class="modal fade"
                                                                                    id="modalMotivoRechazo"
                                                                                    tabindex="-1">
                                                                                    <div
                                                                                        class="modal-dialog modal-dialog-centered">
                                                                                        <div class="modal-content">
                                                                                            <div class="modal-header">
                                                                                                <h5 class="modal-title">
                                                                                                    Motivo del rechazo
                                                                                                </h5>
                                                                                                <button type="button"
                                                                                                    class="btn-close"
                                                                                                    data-bs-dismiss="modal"></button>
                                                                                            </div>
                                                                                            <div class="modal-body">
                                                                                                <p id="textoMotivoRechazo"
                                                                                                    class="mb-0 text-break">
                                                                                                </p>
                                                                                            </div>
                                                                                            <div class="modal-footer">
                                                                                                <button type="button"
                                                                                                    class="btn btn-secondary"
                                                                                                    data-bs-dismiss="modal">Cerrar</button>
                                                                                            </div>
                                                                                        </div>
                                                                                    </div>
                                                                                </div>
                                                                                <script>
                                                                                    function validarArchivoPdf(input) {
                                                                                        if (!input || !input.files || input.files.length === 0) {
                                                                                            mostrarModalMensaje(
                                                                                                'Archivo Requerido',
                                                                                                'Por favor selecciona un archivo PDF.',
                                                                                                'warning'
                                                                                            );
                                                                                            return false;
                                                                                        }

                                                                                        const archivo = input.files[0];

                                                                                        if (!archivo.name.toLowerCase().endsWith('.pdf')) {
                                                                                            mostrarModalMensaje(
                                                                                                'Formato Inválido',
                                                                                                'El archivo seleccionado debe ser un documento en formato .pdf.',
                                                                                                'danger'
                                                                                            );
                                                                                            return false;
                                                                                        }

                                                                                        if (archivo.size > 5 * 1024 * 1024) {
                                                                                            mostrarModalMensaje(
                                                                                                'Límite Excedido',
                                                                                                'El archivo supera el tamaño máximo permitido de 5 MB.',
                                                                                                'danger'
                                                                                            );
                                                                                            return false;
                                                                                        }

                                                                                        return true;
                                                                                    }

                                                                                    function confirmarEliminarCv() {
                                                                                        const modalEl = document.getElementById('modalEliminarCv');

                                                                                        if (window.bootstrap && window.bootstrap.Modal) {
                                                                                            const inst = window.bootstrap.Modal.getOrCreateInstance(modalEl);

                                                                                            if (inst) {
                                                                                                inst.show();
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                    let pasoActualPostular = 1;

                                                                                    const modalPostular = document.getElementById("modalPostular");

                                                                                    function seleccionarOfertaPostular(boton) {
                                                                                        const idOferta = boton.getAttribute("data-id-oferta");
                                                                                        const titulo = boton.getAttribute("data-titulo");

                                                                                        const formulario = document.getElementById("formPostular");
                                                                                        formulario.reset();

                                                                                        document.getElementById("postularIdOferta").value = idOferta || "";
                                                                                        document.getElementById("postularTituloOferta").textContent = titulo || "";

                                                                                        pasoActualPostular = 1;
                                                                                        document.getElementById("btnConfirmarPostular").disabled = false;

                                                                                        mostrarPasoPostular();

                                                                                        console.log("Oferta seleccionada:", idOferta);
                                                                                    }


                                                                                    async function cargarRespuestasPostulacion(boton) {
                                                                                        const formulario = document.getElementById("formPostular");
                                                                                        const idPostulacion = boton.getAttribute("data-id-postulacion");
                                                                                        const btnConfirmar = document.getElementById("btnConfirmarPostular");

                                                                                        formulario.reset();
                                                                                        pasoActualPostular = 1;

                                                                                        document.getElementById("postularAccion").value = "editarRespuestas";
                                                                                        document.getElementById("postularIdPostulacion").value = idPostulacion;
                                                                                        document.getElementById("postularIdOferta").value = "";
                                                                                        document.getElementById("postularTituloOferta").textContent =
                                                                                            boton.getAttribute("data-titulo") || "";

                                                                                        btnConfirmar.textContent = "Guardar cambios";
                                                                                        btnConfirmar.disabled = true;
                                                                                        mostrarPasoPostular();

                                                                                        try {
                                                                                            const url = formulario.action
                                                                                                + "?idPostulacion=" + encodeURIComponent(idPostulacion);

                                                                                            const response = await fetch(url, {
                                                                                                credentials: "same-origin",
                                                                                                cache: "no-store"
                                                                                            });

                                                                                            if (!response.ok) {
                                                                                                throw new Error("No se pudieron cargar las respuestas. HTTP " + response.status);
                                                                                            }

                                                                                            const registros = await response.json();

                                                                                            if (!Array.isArray(registros) || registros.length !== 12) {
                                                                                                throw new Error("El servidor no devolvió las 12 respuestas.");
                                                                                            }

                                                                                            for (const registro of registros) {
                                                                                                const numero = Number(registro.numeroPregunta);
                                                                                                const campo = formulario.elements.namedItem("respuesta" + numero);

                                                                                                if (!campo || numero < 1 || numero > 12) {
                                                                                                    throw new Error("Las respuestas recibidas no son válidas.");
                                                                                                }

                                                                                                campo.value = registro.respuesta || "";
                                                                                            }

                                                                                            btnConfirmar.disabled = false;
                                                                                            pasoActualPostular = 2;
                                                                                            mostrarPasoPostular();

                                                                                        } catch (error) {
                                                                                            alert(error.message);
                                                                                            console.error("Error cargando respuestas:", error);
                                                                                        }
                                                                                    }


                                                                                    function mostrarPasoPostular() {
                                                                                        for (let i = 1; i <= 4; i++) {
                                                                                            document.getElementById("pasoPostular" + i).style.display =
                                                                                                i === pasoActualPostular ? "block" : "none";
                                                                                        }

                                                                                        document.getElementById("btnAnteriorPostular").style.display =
                                                                                            pasoActualPostular > 1 ? "inline-block" : "none";

                                                                                        document.getElementById("btnSiguientePostular").style.display =
                                                                                            pasoActualPostular < 4 ? "inline-block" : "none";

                                                                                        document.getElementById("btnConfirmarPostular").style.display =
                                                                                            pasoActualPostular === 4 ? "inline-block" : "none";
                                                                                    }

                                                                                    function cambiarPasoPostular(direccion) {
                                                                                        if (direccion === 1 && pasoActualPostular >= 2 &&
                                                                                            pasoActualPostular <= 3) {

                                                                                            const campos = document.querySelectorAll(
                                                                                                "#pasoPostular" + pasoActualPostular + " textarea"
                                                                                            );

                                                                                            for (const campo of campos) {
                                                                                                if (!campo.value.trim()) {
                                                                                                    campo.setCustomValidity("Esta respuesta es obligatoria.");
                                                                                                    campo.reportValidity();
                                                                                                    campo.setCustomValidity("");
                                                                                                    campo.focus();
                                                                                                    return;
                                                                                                }
                                                                                            }
                                                                                        }

                                                                                        const siguiente = pasoActualPostular + direccion;
                                                                                        if (siguiente < 1 || siguiente > 4) return;

                                                                                        pasoActualPostular = siguiente;

                                                                                        if (pasoActualPostular === 4) {
                                                                                            const resumen = document.getElementById("resumenPostulacion");
                                                                                            resumen.replaceChildren();

                                                                                            document.querySelectorAll("#formPostular textarea")
                                                                                                .forEach((campo, indice) => {
                                                                                                    const bloque = document.createElement("div");
                                                                                                    bloque.className = "mb-2";

                                                                                                    const titulo = document.createElement("strong");
                                                                                                    titulo.textContent = "Pregunta " + (indice + 1);

                                                                                                    const respuesta = document.createElement("p");
                                                                                                    respuesta.textContent = campo.value;

                                                                                                    bloque.append(titulo, respuesta);
                                                                                                    resumen.appendChild(bloque);
                                                                                                });
                                                                                        }

                                                                                        mostrarPasoPostular();
                                                                                    }

                                                                                    function abrirSubidaCv() {
                                                                                        modalPostular.addEventListener("hidden.bs.modal", function () {
                                                                                            bootstrap.Modal.getOrCreateInstance(
                                                                                                document.getElementById("modalSubirCv")
                                                                                            ).show();
                                                                                        }, { once: true });

                                                                                        bootstrap.Modal.getOrCreateInstance(modalPostular).hide();
                                                                                    }

                                                                                    document.getElementById("formPostular")
                                                                                        .addEventListener("submit", function (event) {

                                                                                            if (pasoActualPostular !== 4) {
                                                                                                event.preventDefault();
                                                                                                return;
                                                                                            }

                                                                                            const campos = this.querySelectorAll("textarea");

                                                                                            for (const campo of campos) {
                                                                                                if (!campo.value.trim()) {
                                                                                                    event.preventDefault();
                                                                                                    alert("Completa las 12 preguntas.");
                                                                                                    return;
                                                                                                }
                                                                                            }
                                                                                            const idOferta = document.getElementById("postularIdOferta").value;

                                                                                            const esEdicion =
                                                                                                document.getElementById("postularAccion").value === "editarRespuestas";

                                                                                            if (!esEdicion && (!idOferta || !/^[1-9]\d*$/.test(idOferta))) {
                                                                                                event.preventDefault();
                                                                                                alert("Error: no se identificó correctamente la oferta.");
                                                                                                return;
                                                                                            }

                                                                                            console.log("ID de oferta enviado:", idOferta);
                                                                                            document.getElementById("btnConfirmarPostular").disabled = true;
                                                                                        });
                                                                                    function mostrarMotivoRechazo(boton) {
                                                                                        document.getElementById("textoMotivoRechazo").textContent =
                                                                                            boton.getAttribute("data-motivo") || "No se registró un motivo.";
                                                                                    }
                                                                                    window.confirmarEliminarCv = confirmarEliminarCv;
                                                                                    window.validarArchivoPdf = validarArchivoPdf;
                                                                                </script>
                                                                                <script type="module"
                                                                                    src="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/js/bootstrap.bundle.min.js"></script>
                                                                                <script type="module"
                                                                                    src="${pageContext.request.contextPath}/js/script.js"></script>
                                                                            </body>

                                                                            </html>