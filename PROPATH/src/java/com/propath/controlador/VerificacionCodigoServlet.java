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

@WebServlet(name = "VerificacionCodigoServlet", urlPatterns = {"/verificar-codigo", "/VerificacionCodigoServlet"})
public class VerificacionCodigoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if ("estado".equalsIgnoreCase(accion)) {
            consultarEstado(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");
        if (accion == null) accion = "";

        switch (accion.toLowerCase(Locale.ROOT)) {
            case "solicitar_registro":
                solicitarCodigoRegistro(request, response);
                break;
            case "verificar_registro":
                verificarCodigoRegistro(request, response);
                break;
            case "verificar_login_2fa":
                verificarLogin2FA(request, response);
                break;
            case "reenviar":
                reenviarCodigo(request, response);
                break;
            case "cancelar_2fa":
                cancelar2FA(request, response);
                break;
            default:
                responderJson(response, false, "Acción no reconocida.", null);
                break;
        }
    }

    private void solicitarCodigoRegistro(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String correo = request.getParameter("correo");
        String nombre = request.getParameter("nombre");
        String ip = obtenerIpCliente(request);

        if (correo == null || correo.trim().isBlank()) {
            responderJson(response, false, "Ingresa un correo electrónico válido.", null);
            return;
        }

        correo = correo.trim().toLowerCase(Locale.ROOT);
        if (!correo.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            responderJson(response, false, "El formato del correo electrónico no es válido.", null);
            return;
        }

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        if (usuarioDAO.obtenerPorCorreo(correo) != null) {
            responderJson(response, false, "Este correo electrónico ya se encuentra registrado. Por favor inicia sesión.", null);
            return;
        }

        if (!ServicioCorreo.isConfigurado()) {
            responderJson(response, false, "El servicio de correo SMTP no está configurado en el servidor.", null);
            return;
        }

        ResultadoSolicitud resultado = DesafioSeguridadServicio.solicitarODesafiar(
                correo, PropositoDesafio.REGISTRO, null, nombre, ip
        );

        if (!resultado.isExito()) {
            responderJson(response, false, resultado.getMensaje(), null);
            return;
        }

        try {
            String baseUrl = construirBaseUrl(request);
            ServicioCorreo.enviarCodigoRegistro(correo, nombre, resultado.getCodigoPlano(), baseUrl + "/registro");
            
            // Guardar contexto temporal en sesión
            HttpSession session = request.getSession(true);
            session.setAttribute("CORREO_EN_PROCESO_REGISTRO", correo);

            String mascara = DesafioSeguridadServicio.enmascararCorreo(correo);
            String extraJson = "\"correoMascara\":\"" + escaparJson(mascara) + "\",\"segundosEspera\":" + resultado.getSegundosEspera()
                    + ",\"segundosExpiracion\":" + resultado.getSegundosExpiracion();
            responderJson(response, true, "Hemos enviado un código de 6 caracteres a tu correo.", extraJson);
        } catch (Exception e) {
            getServletContext().log("Fallo al enviar correo de verificación de registro a: " + correo, e);
            responderJson(response, false, "No fue posible enviar el correo de verificación. Verifica la conexión SMTP.", null);
        }
    }

