<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String activePage = (String) request.getAttribute("activePage");
    if (activePage == null) {
        activePage = "";
    }
%>

<aside class="admin-sidebar">
    
    <div class="sidebar-brand">
        <div class="sidebar-logo-container">
            <a href="${pageContext.request.contextPath}/admin/dashboard.jsp" class="d-block text-decoration-none">
                <img src="${pageContext.request.contextPath}/img/logo.png" 
                     alt="ProPath" 
                     class="sidebar-logo">
            </a>
        </div>
    </div>

    
    <div class="sidebar-nav-container">
        <div class="sidebar-heading">Menú Principal</div>

        <nav class="nav flex-column">
            
            <a class="nav-link-admin <%= "dashboard".equals(activePage) ? "active" : "" %>" 
               href="${pageContext.request.contextPath}/admin/dashboard.jsp">
                <i class="bi bi-grid-1x2-fill"></i>
                <span>Inicio</span>
            </a>

            
            <a class="nav-link-admin <%= "estudiantes".equals(activePage) ? "active" : "" %>" 
               href="${pageContext.request.contextPath}/admin/estudiantes.jsp">
                <i class="bi bi-mortarboard-fill"></i>
                <span>Estudiantes</span>
            </a>

            
            <a class="nav-link-admin <%= "empresas".equals(activePage) ? "active" : "" %>" 
               href="${pageContext.request.contextPath}/admin/empresas.jsp">
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

            <a class="nav-link-admin <%= "usuarios".equals(activePage) ? "active" : "" %>" 
               href="${pageContext.request.contextPath}/admin/usuarios.jsp">
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
