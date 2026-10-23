package com.propath.controlador;

import com.propath.dao.UsuarioDAO;
import com.propath.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet", "/login"})
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        Usuario usuario = usuarioDAO.autenticar(correo, contrasena);

        if (usuario != null) {
            if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
                request.setAttribute("error", "Su cuenta está " + usuario.getEstado().toLowerCase() + ". Comuníquese con el administrador.");
                request.setAttribute("correoIngresado", correo != null ? correo : "");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }

            HttpSession session = request.getSession(true);

            session.setAttribute("usuarioLogueado", usuario);
            session.setAttribute("rol", usuario.getRol());
            session.setAttribute("nombreUsuario", usuario.getNombre());
            session.setAttribute("correoUsuario", usuario.getCorreo());

            if ("ADMIN".equalsIgnoreCase(usuario.getRol())) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");
            } else if ("ESTUDIANTE".equalsIgnoreCase(usuario.getRol())) {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp");
            } else if ("EMPRESA".equalsIgnoreCase(usuario.getRol())) {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp");
            } else {
                response.sendRedirect(request.getContextPath() + "/index.jsp");
            }
        } else {
            request.setAttribute("error", "Correo o contraseña incorrectos");
            request.setAttribute("correoIngresado", correo != null ? correo : "");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}