    private void verificarCodigoRegistro(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String correo = request.getParameter("correo");
        String codigo = request.getParameter("codigo");

        if (correo == null || correo.trim().isBlank()) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                correo = (String) session.getAttribute("CORREO_EN_PROCESO_REGISTRO");
            }
        }

        ResultadoValidacion resultado = DesafioSeguridadServicio.verificarCodigo(correo, codigo, PropositoDesafio.REGISTRO);

        if (!resultado.isExito()) {
            String extraJson = "\"intentosRestantes\":" + resultado.getIntentosRestantes();
            responderJson(response, false, resultado.getMensaje(), extraJson);
            return;
        }

        // Marcar correo como validado en la sesión
        HttpSession session = request.getSession(true);
        session.setAttribute("CORREO_VERIFICADO_REGISTRO", resultado.getCorreo());
        session.setAttribute("TOKEN_VERIFICADO_REGISTRO", resultado.getTokenAutorizacion());

        String extraJson = "\"token\":\"" + escaparJson(resultado.getTokenAutorizacion()) + "\",\"correo\":\"" + escaparJson(resultado.getCorreo()) + "\"";
        responderJson(response, true, "¡Correo verificado exitosamente!", extraJson);
    }

    private void verificarLogin2FA(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            responderJson(response, false, "Tu sesión ha expirado. Por favor inicia sesión nuevamente.", null);
            return;
        }

        Usuario usuarioPendiente = (Usuario) session.getAttribute("AUTH_2FA_PENDIENTE_USUARIO");
        if (usuarioPendiente == null) {
            responderJson(response, false, "No hay ninguna solicitud de autenticación pendiente.", null);
            return;
        }

        String codigo = request.getParameter("codigo");
        String correo = usuarioPendiente.getCorreo();

        ResultadoValidacion resultado = DesafioSeguridadServicio.verificarCodigo(correo, codigo, PropositoDesafio.LOGIN);

        if (!resultado.isExito()) {
            String extraJson = "\"intentosRestantes\":" + resultado.getIntentosRestantes();
            responderJson(response, false, resultado.getMensaje(), extraJson);
            return;
        }

        // Éxito: Renovar ID de sesión para prevenir Session Fixation
        try {
            request.changeSessionId();
        } catch (Throwable ignored) {
        }

        // Limpiar estado temporal
        session.removeAttribute("AUTH_2FA_PENDIENTE_USUARIO");
        session.removeAttribute("AUTH_2FA_PENDIENTE_CORREO");

        // Establecer sesión autenticada definitiva
        session.setAttribute("usuarioLogueado", usuarioPendiente);
        session.setAttribute("rol", usuarioPendiente.getRol());
        session.setAttribute("nombreUsuario", usuarioPendiente.getNombre());
        session.setAttribute("correoUsuario", usuarioPendiente.getCorreo());

        byte[] csrfBytes = new byte[24];
        RANDOM.nextBytes(csrfBytes);
        String csrfToken = Base64.getUrlEncoder().withoutPadding().encodeToString(csrfBytes);
        session.setAttribute("csrfToken", csrfToken);

        // Destino según rol
        String redirectUrl = request.getContextPath();
        String rol = usuarioPendiente.getRol();
        if ("ADMIN".equalsIgnoreCase(rol)) {
            redirectUrl += "/admin/dashboard.jsp";
        } else if ("ESTUDIANTE".equalsIgnoreCase(rol)) {
            redirectUrl += "/estudiante/dashboard.jsp";
        } else if ("EMPRESA".equalsIgnoreCase(rol)) {
            redirectUrl += "/empresa/dashboard.jsp";
        } else {
            redirectUrl += "/index.jsp";
        }

        String extraJson = "\"redirectUrl\":\"" + escaparJson(redirectUrl) + "\"";
        responderJson(response, true, "Acceso concedido exitosamente.", extraJson);
    }

    private void reenviarCodigo(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String correo = request.getParameter("correo");
        String propositoStr = request.getParameter("proposito");
        String ip = obtenerIpCliente(request);

        PropositoDesafio proposito = PropositoDesafio.LOGIN;
        if ("REGISTRO".equalsIgnoreCase(propositoStr)) {
            proposito = PropositoDesafio.REGISTRO;
        } else if ("RECUPERACION".equalsIgnoreCase(propositoStr)) {
            proposito = PropositoDesafio.RECUPERACION;
        }

        if (correo == null || correo.trim().isBlank()) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                if (proposito == PropositoDesafio.LOGIN) {
                    Usuario u = (Usuario) session.getAttribute("AUTH_2FA_PENDIENTE_USUARIO");
                    if (u != null) correo = u.getCorreo();
                } else if (proposito == PropositoDesafio.REGISTRO) {
                    correo = (String) session.getAttribute("CORREO_EN_PROCESO_REGISTRO");
                }
            }
        }

        if (correo == null || correo.trim().isBlank()) {
            responderJson(response, false, "No se identificó el correo destinatario.", null);
            return;
        }

        ResultadoSolicitud resultado = DesafioSeguridadServicio.solicitarODesafiar(
                correo.trim(), proposito, null, null, ip
        );

        if (!resultado.isExito()) {
            String extraJson = "\"segundosEspera\":" + resultado.getSegundosEspera();
            responderJson(response, false, resultado.getMensaje(), extraJson);
            return;
        }

        try {
            String baseUrl = construirBaseUrl(request);
            if (proposito == PropositoDesafio.REGISTRO) {
                ServicioCorreo.enviarCodigoRegistro(correo.trim(), null, resultado.getCodigoPlano(), baseUrl + "/registro");
            } else if (proposito == PropositoDesafio.LOGIN) {
                ServicioCorreo.enviarCodigoLogin(correo.trim(), null, resultado.getCodigoPlano(), baseUrl + "/login.jsp");
            } else {
                ServicioCorreo.enviarCodigoRecuperacion(correo.trim(), null, resultado.getCodigoPlano(), baseUrl + "/recuperar-contrasena.jsp");
            }

            String extraJson = "\"segundosEspera\":" + resultado.getSegundosEspera()
                    + ",\"segundosExpiracion\":" + resultado.getSegundosExpiracion();
            responderJson(response, true, "Nuevo código enviado a tu correo.", extraJson);
        } catch (Exception e) {
            getServletContext().log("Fallo al reenviar código SMTP a: " + correo, e);
            responderJson(response, false, "No se pudo reenviar el correo. Verifica el servicio SMTP.", null);
        }
    }

    private void cancelar2FA(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Usuario u = (Usuario) session.getAttribute("AUTH_2FA_PENDIENTE_USUARIO");
            if (u != null) {
                DesafioSeguridadServicio.invalidarTodoPorCorreo(u.getCorreo());
            }
            session.removeAttribute("AUTH_2FA_PENDIENTE_USUARIO");
            session.removeAttribute("AUTH_2FA_PENDIENTE_CORREO");
        }
        responderJson(response, true, "Inicio de sesión cancelado.", null);
    }

    private void consultarEstado(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String correo = request.getParameter("correo");
        String propositoStr = request.getParameter("proposito");

        PropositoDesafio proposito = PropositoDesafio.LOGIN;
        if ("REGISTRO".equalsIgnoreCase(propositoStr)) {
            proposito = PropositoDesafio.REGISTRO;
        } else if ("RECUPERACION".equalsIgnoreCase(propositoStr)) {
            proposito = PropositoDesafio.RECUPERACION;
        }

        long espera = DesafioSeguridadServicio.obtenerSegundosEspera(correo, proposito);
        long expira = DesafioSeguridadServicio.obtenerSegundosExpiracion(correo, proposito);

        String extraJson = "\"segundosEspera\":" + espera + ",\"segundosExpiracion\":" + expira;
        responderJson(response, true, "Estado consultado.", extraJson);
    }

    private void responderJson(HttpServletResponse response, boolean exito, String mensaje, String extraCampos)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        PrintWriter out = response.getWriter();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"exito\":").append(exito).append(",");
        sb.append("\"mensaje\":\"").append(escaparJson(mensaje)).append("\"");
        if (extraCampos != null && !extraCampos.isBlank()) {
            sb.append(",").append(extraCampos);
        }
        sb.append("}");
        out.print(sb.toString());
        out.flush();
    }

    private String escaparJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }

    private String construirBaseUrl(HttpServletRequest request) {
        String configured = ServicioCorreo.getAppUrl();
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        return request.getContextPath();
    }
}

