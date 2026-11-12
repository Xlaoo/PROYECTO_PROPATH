package com.propath.controlador;

import com.propath.dao.UsuarioDAO;
import com.propath.modelo.Usuario;
import com.propath.servicio.DesafioSeguridadServicio;
import com.propath.servicio.DesafioSeguridadServicio.PropositoDesafio;
import com.propath.servicio.DesafioSeguridadServicio.ResultadoSolicitud;
import com.propath.servicio.DesafioSeguridadServicio.ResultadoValidacion;
import com.propath.servicio.ServicioCorreo;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;

@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet", "/login"})
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("usuarioLogueado") != null) {
            String rol = (String) session.getAttribute("rol");
            if ("ADMIN".equalsIgnoreCase(rol)) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");
                return;
            } else if ("ESTUDIANTE".equalsIgnoreCase(rol)) {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp");
                return;
            } else if ("EMPRESA".equalsIgnoreCase(rol)) {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp");
                return;
            }
        }
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        String accion = request.getParameter("accion");
        boolean esAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
                || "application/json".equalsIgnoreCase(request.getHeader("Accept"));

        if ("verificar_2fa".equalsIgnoreCase(accion)) {
            procesarVerificacion2FA(request, response, esAjax);
            return;
        }

        String correo = request.getParameter("correo");
        String contrasena = request.getParameter("contrasena");

        if (correo != null) {
            correo = correo.trim().toLowerCase(Locale.ROOT);
        }

        if (correo == null || correo.isBlank() || contrasena == null || contrasena.isBlank()) {
            manejarError(request, response, "Por favor ingresa tu correo y contraseña.", correo, esAjax);
            return;
        }

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        Usuario usuario = usuarioDAO.autenticar(correo, contrasena);

        if (usuario == null) {
            manejarError(request, response, "Correo o contraseña incorrectos.", correo, esAjax);
            return;
        }

        // Validación estricta del estado de la cuenta en el servidor
        String estado = usuario.getEstado() != null ? usuario.getEstado().trim().toUpperCase(Locale.ROOT) : "INACTIVO";
        if (!"ACTIVO".equalsIgnoreCase(estado)) {
            String msg = "Tu cuenta se encuentra en estado " + estado.toLowerCase() + ". Comunícate con el administrador.";
            if ("BLOQUEADO".equalsIgnoreCase(estado)) {
                msg = "Tu cuenta ha sido bloqueada por motivos de seguridad. Contacta al administrador.";
            } else if ("PENDIENTE".equalsIgnoreCase(estado)) {
                msg = "Tu cuenta aún no ha sido activada o aprobada. Revisa tu correo o espera la validación del administrador.";
            }
            manejarError(request, response, msg, correo, esAjax);
            return;
        }

        // Si el servicio SMTP no está configurado (p. ej. desarrollo local sin credenciales), permitir acceso directo
        if (!ServicioCorreo.isConfigurado()) {
            establecerSesionAutenticada(request, response, usuario, esAjax);
            return;
        }

        // Credenciales correctas y SMTP configurado: Generar desafío de doble factor (2FA) y enviar código por correo
        String ip = obtenerIpCliente(request);
        ResultadoSolicitud desafio = DesafioSeguridadServicio.solicitarODesafiar(
                usuario.getCorreo(), PropositoDesafio.LOGIN, usuario.getId(), usuario.getNombre(), ip
        );

        if (!desafio.isExito()) {
            manejarError(request, response, desafio.getMensaje(), correo, esAjax);
            return;
        }

        try {
            String appUrl = ServicioCorreo.getAppUrl() != null ? ServicioCorreo.getAppUrl() : request.getContextPath();
            ServicioCorreo.enviarCodigoLogin(usuario.getCorreo(), usuario.getNombre(), desafio.getCodigoPlano(), appUrl + "/login.jsp");
        } catch (Exception e) {
            getServletContext().log("Fallo al enviar correo 2FA a: " + usuario.getCorreo(), e);
            manejarError(request, response, "No se pudo enviar el código de seguridad a tu correo. Intenta nuevamente.", correo, esAjax);
            return;
        }

        // Establecer ÚNICAMENTE estado temporal de autenticación pendiente (NO dar acceso)
        HttpSession session = request.getSession(true);
        session.removeAttribute("usuarioLogueado");
        session.removeAttribute("rol");
        session.setAttribute("AUTH_2FA_PENDIENTE_USUARIO", usuario);
        session.setAttribute("AUTH_2FA_PENDIENTE_CORREO", usuario.getCorreo());

        String mascara = DesafioSeguridadServicio.enmascararCorreo(usuario.getCorreo());

        if (esAjax) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("{\"exito\":true,\"requiere2FA\":true,\"correoMascara\":\"" + mascara
                    + "\",\"segundosEspera\":" + desafio.getSegundosEspera()
                    + ",\"segundosExpiracion\":" + desafio.getSegundosExpiracion() + "}");
            out.flush();
            return;
        }

        request.setAttribute("mostrarModal2FA", true);
        request.setAttribute("correoMascara", mascara);
        request.setAttribute("segundosEspera", desafio.getSegundosEspera());
        request.setAttribute("segundosExpiracion", desafio.getSegundosExpiracion());
        request.setAttribute("correoIngresado", correo);
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    private void procesarVerificacion2FA(HttpServletRequest request, HttpServletResponse response, boolean esAjax)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            manejarError(request, response, "Tu sesión ha expirado. Ingresa tus credenciales nuevamente.", "", esAjax);
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("AUTH_2FA_PENDIENTE_USUARIO");
        if (usuario == null) {
            manejarError(request, response, "No hay una autenticación pendiente. Inicia sesión.", "", esAjax);
            return;
        }

        String codigo = request.getParameter("codigo");
        ResultadoValidacion resultado = DesafioSeguridadServicio.verificarCodigo(usuario.getCorreo(), codigo, PropositoDesafio.LOGIN);

        if (!resultado.isExito()) {
            if (esAjax) {
                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.print("{\"exito\":false,\"mensaje\":\"" + escaparJson(resultado.getMensaje())
                        + "\",\"intentosRestantes\":" + resultado.getIntentosRestantes() + "}");
                out.flush();
                return;
            }
            request.setAttribute("mostrarModal2FA", true);
            request.setAttribute("error2FA", resultado.getMensaje());
            request.setAttribute("correoMascara", DesafioSeguridadServicio.enmascararCorreo(usuario.getCorreo()));
            request.setAttribute("intentosRestantes", resultado.getIntentosRestantes());
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        establecerSesionAutenticada(request, response, usuario, esAjax);
    }

    private void establecerSesionAutenticada(HttpServletRequest request, HttpServletResponse response,
                                             Usuario usuario, boolean esAjax) throws IOException {
        HttpSession session = request.getSession(true);

        // Renovación del identificador de sesión para prevenir Session Fixation
        try {
            request.changeSessionId();
        } catch (Throwable ignored) {
        }

        // Limpiar estado temporal y fijar sesión autenticada definitiva
        session.removeAttribute("AUTH_2FA_PENDIENTE_USUARIO");
        session.removeAttribute("AUTH_2FA_PENDIENTE_CORREO");

        session.setAttribute("usuarioLogueado", usuario);
        session.setAttribute("rol", usuario.getRol());
        session.setAttribute("nombreUsuario", usuario.getNombre());
        session.setAttribute("correoUsuario", usuario.getCorreo());

        byte[] csrfBytes = new byte[24];
        RANDOM.nextBytes(csrfBytes);
        String csrfToken = Base64.getUrlEncoder().withoutPadding().encodeToString(csrfBytes);
        session.setAttribute("csrfToken", csrfToken);

        String redirectUrl = request.getContextPath();
        String rol = usuario.getRol();
        if ("ADMIN".equalsIgnoreCase(rol)) {
            redirectUrl += "/admin/dashboard";
        } else if ("ESTUDIANTE".equalsIgnoreCase(rol)) {
            redirectUrl += "/estudiante/dashboard.jsp";
        } else if ("EMPRESA".equalsIgnoreCase(rol)) {
            redirectUrl += "/empresa/dashboard.jsp";
        } else {
            redirectUrl += "/index.jsp";
        }

        if (esAjax) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("{\"exito\":true,\"redirectUrl\":\"" + escaparJson(redirectUrl)
                    + "\",\"redirect\":\"" + escaparJson(redirectUrl) + "\"}");
            out.flush();
            return;
        }

        response.sendRedirect(redirectUrl);
    }

    private void manejarError(HttpServletRequest request, HttpServletResponse response,
                              String mensaje, String correo, boolean esAjax) throws ServletException, IOException {
        if (esAjax) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("{\"exito\":false,\"mensaje\":\"" + escaparJson(mensaje) + "\"}");
            out.flush();
            return;
        }
        request.setAttribute("error", mensaje);
        request.setAttribute("correoIngresado", correo != null ? correo : "");
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }

    private String escaparJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
