package com.propath.controlador;

import com.propath.dao.AdminDAO;
import com.propath.dao.EmpresaDAO;
import com.propath.dao.EstudianteDAO;
import com.propath.dao.UsuarioDAO;
import com.propath.dto.DashboardAdministradoresDTO;
import com.propath.dto.DashboardEmpresasDTO;
import com.propath.dto.DashboardEstudiantesDTO;
import com.propath.modelo.Administrador;
import com.propath.modelo.Empresa;
import com.propath.modelo.Estudiante;
import com.propath.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Controlador principal para las vistas administrativas de PROPATH según el patrón MVC.
 * Recibe las peticiones GET, consulta a los DAO con JDBC, almacena los datos en atributos
 * de la solicitud (request.setAttribute) y delega la presentación a las vistas JSP mediante forward.
 */
@WebServlet(name = "AdminServlet", urlPatterns = {
    "/admin/dashboard",
    "/admin/estudiantes",
    "/admin/estudiantes/dashboard",
    "/admin/empresas",
    "/admin/empresas/dashboard",
    "/admin/administradores",
    "/admin/administradores/dashboard",
    "/admin/usuarios",
    "/admin/reporte-usuarios",
    "/admin/api/estudiantes-dashboard",
    "/admin/api/empresas-dashboard",
    "/admin/api/administradores-dashboard"
})
public class AdminServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            if (isApiRequest(request)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"exito\":false,\"mensaje\":\"No autorizado\"}");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=no_autorizado");
            return;
        }

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (!"ADMIN".equalsIgnoreCase(usuarioLogueado.getRol())) {
            if (isApiRequest(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"exito\":false,\"mensaje\":\"Permisos insuficientes\"}");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=no_autorizado");
            return;
        }

        String path = request.getServletPath();
        if (path == null) {
            path = "";
        }

        // Restricción estricta de todo el módulo Administradores al Administrador Principal
        if (path.endsWith("/administradores") || path.endsWith("/administradores/dashboard") || path.endsWith("/api/administradores-dashboard")) {
            if (!usuarioLogueado.esAdminPrincipal()) {
                if (isApiRequest(request)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"exito\":false,\"prohibido\":true,\"mensaje\":\"Acceso restringido al Administrador Principal.\"}");
                    return;
                }
                response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=acceso_denegado");
                return;
            }
        }

        if (path.endsWith("/administradores/dashboard")) {
            mostrarDashboardAdministradores(request, response);
        } else if (path.endsWith("/api/administradores-dashboard")) {
            apiDashboardAdministradores(request, response);
        } else if (path.endsWith("/administradores")) {
            mostrarAdministradores(request, response);
        } else if (path.endsWith("/estudiantes/dashboard")) {
            mostrarDashboardEstudiantes(request, response);
        } else if (path.endsWith("/empresas/dashboard")) {
            mostrarDashboardEmpresas(request, response);
        } else if (path.endsWith("/api/estudiantes-dashboard")) {
            apiDashboardEstudiantes(request, response);
        } else if (path.endsWith("/api/empresas-dashboard")) {
            apiDashboardEmpresas(request, response);
        } else if (path.endsWith("/dashboard")) {
            mostrarDashboard(request, response);
        } else if (path.endsWith("/estudiantes")) {
            mostrarEstudiantes(request, response);
        } else if (path.endsWith("/empresas")) {
            mostrarEmpresas(request, response);
        } else if (path.endsWith("/usuarios")) {
            mostrarUsuarios(request, response);
        } else if (path.endsWith("/reporte-usuarios")) {
            mostrarReporteUsuarios(request, response, usuarioLogueado);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        }
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String sp = request.getServletPath();
        return sp != null && sp.contains("/api/");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private void mostrarDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        AdminDAO adminDAO = new AdminDAO();
        Map<String, Object> stats = adminDAO.obtenerEstadisticasAdmin();

        request.setAttribute("stats", stats);
        request.setAttribute("totalEstudiantes", stats.getOrDefault("totalEstudiantes", 0L));
        request.setAttribute("estudiantesSemana", stats.getOrDefault("estudiantesEstaSemana", 0L));
        request.setAttribute("estudiantesActivos", stats.getOrDefault("estudiantesActivos", 0L));
        request.setAttribute("totalEmpresas", stats.getOrDefault("totalEmpresas", 0L));
        request.setAttribute("empresasAprobadas", stats.getOrDefault("empresasAprobadas", 0L));
        request.setAttribute("empresasPendientes", stats.getOrDefault("empresasPendientes", 0L));
        request.setAttribute("totalOfertas", stats.getOrDefault("totalOfertas", 0L));
        request.setAttribute("ofertasActivas", stats.getOrDefault("ofertasActivas", 0L));
        request.setAttribute("ofertasPendientes", stats.getOrDefault("ofertasPendientes", 0L));
        request.setAttribute("totalPostulaciones", stats.getOrDefault("totalPostulaciones", 0L));
        request.setAttribute("postRevision", stats.getOrDefault("postulacionesEnRevision", 0L));
        request.setAttribute("postSeleccionadas", stats.getOrDefault("postulacionesSeleccionadas", 0L));
        request.setAttribute("totalUsuarios", stats.getOrDefault("totalUsuarios", 0L));
        request.setAttribute("activePage", "dashboard");

        request.getRequestDispatcher("/admin/dashboard.jsp").forward(request, response);
    }

    private void mostrarAdministradores(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String busqueda = request.getParameter("busqueda");
        AdminDAO adminDAO = new AdminDAO();
        List<Administrador> lista = adminDAO.listarAdministradores(busqueda);

        request.setAttribute("listaAdministradores", lista);
        request.setAttribute("busqueda", busqueda != null ? busqueda.trim() : "");
        request.setAttribute("totalAdministradores", lista.size());
        request.setAttribute("activePage", "administradores");

        request.getRequestDispatcher("/admin/administradores.jsp").forward(request, response);
    }

    private void mostrarDashboardAdministradores(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        AdminDAO adminDAO = new AdminDAO();
        DashboardAdministradoresDTO dashboard = adminDAO.obtenerDashboardAdministradores("TODOS", "TODOS");

        request.setAttribute("dashboard", dashboard);
        request.setAttribute("activePage", "administradores");

        request.getRequestDispatcher("/admin/administradores-dashboard.jsp").forward(request, response);
    }

    private void apiDashboardAdministradores(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String estado = request.getParameter("estado");
        String cargo = request.getParameter("cargo");

        AdminDAO adminDAO = new AdminDAO();
        DashboardAdministradoresDTO dashboard = adminDAO.obtenerDashboardAdministradores(estado, cargo);

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(dashboard.toJson());
    }

    private void mostrarEstudiantes(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        EstudianteDAO estudianteDAO = new EstudianteDAO();
        List<Estudiante> lista = estudianteDAO.listarTodos();

        request.setAttribute("listaEstudiantes", lista);
        request.setAttribute("activePage", "estudiantes");

        request.getRequestDispatcher("/admin/estudiantes.jsp").forward(request, response);
    }

    private void mostrarDashboardEstudiantes(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        EstudianteDAO estudianteDAO = new EstudianteDAO();
        DashboardEstudiantesDTO dashboard = estudianteDAO.obtenerDashboardEstudiantes(
                "TODOS", "TODOS", 0, "TODOS", "TODOS");

        request.setAttribute("dashboard", dashboard);
        request.setAttribute("activePage", "estudiantes");

        request.getRequestDispatcher("/admin/estudiantes-dashboard.jsp").forward(request, response);
    }

    private void apiDashboardEstudiantes(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String universidad = request.getParameter("universidad");
        String carrera = request.getParameter("carrera");
        String cicloStr = request.getParameter("ciclo");
        String estadoCuenta = request.getParameter("estadoCuenta");
        String cv = request.getParameter("cv");

        int ciclo = 0;
        if (cicloStr != null && !cicloStr.trim().isEmpty()) {
            try {
                ciclo = Integer.parseInt(cicloStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        EstudianteDAO estudianteDAO = new EstudianteDAO();
        DashboardEstudiantesDTO dashboard = estudianteDAO.obtenerDashboardEstudiantes(
                universidad, carrera, ciclo, estadoCuenta, cv);

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(dashboard.toJson());
    }

    private void mostrarEmpresas(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        EmpresaDAO empresaDAO = new EmpresaDAO();
        List<Empresa> lista = empresaDAO.listarTodas();

        request.setAttribute("listaEmpresas", lista);
        request.setAttribute("activePage", "empresas");

        request.getRequestDispatcher("/admin/empresas.jsp").forward(request, response);
    }

    private void mostrarDashboardEmpresas(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        EmpresaDAO empresaDAO = new EmpresaDAO();
        DashboardEmpresasDTO dashboard = empresaDAO.obtenerDashboardEmpresas(
                "TODOS", "TODOS", "TODOS", "TODOS");

        request.setAttribute("dashboard", dashboard);
        request.setAttribute("activePage", "empresas");

        request.getRequestDispatcher("/admin/empresas-dashboard.jsp").forward(request, response);
    }

    private void apiDashboardEmpresas(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String estadoPerfil = request.getParameter("estadoPerfil");
        String estadoCuenta = request.getParameter("estadoCuenta");
        String conOfertas = request.getParameter("conOfertas");
        String conOfertasActivas = request.getParameter("conOfertasActivas");

        EmpresaDAO empresaDAO = new EmpresaDAO();
        DashboardEmpresasDTO dashboard = empresaDAO.obtenerDashboardEmpresas(
                estadoPerfil, estadoCuenta, conOfertas, conOfertasActivas);

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(dashboard.toJson());
    }

    private void mostrarUsuarios(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        FiltrosUsuarioDTO filtros = extraerFiltros(request);

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        EstudianteDAO estudianteDAO = new EstudianteDAO();
        EmpresaDAO empresaDAO = new EmpresaDAO();

        List<Usuario> listaUsuarios = usuarioDAO.buscarYFiltrar(filtros.busqueda, filtros.rol, filtros.estado);
        List<Estudiante> listaEstudiantes = estudianteDAO.listarTodos();
        List<Empresa> listaEmpresas = empresaDAO.listarTodas();

        boolean filtrosActivos = !filtros.busqueda.isEmpty() 
                || !"TODOS".equals(filtros.rol) 
                || !"TODOS".equals(filtros.estado);

        request.setAttribute("listaUsuarios", listaUsuarios);
        request.setAttribute("listaEstudiantes", listaEstudiantes);
        request.setAttribute("listaEmpresas", listaEmpresas);
        request.setAttribute("busqueda", filtros.busqueda);
        request.setAttribute("rol", filtros.rol);
        request.setAttribute("estado", filtros.estado);
        request.setAttribute("totalUsuarios", listaUsuarios.size());
        request.setAttribute("filtrosActivos", filtrosActivos);
        request.setAttribute("activePage", "usuarios");

        request.getRequestDispatcher("/admin/usuarios.jsp").forward(request, response);
    }

    private void mostrarReporteUsuarios(HttpServletRequest request, HttpServletResponse response, Usuario uLog)
            throws ServletException, IOException {
        FiltrosUsuarioDTO filtros = extraerFiltros(request);

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        List<Usuario> listaUsuarios = usuarioDAO.buscarYFiltrar(filtros.busqueda, filtros.rol, filtros.estado);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.forLanguageTag("es-PE"));
        String fechaGeneracion = sdf.format(new Date());

        String adminGenerador = (uLog != null && uLog.getNombre() != null)
                ? (uLog.getNombre() + " " + (uLog.getApellidos() != null ? uLog.getApellidos() : "")).trim()
                : "Administrador del Sistema";

        request.setAttribute("listaUsuarios", listaUsuarios);
        request.setAttribute("busqueda", filtros.busqueda);
        request.setAttribute("rol", filtros.rol);
        request.setAttribute("estado", filtros.estado);
        request.setAttribute("totalUsuarios", listaUsuarios.size());
        request.setAttribute("fechaGeneracion", fechaGeneracion);
        request.setAttribute("adminGenerador", adminGenerador);
        request.setAttribute("activePage", "usuarios");

        request.getRequestDispatcher("/admin/reporte-usuarios.jsp").forward(request, response);
    }

    /**
     * Extrae y normaliza los filtros compartidos entre la vista de Usuarios y el Reporte PDF.
     */
    private FiltrosUsuarioDTO extraerFiltros(HttpServletRequest request) {
        String busqueda = request.getParameter("busqueda");
        String rol = request.getParameter("rol");
        String estado = request.getParameter("estado");

        if (busqueda == null) {
            busqueda = "";
        }
        if (rol == null || rol.trim().isEmpty()) {
            rol = "TODOS";
        }
        if (estado == null || estado.trim().isEmpty()) {
            estado = "TODOS";
        }

        return new FiltrosUsuarioDTO(busqueda.trim(), rol.trim().toUpperCase(), estado.trim().toUpperCase());
    }

    private static class FiltrosUsuarioDTO {
        final String busqueda;
        final String rol;
        final String estado;

        FiltrosUsuarioDTO(String busqueda, String rol, String estado) {
            this.busqueda = busqueda;
            this.rol = rol;
            this.estado = estado;
        }
    }
}
