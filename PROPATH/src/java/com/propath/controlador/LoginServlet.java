package com.propath.controlador;

import com.propath.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet controlador para gestionar la autenticación de usuarios en ProPath.
 * En esta etapa utiliza credenciales temporales en memoria para el rol Administrador.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet", "/login"})
public class LoginServlet extends HttpServlet {

    private static final String CORREO_ADMIN = "admin@propath.com";
    private static final String CLAVE_ADMIN = "Admin123";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirige al formulario de inicio de sesión
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String correo = request.getParameter("correo");
        String contrasena = request.getParameter("contrasena");

        if (correo != null) {
            correo = correo.trim();
        }

        // Validación de credenciales temporales para el administrador
        if (CORREO_ADMIN.equalsIgnoreCase(correo) && CLAVE_ADMIN.equals(contrasena)) {
            // Credenciales válidas: crear sesión
            HttpSession session = request.getSession(true);

            Usuario admin = new Usuario(1, "Administrador", "General", CORREO_ADMIN, "ADMINISTRADOR", "Activo");
            session.setAttribute("usuarioLogueado", admin);
            session.setAttribute("rol", "ADMINISTRADOR");
            session.setAttribute("nombreUsuario", "Administrador ProPath");
            session.setAttribute("correoUsuario", CORREO_ADMIN);

            // Redirigir al dashboard del administrador
            response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");
        } else {
            // Credenciales incorrectas
            request.setAttribute("error", "Correo o contraseña incorrectos");
            request.setAttribute("correoIngresado", correo != null ? correo : "");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}
