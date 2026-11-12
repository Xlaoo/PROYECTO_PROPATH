<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.propath.modelo.Usuario"%>
<%
    String activePage = (String) request.getAttribute("activePage");
    if (activePage == null) {
        activePage = "";
    }
    Usuario uSidebarLog = (Usuario) session.getAttribute("usuarioLogueado");
    boolean esAdminPrinc = (uSidebarLog != null && uSidebarLog.esAdminPrincipal());
%>

<aside class="admin-sidebar">
    
    <div class="sidebar-brand">
        <div class="sidebar-logo-container">
            <a href="${pageContext.request.contextPath}/admin/dashboard" class="d-block text-decoration-none">
                <img src="${pageContext.request.contextPath}/img/logo-propath.png" 
                     alt="ProPath" 
                     class="sidebar-logo"
                     style="object-fit: contain;">
            </a>
        </div>
    </div>

    
    <div class="sidebar-nav-container">
        <div class="sidebar-heading">Menú Principal</div>

        <nav class="nav flex-column">
            
            <a class="nav-link-admin <%= "dashboard".equals(activePage) ? "active" : "" %>" 
               href="${pageContext.request.contextPath}/admin/dashboard">
                <i class="bi bi-grid-1x2-fill"></i>
                <span>Inicio</span>
            </a>

            
            <a class="nav-link-admin <%= "estudiantes".equals(activePage) ? "active" : "" %>" 
               href="${pageContext.request.contextPath}/admin/estudiantes">
                <i class="bi bi-mortarboard-fill"></i>
                <span>Estudiantes</span>
            </a>

            
            <a class="nav-link-admin <%= "empresas".equals(activePage) ? "active" : "" %>" 
               href="${pageContext.request.contextPath}/admin/empresas">
                <i class="bi bi-building-fill"></i>
                <span>Empresas</span>
            </a>

            
            <a class="nav-link-admin <%= "ofertas".equals(activePage) ? "active" : "" %>" 
               href="${pageContext.request.contextPath}/admin/ofertas.jsp">
                <i class="bi bi-briefcase-fill"></i>
                <span>Ofertas</span>
            </a>

            
            <a class="nav-link-admin <%= "postulaciones".equals(activePage) ? "active" : "" %>" 
               href="${pageContext.request.contextPath}/admin/postulaciones.jsp">
                <i class="bi bi-file-earmark-person-fill"></i>
                <span>Postulaciones</span>
            </a>

            <% if (esAdminPrinc) { %>
            <a class="nav-link-admin <%= "administradores".equals(activePage) ? "active" : "" %>" 
               href="${pageContext.request.contextPath}/admin/administradores">
                <i class="bi bi-shield-lock-fill"></i>
                <span>Administradores</span>
            </a>
            <% } %>

            <a class="nav-link-admin <%= "usuarios".equals(activePage) ? "active" : "" %>" 
               href="${pageContext.request.contextPath}/admin/usuarios">
                <i class="bi bi-people-fill"></i>
                <span>Usuarios</span>
            </a>
        </nav>
    </div>

    
    <div class="sidebar-footer">
        <a class="nav-link-admin nav-link-logout" 
           href="${pageContext.request.contextPath}/LogoutServlet">
            <i class="bi bi-box-arrow-right"></i>
            <span>Cerrar sesión</span>
        </a>
    </div>
</aside>