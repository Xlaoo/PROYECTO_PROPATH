package com.propath.filtro;

import com.propath.modelo.Usuario;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "AdminAuthFilter", urlPatterns = { "/admin/*" })
public class AdminAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Cabeceras estrictas anti-caché para evitar que páginas protegidas queden accesibles con botón Atrás
        httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        httpResponse.setHeader("Pragma", "no-cache");
        httpResponse.setDateHeader("Expires", 0);

        HttpSession session = httpRequest.getSession(false);
        boolean autorizado = false;

        if (session != null) {
            // Verificar que no se encuentre en estado temporal 2FA
            boolean es2FaPendiente = session.getAttribute("AUTH_2FA_PENDIENTE_USUARIO") != null;

            if (!es2FaPendiente) {
                Object obj = session.getAttribute("usuarioLogueado");
                if (obj instanceof Usuario) {
                    Usuario usuario = (Usuario) obj;
                    String rol = usuario.getRol();

                    if ("ADMIN".equalsIgnoreCase(rol)) {
                        com.propath.dao.UsuarioDAO usuarioDAO = new com.propath.dao.UsuarioDAO();
                        String estadoActual = usuarioDAO.obtenerEstadoPorId(usuario.getId());
                        if ("ACTIVO".equalsIgnoreCase(estadoActual)) {
                            usuario.setEstado(estadoActual);
                            autorizado = true;
                        } else {
                            try {
                                session.invalidate();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            }
        }

        if (autorizado) {
            chain.doFilter(request, response);
        } else {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp?error=no_autorizado");
        }
    }
}
