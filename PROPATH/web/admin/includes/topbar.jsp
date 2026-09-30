<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.text.SimpleDateFormat, java.util.Date, java.util.Locale"%>
<%
    SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
    String fechaActual = sdf.format(new Date());
    // Capitalizar primer caracter
    if (fechaActual != null && !fechaActual.isEmpty()) {
        fechaActual = fechaActual.substring(0, 1).toUpperCase() + fechaActual.substring(1);
    }
%>
<header class="admin-topbar">
    <div class="topbar-left">
        <!-- Botón toggle para pantallas móviles -->
        <button class="sidebar-toggle-btn" id="sidebarToggle" type="button" aria-label="Abrir menú">
            <i class="bi bi-list"></i>
        </button>

        <div class="topbar-welcome">
            <h2 class="welcome-title">Bienvenido, Administrador</h2>
            <span class="welcome-date"><i class="bi bi-calendar3 me-1"></i> <%= fechaActual %></span>
        </div>
    </div>

    <div class="topbar-right">
        <!-- Indicador de Plataforma Activa -->
        <div class="d-none d-md-flex align-items-center me-2">
            <span class="badge bg-success-subtle text-success border border-success-subtle rounded-pill px-3 py-2 fw-semibold">
                <i class="bi bi-circle-fill fs-6 me-1" style="font-size: 8px !important;"></i> Sistema en Línea
            </span>
        </div>

        <!-- Perfil del usuario -->
        <div class="user-profile-badge">
            <div class="user-avatar">
                AD
            </div>
            <div class="user-info d-none d-sm-flex">
                <span class="user-name">Administrador</span>
                <span class="user-role">Panel ProPath</span>
            </div>
        </div>
    </div>
</header>
