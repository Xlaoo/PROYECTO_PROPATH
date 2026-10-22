package com.propath.controlador;

import com.propath.modelo.Estudiante;
import com.propath.modelo.Usuario;
import com.propath.servicio.BackendCliente;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet(name = "PostularServlet", urlPatterns = { "/estudiante/postular", "/PostularServlet" })
public class PostularServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("usuarioLogueado") instanceof Usuario)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())
                || !"ESTUDIANTE".equalsIgnoreCase(usuario.getRol())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Integer idEstudiante = usuario.getIdEstudiante();

        if (idEstudiante == null || idEstudiante <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        int idPostulacion;

        try {
            idPostulacion = Integer.parseInt(
                    request.getParameter("idPostulacion"));
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (idPostulacion <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String json = BackendCliente.obtenerRespuestasPostulacion(
                idPostulacion, idEstudiante);

        if (json == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No se pudieron recuperar las respuestas.");
            return;
        }

        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(json);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario.getEstado() != null && !usuario.getEstado().equalsIgnoreCase("ACTIVO")) {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=" + URLEncoder
                    .encode("Tu cuenta ha sido dada de baja o no se encuentra activa.", StandardCharsets.UTF_8));
            return;
        }
        Integer idEstudiante = usuario.getIdEstudiante();

        if (idEstudiante == null || idEstudiante == 0) {
            List<Estudiante> estudiantes = BackendCliente.listarEstudiantes();
            for (Estudiante e : estudiantes) {
                if (e.getCorreo() != null && e.getCorreo().equalsIgnoreCase(usuario.getCorreo())) {
                    idEstudiante = e.getId();
                    usuario.setIdEstudiante(idEstudiante);
                    break;
                }
            }
        }

        if (idEstudiante == null || idEstudiante == 0) {
            response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                    URLEncoder.encode("Perfil de estudiante no vinculado.", StandardCharsets.UTF_8));
            return;
        }

        String accion = request.getParameter("accion");
        if ("editarRespuestas".equalsIgnoreCase(accion)) {
            int idPostulacion;

            try {
                idPostulacion = Integer.parseInt(
                        request.getParameter("idPostulacion"));
            } catch (NumberFormatException | NullPointerException e) {
                response.sendRedirect(request.getContextPath()
                        + "/estudiante/dashboard.jsp?error="
                        + URLEncoder.encode("Postulación no válida.",
                                StandardCharsets.UTF_8));
                return;
            }

            if (idPostulacion <= 0) {
                response.sendRedirect(request.getContextPath()
                        + "/estudiante/dashboard.jsp?error="
                        + URLEncoder.encode("Postulación no válida.",
                                StandardCharsets.UTF_8));
                return;
            }

            String[] respuestas = new String[12];

            for (int i = 0; i < 12; i++) {
                String valor = request.getParameter("respuesta" + (i + 1));

                if (valor == null || valor.trim().isEmpty()
                        || valor.trim().length() > 1000) {
                    response.sendRedirect(request.getContextPath()
                            + "/estudiante/dashboard.jsp?error="
                            + URLEncoder.encode(
                                    "Debes completar las 12 respuestas.",
                                    StandardCharsets.UTF_8));
                    return;
                }

                respuestas[i] = valor.trim();
            }

            String errorEdicion = BackendCliente.actualizarRespuestasPostulacion(
                    idPostulacion, idEstudiante, respuestas);

            if (errorEdicion == null) {
                response.sendRedirect(request.getContextPath()
                        + "/estudiante/dashboard.jsp?exito=postulacion_editada");
            } else {
                response.sendRedirect(request.getContextPath()
                        + "/estudiante/dashboard.jsp?error="
                        + URLEncoder.encode(errorEdicion,
                                StandardCharsets.UTF_8));
            }
            return;
        }
        if ("retirar".equalsIgnoreCase(accion) || "eliminar".equalsIgnoreCase(accion)) {
            String idPostulacionStr = request.getParameter("idPostulacion");
            int idPostulacion = 0;
            try {
                if (idPostulacionStr != null)
                    idPostulacion = Integer.parseInt(idPostulacionStr);
            } catch (NumberFormatException ignored) {
            }

            if (idPostulacion > 0) {
                boolean ok = BackendCliente.eliminarRegistro("postulaciones", idPostulacion);
                if (ok) {
                    response.sendRedirect(
                            request.getContextPath() + "/estudiante/dashboard.jsp?exito=postulacion_retirada");
                } else {
                    response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                            URLEncoder.encode("No se pudo retirar la postulación.", StandardCharsets.UTF_8));
                }
                return;
            }
        } else if ("darDeBaja".equalsIgnoreCase(accion)) {
            String error = BackendCliente.actualizarUsuario(usuario.getId(), usuario.getNombre(),
                    usuario.getApellidos(), usuario.getCorreo(), "", "INACTIVO");
            if (error == null) {
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login.jsp?exito=cuenta_desactivada");
            } else {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                        URLEncoder.encode(error, StandardCharsets.UTF_8));
            }
            return;
        } else if ("actualizarPerfil".equalsIgnoreCase(accion)) {
            String nombre = request.getParameter("nombre");
            String apellidos = request.getParameter("apellidos");
            String correo = request.getParameter("correo");
            String contrasena = request.getParameter("contrasena");
            String universidad = request.getParameter("universidad");
            String carrera = request.getParameter("carrera");
            String cicloStr = request.getParameter("ciclo");
            String telefono = request.getParameter("telefono");

            int ciclo = 1;
            try {
                if (cicloStr != null)
                    ciclo = Integer.parseInt(cicloStr);
            } catch (NumberFormatException ignored) {
            }

            if (apellidos == null || apellidos.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                        URLEncoder.encode("Los apellidos son obligatorios.", StandardCharsets.UTF_8));
                return;
            }

            String correoFinal = (correo != null && !correo.trim().isEmpty()) ? correo.trim() : usuario.getCorreo();

            String errUsuario = BackendCliente.actualizarUsuario(usuario.getId(),
                    nombre != null ? nombre.trim() : usuario.getNombre(),
                    apellidos.trim(),
                    correoFinal,
                    contrasena != null ? contrasena.trim() : "",
                    usuario.getEstado());

            if (errUsuario != null) {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                        URLEncoder.encode(errUsuario, StandardCharsets.UTF_8));
                return;
            }

            String error = BackendCliente.actualizarPerfilEstudiante(idEstudiante,
                    nombre != null ? nombre.trim() : usuario.getNombre(),
                    apellidos.trim(),
                    universidad != null ? universidad.trim() : "",
                    carrera != null ? carrera.trim() : "",
                    ciclo,
                    telefono != null ? telefono.trim() : "");

            if (error == null) {
                if (nombre != null && !nombre.trim().isEmpty()) {
                    usuario.setNombre(nombre.trim());
                    session.setAttribute("nombreUsuario", usuario.getNombre());
                }
                if (apellidos != null && !apellidos.trim().isEmpty()) {
                    usuario.setApellidos(apellidos.trim());
                }
                usuario.setCorreo(correoFinal);
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?exito=perfil_actualizado");
            } else {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                        URLEncoder.encode(error, StandardCharsets.UTF_8));
            }
            return;
        }

        String idOfertaStr = request.getParameter("idOferta");
        int idOferta = 0;
        try {
            if (idOfertaStr != null)
                idOferta = Integer.parseInt(idOfertaStr);
        } catch (NumberFormatException ignored) {
        }

        if (idOferta <= 0) {
            response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                    URLEncoder.encode("Oferta no válida.", StandardCharsets.UTF_8));
            return;
        }
        if (BackendCliente.obtenerCvEstudiante(idEstudiante) == null) {
            response.sendRedirect(
                    request.getContextPath() + "/estudiante/dashboard.jsp?error="
                            + URLEncoder.encode(
                                    "Debes subir tu currículum vitae en PDF antes de postular.",
                                    StandardCharsets.UTF_8));
            return;
        }

        String[] respuestas = new String[12];

        for (int i = 0; i < 12; i++) {
            String valor = request.getParameter("respuesta" + (i + 1));

            if (valor == null || valor.trim().isEmpty()
                    || valor.trim().length() > 1000) {
                response.sendRedirect(
                        request.getContextPath() + "/estudiante/dashboard.jsp?error="
                                + URLEncoder.encode(
                                        "Debes responder las 12 preguntas antes de postular.",
                                        StandardCharsets.UTF_8));
                return;
            }

            respuestas[i] = valor.trim();
        }
        String error = BackendCliente.postular(
                idEstudiante, idOferta, respuestas);

        if (error == null) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/estudiante/dashboard.jsp?exito=postulacion");
        } else {
            response.sendRedirect(
                    request.getContextPath()
                            + "/estudiante/dashboard.jsp?error="
                            + URLEncoder.encode(error, StandardCharsets.UTF_8));
        }
    }
}
