package com.propath.controlador;

import com.propath.modelo.Usuario;
import com.propath.servicio.DesafioSeguridadServicio;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/LogoutServlet", "/logout"})
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        procesarCierre(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        procesarCierre(request, response);
    }

    private void procesarCierre(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Encabezados estrictos para evitar almacenamiento en caché y acceso mediante el botón Atrás
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        HttpSession session = request.getSession(false);
        if (session != null) {
            Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
            if (u != null) {
                DesafioSeguridadServicio.invalidarTodoPorCorreo(u.getCorreo());
            }
            try {
                session.invalidate();
            } catch (IllegalStateException ignored) {
            }
        }

        // Expirar la cookie de sesión JSESSIONID
        Cookie cookie = new Cookie("JSESSIONID", "");
        cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        response.sendRedirect(request.getContextPath() + "/login.jsp?logout=1");
    }
}
