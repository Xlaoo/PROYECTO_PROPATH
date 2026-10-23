package com.propath.controlador;

import com.propath.dao.DocumentoEstudianteDAO;
import com.propath.dao.EstudianteDAO;
import com.propath.dao.OfertaPracticaDAO;
import com.propath.dao.PostulacionDAO;
import com.propath.dao.UsuarioDAO;
import com.propath.modelo.DocumentoEstudiante;
import com.propath.modelo.Estudiante;
import com.propath.modelo.OfertaPractica;
import com.propath.modelo.Postulacion;
import com.propath.modelo.RespuestaPostulacion;
import com.propath.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "PostularServlet", urlPatterns = { "/estudiante/postular", "/PostularServlet" })
public class PostularServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final PostulacionDAO postulacionDAO = new PostulacionDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final OfertaPracticaDAO ofertaDAO = new OfertaPracticaDAO();
    private final DocumentoEstudianteDAO documentoDAO = new DocumentoEstudianteDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || !(session.getAttribute("usuarioLogueado") instanceof Usuario)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado()) || !"ESTUDIANTE".equalsIgnoreCase(usuario.getRol())) {
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
            idPostulacion = Integer.parseInt(request.getParameter("idPostulacion"));
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (idPostulacion <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Postulacion postulacion = postulacionDAO.obtenerPorId(idPostulacion);
        if (postulacion == null || postulacion.getIdEstudiante() != idEstudiante) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Postulación no autorizada.");
            return;
        }

        List<RespuestaPostulacion> respuestas = postulacionDAO.obtenerRespuestas(idPostulacion);
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < respuestas.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            RespuestaPostulacion r = respuestas.get(i);
            String preg = r.getPregunta() != null ? r.getPregunta().replace("\"", "\\\"") : "";
            String resp = r.getRespuesta() != null ? r.getRespuesta().replace("\"", "\\\"") : "";
            json.append("{\"numeroPregunta\":").append(r.getNumeroPregunta())
                    .append(",\"pregunta\":\"").append(preg).append("\"")
                    .append(",\"respuesta\":\"").append(resp).append("\"}");
        }
        json.append("]");

        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(json.toString());
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
            Estudiante est = estudianteDAO.obtenerPorCorreo(usuario.getCorreo());
            if (est != null) {
                idEstudiante = est.getId();
                usuario.setIdEstudiante(idEstudiante);
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
                idPostulacion = Integer.parseInt(request.getParameter("idPostulacion"));
            } catch (NumberFormatException | NullPointerException e) {
                response.sendRedirect(request.getContextPath()
                        + "/estudiante/dashboard.jsp?error="
                        + URLEncoder.encode("Postulación no válida.", StandardCharsets.UTF_8));
                return;
            }

            if (idPostulacion <= 0) {
                response.sendRedirect(request.getContextPath()
                        + "/estudiante/dashboard.jsp?error="
                        + URLEncoder.encode("Postulación no válida.", StandardCharsets.UTF_8));
                return;
            }

            Postulacion postulacion = postulacionDAO.obtenerPorId(idPostulacion);
            if (postulacion == null || postulacion.getIdEstudiante() != idEstudiante) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Esta postulación no te pertenece.");
                return;
            }

            String[] respuestas = new String[12];

            for (int i = 0; i < 12; i++) {
                String valor = request.getParameter("respuesta" + (i + 1));

                if (valor == null || valor.trim().isEmpty() || valor.trim().length() > 1000) {
                    response.sendRedirect(request.getContextPath()
                            + "/estudiante/dashboard.jsp?error="
                            + URLEncoder.encode("Debes completar las 12 respuestas.", StandardCharsets.UTF_8));
                    return;
                }

                respuestas[i] = valor.trim();
            }

            boolean okEdicion = postulacionDAO.actualizarRespuestas(idPostulacion, respuestas);

            if (okEdicion) {
                response.sendRedirect(request.getContextPath()
                        + "/estudiante/dashboard.jsp?exito=postulacion_editada");
            } else {
                response.sendRedirect(request.getContextPath()
                        + "/estudiante/dashboard.jsp?error="
                        + URLEncoder.encode("Error al actualizar respuestas.", StandardCharsets.UTF_8));
            }
            return;
        } else if ("retirar".equalsIgnoreCase(accion) || "eliminar".equalsIgnoreCase(accion)) {
            int idPostulacion;

            try {
                idPostulacion = Integer.parseInt(request.getParameter("idPostulacion"));
            } catch (Exception e) {
                response.sendError(400, "Postulación inválida.");
                return;
            }

            if (idPostulacion <= 0) {
                response.sendError(400, "Postulación inválida.");
                return;
            }

            Postulacion postulacionActual = postulacionDAO.obtenerPorId(idPostulacion);

            if (postulacionActual == null) {
                response.sendError(404, "Postulación no encontrada.");
                return;
            }

            if (postulacionActual.getIdEstudiante() != idEstudiante) {
                response.sendError(403, "Esta postulación no te pertenece.");
                return;
            }

            String estado = postulacionActual.getEstado();

            boolean retirar = "retirar".equalsIgnoreCase(accion);
            boolean eliminar = "eliminar".equalsIgnoreCase(accion);

            if (retirar && !"POSTULADO".equalsIgnoreCase(estado)) {
                response.sendError(409, "Esta postulación ya no puede retirarse.");
                return;
            }

            if (eliminar && !"NO_SELECCIONADO".equalsIgnoreCase(estado)) {
                response.sendError(409, "Solo puedes eliminar postulaciones rechazadas.");
                return;
            }

            boolean ok = postulacionDAO.retirar(idPostulacion);

            if (ok) {
                String mensaje = retirar ? "postulacion_retirada" : "postulacion_eliminada";
                response.sendRedirect(request.getContextPath()
                        + "/estudiante/dashboard.jsp?exito=" + mensaje);
            } else {
                response.sendRedirect(request.getContextPath()
                        + "/estudiante/dashboard.jsp?error="
                        + URLEncoder.encode("No se pudo eliminar la postulación.", StandardCharsets.UTF_8));
            }
            return;
        } else if ("darDeBaja".equalsIgnoreCase(accion)) {
            usuarioDAO.actualizarEstado(usuario.getId(), "INACTIVO");
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login.jsp?exito=cuenta_desactivada");
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
                if (cicloStr != null) {
                    ciclo = Integer.parseInt(cicloStr);
                }
            } catch (NumberFormatException ignored) {
            }

            if (apellidos == null || apellidos.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                        URLEncoder.encode("Los apellidos son obligatorios.", StandardCharsets.UTF_8));
                return;
            }

            String correoFinal = (correo != null && !correo.trim().isEmpty()) ? correo.trim() : usuario.getCorreo();

            usuario.setNombre(nombre != null ? nombre.trim() : usuario.getNombre());
            usuario.setApellidos(apellidos.trim());
            usuario.setCorreo(correoFinal);
            if (contrasena != null && !contrasena.trim().isEmpty()) {
                usuario.setContrasena(com.propath.conexion.SeguridadUtil.encriptar(contrasena.trim()));
            }
            usuarioDAO.actualizar(usuario);

            Estudiante estudiante = estudianteDAO.obtenerPorId(idEstudiante);
            if (estudiante != null) {
                estudiante.setNombres(nombre != null ? nombre.trim() : usuario.getNombre());
                estudiante.setApellidos(apellidos.trim());
                estudiante.setUniversidad(universidad != null ? universidad.trim() : "");
                estudiante.setCarrera(carrera != null ? carrera.trim() : "");
                estudiante.setCiclo(ciclo);
                estudiante.setTelefono(telefono != null ? telefono.trim() : "");
                estudianteDAO.actualizar(estudiante);
            }

            if (nombre != null && !nombre.trim().isEmpty()) {
                session.setAttribute("nombreUsuario", nombre.trim());
            }
            session.setAttribute("usuarioLogueado", usuario);
            session.setAttribute("correoUsuario", correoFinal);
            response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?exito=perfil_actualizado");
            return;
        }

        String idOfertaStr = request.getParameter("idOferta");
        int idOferta = 0;
        try {
            if (idOfertaStr != null) {
                idOferta = Integer.parseInt(idOfertaStr);
            }
        } catch (NumberFormatException ignored) {
        }

        if (idOferta <= 0) {
            response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                    URLEncoder.encode("Oferta no válida.", StandardCharsets.UTF_8));
            return;
        }

        DocumentoEstudiante doc = documentoDAO.obtenerCvPorEstudiante(idEstudiante);
        if (doc == null || doc.getRutaArchivo() == null) {
            response.sendRedirect(
                    request.getContextPath() + "/estudiante/dashboard.jsp?error="
                            + URLEncoder.encode("Debes subir tu currículum vitae en PDF antes de postular.",
                                    StandardCharsets.UTF_8));
            return;
        }

        if (postulacionDAO.yaPostulo(idEstudiante, idOferta)) {
            response.sendRedirect(
                    request.getContextPath() + "/estudiante/dashboard.jsp?error="
                            + URLEncoder.encode("Ya te has postulado previamente a esta oferta.",
                                    StandardCharsets.UTF_8));
            return;
        }

        OfertaPractica oferta = ofertaDAO.obtenerPorId(idOferta);
        if (oferta == null || !"ACTIVA".equalsIgnoreCase(oferta.getEstado())) {
            response.sendRedirect(
                    request.getContextPath() + "/estudiante/dashboard.jsp?error="
                            + URLEncoder.encode("La oferta seleccionada ya no está disponible.",
                                    StandardCharsets.UTF_8));
            return;
        }

        List<RespuestaPostulacion> respuestasList = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            String valor = request.getParameter("respuesta" + (i + 1));

            if (valor == null || valor.trim().isEmpty() || valor.trim().length() > 1000) {
                response.sendRedirect(
                        request.getContextPath() + "/estudiante/dashboard.jsp?error="
                                + URLEncoder.encode("Debes responder las 12 preguntas antes de postular.",
                                        StandardCharsets.UTF_8));
                return;
            }

            RespuestaPostulacion resp = new RespuestaPostulacion();
            resp.setNumeroPregunta(i + 1);
            resp.setPregunta("Pregunta " + (i + 1));
            resp.setRespuesta(valor.trim());
            respuestasList.add(resp);
        }

        int idGenerado = postulacionDAO.registrarConRespuestas(idEstudiante, idOferta, respuestasList);

        if (idGenerado > 0) {
            response.sendRedirect(
                    request.getContextPath() + "/estudiante/dashboard.jsp?exito=postulacion");
        } else {
            response.sendRedirect(
                    request.getContextPath() + "/estudiante/dashboard.jsp?error="
                            + URLEncoder.encode("Error al procesar la postulación.", StandardCharsets.UTF_8));
        }
    }
}