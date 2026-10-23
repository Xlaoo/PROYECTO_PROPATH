package com.propath.controlador;

import com.propath.dao.UsuarioDAO;
import com.propath.modelo.Usuario;
import com.propath.conexion.SeguridadUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@WebServlet(name = "RecuperarContrasenaServlet", urlPatterns = {"/RecuperarContrasenaServlet", "/recuperar-contrasena"})
public class RecuperarContrasenaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        session.removeAttribute("recuperacionUsuarioId");
        session.removeAttribute("recuperacionCorreo");
        session.removeAttribute("recuperacionAutorizada");
        asegurarCsrfToken(request);
        request.setAttribute("paso", "1");
        mostrar(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(true);
        String esperado = (String) session.getAttribute("csrfToken");
        String recibido = request.getParameter("csrfToken");

        if (esperado == null || recibido == null
                || !MessageDigest.isEqual(
                        esperado.getBytes(StandardCharsets.UTF_8),
                        recibido.getBytes(StandardCharsets.UTF_8))) {
            request.setAttribute("error", "La sesión ha expirado. Vuelve a intentarlo.");
            request.setAttribute("paso", "1");
            mostrar(request, response);
            return;
        }

        String accion = request.getParameter("accion");

        if ("verificar_correo".equals(accion)) {
            verificarCorreo(request, response);
        } else if ("verificar_identidad".equals(accion)) {
            verificarIdentidad(request, response);
        } else if ("cambiar_contrasena".equals(accion)) {
            cambiarContrasena(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/recuperar-contrasena");
        }
    }

    private void verificarCorreo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        session.removeAttribute("recuperacionUsuarioId");
        session.removeAttribute("recuperacionCorreo");
        session.removeAttribute("recuperacionAutorizada");

        String correo = request.getParameter("correo");

        if (correo == null || correo.trim().isEmpty()) {
            request.setAttribute("error", "Ingresa tu correo electrónico.");
            request.setAttribute("paso", "1");
            mostrar(request, response);
            return;
        }

        correo = correo.trim().toLowerCase(java.util.Locale.ROOT);

        Usuario usuario = new UsuarioDAO().obtenerPorCorreo(correo);

        if (usuario == null) {
            request.setAttribute("error", "No se pudo verificar la cuenta.");
            request.setAttribute("paso", "1");
            mostrar(request, response);
            return;
        }

        session.setAttribute("recuperacionUsuarioId", usuario.getId());
        session.setAttribute("recuperacionCorreo", usuario.getCorreo());
        session.setAttribute("recuperacionIntentos", 0);

        request.setAttribute("correo", usuario.getCorreo());
        request.setAttribute("paso", "2");
        mostrar(request, response);
    }

    private void verificarIdentidad(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("recuperacionUsuarioId") == null) {
            response.sendRedirect(request.getContextPath() + "/recuperar-contrasena");
            return;
        }

        Integer intentos = (Integer) session.getAttribute("recuperacionIntentos");

        if (intentos == null) {
            intentos = 0;
        }

        if (intentos >= 5) {
            limpiarRecuperacion(session);
            request.setAttribute("error", "Demasiados intentos. Inicia nuevamente.");
            request.setAttribute("paso", "1");
            mostrar(request, response);
            return;
        }

        String correo = (String) session.getAttribute("recuperacionCorreo");
        String contrasenaActual = request.getParameter("contrasenaActual");

        Usuario usuario = new UsuarioDAO().obtenerPorCorreo(correo);

        boolean valida = usuario != null
                && usuario.getId() == (Integer) session.getAttribute("recuperacionUsuarioId")
                && SeguridadUtil.verificarPassword(
                        contrasenaActual,
                        usuario.getContrasena());

        if (!valida) {
            session.setAttribute("recuperacionIntentos", intentos + 1);
            request.setAttribute("error", "No se pudo verificar la identidad.");
            request.setAttribute("correo", correo);
            request.setAttribute("paso", "2");
            mostrar(request, response);
            return;
        }

        session.setAttribute("recuperacionAutorizada", Boolean.TRUE);
        session.setAttribute("recuperacionAutorizadaHasta",
                System.currentTimeMillis() + 300000L);

        request.setAttribute("correo", correo);
        request.setAttribute("paso", "3");
        mostrar(request, response);
    }

    private void cambiarContrasena(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || !Boolean.TRUE.equals(session.getAttribute("recuperacionAutorizada"))
                || !(session.getAttribute("recuperacionUsuarioId") instanceof Integer)
                || !(session.getAttribute("recuperacionAutorizadaHasta") instanceof Long)
                || System.currentTimeMillis()
                    > (Long) session.getAttribute("recuperacionAutorizadaHasta")) {

            if (session != null) {
                limpiarRecuperacion(session);
            }

            response.sendRedirect(request.getContextPath() + "/recuperar-contrasena");
            return;
        }

        String nueva = request.getParameter("nuevaContrasena");
        String confirmar = request.getParameter("confirmarContrasena");
        String correo = (String) session.getAttribute("recuperacionCorreo");

        if (nueva == null || nueva.length() < 8) {
            request.setAttribute("error", "La contraseña debe tener al menos 8 caracteres.");
            request.setAttribute("correo", correo);
            request.setAttribute("paso", "3");
            mostrar(request, response);
            return;
        }

        if (!nueva.equals(confirmar)) {
            request.setAttribute("error", "Las contraseñas no coinciden.");
            request.setAttribute("correo", correo);
            request.setAttribute("paso", "3");
            mostrar(request, response);
            return;
        }

        int idUsuario = (Integer) session.getAttribute("recuperacionUsuarioId");

        boolean actualizado = new UsuarioDAO().actualizarContrasena(idUsuario, nueva);

        if (!actualizado) {
            request.setAttribute("error", "No se pudo actualizar la contraseña.");
            request.setAttribute("correo", correo);
            request.setAttribute("paso", "3");
            mostrar(request, response);
            return;
        }

        session.invalidate();

        response.sendRedirect(
                request.getContextPath() + "/login.jsp?exito=password_actualizada");
    }

    private void limpiarRecuperacion(HttpSession session) {
        session.removeAttribute("recuperacionUsuarioId");
        session.removeAttribute("recuperacionCorreo");
        session.removeAttribute("recuperacionAutorizada");
        session.removeAttribute("recuperacionAutorizadaHasta");
        session.removeAttribute("recuperacionIntentos");
    }

    private void asegurarCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);

        if (session.getAttribute("csrfToken") == null) {
            byte[] bytes = new byte[24];
            RANDOM.nextBytes(bytes);
            session.setAttribute("csrfToken",
                    Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
        }
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        asegurarCsrfToken(request);
        response.setHeader("Cache-Control", "no-store");
        request.getRequestDispatcher("/recuperar-contrasena.jsp")
                .forward(request, response);
    }
}