package com.propath.controlador;

import com.propath.dao.UsuarioDAO;
import com.propath.modelo.Usuario;
import com.propath.servicio.DesafioSeguridadServicio;
import com.propath.servicio.DesafioSeguridadServicio.PropositoDesafio;
import com.propath.servicio.DesafioSeguridadServicio.ResultadoSolicitud;
import com.propath.servicio.DesafioSeguridadServicio.ResultadoValidacion;
import com.propath.servicio.DesafioSeguridadServicio.TokenAutorizacion;
import com.propath.servicio.ServicioCorreo;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;

@WebServlet(name = "RecuperarContrasenaServlet", urlPatterns = {"/RecuperarContrasenaServlet", "/recuperar-contrasena"})
public class RecuperarContrasenaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        HttpSession session = request.getSession(true);
        limpiarRecuperacion(session);
        asegurarCsrfToken(request);
        request.setAttribute("paso", "1");
        mostrar(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        HttpSession session = request.getSession(true);
        String esperado = (String) session.getAttribute("csrfToken");
        String recibido = request.getParameter("csrfToken");

        if (esperado == null || recibido == null
                || !MessageDigest.isEqual(
                        esperado.getBytes(StandardCharsets.UTF_8),
                        recibido.getBytes(StandardCharsets.UTF_8))) {
            request.setAttribute("error", "La sesión ha expirado o el token CSRF es inválido. Intenta nuevamente.");
            request.setAttribute("paso", "1");
            mostrar(request, response);
            return;
        }

        String accion = request.getParameter("accion");
        if (accion == null) accion = "";

        switch (accion.toLowerCase(Locale.ROOT)) {
            case "solicitar_codigo":
                solicitarCodigo(request, response);
                break;
            case "verificar_codigo":
                verificarCodigo(request, response);
                break;
            case "cambiar_contrasena":
                cambiarContrasena(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/recuperar-contrasena");
                break;
        }
    }

    private void solicitarCodigo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        limpiarRecuperacion(session);

        String correo = request.getParameter("correo");
        if (correo == null || correo.trim().isBlank()) {
            request.setAttribute("error", "Por favor ingresa tu correo electrónico.");
            request.setAttribute("paso", "1");
            mostrar(request, response);
            return;
        }

        correo = correo.trim().toLowerCase(Locale.ROOT);
        String ip = obtenerIpCliente(request);

        Usuario usuario = new UsuarioDAO().obtenerPorCorreo(correo);

        // Respuesta genérica que no revela si el correo existe o no en el sistema
        if (usuario == null) {
            request.setAttribute("info", "Si tu correo se encuentra registrado en ProPath, recibirás un código de verificación en los próximos minutos.");
            request.setAttribute("correo", correo);
            request.setAttribute("correoMascara", DesafioSeguridadServicio.enmascararCorreo(correo));
            request.setAttribute("paso", "2");
            request.setAttribute("abrirModalCodigo", true);
            session.setAttribute("recuperacionCorreoPendiente", correo);
            mostrar(request, response);
            return;
        }

        // Si la cuenta no está activa, mensaje seguro
        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            request.setAttribute("error", "Tu cuenta está " + usuario.getEstado().toLowerCase() + ". Comunícate con soporte para asistencia.");
            request.setAttribute("paso", "1");
            mostrar(request, response);
            return;
        }

        if (!ServicioCorreo.isConfigurado()) {
            request.setAttribute("error", "El servicio de correo no está disponible en este momento. Contacta al administrador.");
            request.setAttribute("paso", "1");
            mostrar(request, response);
            return;
        }

        ResultadoSolicitud resultado = DesafioSeguridadServicio.solicitarODesafiar(
                correo, PropositoDesafio.RECUPERACION, usuario.getId(), usuario.getNombre(), ip
        );

        if (!resultado.isExito()) {
            request.setAttribute("error", resultado.getMensaje());
            request.setAttribute("paso", "1");
            mostrar(request, response);
            return;
        }

        try {
            String appUrl = ServicioCorreo.getAppUrl() != null ? ServicioCorreo.getAppUrl() : request.getContextPath();
            ServicioCorreo.enviarCodigoRecuperacion(correo, usuario.getNombre(), resultado.getCodigoPlano(), appUrl + "/recuperar-contrasena.jsp");

            session.setAttribute("recuperacionCorreoPendiente", correo);
            request.setAttribute("correo", correo);
            request.setAttribute("correoMascara", DesafioSeguridadServicio.enmascararCorreo(correo));
            request.setAttribute("info", "Hemos enviado un código de 6 caracteres a tu correo. Revisa tu bandeja de entrada o spam.");
            request.setAttribute("paso", "2");
            request.setAttribute("abrirModalCodigo", true);
            request.setAttribute("segundosEspera", resultado.getSegundosEspera());
            request.setAttribute("segundosExpiracion", resultado.getSegundosExpiracion());
            mostrar(request, response);
        } catch (Exception e) {
            getServletContext().log("Fallo al enviar correo de recuperación a: " + correo, e);
            request.setAttribute("error", "No fue posible enviar el código de recuperación por correo. Intenta nuevamente.");
            request.setAttribute("paso", "1");
            mostrar(request, response);
        }
    }

    private void verificarCodigo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/recuperar-contrasena");
            return;
        }

        String correoSesion = (String) session.getAttribute("recuperacionCorreoPendiente");
        String correoForm = request.getParameter("correo");
        String codigo = request.getParameter("codigo");

        if (correoSesion == null || correoSesion.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/recuperar-contrasena");
            return;
        }

        // Validación estricta en el servidor para evitar manipulación de correo
        if (correoForm != null && !correoSesion.equalsIgnoreCase(correoForm.trim())) {
            request.setAttribute("error", "El correo enviado no coincide con la solicitud en curso.");
            request.setAttribute("correo", correoSesion);
            request.setAttribute("correoMascara", DesafioSeguridadServicio.enmascararCorreo(correoSesion));
            request.setAttribute("paso", "2");
            request.setAttribute("abrirModalCodigo", true);
            mostrar(request, response);
            return;
        }

        ResultadoValidacion resultado = DesafioSeguridadServicio.verificarCodigo(
                correoSesion, codigo, PropositoDesafio.RECUPERACION
        );

        if (!resultado.isExito()) {
            request.setAttribute("error", resultado.getMensaje());
            request.setAttribute("correo", correoSesion);
            request.setAttribute("correoMascara", DesafioSeguridadServicio.enmascararCorreo(correoSesion));
            request.setAttribute("intentosRestantes", resultado.getIntentosRestantes());
            request.setAttribute("paso", "2");
            request.setAttribute("abrirModalCodigo", true);
            mostrar(request, response);
            return;
        }

        // Éxito: Guardar token de autorización temporal de un solo uso
        session.setAttribute("recuperacionTokenReset", resultado.getTokenAutorizacion());
        request.setAttribute("tokenReset", resultado.getTokenAutorizacion());
        request.setAttribute("correo", correoSesion);
        request.setAttribute("correoMascara", DesafioSeguridadServicio.enmascararCorreo(correoSesion));
        request.setAttribute("paso", "3");
        mostrar(request, response);
    }

    private void cambiarContrasena(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/recuperar-contrasena");
            return;
        }

        String tokenSesion = (String) session.getAttribute("recuperacionTokenReset");
        String tokenForm = request.getParameter("tokenReset");
        String tokenAValidar = (tokenForm != null && !tokenForm.isBlank()) ? tokenForm.trim() : tokenSesion;

        // Validar la vigencia del token de autorización temporal sin consumirlo prematuramente
        TokenAutorizacion auth = DesafioSeguridadServicio.consultarAutorizacion(tokenAValidar, PropositoDesafio.RECUPERACION);

        if (auth == null || auth.idUsuario == null || auth.idUsuario <= 0) {
            limpiarRecuperacion(session);
            request.setAttribute("error", "La autorización para cambio de contraseña expiró o es inválida. Inicia el proceso nuevamente.");
            request.setAttribute("paso", "1");
            mostrar(request, response);
            return;
        }

        String nueva = request.getParameter("nuevaContrasena");
        String confirmar = request.getParameter("confirmarContrasena");

        if (nueva == null || nueva.length() < 8) {
            session.setAttribute("recuperacionTokenReset", auth.token);
            request.setAttribute("tokenReset", auth.token);
            request.setAttribute("error", "La contraseña debe tener al menos 8 caracteres.");
            request.setAttribute("correo", auth.correo);
            request.setAttribute("paso", "3");
            mostrar(request, response);
            return;
        }

        if (!nueva.equals(confirmar)) {
            session.setAttribute("recuperacionTokenReset", auth.token);
            request.setAttribute("tokenReset", auth.token);
            request.setAttribute("error", "Las contraseñas no coinciden. Por favor confirma tu nueva contraseña exactamente igual.");
            request.setAttribute("correo", auth.correo);
            request.setAttribute("paso", "3");
            mostrar(request, response);
            return;
        }

        // Consumir atómicamente el token una vez superadas las validaciones de entrada
        DesafioSeguridadServicio.consumirAutorizacion(tokenAValidar, PropositoDesafio.RECUPERACION);

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        boolean actualizado = usuarioDAO.actualizarContrasena(auth.idUsuario, nueva);

        if (!actualizado) {
            request.setAttribute("error", "No se pudo actualizar la contraseña en la base de datos. Intenta nuevamente.");
            request.setAttribute("paso", "1");
            limpiarRecuperacion(session);
            mostrar(request, response);
            return;
        }

        // Invalidar desafíos previos y sesiones anteriores tras el restablecimiento exitoso
        DesafioSeguridadServicio.invalidarTodoPorCorreo(auth.correo);

        // Notificación por correo sin incluir la contraseña
        try {
            Usuario u = usuarioDAO.obtenerPorId(auth.idUsuario);
            String nombre = u != null ? u.getNombre() : "Usuario";
            ServicioCorreo.enviarConfirmacionCambioPassword(auth.correo, nombre);
        } catch (Exception e) {
            getServletContext().log("No se pudo enviar correo de confirmación de cambio de contraseña a: " + auth.correo, e);
        }

        limpiarRecuperacion(session);
        session.invalidate();

        response.sendRedirect(request.getContextPath() + "/login.jsp?exito=password_actualizada");
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            String ip = (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
            // Validación para no confiar ciegamente en cabeceras manipuladas o proxies anónimos
            if (!ip.equalsIgnoreCase("unknown") && ip.matches("^[0-9a-fA-F:.]+$")) {
                return ip;
            }
        }
        String remote = request.getRemoteAddr();
        return (remote != null && !remote.isBlank()) ? remote : "127.0.0.1";
    }

    private void limpiarRecuperacion(HttpSession session) {
        if (session != null) {
            session.removeAttribute("recuperacionCorreoPendiente");
            session.removeAttribute("recuperacionTokenReset");
        }
    }

    private void asegurarCsrfToken(HttpServletRequest request) {
        if (request != null) {
            HttpSession session = request.getSession(true);
            if (session.getAttribute("csrfToken") == null) {
                byte[] bytes = new byte[24];
                RANDOM.nextBytes(bytes);
                session.setAttribute("csrfToken",
                        Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
            }
        }
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        asegurarCsrfToken(request);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        request.getRequestDispatcher("/recuperar-contrasena.jsp")
                .forward(request, response);
    }
}
