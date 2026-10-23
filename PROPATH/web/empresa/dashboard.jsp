<%! private String entrevistaCampo(String objeto, String campo) { java.util.regex.Matcher
    m=java.util.regex.Pattern.compile( java.util.regex.Pattern.quote("\"" + campo + "\"")
        + "\\s*:\\s*(?:\"([^\"]*)\"|([0-9]+))").matcher(objeto); return m.find() ? (m.group(1) !=null ? m.group(1) :
    m.group(2)) : "" ; } private java.util.List<String> entrevistasJson(String json) {
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
                        <%@page import="com.propath.modelo.Empresa" %>
                            <%@page import="com.propath.modelo.OfertaPractica" %>
                                <%@page import="com.propath.modelo.Postulacion" %>
                                    <%@page import="com.propath.servicio.BackendCliente" %>
                                        <%@page import="java.util.List" %>
                                            <%@page import="java.util.ArrayList" %>
                                                <% Usuario usuario=(Usuario) session.getAttribute("usuarioLogueado"); if
                                                    (usuario==null) { response.sendRedirect(request.getContextPath()
                                                    + "/login.jsp" ); return; } Empresa miEmpresa=null; if
                                                    (usuario.getIdEmpresa() !=null && usuario.getIdEmpresa()> 0) {
                                                    miEmpresa =
                                                    BackendCliente.obtenerEmpresaPorId(usuario.getIdEmpresa());
                                                    }
                                                    if (miEmpresa == null) {
                                                    List<Empresa> empresas = BackendCliente.listarEmpresas();
                                                        for (Empresa e : empresas) {
                                                        if (e.getCorreo() != null &&
                                                        e.getCorreo().equalsIgnoreCase(usuario.getCorreo())) {
                                                        miEmpresa = e;
                                                        if (usuario.getIdEmpresa() == null || usuario.getIdEmpresa() ==
                                                        0) {
                                                        usuario.setIdEmpresa(e.getId());
                                                        }
                                                        break;
                                                        }
                                                        }
                                                        }

                                                        String razon = miEmpresa != null ? miEmpresa.getRazonSocial() :
                                                        (usuario.getRazonSocial() != null ? usuario.getRazonSocial() :
                                                        usuario.getNombre());

                                                        List<OfertaPractica> todasOfertas =
                                                            BackendCliente.listarOfertas();
                                                            List<OfertaPractica> misOfertas = new ArrayList<>();
                                                                    for (OfertaPractica o : todasOfertas) {
                                                                    if (o.getEmpresa() != null && razon != null &&
                                                                    (o.getEmpresa().equalsIgnoreCase(razon) ||
                                                                    razon.toLowerCase().contains(o.getEmpresa().toLowerCase())
                                                                    ||
                                                                    o.getEmpresa().toLowerCase().contains(razon.toLowerCase())))
                                                                    {
                                                                    misOfertas.add(o);
                                                                    }
                                                                    }

                                                                    List<Postulacion> todasPostulaciones =
                                                                        BackendCliente.listarPostulaciones();
                                                                        List<Postulacion> misPostulaciones = new
                                                                            ArrayList<>();
                                                                                for (Postulacion p : todasPostulaciones)
                                                                                {
                                                                                if (p.getEmpresa() != null && razon !=
                                                                                null &&
                                                                                (p.getEmpresa().equalsIgnoreCase(razon)
                                                                                ||
                                                                                razon.toLowerCase().contains(p.getEmpresa().toLowerCase())
                                                                                ||
                                                                                p.getEmpresa().toLowerCase().contains(razon.toLowerCase())))
                                                                                {
                                                                                if
                                                                                (!"NO_SELECCIONADO".equalsIgnoreCase(p.getEstado()))
                                                                                {
                                                                                misPostulaciones.add(p);
                                                                                }
                                                                                }
                                                                                }

                                                                                String estadoEmpresa = (miEmpresa !=
                                                                                null &&
                                                                                miEmpresa.getEstado() != null) ?
                                                                                miEmpresa.getEstado().trim() :
                                                                                "PENDIENTE";
                                                                                String estadoCuenta =
                                                                                (usuario.getEstado() != null) ?
                                                                                usuario.getEstado().trim() : "ACTIVO";
                                                                                boolean esActivo =
                                                                                "ACTIVO".equalsIgnoreCase(estadoCuenta);
                                                                                boolean esAprobada =
                                                                                "APROBADA".equalsIgnoreCase(estadoEmpresa);
                                                                                boolean puedeOperar = esActivo &&
                                                                                esAprobada;

                                                                                String entrevistasEmpresaJson = "[]";
                                                                                try { if (usuario.getIdEmpresa() != null
                                                                                && usuario.getIdEmpresa() > 0)
                                                                                entrevistasEmpresaJson =
                                                                                BackendCliente.consultarEntrevistas(usuario.getIdEmpresa());
                                                                                } catch (Exception ignored) { }
                                                                                java.util.List<String>
                                                                                    entrevistasEmpresa =
                                                                                    entrevistasJson(entrevistasEmpresaJson);
                                                                                    String exito =
                                                                                    request.getParameter("exito");
                                                                                    String error =
                                                                                    request.getParameter("error");
                                                                                    %>
                                                                                    <!DOCTYPE html>
                                                                                    <html lang="es"
                                                                                        data-bs-theme="light">

                                                                                    <head>
                                                                                        <meta charset="UTF-8">
                                                                                        <meta name="viewport"
                                                                                            content="width=device-width, initial-scale=1.0">
                                                                                        <title>Panel de Empresa |
                                                                                            ProPath</title>
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
                                                                                                    href="${pageContext.request.contextPath}/empresa/dashboard.jsp">
                                                                                                    <img src="${pageContext.request.contextPath}/img/logo.png"
                                                                                                        alt="ProPath"
                                                                                                        height="38">
                                                                                                </a>
                                                                                                <div
                                                                                                    class="d-flex align-items-center gap-3 ms-auto">
                                                                                                    <span
                                                                                                        class="badge bg-info-subtle text-info border border-info-subtle px-3 py-2 rounded-pill small"
                                                                                                        style="color: #0891b2 !important;">
                                                                                                        <i
                                                                                                            class="bi bi-building-fill me-1"></i>
                                                                                                        <%= razon %>
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
                                                                                                !exito.isEmpty()) { %>
                                                                                                <div class="alert alert-success alert-dismissible fade show rounded-3 mb-4"
                                                                                                    role="alert">
                                                                                                    <i
                                                                                                        class="bi bi-check-circle-fill me-2"></i>
                                                                                                    Operación completada
                                                                                                    exitosamente.
                                                                                                    <button
                                                                                                        type="button"
                                                                                                        class="btn-close"
                                                                                                        data-bs-dismiss="alert"></button>
                                                                                                </div>
                                                                                                <% } %>

                                                                                                    <% if (error !=null
                                                                                                        &&
                                                                                                        !error.isEmpty())
                                                                                                        { %>
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

                                                                                                            <% if
                                                                                                                (!puedeOperar)
                                                                                                                { %>
                                                                                                                <% if
                                                                                                                    (!esActivo)
                                                                                                                    { %>
                                                                                                                    <div class="alert alert-danger rounded-3 mb-4 shadow-sm"
                                                                                                                        role="alert">
                                                                                                                        <div
                                                                                                                            class="d-flex align-items-center">
                                                                                                                            <i
                                                                                                                                class="bi bi-shield-x fs-3 me-3 text-danger"></i>
                                                                                                                            <div>
                                                                                                                                <h4
                                                                                                                                    class="alert-heading h6 fw-bold mb-1">
                                                                                                                                    Cuenta
                                                                                                                                    Inactiva
                                                                                                                                </h4>
                                                                                                                                <p
                                                                                                                                    class="mb-0 small">
                                                                                                                                    Tu
                                                                                                                                    cuenta
                                                                                                                                    corporativa
                                                                                                                                    se
                                                                                                                                    encuentra
                                                                                                                                    inactiva.
                                                                                                                                    No
                                                                                                                                    puedes
                                                                                                                                    realizar
                                                                                                                                    operaciones
                                                                                                                                    empresariales
                                                                                                                                    hasta
                                                                                                                                    que
                                                                                                                                    sea
                                                                                                                                    reactivada.
                                                                                                                                </p>
                                                                                                                            </div>
                                                                                                                        </div>
                                                                                                                    </div>
                                                                                                                    <% } else
                                                                                                                        if
                                                                                                                        ("RECHAZADA".equalsIgnoreCase(estadoEmpresa))
                                                                                                                        {
                                                                                                                        %>
                                                                                                                        <div class="alert alert-danger rounded-3 mb-4 shadow-sm"
                                                                                                                            role="alert">
                                                                                                                            <div
                                                                                                                                class="d-flex align-items-center">
                                                                                                                                <i
                                                                                                                                    class="bi bi-x-octagon-fill fs-3 me-3 text-danger"></i>
                                                                                                                                <div>
                                                                                                                                    <h4
                                                                                                                                        class="alert-heading h6 fw-bold mb-1">
                                                                                                                                        Empresa
                                                                                                                                        No
                                                                                                                                        Aprobada
                                                                                                                                    </h4>
                                                                                                                                    <p
                                                                                                                                        class="mb-0 small">
                                                                                                                                        Tu
                                                                                                                                        empresa
                                                                                                                                        no
                                                                                                                                        ha
                                                                                                                                        sido
                                                                                                                                        aprobada.
                                                                                                                                        No
                                                                                                                                        puedes
                                                                                                                                        publicar
                                                                                                                                        ofertas
                                                                                                                                        ni
                                                                                                                                        gestionar
                                                                                                                                        postulaciones.
                                                                                                                                    </p>
                                                                                                                                </div>
                                                                                                                            </div>
                                                                                                                        </div>
                                                                                                                        <% } else
                                                                                                                            if
                                                                                                                            ("SUSPENDIDA".equalsIgnoreCase(estadoEmpresa))
                                                                                                                            {
                                                                                                                            %>
                                                                                                                            <div class="alert alert-warning rounded-3 mb-4 shadow-sm"
                                                                                                                                role="alert">
                                                                                                                                <div
                                                                                                                                    class="d-flex align-items-center">
                                                                                                                                    <i
                                                                                                                                        class="bi bi-exclamation-octagon-fill fs-3 me-3 text-warning"></i>
                                                                                                                                    <div>
                                                                                                                                        <h4
                                                                                                                                            class="alert-heading h6 fw-bold mb-1">
                                                                                                                                            Empresa
                                                                                                                                            Suspendida
                                                                                                                                        </h4>
                                                                                                                                        <p
                                                                                                                                            class="mb-0 small">
                                                                                                                                            Tu
                                                                                                                                            empresa
                                                                                                                                            se
                                                                                                                                            encuentra
                                                                                                                                            suspendida.
                                                                                                                                            Las
                                                                                                                                            funciones
                                                                                                                                            de
                                                                                                                                            publicación
                                                                                                                                            y
                                                                                                                                            gestión
                                                                                                                                            de
                                                                                                                                            postulaciones
                                                                                                                                            están
                                                                                                                                            deshabilitadas.
                                                                                                                                        </p>
                                                                                                                                    </div>
                                                                                                                                </div>
                                                                                                                            </div>
                                                                                                                            <% } else
                                                                                                                                {
                                                                                                                                %>
                                                                                                                                <div class="alert alert-warning rounded-3 mb-4 shadow-sm"
                                                                                                                                    role="alert">
                                                                                                                                    <div
                                                                                                                                        class="d-flex align-items-center">
                                                                                                                                        <i
                                                                                                                                            class="bi bi-hourglass-split fs-3 me-3 text-warning"></i>
                                                                                                                                        <div>
                                                                                                                                            <h4
                                                                                                                                                class="alert-heading h6 fw-bold mb-1">
                                                                                                                                                Aprobación
                                                                                                                                                Institucional
                                                                                                                                                Pendiente
                                                                                                                                            </h4>
                                                                                                                                            <p
                                                                                                                                                class="mb-0 small">
                                                                                                                                                Tu
                                                                                                                                                empresa
                                                                                                                                                está
                                                                                                                                                pendiente
                                                                                                                                                de
                                                                                                                                                aprobación
                                                                                                                                                por
                                                                                                                                                el
                                                                                                                                                administrador
                                                                                                                                                de
                                                                                                                                                PROPATH.
                                                                                                                                                Puedes
                                                                                                                                                consultar
                                                                                                                                                y
                                                                                                                                                actualizar
                                                                                                                                                tu
                                                                                                                                                perfil,
                                                                                                                                                pero
                                                                                                                                                no
                                                                                                                                                podrás
                                                                                                                                                publicar
                                                                                                                                                ofertas
                                                                                                                                                ni
                                                                                                                                                gestionar
                                                                                                                                                postulaciones
                                                                                                                                                hasta
                                                                                                                                                que
                                                                                                                                                tu
                                                                                                                                                empresa
                                                                                                                                                sea
                                                                                                                                                aprobada.
                                                                                                                                            </p>
                                                                                                                                        </div>
                                                                                                                                    </div>
                                                                                                                                </div>
                                                                                                                                <% }
                                                                                                                                    %>
                                                                                                                                    <% }
                                                                                                                                        %>

                                                                                                                                        <div
                                                                                                                                            class="row g-4 mb-4">
                                                                                                                                            <div
                                                                                                                                                class="col-12 col-md-4">
                                                                                                                                                <div
                                                                                                                                                    class="stat-card stat-card-blue bg-white p-4 rounded-4 border shadow-sm">
                                                                                                                                                    <div
                                                                                                                                                        class="d-flex justify-content-between align-items-start">
                                                                                                                                                        <div>
                                                                                                                                                            <div
                                                                                                                                                                class="stat-value fs-2 fw-bold text-dark">
                                                                                                                                                                <%= misOfertas.size()
                                                                                                                                                                    %>
                                                                                                                                                            </div>
                                                                                                                                                            <h3
                                                                                                                                                                class="stat-label small text-secondary mb-0">
                                                                                                                                                                Mis
                                                                                                                                                                Ofertas
                                                                                                                                                                Publicadas
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
                                                                                                                                                class="col-12 col-md-4">
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
                                                                                                                                                                Postulaciones
                                                                                                                                                                Recibidas
                                                                                                                                                            </h3>
                                                                                                                                                        </div>
                                                                                                                                                        <div
                                                                                                                                                            class="stat-icon-wrapper rounded-circle p-3 bg-info-subtle text-info">
                                                                                                                                                            <i
                                                                                                                                                                class="bi bi-people-fill fs-4"></i>
                                                                                                                                                        </div>
                                                                                                                                                    </div>
                                                                                                                                                </div>
                                                                                                                                            </div>
                                                                                                                                            <div
                                                                                                                                                class="col-12 col-md-4">
                                                                                                                                                <div
                                                                                                                                                    class="bg-white p-4 rounded-4 border shadow-sm h-100 d-flex flex-column justify-content-between">
                                                                                                                                                    <div>
                                                                                                                                                        <div
                                                                                                                                                            class="d-flex justify-content-between align-items-center mb-2">
                                                                                                                                                            <span
                                                                                                                                                                class="small fw-bold text-muted text-uppercase tracking-wider">Mi
                                                                                                                                                                Empresa</span>
                                                                                                                                                            <% String
                                                                                                                                                                estEmp=miEmpresa
                                                                                                                                                                !=null
                                                                                                                                                                &&
                                                                                                                                                                miEmpresa.getEstado()
                                                                                                                                                                !=null
                                                                                                                                                                ?
                                                                                                                                                                miEmpresa.getEstado()
                                                                                                                                                                : "PENDIENTE"
                                                                                                                                                                ;
                                                                                                                                                                String
                                                                                                                                                                badgeClass="APROBADA"
                                                                                                                                                                .equalsIgnoreCase(estEmp)
                                                                                                                                                                ? "bg-success-subtle text-success border-success-subtle"
                                                                                                                                                                :
                                                                                                                                                                ("PENDIENTE".equalsIgnoreCase(estEmp)
                                                                                                                                                                ? "bg-warning-subtle text-warning border-warning-subtle"
                                                                                                                                                                : "bg-secondary-subtle text-secondary border-secondary-subtle"
                                                                                                                                                                );
                                                                                                                                                                %>
                                                                                                                                                                <span
                                                                                                                                                                    class="badge <%= badgeClass %> border rounded-pill">
                                                                                                                                                                    <%= estEmp
                                                                                                                                                                        %>
                                                                                                                                                                </span>
                                                                                                                                                        </div>
                                                                                                                                                        <div
                                                                                                                                                            class="fw-bold text-dark fs-5">
                                                                                                                                                            <%= razon
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
                                                                                                                                                                class="bi bi-card-text me-1 text-primary"></i>
                                                                                                                                                            <strong>RUC:</strong>
                                                                                                                                                            <%= miEmpresa
                                                                                                                                                                !=null
                                                                                                                                                                ?
                                                                                                                                                                miEmpresa.getRuc()
                                                                                                                                                                : "—"
                                                                                                                                                                %>
                                                                                                                                                        </div>
                                                                                                                                                        <div
                                                                                                                                                            class="small text-secondary mb-1">
                                                                                                                                                            <i
                                                                                                                                                                class="bi bi-telephone me-1 text-primary"></i>
                                                                                                                                                            <strong>Teléfono:</strong>
                                                                                                                                                            <%= (miEmpresa
                                                                                                                                                                !=null
                                                                                                                                                                &&
                                                                                                                                                                miEmpresa.getTelefono()
                                                                                                                                                                !=null
                                                                                                                                                                &&
                                                                                                                                                                !miEmpresa.getTelefono().isEmpty())
                                                                                                                                                                ?
                                                                                                                                                                miEmpresa.getTelefono()
                                                                                                                                                                : "—"
                                                                                                                                                                %>
                                                                                                                                                        </div>
                                                                                                                                                        <div
                                                                                                                                                            class="small text-secondary">
                                                                                                                                                            <i
                                                                                                                                                                class="bi bi-geo-alt me-1 text-primary"></i>
                                                                                                                                                            <strong>Dirección:</strong>
                                                                                                                                                            <%= (miEmpresa
                                                                                                                                                                !=null
                                                                                                                                                                &&
                                                                                                                                                                miEmpresa.getDireccion()
                                                                                                                                                                !=null
                                                                                                                                                                &&
                                                                                                                                                                !miEmpresa.getDireccion().isEmpty())
                                                                                                                                                                ?
                                                                                                                                                                miEmpresa.getDireccion()
                                                                                                                                                                : "No registrada"
                                                                                                                                                                %>
                                                                                                                                                        </div>
                                                                                                                                                    </div>
                                                                                                                                                    <div
                                                                                                                                                        class="pt-3 border-top mt-3">
                                                                                                                                                        <button
                                                                                                                                                            type="button"
                                                                                                                                                            class="btn btn-sm btn-outline-primary w-100 rounded-pill fw-semibold"
                                                                                                                                                            data-bs-toggle="modal"
                                                                                                                                                            data-bs-target="#modalEditarPerfilEmpresa">
                                                                                                                                                            <i
                                                                                                                                                                class="bi bi-pencil-square me-1"></i>
                                                                                                                                                            Editar
                                                                                                                                                            Mi
                                                                                                                                                            Empresa
                                                                                                                                                        </button>
                                                                                                                                                    </div>
                                                                                                                                                </div>
                                                                                                                                            </div>
                                                                                                                                        </div>

                                                                                                                                        <div
                                                                                                                                            class="row g-4 mb-4">
                                                                                                                                            <div
                                                                                                                                                class="col-12">
                                                                                                                                                <div
                                                                                                                                                    class="bg-white p-4 rounded-4 border shadow-sm">
                                                                                                                                                    <div
                                                                                                                                                        class="d-flex justify-content-between align-items-center mb-3">
                                                                                                                                                        <h2
                                                                                                                                                            class="h5 fw-bold text-dark mb-0">
                                                                                                                                                            <i
                                                                                                                                                                class="bi bi-plus-circle me-2 text-primary"></i>Publicar
                                                                                                                                                            Nueva
                                                                                                                                                            Oferta
                                                                                                                                                            de
                                                                                                                                                            Prácticas
                                                                                                                                                        </h2>
                                                                                                                                                        <% if
                                                                                                                                                            (!puedeOperar)
                                                                                                                                                            {
                                                                                                                                                            %>
                                                                                                                                                            <span
                                                                                                                                                                class="badge bg-warning-subtle text-warning border border-warning-subtle">
                                                                                                                                                                <i
                                                                                                                                                                    class="bi bi-lock-fill me-1"></i>
                                                                                                                                                                Publicación
                                                                                                                                                                Bloqueada
                                                                                                                                                                (Requiere
                                                                                                                                                                Aprobación)
                                                                                                                                                            </span>
                                                                                                                                                            <% }
                                                                                                                                                                %>
                                                                                                                                                    </div>
                                                                                                                                                    <form
                                                                                                                                                        action="${pageContext.request.contextPath}/empresa/oferta"
                                                                                                                                                        method="POST"
                                                                                                                                                        class="row g-3">
                                                                                                                                                        <input
                                                                                                                                                            type="hidden"
                                                                                                                                                            name="accion"
                                                                                                                                                            value="crear">
                                                                                                                                                        <div
                                                                                                                                                            class="col-12 col-md-6">
                                                                                                                                                            <label
                                                                                                                                                                class="form-label small fw-semibold text-secondary">Título
                                                                                                                                                                de
                                                                                                                                                                la
                                                                                                                                                                Convocatoria
                                                                                                                                                                *</label>
                                                                                                                                                            <input
                                                                                                                                                                type="text"
                                                                                                                                                                class="form-control"
                                                                                                                                                                name="titulo"
                                                                                                                                                                placeholder="Ej. Practicante de Desarrollo Java"
                                                                                                                                                                <%=!puedeOperar
                                                                                                                                                                ? "disabled"
                                                                                                                                                                : ""
                                                                                                                                                                %>
                                                                                                                                                            required>
                                                                                                                                                        </div>
                                                                                                                                                        <div
                                                                                                                                                            class="col-12 col-md-6">
                                                                                                                                                            <label
                                                                                                                                                                class="form-label small fw-semibold text-secondary">Carrera
                                                                                                                                                                Profesional
                                                                                                                                                                *</label>
                                                                                                                                                            <input
                                                                                                                                                                type="text"
                                                                                                                                                                class="form-control"
                                                                                                                                                                name="carrera"
                                                                                                                                                                placeholder="Ej. Ingeniería de Sistemas"
                                                                                                                                                                <%=!puedeOperar
                                                                                                                                                                ? "disabled"
                                                                                                                                                                : ""
                                                                                                                                                                %>
                                                                                                                                                            required>
                                                                                                                                                        </div>
                                                                                                                                                        <div
                                                                                                                                                            class="col-12 col-md-4">
                                                                                                                                                            <label
                                                                                                                                                                class="form-label small fw-semibold text-secondary">Modalidad</label>
                                                                                                                                                            <select
                                                                                                                                                                class="form-select"
                                                                                                                                                                name="modalidad"
                                                                                                                                                                <%=!puedeOperar
                                                                                                                                                                ? "disabled"
                                                                                                                                                                : ""
                                                                                                                                                                %>>
                                                                                                                                                                <option
                                                                                                                                                                    value="Presencial">
                                                                                                                                                                    Presencial
                                                                                                                                                                </option>
                                                                                                                                                                <option
                                                                                                                                                                    value="Híbrida"
                                                                                                                                                                    selected>
                                                                                                                                                                    Híbrida
                                                                                                                                                                </option>
                                                                                                                                                                <option
                                                                                                                                                                    value="Remota">
                                                                                                                                                                    Remota
                                                                                                                                                                </option>
                                                                                                                                                            </select>
                                                                                                                                                        </div>
                                                                                                                                                        <div
                                                                                                                                                            class="col-12 col-md-4">
                                                                                                                                                            <label
                                                                                                                                                                class="form-label small fw-semibold text-secondary">Vacantes
                                                                                                                                                                Disponibles
                                                                                                                                                                *</label>
                                                                                                                                                            <input
                                                                                                                                                                type="number"
                                                                                                                                                                class="form-control"
                                                                                                                                                                name="vacantes"
                                                                                                                                                                min="1"
                                                                                                                                                                max="50"
                                                                                                                                                                value="2"
                                                                                                                                                                <%=!puedeOperar
                                                                                                                                                                ? "disabled"
                                                                                                                                                                : ""
                                                                                                                                                                %>
                                                                                                                                                            required>
                                                                                                                                                        </div>
                                                                                                                                                        <div
                                                                                                                                                            class="col-12 col-md-4">
                                                                                                                                                            <label
                                                                                                                                                                class="form-label small fw-semibold text-secondary">Fecha
                                                                                                                                                                Límite
                                                                                                                                                                de
                                                                                                                                                                Postulación
                                                                                                                                                                *</label>
                                                                                                                                                            <input
                                                                                                                                                                type="date"
                                                                                                                                                                class="form-control"
                                                                                                                                                                name="fechaLimite"
                                                                                                                                                                <%=!puedeOperar
                                                                                                                                                                ? "disabled"
                                                                                                                                                                : ""
                                                                                                                                                                %>
                                                                                                                                                            required>
                                                                                                                                                        </div>
                                                                                                                                                        <div
                                                                                                                                                            class="col-12 col-md-6">
                                                                                                                                                            <label
                                                                                                                                                                class="form-label small fw-semibold text-secondary">Ubicación
                                                                                                                                                                /
                                                                                                                                                                Distrito</label>
                                                                                                                                                            <input
                                                                                                                                                                type="text"
                                                                                                                                                                class="form-control"
                                                                                                                                                                name="ubicacion"
                                                                                                                                                                placeholder="San Isidro, Lima"
                                                                                                                                                                <%=!puedeOperar
                                                                                                                                                                ? "disabled"
                                                                                                                                                                : ""
                                                                                                                                                                %>>
                                                                                                                                                        </div>
                                                                                                                                                        <div
                                                                                                                                                            class="col-12 col-md-6">
                                                                                                                                                            <label
                                                                                                                                                                class="form-label small fw-semibold text-secondary">Subvención
                                                                                                                                                                Económica
                                                                                                                                                                (Opcional)</label>
                                                                                                                                                            <div
                                                                                                                                                                class="input-group">
                                                                                                                                                                <span
                                                                                                                                                                    class="input-group-text">S/</span>
                                                                                                                                                                <input
                                                                                                                                                                    type="number"
                                                                                                                                                                    class="form-control"
                                                                                                                                                                    name="remuneracion"
                                                                                                                                                                    min="0"
                                                                                                                                                                    step="0.01"
                                                                                                                                                                    placeholder="520.00"
                                                                                                                                                                    <%=!puedeOperar
                                                                                                                                                                    ? "disabled"
                                                                                                                                                                    : ""
                                                                                                                                                                    %>>
                                                                                                                                                            </div>
                                                                                                                                                        </div>
                                                                                                                                                        <div
                                                                                                                                                            class="col-12">
                                                                                                                                                            <label
                                                                                                                                                                class="form-label small fw-semibold text-secondary">
                                                                                                                                                                Descripción
                                                                                                                                                                de
                                                                                                                                                                la
                                                                                                                                                                Oferta
                                                                                                                                                                *
                                                                                                                                                            </label>
                                                                                                                                                            <textarea
                                                                                                                                                                class="form-control"
                                                                                                                                                                name="descripcion"
                                                                                                                                                                rows="3"
                                                                                                                                                                placeholder="Describe las funciones, responsabilidades y características de la práctica..."
                                                                                                                                                                <%=!puedeOperar
                                                                                                                                                                ? "disabled"
                                                                                                                                                                : ""
                                                                                                                                                                %>
              required></textarea>
                                                                                                                                                        </div>
                                                                                                                                                        <div
                                                                                                                                                            class="col-12">
                                                                                                                                                            <label
                                                                                                                                                                class="form-label small fw-semibold text-secondary">
                                                                                                                                                                Requisitos
                                                                                                                                                                del
                                                                                                                                                                Postulante
                                                                                                                                                            </label>
                                                                                                                                                            <textarea
                                                                                                                                                                class="form-control"
                                                                                                                                                                name="requisitos"
                                                                                                                                                                rows="3"
                                                                                                                                                                placeholder="Carrera, conocimientos, habilidades y experiencia requerida..."
                                                                                                                                                                <%=!puedeOperar
                                                                                                                                                                ? "disabled"
                                                                                                                                                                : ""
                                                                                                                                                                %>></textarea>
                                                                                                                                                        </div>

                                                                                                                                                        <div
                                                                                                                                                            class="col-12 d-flex justify-content-end">
                                                                                                                                                            <button
                                                                                                                                                                type="submit"
                                                                                                                                                                class="btn btn-primary rounded-pill px-4 fw-semibold"
                                                                                                                                                                <%=!puedeOperar
                                                                                                                                                                ? "disabled style='opacity: 0.5; cursor: not-allowed;'"
                                                                                                                                                                : ""
                                                                                                                                                                %>>
                                                                                                                                                                <i
                                                                                                                                                                    class="bi bi-send-plus me-1"></i>
                                                                                                                                                                Publicar
                                                                                                                                                                Oferta
                                                                                                                                                                Real
                                                                                                                                                            </button>
                                                                                                                                                        </div>
                                                                                                                                                    </form>
                                                                                                                                                </div>
                                                                                                                                            </div>
                                                                                                                                        </div>

                                                                                                                                        <div
                                                                                                                                            class="row g-4">
                                                                                                                                            <div
                                                                                                                                                class="col-12 col-lg-7">
                                                                                                                                                <div
                                                                                                                                                    class="bg-white p-4 rounded-4 border shadow-sm">
                                                                                                                                                    <div
                                                                                                                                                        class="d-flex justify-content-between align-items-center mb-3">
                                                                                                                                                        <h2
                                                                                                                                                            class="h5 fw-bold text-dark mb-0">
                                                                                                                                                            <i
                                                                                                                                                                class="bi bi-briefcase me-2 text-primary"></i>Mis
                                                                                                                                                            Ofertas
                                                                                                                                                            Publicadas
                                                                                                                                                        </h2>
                                                                                                                                                        <span
                                                                                                                                                            class="badge bg-light text-secondary border">
                                                                                                                                                            <%= misOfertas.size()
                                                                                                                                                                %>
                                                                                                                                                                convocatorias
                                                                                                                                                        </span>
                                                                                                                                                    </div>

                                                                                                                                                    <% if
                                                                                                                                                        (misOfertas.isEmpty())
                                                                                                                                                        {
                                                                                                                                                        %>
                                                                                                                                                        <div
                                                                                                                                                            class="text-center py-5 text-muted">
                                                                                                                                                            <i
                                                                                                                                                                class="bi bi-inbox fs-1 d-block mb-2"></i>
                                                                                                                                                            Aún
                                                                                                                                                            no
                                                                                                                                                            has
                                                                                                                                                            publicado
                                                                                                                                                            ofertas
                                                                                                                                                            de
                                                                                                                                                            prácticas.
                                                                                                                                                            Utiliza
                                                                                                                                                            el
                                                                                                                                                            formulario
                                                                                                                                                            superior
                                                                                                                                                            para
                                                                                                                                                            crear
                                                                                                                                                            tu
                                                                                                                                                            primera
                                                                                                                                                            vacante.
                                                                                                                                                        </div>
                                                                                                                                                        <% } else
                                                                                                                                                            {
                                                                                                                                                            %>
                                                                                                                                                            <div
                                                                                                                                                                class="d-flex flex-column gap-3">
                                                                                                                                                                <% for
                                                                                                                                                                    (OfertaPractica
                                                                                                                                                                    o
                                                                                                                                                                    :
                                                                                                                                                                    misOfertas)
                                                                                                                                                                    {
                                                                                                                                                                    %>
                                                                                                                                                                    <div
                                                                                                                                                                        class="p-3 border rounded-3 bg-light bg-opacity-50">
                                                                                                                                                                        <div
                                                                                                                                                                            class="d-flex justify-content-between align-items-start mb-2">
                                                                                                                                                                            <div>
                                                                                                                                                                                <span
                                                                                                                                                                                    class="badge bg-primary-subtle text-primary mb-1">
                                                                                                                                                                                    <%= o.getCarrera()
                                                                                                                                                                                        %>
                                                                                                                                                                                </span>
                                                                                                                                                                                <h3
                                                                                                                                                                                    class="h6 fw-bold text-dark mb-1">
                                                                                                                                                                                    <%= o.getTitulo()
                                                                                                                                                                                        %>
                                                                                                                                                                                </h3>
                                                                                                                                                                                <div
                                                                                                                                                                                    class="small text-muted">
                                                                                                                                                                                    Modalidad:
                                                                                                                                                                                    <%= o.getModalidad()
                                                                                                                                                                                        %>
                                                                                                                                                                                        &bull;
                                                                                                                                                                                        <%= o.getVacantes()
                                                                                                                                                                                            %>
                                                                                                                                                                                            vacantes
                                                                                                                                                                                            &bull;
                                                                                                                                                                                            Publicado
                                                                                                                                                                                            <%= o.getFechaPublicacion()
                                                                                                                                                                                                %>
                                                                                                                                                                                </div>
                                                                                                                                                                            </div>
                                                                                                                                                                            <div
                                                                                                                                                                                class="d-flex gap-2 align-items-center">
                                                                                                                                                                                <% String
                                                                                                                                                                                    estadoOferta=o.getEstado()
                                                                                                                                                                                    !=null
                                                                                                                                                                                    ?
                                                                                                                                                                                    o.getEstado().trim().toUpperCase()
                                                                                                                                                                                    : "PENDIENTE"
                                                                                                                                                                                    ;
                                                                                                                                                                                    String
                                                                                                                                                                                    claseEstadoOferta="ACTIVA"
                                                                                                                                                                                    .equals(estadoOferta)
                                                                                                                                                                                    ? "bg-success-subtle text-success"
                                                                                                                                                                                    : "PENDIENTE"
                                                                                                                                                                                    .equals(estadoOferta)
                                                                                                                                                                                    ? "bg-warning-subtle text-warning"
                                                                                                                                                                                    : "RECHAZADA"
                                                                                                                                                                                    .equals(estadoOferta)
                                                                                                                                                                                    ? "bg-danger-subtle text-danger"
                                                                                                                                                                                    : "bg-secondary-subtle text-secondary"
                                                                                                                                                                                    ;
                                                                                                                                                                                    %>

                                                                                                                                                                                    <span
                                                                                                                                                                                        class="badge <%= claseEstadoOferta %> border rounded-pill">
                                                                                                                                                                                        <%= estadoOferta
                                                                                                                                                                                            %>
                                                                                                                                                                                    </span>

                                                                                                                                                                                    <button
                                                                                                                                                                                        type="button"
                                                                                                                                                                                        class="btn btn-sm btn-outline-primary py-0 px-2 rounded-2"
                                                                                                                                                                                        <%=!puedeOperar
                                                                                                                                                                                        ? "disabled"
                                                                                                                                                                                        : ""
                                                                                                                                                                                        %>
                                                                                                                                                                                        title="
                                                                                                                                                                                        <%= puedeOperar
                                                                                                                                                                                            ? "Editar oferta"
                                                                                                                                                                                            : "Empresa no aprobada"
                                                                                                                                                                                            %>
                                                                                                                                                                                            "
                                                                                                                                                                                            data-id="
                                                                                                                                                                                            <%= o.getId()
                                                                                                                                                                                                %>
                                                                                                                                                                                                "
                                                                                                                                                                                                data-titulo="
                                                                                                                                                                                                <%= o.getTitulo()
                                                                                                                                                                                                    %>
                                                                                                                                                                                                    "
                                                                                                                                                                                                    data-carrera="
                                                                                                                                                                                                    <%= o.getCarrera()
                                                                                                                                                                                                        %>
                                                                                                                                                                                                        "
                                                                                                                                                                                                        data-modalidad="
                                                                                                                                                                                                        <%= o.getModalidad()
                                                                                                                                                                                                            %>
                                                                                                                                                                                                            "
                                                                                                                                                                                                            data-vacantes="
                                                                                                                                                                                                            <%= o.getVacantes()
                                                                                                                                                                                                                %>
                                                                                                                                                                                                                "
                                                                                                                                                                                                                data-fecha="
                                                                                                                                                                                                                <%= o.getFechaLimite()
                                                                                                                                                                                                                    !=null
                                                                                                                                                                                                                    ?
                                                                                                                                                                                                                    o.getFechaLimite()
                                                                                                                                                                                                                    : ""
                                                                                                                                                                                                                    %>
                                                                                                                                                                                                                    "
                                                                                                                                                                                                                    data-ubicacion="
                                                                                                                                                                                                                    <%= o.getUbicacion()
                                                                                                                                                                                                                        !=null
                                                                                                                                                                                                                        ?
                                                                                                                                                                                                                        o.getUbicacion()
                                                                                                                                                                                                                        : ""
                                                                                                                                                                                                                        %>
                                                                                                                                                                                                                        "
                                                                                                                                                                                                                        data-descripcion="
                                                                                                                                                                                                                        <%= escaparHtml(o.getDescripcion())
                                                                                                                                                                                                                            %>
                                                                                                                                                                                                                            "
                                                                                                                                                                                                                            data-estado="
                                                                                                                                                                                                                            <%= o.getEstado()
                                                                                                                                                                                                                                %>
                                                                                                                                                                                                                                "

                                                                                                                                                                                                                                data-requisitos="
                                                                                                                                                                                                                                <%= o.getRequisitos()
                                                                                                                                                                                                                                    !=null
                                                                                                                                                                                                                                    ?
                                                                                                                                                                                                                                    o.getRequisitos().replace("&", "&amp;"
                                                                                                                                                                                                                                    ).replace("\"", "&quot;"
                                                                                                                                                                                                                                    ).replace("<", "&lt;"
                                                                                                                                                                                                                                    )
                                                                                                                                                                                                                                    : ""
                                                                                                                                                                                                                                    %>
                                                                                                                                                                                                                                    "
                                                                                                                                                                                                                                    data-remuneracion="
                                                                                                                                                                                                                                    <%= o.getRemuneracion()
                                                                                                                                                                                                                                        !=null
                                                                                                                                                                                                                                        ?
                                                                                                                                                                                                                                        o.getRemuneracion().replace("&", "&amp;"
                                                                                                                                                                                                                                        ).replace("\"", "&quot;"
                                                                                                                                                                                                                                        ).replace("<", "&lt;"
                                                                                                                                                                                                                                        )
                                                                                                                                                                                                                                        : ""
                                                                                                                                                                                                                                        %>
                                                                                                                                                                                                                                        "

                                                                                                                                                                                                                                        <%= !puedeOperar
                                                                                                                                                                                                                                            ? "disabled style='opacity: 0.4; cursor: not-allowed;'"
                                                                                                                                                                                                                                            : ""
                                                                                                                                                                                                                                            %>
                                                                                                                                                                                                                                            onclick="
                                                                                                                                                                                                                                            <%= puedeOperar
                                                                                                                                                                                                                                                ? "abrirModalEditarOferta(this)"
                                                                                                                                                                                                                                                : ""
                                                                                                                                                                                                                                                %>
                                                                                                                                                                                                                                                ">
                                                                                                                                                                                                                                                <i
                                                                                                                                                                                                                                                    class="bi bi-pencil-square"></i>
                                                                                                                                                                                    </button>
                                                                                                                                                                                    <form
                                                                                                                                                                                        action="${pageContext.request.contextPath}/empresa/oferta"
                                                                                                                                                                                        method="POST"
                                                                                                                                                                                        onsubmit="event.preventDefault(); const f = this; mostrarModalConfirmacion('Eliminar Oferta', '¿Seguro que deseas eliminar esta oferta de práctica?', function() { f.submit(); });"
                                                                                                                                                                                        class="m-0">
                                                                                                                                                                                        <input
                                                                                                                                                                                            type="hidden"
                                                                                                                                                                                            name="accion"
                                                                                                                                                                                            value="eliminar">
                                                                                                                                                                                        <input
                                                                                                                                                                                            type="hidden"
                                                                                                                                                                                            name="id"
                                                                                                                                                                                            value="<%= o.getId() %>">
                                                                                                                                                                                        <button
                                                                                                                                                                                            type="submit"
                                                                                                                                                                                            class="btn btn-sm btn-outline-danger py-0 px-2 rounded-2"
                                                                                                                                                                                            <%=!puedeOperar
                                                                                                                                                                                            ? "disabled style='opacity: 0.4; cursor: not-allowed;'"
                                                                                                                                                                                            : ""
                                                                                                                                                                                            %>
                                                                                                                                                                                            title="
                                                                                                                                                                                            <%= puedeOperar
                                                                                                                                                                                                ? "Eliminar oferta"
                                                                                                                                                                                                : "Empresa no aprobada"
                                                                                                                                                                                                %>
                                                                                                                                                                                                ">
                                                                                                                                                                                                <i
                                                                                                                                                                                                    class="bi bi-trash"></i>
                                                                                                                                                                                        </button>
                                                                                                                                                                                    </form>
                                                                                                                                                                            </div>
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
                                                                                                                                                    class="bg-white p-4 rounded-4 border shadow-sm">
                                                                                                                                                    <div
                                                                                                                                                        class="d-flex justify-content-between align-items-center mb-3">
                                                                                                                                                        <h2
                                                                                                                                                            class="h5 fw-bold text-dark mb-0">
                                                                                                                                                            <i
                                                                                                                                                                class="bi bi-people me-2 text-info"></i>Postulantes
                                                                                                                                                            a
                                                                                                                                                            mis
                                                                                                                                                            Ofertas
                                                                                                                                                        </h2>
                                                                                                                                                        <span
                                                                                                                                                            class="badge bg-light text-secondary border">
                                                                                                                                                            <%= misPostulaciones.size()
                                                                                                                                                                %>
                                                                                                                                                                postulaciones
                                                                                                                                                        </span>
                                                                                                                                                    </div>

                                                                                                                                                    <% if
                                                                                                                                                        (misPostulaciones.isEmpty())
                                                                                                                                                        {
                                                                                                                                                        %>
                                                                                                                                                        <div
                                                                                                                                                            class="text-center py-5 text-muted">
                                                                                                                                                            <i
                                                                                                                                                                class="bi bi-person-x fs-1 d-block mb-2"></i>
                                                                                                                                                            Aún
                                                                                                                                                            no
                                                                                                                                                            hay
                                                                                                                                                            postulaciones
                                                                                                                                                            recibidas
                                                                                                                                                            para
                                                                                                                                                            tus
                                                                                                                                                            ofertas.
                                                                                                                                                        </div>
                                                                                                                                                        <% } else
                                                                                                                                                            {
                                                                                                                                                            %>
                                                                                                                                                            <div
                                                                                                                                                                class="d-flex flex-column gap-3">
                                                                                                                                                                <% for
                                                                                                                                                                    (Postulacion
                                                                                                                                                                    p
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
                                                                                                                                                                                <%= p.getEstudiante()
                                                                                                                                                                                    %>
                                                                                                                                                                            </h3>
                                                                                                                                                                            <span
                                                                                                                                                                                class="badge bg-info-subtle text-info border border-info-subtle rounded-pill small">
                                                                                                                                                                                <%= p.getEstado()
                                                                                                                                                                                    %>
                                                                                                                                                                            </span>
                                                                                                                                                                        </div>
                                                                                                                                                                        <div
                                                                                                                                                                            class="small text-muted mb-2">
                                                                                                                                                                            Oferta:
                                                                                                                                                                            <strong>
                                                                                                                                                                                <%= p.getOferta()
                                                                                                                                                                                    %>
                                                                                                                                                                            </strong>
                                                                                                                                                                            &bull;
                                                                                                                                                                            Fecha:
                                                                                                                                                                            <%= p.getFechaPostulacion()
                                                                                                                                                                                %>
                                                                                                                                                                        </div>
                                                                                                                                                                        <div
                                                                                                                                                                            class="d-flex gap-2 align-items-center pt-2 border-top">
                                                                                                                                                                           <button type="button"
        class="btn btn-sm btn-outline-primary rounded-pill"
        data-id-postulacion="<%= p.getId() %>"
        data-estudiante="<%= escaparHtml(p.getEstudiante()) %>"
        data-oferta="<%= escaparHtml(p.getOferta()) %>"
        data-seleccionado="<%= "SELECCIONADO".equalsIgnoreCase(p.getEstado()) %>"
        data-evaluado="<%= entrevistasEmpresa.stream().anyMatch(item -> String.valueOf(p.getId()).equals(entrevistaCampo(item, "idPostulacion")) && !"CANCELADA".equalsIgnoreCase(entrevistaCampo(item, "estado"))) %>"
        data-bs-toggle="modal"
        data-bs-target="#modalEvaluacion"
        onclick="abrirEvaluacion(this)"
        <%= !puedeOperar ? "disabled" : "" %>>
    <i class="bi bi-eye me-1"></i>
    Evaluar
</button> 
                                                                                                                                                                            
                                                                                                                                                                        </div>

                                                                                                                                                                        <% if
                                                                                                                                                                            (puedeOperar
                                                                                                                                                                            && "SELECCIONADO"
                                                                                                                                                                            .equalsIgnoreCase(p.getEstado()))
                                                                                                                                                                            {
                                                                                                                                                                            %>
                                                                                                                                                                            <% boolean
                                                                                                                                                                                tieneEntrevista=false;
                                                                                                                                                                                for
                                                                                                                                                                                (String
                                                                                                                                                                                item
                                                                                                                                                                                :
                                                                                                                                                                                entrevistasEmpresa)
                                                                                                                                                                                {
                                                                                                                                                                                if
                                                                                                                                                                                (String.valueOf(p.getId()).equals(entrevistaCampo(item, "idPostulacion"
                                                                                                                                                                                )))
                                                                                                                                                                                {
                                                                                                                                                                                tieneEntrevista=true;
                                                                                                                                                                                break;
                                                                                                                                                                                }
                                                                                                                                                                                }
                                                                                                                                                                                %>
                                                                                                                                                                                <% if
                                                                                                                                                                                    (tieneEntrevista)
                                                                                                                                                                                    {
                                                                                                                                                                                    %>
                                                                                                                                                                                    <button
                                                                                                                                                                                        type="button"
                                                                                                                                                                                        class="btn btn-sm btn-secondary rounded-pill mt-2"
                                                                                                                                                                                        disabled>
                                                                                                                                                                                        <i
                                                                                                                                                                                            class="bi bi-lock-fill me-1"></i>Entrevista
                                                                                                                                                                                        programada
                                                                                                                                                                                    </button>
                                                                                                                                                                                    <% } else
                                                                                                                                                                                        {
                                                                                                                                                                                        %>
                                                                                                                                                                                        <button
                                                                                                                                                                                            type="button"
                                                                                                                                                                                            class="btn btn-sm btn-primary rounded-pill mt-2"
                                                                                                                                                                                            data-id-postulacion="<%= p.getId() %>"
                                                                                                                                                                                            data-bs-toggle="modal"
                                                                                                                                                                                            data-bs-target="#modalEntrevista"
                                                                                                                                                                                            onclick="abrirEntrevista(this.dataset.idPostulacion)">
                                                                                                                                                                                            <i
                                                                                                                                                                                                class="bi bi-calendar-plus me-1"></i>Programar
                                                                                                                                                                                            entrevista
                                                                                                                                                                                        </button>
                                                                                                                                                                                        <% }
                                                                                                                                                                                            %>
                                                                                                                                                                                            <% for
                                                                                                                                                                                                (String
                                                                                                                                                                                                entrevistaItem
                                                                                                                                                                                                :
                                                                                                                                                                                                entrevistasEmpresa)
                                                                                                                                                                                                {
                                                                                                                                                                                                if
                                                                                                                                                                                                (!String.valueOf(p.getId()).equals(entrevistaCampo(entrevistaItem, "idPostulacion"
                                                                                                                                                                                                )))
                                                                                                                                                                                                continue;
                                                                                                                                                                                                String
                                                                                                                                                                                                eid=entrevistaCampo(entrevistaItem, "id"
                                                                                                                                                                                                );
                                                                                                                                                                                                String
                                                                                                                                                                                                ef=entrevistaCampo(entrevistaItem, "fecha"
                                                                                                                                                                                                );
                                                                                                                                                                                                String
                                                                                                                                                                                                eh=entrevistaCampo(entrevistaItem, "hora"
                                                                                                                                                                                                );
                                                                                                                                                                                                String
                                                                                                                                                                                                em=entrevistaCampo(entrevistaItem, "modalidad"
                                                                                                                                                                                                );
                                                                                                                                                                                                String
                                                                                                                                                                                                el=entrevistaCampo(entrevistaItem, "enlaceLugar"
                                                                                                                                                                                                );
                                                                                                                                                                                                String
                                                                                                                                                                                                ee=entrevistaCampo(entrevistaItem, "estado"
                                                                                                                                                                                                );
                                                                                                                                                                                                String
                                                                                                                                                                                                eo=entrevistaCampo(entrevistaItem, "observaciones"
                                                                                                                                                                                                );
                                                                                                                                                                                                %>
                                                                                                                                                                                                <div
                                                                                                                                                                                                    class="border rounded-3 p-2 mt-2 bg-white">
                                                                                                                                                                                                    <div
                                                                                                                                                                                                        class="small fw-semibold mb-2">
                                                                                                                                                                                                        <i
                                                                                                                                                                                                            class="bi bi-calendar-check me-1"></i>
                                                                                                                                                                                                        Entrevista:
                                                                                                                                                                                                        <%= escaparHtml(ef)
                                                                                                                                                                                                            %>
                                                                                                                                                                                                            a
                                                                                                                                                                                                            las
                                                                                                                                                                                                            <%= escaparHtml(eh)
                                                                                                                                                                                                                %>
                                                                                                                                                                                                                ·
                                                                                                                                                                                                                <%= escaparHtml(em)
                                                                                                                                                                                                                    %>
                                                                                                                                                                                                                    ·
                                                                                                                                                                                                                    <%= escaparHtml(ee)
                                                                                                                                                                                                                        %>
                                                                                                                                                                                                    </div>
                                                                                                                                                                                                    <div
                                                                                                                                                                                                        class="d-flex flex-wrap gap-2">
                                                                                                                                                                                                        <button
                                                                                                                                                                                                            type="button"
                                                                                                                                                                                                            class="btn btn-sm btn-outline-primary rounded-pill"
                                                                                                                                                                                                            data-bs-toggle="modal"
                                                                                                                                                                                                            data-bs-target="#modalEntrevista"
                                                                                                                                                                                                            data-id="<%= escaparHtml(eid) %>"
                                                                                                                                                                                                            data-postulacion="<%= p.getId() %>"
                                                                                                                                                                                                            data-fecha="<%= escaparHtml(ef) %>"
                                                                                                                                                                                                            data-hora="<%= escaparHtml(eh) %>"
                                                                                                                                                                                                            data-modalidad="<%= escaparHtml(em) %>"
                                                                                                                                                                                                            data-lugar="<%= escaparHtml(el) %>"
                                                                                                                                                                                                            data-estado="<%= escaparHtml(ee) %>"
                                                                                                                                                                                                            data-observaciones="<%= escaparHtml(eo) %>"
                                                                                                                                                                                                            onclick="editarEntrevista(this)"><i
                                                                                                                                                                                                                class="bi bi-pencil-square me-1"></i>Editar
                                                                                                                                                                                                            entrevista</button>
                                                                                                                                                                                                        <form
                                                                                                                                                                                                            action="${pageContext.request.contextPath}/empresa/entrevista"
                                                                                                                                                                                                            method="POST"
                                                                                                                                                                                                            class="m-0"
                                                                                                                                                                                                            onsubmit="return confirm('¿Eliminar esta entrevista?')">
                                                                                                                                                                                                            <input
                                                                                                                                                                                                                type="hidden"
                                                                                                                                                                                                                name="accion"
                                                                                                                                                                                                                value="eliminar"><input
                                                                                                                                                                                                                type="hidden"
                                                                                                                                                                                                                name="idEntrevista"
                                                                                                                                                                                                                value="<%= escaparHtml(eid) %>">
                                                                                                                                                                                                            <button
                                                                                                                                                                                                                type="submit"
                                                                                                                                                                                                                class="btn btn-sm btn-outline-danger rounded-pill"><i
                                                                                                                                                                                                                    class="bi bi-trash me-1"></i>Eliminar
                                                                                                                                                                                                                entrevista</button>
                                                                                                                                                                                                        </form>
                                                                                                                                                                                                    </div>
                                                                                                                                                                                                </div>
                                                                                                                                                                                                <% }
                                                                                                                                                                                                    %>

                                                                                                                                                                                                    <% }
                                                                                                                                                                                                        %>
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
                                                                                            id="modalEvaluacion"
                                                                                            tabindex="-1"
                                                                                            data-bs-backdrop="static">
                                                                                            <div
                                                                                                class="modal-dialog modal-dialog-centered modal-lg modal-dialog-scrollable">
                                                                                                <div
                                                                                                    class="modal-content">
                                                                                                    <div
                                                                                                        class="modal-header">
                                                                                                        <h5
                                                                                                            class="modal-title">
                                                                                                            <i
                                                                                                                class="bi bi-eye me-2"></i>
                                                                                                            Evaluación
                                                                                                            del
                                                                                                            postulante
                                                                                                        </h5>
                                                                                                    </div>

                                                                                                    <div
                                                                                                        class="modal-body">
                                                                                                        <h6 id="evaluacionEstudiante"
                                                                                                            class="fw-bold">
                                                                                                        </h6>
                                                                                                        <p id="evaluacionOferta"
                                                                                                            class="text-muted">
                                                                                                        </p>

                                                                                                        <h6
                                                                                                            class="fw-bold">
                                                                                                            Currículum
                                                                                                            vitae
                                                                                                        </h6>
                                                                                                        <div id="evaluacionCV"
                                                                                                            class="mb-4">
                                                                                                            Pendiente de
                                                                                                            conectar el
                                                                                                            CV.
                                                                                                        </div>

                                                                                                        <h6
                                                                                                            class="fw-bold">
                                                                                                            Preguntas y
                                                                                                            respuestas
                                                                                                        </h6>
                                                                                                        <div id="evaluacionRespuestas"
                                                                                                            class="mb-3">
                                                                                                            Pendiente de
                                                                                                            conectar las
                                                                                                            12
                                                                                                            respuestas.
                                                                                                        </div>
                                                                                                    </div>

                                                                                                    <div
                                                                                                        class="modal-footer">
                                                                                                        <form
                                                                                                            action="${pageContext.request.contextPath}/empresa/oferta"
                                                                                                            method="POST"
                                                                                                            class="d-flex gap-2">
                                                                                                            <input
                                                                                                                type="hidden"
                                                                                                                name="accion"
                                                                                                                value="cambiarEstadoPostulacion">
                                                                                                            <input
                                                                                                                type="hidden"
                                                                                                                name="idPostulacion"
                                                                                                                id="evaluacionIdPostulacion">

                                                                                                            <button
                                                                                                                type="submit"
                                                                                                                name="estado"
                                                                                                                value="SELECCIONADO"
                                                                                                                class="btn btn-success">Seleccionar</button>

                                                                                                            <button
                                                                                                                type="button"
                                                                                                                name="estado"
                                                                                                                value="NO_SELECCIONADO"
                                                                                                                class="btn btn-danger"
                                                                                                                onclick="abrirModalRechazo()">
                                                                                                                Rechazar
                                                                                                            </button>
                                                                                                        </form>

                                                                                                        <button
                                                                                                            type="button"
                                                                                                            class="btn btn-secondary"
                                                                                                            data-bs-dismiss="modal">Cancelar</button>
                                                                                                    </div>
                                                                                                </div>
                                                                                            </div>
                                                                                        </div>
                                                                                        <div class="modal fade"
                                                                                            id="modalRechazo"
                                                                                            tabindex="-1">
                                                                                            <div
                                                                                                class="modal-dialog modal-dialog-centered">
                                                                                                <div
                                                                                                    class="modal-content">
                                                                                                    <form
                                                                                                        action="${pageContext.request.contextPath}/empresa/oferta"
                                                                                                        method="POST">
                                                                                                        <div
                                                                                                            class="modal-header">
                                                                                                            <h5
                                                                                                                class="modal-title">
                                                                                                                Rechazar
                                                                                                                postulante
                                                                                                            </h5>
                                                                                                            <button
                                                                                                                type="button"
                                                                                                                class="btn-close"
                                                                                                                data-bs-dismiss="modal"></button>
                                                                                                        </div>
                                                                                                        <div
                                                                                                            class="modal-body">
                                                                                                            <p>Escribe
                                                                                                                el
                                                                                                                motivo
                                                                                                                que
                                                                                                                podrá
                                                                                                                consultar
                                                                                                                el
                                                                                                                estudiante.
                                                                                                            </p>
                                                                                                            <input
                                                                                                                type="hidden"
                                                                                                                name="accion"
                                                                                                                value="cambiarEstadoPostulacion">
                                                                                                            <input
                                                                                                                type="hidden"
                                                                                                                name="estado"
                                                                                                                value="NO_SELECCIONADO">
                                                                                                            <input
                                                                                                                type="hidden"
                                                                                                                name="idPostulacion"
                                                                                                                id="rechazoIdPostulacion">
                                                                                                            <textarea
                                                                                                                name="motivoRechazo"
                                                                                                                class="form-control"
                                                                                                                maxlength="1000"
                                                                                                                rows="4"
                                                                                                                required></textarea>
                                                                                                        </div>
                                                                                                        <div
                                                                                                            class="modal-footer">
                                                                                                            <button
                                                                                                                type="button"
                                                                                                                class="btn btn-secondary"
                                                                                                                data-bs-dismiss="modal">Cancelar</button>
                                                                                                            <button
                                                                                                                type="submit"
                                                                                                                class="btn btn-danger">Confirmar
                                                                                                                rechazo</button>
                                                                                                        </div>
                                                                                                    </form>
                                                                                                </div>
                                                                                            </div>
                                                                                        </div>
                                                                                        <div class="modal fade"
                                                                                            id="modalEntrevista"
                                                                                            tabindex="-1">
                                                                                            <div
                                                                                                class="modal-dialog modal-dialog-centered">
                                                                                                <div
                                                                                                    class="modal-content">
                                                                                                    <form
                                                                                                        action="${pageContext.request.contextPath}/empresa/entrevista"
                                                                                                        method="POST">
                                                                                                        <div
                                                                                                            class="modal-header">
                                                                                                            <h5
                                                                                                                class="modal-title">
                                                                                                                Programar
                                                                                                                entrevista
                                                                                                            </h5>
                                                                                                            <button
                                                                                                                type="button"
                                                                                                                class="btn-close"
                                                                                                                data-bs-dismiss="modal"></button>
                                                                                                        </div>

                                                                                                        <div
                                                                                                            class="modal-body">
                                                                                                            <input
                                                                                                                type="hidden"
                                                                                                                name="accion"
                                                                                                                id="entrevistaAccion"
                                                                                                                value="crear">
                                                                                                            <input
                                                                                                                type="hidden"
                                                                                                                name="idPostulacion"
                                                                                                                id="entrevistaPostulacion">
                                                                                                            <input
                                                                                                                type="hidden"
                                                                                                                name="idEntrevista"
                                                                                                                id="entrevistaId">

                                                                                                            <label
                                                                                                                class="form-label">Fecha</label>
                                                                                                            <input
                                                                                                                type="date"
                                                                                                                name="fecha"
                                                                                                                id="entrevistaFecha"
                                                                                                                class="form-control mb-3"
                                                                                                                required>

                                                                                                            <label
                                                                                                                class="form-label">Hora</label>
                                                                                                            <input
                                                                                                                type="time"
                                                                                                                name="hora"
                                                                                                                id="entrevistaHora"
                                                                                                                class="form-control mb-3"
                                                                                                                required>

                                                                                                            <label
                                                                                                                class="form-label">Modalidad</label>
                                                                                                            <select
                                                                                                                name="modalidad"
                                                                                                                id="entrevistaModalidad"
                                                                                                                class="form-select mb-3"
                                                                                                                onchange="cambiarModalidadEntrevista()">
                                                                                                                <option
                                                                                                                    value="VIRTUAL">
                                                                                                                    Virtual
                                                                                                                </option>
                                                                                                                <option
                                                                                                                    value="PRESENCIAL">
                                                                                                                    Presencial
                                                                                                                </option>
                                                                                                            </select>

                                                                                                            <label
                                                                                                                class="form-label"
                                                                                                                id="entrevistaLugarLabel">
                                                                                                                Enlace
                                                                                                                de
                                                                                                                Google
                                                                                                                Meet,
                                                                                                                Zoom
                                                                                                                o Teams
                                                                                                            </label>
                                                                                                            <input
                                                                                                                type="url"
                                                                                                                name="enlaceLugar"
                                                                                                                id="entrevistaLugar"
                                                                                                                class="form-control mb-3"
                                                                                                                placeholder="https://meet.google.com/..."
                                                                                                                maxlength="500"
                                                                                                                required>

                                                                                                            <label
                                                                                                                class="form-label">Estado</label>
                                                                                                            <select
                                                                                                                name="estado"
                                                                                                                id="entrevistaEstado"
                                                                                                                class="form-select mb-3">
                                                                                                                <option
                                                                                                                    value="PROGRAMADA">
                                                                                                                    Programada
                                                                                                                </option>
                                                                                                                <option
                                                                                                                    value="REALIZADA">
                                                                                                                    Realizada
                                                                                                                </option>
                                                                                                                <option
                                                                                                                    value="CANCELADA">
                                                                                                                    Cancelada
                                                                                                                </option>
                                                                                                            </select>

                                                                                                            <label
                                                                                                                class="form-label">Observaciones</label>
                                                                                                            <textarea
                                                                                                                name="observaciones"
                                                                                                                id="entrevistaObservaciones"
                                                                                                                class="form-control"
                                                                                                                maxlength="1000"
                                                                                                                rows="2"></textarea>
                                                                                                        </div>

                                                                                                        <div
                                                                                                            class="modal-footer">
                                                                                                            <button
                                                                                                                type="button"
                                                                                                                class="btn btn-secondary"
                                                                                                                data-bs-dismiss="modal">Cancelar</button>
                                                                                                            <button
                                                                                                                type="submit"
                                                                                                                class="btn btn-primary">
                                                                                                                Guardar
                                                                                                                entrevista
                                                                                                            </button>
                                                                                                        </div>
                                                                                                    </form>
                                                                                                </div>
                                                                                            </div>
                                                                                        </div>

                                                                                        <div class="modal fade"
                                                                                            id="modalEditarOferta"
                                                                                            tabindex="-1"
                                                                                            aria-labelledby="modalEditarOfertaLabel"
                                                                                            aria-hidden="true">
                                                                                            <div
                                                                                                class="modal-dialog modal-dialog-centered modal-lg">
                                                                                                <div
                                                                                                    class="modal-content modal-content-propath">
                                                                                                    <div
                                                                                                        class="modal-header modal-header-propath">
                                                                                                        <h2 class="modal-title h5"
                                                                                                            id="modalEditarOfertaLabel">
                                                                                                            <i
                                                                                                                class="bi bi-pencil-square me-2"></i>
                                                                                                            Editar
                                                                                                            Oferta de
                                                                                                            Prácticas
                                                                                                        </h2>
                                                                                                        <button
                                                                                                            type="button"
                                                                                                            class="btn-close"
                                                                                                            data-bs-dismiss="modal"
                                                                                                            aria-label="Cerrar"></button>
                                                                                                    </div>
                                                                                                    <form
                                                                                                        action="${pageContext.request.contextPath}/empresa/oferta"
                                                                                                        method="POST">
                                                                                                        <input
                                                                                                            type="hidden"
                                                                                                            name="accion"
                                                                                                            value="actualizar">
                                                                                                        <input
                                                                                                            type="hidden"
                                                                                                            name="id"
                                                                                                            id="editOfertaId">
                                                                                                        <div
                                                                                                            class="modal-body modal-body-propath">
                                                                                                            <div
                                                                                                                class="row g-3">
                                                                                                                <div
                                                                                                                    class="col-12 col-md-6">
                                                                                                                    <label
                                                                                                                        class="form-label small fw-semibold">Título
                                                                                                                        de
                                                                                                                        la
                                                                                                                        Convocatoria
                                                                                                                        *</label>
                                                                                                                    <input
                                                                                                                        type="text"
                                                                                                                        class="form-control"
                                                                                                                        name="titulo"
                                                                                                                        id="editOfertaTitulo"
                                                                                                                        required>
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    class="col-12 col-md-6">
                                                                                                                    <label
                                                                                                                        class="form-label small fw-semibold">Carrera
                                                                                                                        Profesional
                                                                                                                        *</label>
                                                                                                                    <input
                                                                                                                        type="text"
                                                                                                                        class="form-control"
                                                                                                                        name="carrera"
                                                                                                                        id="editOfertaCarrera"
                                                                                                                        required>
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    class="col-12 col-md-4">
                                                                                                                    <label
                                                                                                                        class="form-label small fw-semibold">Modalidad</label>
                                                                                                                    <select
                                                                                                                        class="form-select"
                                                                                                                        name="modalidad"
                                                                                                                        id="editOfertaModalidad">
                                                                                                                        <option
                                                                                                                            value="PRESENCIAL">
                                                                                                                            Presencial
                                                                                                                        </option>
                                                                                                                        <option
                                                                                                                            value="HIBRIDA">
                                                                                                                            Híbrida
                                                                                                                        </option>
                                                                                                                        <option
                                                                                                                            value="VIRTUAL">
                                                                                                                            Remota
                                                                                                                        </option>
                                                                                                                    </select>
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    class="col-12 col-md-4">
                                                                                                                    <label
                                                                                                                        class="form-label small fw-semibold">Vacantes
                                                                                                                        Disponibles
                                                                                                                        *</label>
                                                                                                                    <input
                                                                                                                        type="number"
                                                                                                                        class="form-control"
                                                                                                                        name="vacantes"
                                                                                                                        id="editOfertaVacantes"
                                                                                                                        min="1"
                                                                                                                        max="50"
                                                                                                                        required>
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    class="col-12 col-md-4">
                                                                                                                    <label
                                                                                                                        class="form-label small fw-semibold">Fecha
                                                                                                                        Límite
                                                                                                                        *</label>
                                                                                                                    <input
                                                                                                                        type="date"
                                                                                                                        class="form-control"
                                                                                                                        name="fechaLimite"
                                                                                                                        id="editOfertaFecha"
                                                                                                                        required>
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    class="col-12 col-md-6">
                                                                                                                    <label
                                                                                                                        class="form-label small fw-semibold">Ubicación
                                                                                                                        /
                                                                                                                        Distrito</label>
                                                                                                                    <input
                                                                                                                        type="text"
                                                                                                                        class="form-control"
                                                                                                                        name="ubicacion"
                                                                                                                        id="editOfertaUbicacion"
                                                                                                                        placeholder="Lima">
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    class="col-12 col-md-6">
                                                                                                                    <label
                                                                                                                        class="form-label small fw-semibold">
                                                                                                                        Subvención
                                                                                                                        Económica
                                                                                                                        (Opcional)
                                                                                                                    </label>
                                                                                                                    <div
                                                                                                                        class="input-group">
                                                                                                                        <span
                                                                                                                            class="input-group-text">S/</span>
                                                                                                                        <input
                                                                                                                            type="number"
                                                                                                                            class="form-control"
                                                                                                                            name="remuneracion"
                                                                                                                            id="editOfertaRemuneracion"
                                                                                                                            min="0"
                                                                                                                            step="0.01"
                                                                                                                            placeholder="520.00">
                                                                                                                    </div>
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    class="col-12 col-md-6">
                                                                                                                    <label
                                                                                                                        class="form-label small fw-semibold">Estado</label>
                                                                                                                    <input
                                                                                                                        type="text"
                                                                                                                        class="form-control"
                                                                                                                        id="editOfertaEstado"
                                                                                                                        readonly>
                                                                                                                    <div
                                                                                                                        class="form-text">
                                                                                                                        Solo
                                                                                                                        el
                                                                                                                        administrador
                                                                                                                        puede
                                                                                                                        cambiar
                                                                                                                        el
                                                                                                                        estado.
                                                                                                                    </div>
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    class="col-12">
                                                                                                                    <label
                                                                                                                        class="form-label small fw-semibold">Descripción</label>
                                                                                                                    <textarea
                                                                                                                        class="form-control"
                                                                                                                        name="descripcion"
                                                                                                                        id="editOfertaDescripcion"
                                                                                                                        rows="2"></textarea>
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    class="col-12">
                                                                                                                    <label
                                                                                                                        class="form-label small fw-semibold">Requisitos</label>
                                                                                                                    <textarea
                                                                                                                        class="form-control"
                                                                                                                        name="requisitos"
                                                                                                                        id="editOfertaRequisitos"
                                                                                                                        rows="3"></textarea>
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
                                                                                                                    class="bi bi-check2 me-1"></i>
                                                                                                                Guardar
                                                                                                                Cambios
                                                                                                            </button>
                                                                                                        </div>
                                                                                                    </form>
                                                                                                </div>
                                                                                            </div>
                                                                                        </div>

                                                                                        <div class="modal fade"
                                                                                            id="modalEditarPerfilEmpresa"
                                                                                            tabindex="-1"
                                                                                            aria-labelledby="modalEditarPerfilEmpresaLabel"
                                                                                            aria-hidden="true">
                                                                                            <div
                                                                                                class="modal-dialog modal-dialog-centered">
                                                                                                <div
                                                                                                    class="modal-content modal-content-propath">
                                                                                                    <div
                                                                                                        class="modal-header modal-header-propath">
                                                                                                        <h2 class="modal-title h5"
                                                                                                            id="modalEditarPerfilEmpresaLabel">
                                                                                                            <i
                                                                                                                class="bi bi-building-gear me-2"></i>
                                                                                                            Actualizar
                                                                                                            Mi Empresa
                                                                                                        </h2>
                                                                                                        <button
                                                                                                            type="button"
                                                                                                            class="btn-close"
                                                                                                            data-bs-dismiss="modal"
                                                                                                            aria-label="Cerrar"></button>
                                                                                                    </div>
                                                                                                    <form
                                                                                                        action="${pageContext.request.contextPath}/empresa/oferta"
                                                                                                        method="POST">
                                                                                                        <input
                                                                                                            type="hidden"
                                                                                                            name="accion"
                                                                                                            value="actualizarPerfilEmpresa">
                                                                                                        <div
                                                                                                            class="modal-body modal-body-propath">
                                                                                                            <div
                                                                                                                class="row g-3">
                                                                                                                <div
                                                                                                                    class="col-sm-6">
                                                                                                                    <label
                                                                                                                        class="form-label small fw-semibold">Nombres
                                                                                                                        del
                                                                                                                        Representante
                                                                                                                        *</label>
                                                                                                                    <input
                                                                                                                        type="text"
                                                                                                                        class="form-control"
                                                                                                                        name="representanteNombre"
                                                                                                                        value="<%= (miEmpresa != null && miEmpresa.getRepresentanteNombre() != null && !miEmpresa.getRepresentanteNombre().isEmpty()) ? miEmpresa.getRepresentanteNombre() : (usuario.getNombre() != null ? usuario.getNombre() : "") %>"
                                                                                                                        required>
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    class="col-sm-6">
                                                                                                                    <label
                                                                                                                        class="form-label small fw-semibold">Apellidos
                                                                                                                        del
                                                                                                                        Representante
                                                                                                                        *</label>
                                                                                                                    <input
                                                                                                                        type="text"
                                                                                                                        class="form-control"
                                                                                                                        name="representanteApellidos"
                                                                                                                        value="<%= (miEmpresa != null && miEmpresa.getRepresentanteApellidos() != null && !miEmpresa.getRepresentanteApellidos().isEmpty()) ? miEmpresa.getRepresentanteApellidos() : (usuario.getApellidos() != null ? usuario.getApellidos() : "") %>"
                                                                                                                        required>
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
                                                                                                                <div
                                                                                                                    class="col-12">
                                                                                                                    <label
                                                                                                                        class="form-label small fw-semibold">Razón
                                                                                                                        Social
                                                                                                                        *</label>
                                                                                                                    <input
                                                                                                                        type="text"
                                                                                                                        class="form-control"
                                                                                                                        name="razonSocial"
                                                                                                                        value="<%= razon != null ? razon : "" %>"
                                                                                                                        required>
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    class="col-12 col-md-6">
                                                                                                                    <label
                                                                                                                        class="form-label small fw-semibold">RUC
                                                                                                                        (11
                                                                                                                        dígitos)</label>
                                                                                                                    <input
                                                                                                                        type="text"
                                                                                                                        class="form-control"
                                                                                                                        name="ruc"
                                                                                                                        value="<%= miEmpresa != null ? miEmpresa.getRuc() : "" %>"
                                                                                                                        pattern="[0-9]{11}"
                                                                                                                        maxlength="11"
                                                                                                                        inputmode="numeric"
                                                                                                                        title="El RUC debe contener exactamente 11 dígitos"
                                                                                                                        required>
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    class="col-12 col-md-6">
                                                                                                                    <label
                                                                                                                        class="form-label small fw-semibold">Teléfono
                                                                                                                        de
                                                                                                                        Contacto</label>
                                                                                                                    <input
                                                                                                                        type="text"
                                                                                                                        class="form-control"
                                                                                                                        name="telefono"
                                                                                                                        value="<%= (miEmpresa != null && miEmpresa.getTelefono() != null) ? miEmpresa.getTelefono() : "" %>">
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    class="col-12">
                                                                                                                    <label
                                                                                                                        class="form-label small fw-semibold">Dirección</label>
                                                                                                                    <input
                                                                                                                        type="text"
                                                                                                                        class="form-control"
                                                                                                                        name="direccion"
                                                                                                                        value="<%= (miEmpresa != null && miEmpresa.getDireccion() != null) ? miEmpresa.getDireccion() : "" %>">
                                                                                                                </div>
                                                                                                                <div
                                                                                                                    class="col-12">
                                                                                                                    <label
                                                                                                                        class="form-label small fw-semibold">Descripción
                                                                                                                        Corporativa</label>
                                                                                                                    <textarea
                                                                                                                        class="form-control"
                                                                                                                        name="descripcion"
                                                                                                                        rows="2"><%= (miEmpresa != null && miEmpresa.getDescripcion() != null) ? miEmpresa.getDescripcion() : "" %></textarea>
                                                                                                                </div>
                                                                                                            </div>
                                                                                                        </div>
                                                                                                        <div
                                                                                                            class="modal-footer bg-light border-0 d-flex justify-content-between">
                                                                                                            <button
                                                                                                                type="button"
                                                                                                                class="btn btn-outline-danger rounded-pill px-3"
                                                                                                                onclick="mostrarModalConfirmacion('Dar de baja empresa', '¿Seguro que deseas solicitar la baja de tu cuenta corporativa?', function() { const f = document.getElementById('formBajaEmpresa'); f.submit(); });">
                                                                                                                <i
                                                                                                                    class="bi bi-person-x me-1"></i>
                                                                                                                Dar de
                                                                                                                baja
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
                                                                                                        id="formBajaEmpresa"
                                                                                                        action="${pageContext.request.contextPath}/empresa/oferta"
                                                                                                        method="POST"
                                                                                                        style="display: none;">
                                                                                                        <input
                                                                                                            type="hidden"
                                                                                                            name="accion"
                                                                                                            value="darDeBaja">
                                                                                                    </form>
                                                                                                </div>
                                                                                            </div>
                                                                                        </div>

                                                                                        <script>
                                                                                            function abrirModalEditarOferta(btn) {
                                                                                                document.getElementById('editOfertaId').value =
                                                                                                    btn.getAttribute('data-id') || '';

                                                                                                document.getElementById('editOfertaTitulo').value =
                                                                                                    btn.getAttribute('data-titulo') || '';

                                                                                                document.getElementById('editOfertaCarrera').value =
                                                                                                    btn.getAttribute('data-carrera') || '';

                                                                                                document.getElementById('editOfertaModalidad').value =
                                                                                                    btn.getAttribute('data-modalidad') || 'PRESENCIAL';

                                                                                                document.getElementById('editOfertaVacantes').value =
                                                                                                    btn.getAttribute('data-vacantes') || '1';

                                                                                                document.getElementById('editOfertaFecha').value =
                                                                                                    btn.getAttribute('data-fecha') || '';

                                                                                                document.getElementById('editOfertaUbicacion').value =
                                                                                                    btn.getAttribute('data-ubicacion') || '';

                                                                                                document.getElementById('editOfertaDescripcion').value =
                                                                                                    btn.getAttribute('data-descripcion') || '';

                                                                                                document.getElementById('editOfertaEstado').value =
                                                                                                    btn.getAttribute('data-estado') || 'PENDIENTE';
                                                                                                document.getElementById('editOfertaRequisitos').value =
                                                                                                    btn.getAttribute('data-requisitos') || '';

                                                                                                const remuneracionGuardada =
                                                                                                    btn.getAttribute('data-remuneracion') || '';

                                                                                                document.getElementById('editOfertaRemuneracion').value =
                                                                                                    remuneracionGuardada.replace(/^S\/\s*/, '').trim();

                                                                                                const modalEl = document.getElementById('modalEditarOferta');

                                                                                                if (window.bootstrap && window.bootstrap.Modal) {
                                                                                                    window.bootstrap.Modal.getOrCreateInstance(modalEl).show();
                                                                                                }
                                                                                            }
                                                                                            function abrirModalRechazo() {
                                                                                                document.getElementById("rechazoIdPostulacion").value =
                                                                                                    document.getElementById("evaluacionIdPostulacion").value;

                                                                                                bootstrap.Modal.getOrCreateInstance(
                                                                                                    document.getElementById("modalEvaluacion")
                                                                                                ).hide();

                                                                                                bootstrap.Modal.getOrCreateInstance(
                                                                                                    document.getElementById("modalRechazo")
                                                                                                ).show();
                                                                                            }
                                                                                            async function abrirEvaluacion(boton) {
                                                                                                const id = boton.dataset.idPostulacion;
                                                                                                const cv = document.getElementById("evaluacionCV");
                                                                                                const respuestas = document.getElementById("evaluacionRespuestas");
                                                                                                const preguntas = [
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
                                                                                                const seleccionado = boton.dataset.seleccionado === "true";
                                                                                                const tieneEntrevista = boton.dataset.evaluado === "true";
                                                                                                document.querySelector('#modalEvaluacion button[value="SELECCIONADO"]').disabled = seleccionado || tieneEntrevista;
                                                                                                document.querySelector('#modalEvaluacion button[value="NO_SELECCIONADO"]').disabled = tieneEntrevista;
                                                                                                document.getElementById("evaluacionIdPostulacion").value = id;
                                                                                                document.getElementById("evaluacionEstudiante").textContent = boton.dataset.estudiante || "Postulante";
                                                                                                document.getElementById("evaluacionOferta").textContent = "Oferta: " + (boton.dataset.oferta || "");
                                                                                                cv.textContent = "Cargando CV...";
                                                                                                respuestas.textContent = "Cargando respuestas...";
                                                                                                const ruta = "${pageContext.request.contextPath}/empresa/oferta";
                                                                                                try {
                                                                                                    const resultado = await fetch(ruta + "?accion=evaluacion&idPostulacion=" + encodeURIComponent(id),
                                                                                                        { credentials: "same-origin", cache: "no-store" });
                                                                                                    if (!resultado.ok) throw new Error("HTTP " + resultado.status);
                                                                                                    const datos = await resultado.json();
                                                                                                    cv.replaceChildren();
                                                                                                    if (datos.cvDisponible) {
                                                                                                        const iframe = document.createElement("iframe");
                                                                                                        iframe.src = ruta + "?accion=cv&idPostulacion=" + encodeURIComponent(id);
                                                                                                        iframe.title = "CV del postulante";
                                                                                                        iframe.style.width = "100%";
                                                                                                        iframe.style.height = "430px";
                                                                                                        iframe.style.border = "1px solid #ddd";
                                                                                                        cv.appendChild(iframe);
                                                                                                        const enlace = document.createElement("a");
                                                                                                        enlace.href = iframe.src;
                                                                                                        enlace.target = "_blank";
                                                                                                        enlace.rel = "noopener";
                                                                                                        enlace.textContent = "Abrir CV en otra pestaña";
                                                                                                        cv.appendChild(enlace);
                                                                                                    } else {
                                                                                                        cv.textContent = "Este postulante no tiene CV registrado.";
                                                                                                    }
                                                                                                    respuestas.replaceChildren();
                                                                                                    const lista = Array.isArray(datos.respuestas) ? datos.respuestas : [];
                                                                                                    if (lista.length === 0) respuestas.textContent = "No hay respuestas registradas.";
                                                                                                    lista.sort((x, y) => Number(x.numeroPregunta) - Number(y.numeroPregunta));
                                                                                                    lista.forEach(item => {
                                                                                                        const numero = Number(item.numeroPregunta);
                                                                                                        const bloque = document.createElement("div");
                                                                                                        bloque.className = "border rounded p-3 mb-2";
                                                                                                        const pregunta = document.createElement("strong");
                                                                                                        pregunta.textContent = numero + ". " + (preguntas[numero - 1] || "Pregunta");
                                                                                                        const respuesta = document.createElement("p");
                                                                                                        respuesta.className = "mb-0 mt-2";
                                                                                                        respuesta.textContent = item.respuesta || "Sin respuesta";
                                                                                                        bloque.append(pregunta, respuesta);
                                                                                                        respuestas.appendChild(bloque);
                                                                                                    });
                                                                                                } catch (error) {
                                                                                                    cv.textContent = "No se pudo cargar el CV.";
                                                                                                    respuestas.textContent = "Error al consultar la evaluación: " + error.message;
                                                                                                }
                                                                                            }
                                                                                            function abrirEntrevista(idPostulacion) {
                                                                                                document.getElementById("entrevistaAccion").value = "crear";
                                                                                                document.getElementById("entrevistaId").value = "";
                                                                                                document.getElementById("entrevistaPostulacion").value = idPostulacion;
                                                                                                document.getElementById("entrevistaFecha").value = "";
                                                                                                document.getElementById("entrevistaHora").value = "";
                                                                                                document.getElementById("entrevistaModalidad").value = "VIRTUAL";
                                                                                                document.getElementById("entrevistaLugar").value = "";
                                                                                                document.getElementById("entrevistaEstado").value = "PROGRAMADA";
                                                                                                document.getElementById("entrevistaObservaciones").value = "";

                                                                                                cambiarModalidadEntrevista();
                                                                                            }

                                                                                            function editarEntrevista(boton) {
                                                                                                const d = boton.dataset;
                                                                                                document.getElementById("entrevistaAccion").value = "actualizar";
                                                                                                document.getElementById("entrevistaId").value = d.id;
                                                                                                document.getElementById("entrevistaPostulacion").value = d.postulacion;
                                                                                                document.getElementById("entrevistaFecha").value = d.fecha;
                                                                                                document.getElementById("entrevistaHora").value = d.hora.substring(0, 5);
                                                                                                document.getElementById("entrevistaModalidad").value = d.modalidad;
                                                                                                cambiarModalidadEntrevista();
                                                                                                document.getElementById("entrevistaLugar").value = d.lugar;
                                                                                                document.getElementById("entrevistaEstado").value = d.estado;
                                                                                                document.getElementById("entrevistaObservaciones").value = d.observaciones;
                                                                                            }

                                                                                            function cambiarModalidadEntrevista() {
                                                                                                const virtual =
                                                                                                    document.getElementById("entrevistaModalidad").value === "VIRTUAL";

                                                                                                const campo = document.getElementById("entrevistaLugar");

                                                                                                campo.type = virtual ? "url" : "text";
                                                                                                campo.placeholder = virtual
                                                                                                    ? "https://meet.google.com/..."
                                                                                                    : "Dirección de la entrevista";

                                                                                                document.getElementById("entrevistaLugarLabel").textContent =
                                                                                                    virtual ? "Enlace de reunión" : "Dirección o lugar";
                                                                                            }



                                                                                            window.abrirModalEditarOferta = abrirModalEditarOferta;

                                                                                        </script>
                                                                                        <script type="module"
                                                                                            src="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/js/bootstrap.bundle.min.js"></script>
                                                                                        <script type="module"
                                                                                            src="${pageContext.request.contextPath}/js/script.js"></script>
                                                                                    </body>

                                                                                    </html>