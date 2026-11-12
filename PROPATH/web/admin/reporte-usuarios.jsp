<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.propath.modelo.Usuario"%>
<%@page import="com.propath.util.HtmlUtil"%>
<%
    // Si se accede directamente al JSP sin pasar por el controlador AdminServlet, redirigir
    if (request.getAttribute("listaUsuarios") == null) {
        String query = request.getQueryString();
        String destino = request.getContextPath() + "/admin/reporte-usuarios" + (query != null && !query.isEmpty() ? "?" + query : "");
        response.sendRedirect(destino);
        return;
    }

    @SuppressWarnings("unchecked")
    List<Usuario> listaUsuarios = (List<Usuario>) request.getAttribute("listaUsuarios");
    String busqueda = (String) request.getAttribute("busqueda");
    String rol = (String) request.getAttribute("rol");
    String estado = (String) request.getAttribute("estado");
    Integer totalUsuariosObj = (Integer) request.getAttribute("totalUsuarios");
    int totalUsuarios = (totalUsuariosObj != null) ? totalUsuariosObj : (listaUsuarios != null ? listaUsuarios.size() : 0);
    String fechaGeneracion = (String) request.getAttribute("fechaGeneracion");
    String adminGenerador = (String) request.getAttribute("adminGenerador");

    if (busqueda == null) busqueda = "";
    if (rol == null) rol = "TODOS";
    if (estado == null) estado = "TODOS";
    if (fechaGeneracion == null) fechaGeneracion = "";
    if (adminGenerador == null) adminGenerador = "Administrador del Sistema";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reporte de Cuentas de Usuario - ProPath</title>
    
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@6.0.0-alpha.1/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

    <style>
        body {
            font-family: 'Inter', system-ui, -apple-system, sans-serif;
            background-color: #f8fafc;
            color: #1e293b;
            padding: 30px 20px;
        }

        .reporte-container {
            max-width: 1000px;
            margin: 0 auto;
            background: #ffffff;
            border-radius: 12px;
            padding: 40px;
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
            border: 1px solid #e2e8f0;
        }

        .reporte-header {
            border-bottom: 2px solid #0891b2;
            padding-bottom: 20px;
            margin-bottom: 25px;
        }

        .reporte-logo {
            max-height: 50px;
            width: auto;
        }

        .reporte-title {
            font-size: 1.5rem;
            font-weight: 800;
            color: #0f172a;
            margin: 0;
        }

        .reporte-subtitle {
            font-size: 0.9rem;
            color: #64748b;
            margin-top: 4px;
        }

        .meta-box {
            background-color: #f1f5f9;
            border-radius: 8px;
            padding: 15px 20px;
            margin-bottom: 25px;
            font-size: 0.85rem;
        }

        .meta-box .meta-label {
            font-weight: 600;
            color: #475569;
        }

        .meta-box .meta-value {
            color: #0f172a;
            font-weight: 500;
        }

        .table-reporte {
            width: 100%;
            border-collapse: collapse;
            font-size: 0.85rem;
        }

        .table-reporte th {
            background-color: #0f172a;
            color: #ffffff;
            font-weight: 600;
            padding: 10px 12px;
            text-align: left;
            border: 1px solid #0f172a;
        }

        .table-reporte td {
            padding: 9px 12px;
            border: 1px solid #cbd5e1;
            color: #334155;
            vertical-align: middle;
        }

        .table-reporte tr:nth-child(even) {
            background-color: #f8fafc;
        }

        .badge-rol {
            display: inline-block;
            padding: 3px 8px;
            border-radius: 12px;
            font-size: 0.72rem;
            font-weight: 600;
            text-align: center;
        }
        .badge-rol-admin { background: #fee2e2; color: #991b1b; }
        .badge-rol-estudiante { background: #dbeafe; color: #1e40af; }
        .badge-rol-empresa { background: #cffafe; color: #155e75; }

        .badge-estado {
            display: inline-block;
            padding: 3px 8px;
            border-radius: 12px;
            font-size: 0.72rem;
            font-weight: 600;
        }
        .badge-estado-activo { background: #dcfce7; color: #166534; }
        .badge-estado-inactivo { background: #f1f5f9; color: #64748b; }
        .badge-estado-bloqueado { background: #fee2e2; color: #991b1b; }
        .badge-estado-pendiente { background: #fef3c7; color: #92400e; }

        .reporte-footer {
            margin-top: 35px;
            padding-top: 15px;
            border-top: 1px solid #e2e8f0;
            font-size: 0.75rem;
            color: #94a3b8;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .no-print-bar {
            max-width: 1000px;
            margin: 0 auto 20px auto;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        @media print {
            body {
                background: #ffffff;
                padding: 0;
                margin: 0;
            }
            .no-print-bar {
                display: none !important;
            }
            .reporte-container {
                box-shadow: none;
                border: none;
                padding: 0;
                max-width: 100%;
            }
            @page {
                size: A4 portrait;
                margin: 1.5cm;
            }
        }
    </style>
</head>
<body>

    <!-- Barra de Control (no imprimible) -->
    <div class="no-print-bar d-flex justify-content-between align-items-center">
        <a href="${pageContext.request.contextPath}/admin/usuarios" class="btn btn-outline-secondary rounded-pill px-3 py-2 fw-semibold">
            <i class="bi bi-arrow-left me-1"></i> Volver a Usuarios
        </a>
        <div class="d-flex gap-2">
            <button type="button" class="btn btn-primary rounded-pill px-4 py-2 fw-semibold shadow-sm" onclick="window.print()">
                <i class="bi bi-printer-fill me-1"></i> Imprimir / Guardar como PDF
            </button>
        </div>
    </div>

    <!-- Contenedor Oficial del Reporte -->
    <div class="reporte-container">
        
        <!-- Encabezado Institucional -->
        <div class="reporte-header d-flex justify-content-between align-items-center">
            <div>
                <img src="${pageContext.request.contextPath}/img/logo.png" alt="ProPath" class="reporte-logo mb-2">
                <h1 class="reporte-title">Reporte Oficial de Cuentas de Usuario</h1>
                <p class="reporte-subtitle mb-0">Sistema de Gestión de Prácticas Pre-Profesionales ProPath</p>
            </div>
            <div class="text-end">
                <span class="badge bg-primary px-3 py-2 rounded-pill fw-semibold" style="font-size: 0.85rem;">
                    Módulo Administrativo
                </span>
            </div>
        </div>

        <!-- Metadatos de la Emisión y Filtros Aplicados -->
        <div class="meta-box">
            <div class="row g-2">
                <div class="col-sm-6">
                    <span class="meta-label">Fecha y hora de emisión:</span> 
                    <span class="meta-value"><%= HtmlUtil.escape(fechaGeneracion) %></span>
                </div>
                <div class="col-sm-6">
                    <span class="meta-label">Generado por:</span> 
                    <span class="meta-value"><%= HtmlUtil.escape(adminGenerador) %> (Rol: ADMIN)</span>
                </div>
                <div class="col-sm-6">
                    <span class="meta-label">Filtro por Rol:</span> 
                    <span class="meta-value"><%= !"TODOS".equals(rol) ? HtmlUtil.escape(rol) : "Todos los roles (ADMIN, ESTUDIANTE, EMPRESA)" %></span>
                </div>
                <div class="col-sm-6">
                    <span class="meta-label">Filtro por Estado:</span> 
                    <span class="meta-value"><%= !"TODOS".equals(estado) ? HtmlUtil.escape(estado) : "Todos los estados (ACTIVO, INACTIVO, BLOQUEADO)" %></span>
                </div>
                <% if (!busqueda.isEmpty()) { %>
                <div class="col-12">
                    <span class="meta-label">Criterio de búsqueda:</span> 
                    <span class="meta-value">&quot;<%= HtmlUtil.escape(busqueda) %>&quot;</span>
                </div>
                <% } %>
                <div class="col-12 pt-1 border-top">
                    <span class="meta-label">Total de registros coincidentes:</span> 
                    <span class="meta-value fw-bold text-primary"><%= totalUsuarios %> usuarios</span>
                </div>
            </div>
        </div>

        <!-- Tabla de Datos Filtrados -->
        <div class="table-responsive">
            <table class="table-reporte">
                <thead>
                    <tr>
                        <th style="width: 5%; text-align: center;">#</th>
                        <th style="width: 8%;">ID</th>
                        <th style="width: 25%;">Nombre Completo</th>
                        <th style="width: 27%;">Correo Electrónico</th>
                        <th style="width: 13%; text-align: center;">Rol</th>
                        <th style="width: 10%; text-align: center;">Estado</th>
                        <th style="width: 12%;">Fecha Registro</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (listaUsuarios == null || listaUsuarios.isEmpty()) { %>
                    <tr>
                        <td colspan="7" class="text-center py-4 text-muted">
                            <em>No se encontraron cuentas de usuario que coincidan con los criterios especificados.</em>
                        </td>
                    </tr>
                    <% } else {
                        int num = 1;
                        for (Usuario u : listaUsuarios) {
                            String rolU = u.getRol() != null ? u.getRol().toUpperCase() : "";
                            String claseRol = "ADMIN".equals(rolU) ? "badge-rol-admin" : ("ESTUDIANTE".equals(rolU) ? "badge-rol-estudiante" : "badge-rol-empresa");
                            String estU = u.getEstado() != null ? u.getEstado().toUpperCase() : "";
                            String claseEstado = "ACTIVO".equals(estU) ? "badge-estado-activo" : ("INACTIVO".equals(estU) ? "badge-estado-inactivo" : "badge-estado-bloqueado");
                            String nombreCompleto = (u.getNombre() != null ? u.getNombre() : "") + " " + (u.getApellidos() != null ? u.getApellidos() : "");
                            String fechaReg = u.getFechaRegistro() != null ? u.getFechaRegistro() : "—";
                            if (fechaReg.length() >= 10) {
                                fechaReg = fechaReg.substring(0, 10);
                            }
                    %>
                    <tr>
                        <td style="text-align: center;"><%= num++ %></td>
                        <td><strong>#<%= u.getId() %></strong></td>
                        <td><%= HtmlUtil.escape(nombreCompleto.trim().isEmpty() ? "—" : nombreCompleto.trim()) %></td>
                        <td><%= HtmlUtil.escape(u.getCorreo()) %></td>
                        <td style="text-align: center;">
                            <span class="badge-rol <%= claseRol %>"><%= HtmlUtil.escape(rolU) %></span>
                        </td>
                        <td style="text-align: center;">
                            <span class="badge-estado <%= claseEstado %>"><%= HtmlUtil.escape(estU) %></span>
                        </td>
                        <td><%= HtmlUtil.escape(fechaReg) %></td>
                    </tr>
                    <%  }
                       } %>
                </tbody>
            </table>
        </div>

        <!-- Pie de Página Institucional -->
        <div class="reporte-footer">
            <span>ProPath &bull; Plataforma de Inserción Laboral para Prácticas Pre-Profesionales</span>
            <span>Documento emitido exclusivamente para fines de auditoría y gestión administrativa</span>
        </div>

    </div>

</body>
</html>
