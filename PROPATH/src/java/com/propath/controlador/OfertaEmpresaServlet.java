package com.propath.controlador;

import com.propath.dao.DocumentoEstudianteDAO;
import com.propath.dao.EmpresaDAO;
import com.propath.dao.EntrevistaDAO;
import com.propath.dao.OfertaPracticaDAO;
import com.propath.dao.PostulacionDAO;
import com.propath.dao.UsuarioDAO;
import com.propath.modelo.DocumentoEstudiante;
import com.propath.modelo.Empresa;
import com.propath.modelo.Entrevista;
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
import java.util.List;

@WebServlet(name = "OfertaEmpresaServlet", urlPatterns = { "/empresa/oferta", "/OfertaEmpresaServlet" })
public class OfertaEmpresaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final OfertaPracticaDAO ofertaDAO = new OfertaPracticaDAO();
    private final EmpresaDAO empresaDAO = new EmpresaDAO();
    private final PostulacionDAO postulacionDAO = new PostulacionDAO();
    private final EntrevistaDAO entrevistaDAO = new EntrevistaDAO();
    private final DocumentoEstudianteDAO documentoDAO = new DocumentoEstudianteDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private int idEstudiantePostulacion(int idPostulacion, int idEmpresa) {
        Postulacion p = postulacionDAO.obtenerPorId(idPostulacion);
        if (p == null) {
            return 0;
        }
        OfertaPractica o = ofertaDAO.obtenerPorId(p.getIdOferta());
        if (o == null || o.getIdEmpresa() != idEmpresa) {
            return 0;
        }
        return p.getIdEstudiante();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("usuarioLogueado") instanceof Usuario)) {
            response.sendError(401);
            return;
        }
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (!"EMPRESA".equalsIgnoreCase(usuario.getRol()) || usuario.getIdEmpresa() == null
                || usuario.getIdEmpresa() <= 0) {
            response.sendError(403);
            return;
        }
        Empresa empresa = empresaDAO.obtenerPorId(usuario.getIdEmpresa());
        if (empresa == null || !"APROBADA".equalsIgnoreCase(empresa.getEstado())
                || !"ACTIVO".equalsIgnoreCase(empresa.getEstadoCuenta())) {
            response.sendError(403);
            return;
        }
        int idPostulacion;
        try {
            idPostulacion = Integer.parseInt(request.getParameter("idPostulacion"));
        } catch (Exception e) {
            response.sendError(400);
            return;
        }
        int idEstudiante = idEstudiantePostulacion(idPostulacion, usuario.getIdEmpresa());
        if (idEstudiante <= 0) {
            response.sendError(403, "Postulación no autorizada o estudiante no identificado");
            return;
        }
        String accion = request.getParameter("accion");
        response.setHeader("Cache-Control", "no-store");
        if ("cv".equals(accion)) {
            DocumentoEstudiante doc = documentoDAO.obtenerCvPorEstudiante(idEstudiante);
            if (doc == null || doc.getRutaArchivo() == null) {
                response.sendError(404, "CV no encontrado");
                return;
            }
            byte[] pdf = documentoDAO.descargarCv(doc.getRutaArchivo());
            if (pdf == null || pdf.length < 4 || pdf[0] != '%' || pdf[1] != 'P'
                    || pdf[2] != 'D' || pdf[3] != 'F') {
                response.sendError(404, "CV PDF no disponible");
                return;
            }
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=CV.pdf");
            response.setContentLength(pdf.length);
            response.getOutputStream().write(pdf);
            return;
        }
        if (!"evaluacion".equals(accion)) {
            response.sendError(400);
            return;
        }
        List<RespuestaPostulacion> lista = postulacionDAO.obtenerRespuestas(idPostulacion);
        StringBuilder jsonRespuestas = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            if (i > 0) {
                jsonRespuestas.append(",");
            }
            RespuestaPostulacion r = lista.get(i);
            String preg = r.getPregunta() != null ? r.getPregunta().replace("\"", "\\\"") : "";
            String resp = r.getRespuesta() != null ? r.getRespuesta().replace("\"", "\\\"") : "";
            jsonRespuestas.append("{\"numeroPregunta\":").append(r.getNumeroPregunta())
                    .append(",\"pregunta\":\"").append(preg).append("\"")
                    .append(",\"respuesta\":\"").append(resp).append("\"}");
        }
        jsonRespuestas.append("]");
        boolean cvDisponible = documentoDAO.obtenerCvPorEstudiante(idEstudiante) != null;
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"cvDisponible\":" + cvDisponible
                + ",\"respuestas\":" + jsonRespuestas.toString() + "}");
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
        if (!"EMPRESA".equalsIgnoreCase(usuario.getRol())) {
            response.sendError(403);
            return;
        }
        Integer idEmpresa = usuario.getIdEmpresa();

        if (idEmpresa == null || idEmpresa == 0) {
            Empresa emp = empresaDAO.obtenerPorCorreo(usuario.getCorreo());
            if (emp != null) {
                idEmpresa = emp.getId();
                usuario.setIdEmpresa(idEmpresa);
                usuario.setRazonSocial(emp.getRazonSocial());
            }
        }

        if (idEmpresa == null || idEmpresa == 0) {
            response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                    URLEncoder.encode("Perfil de empresa no vinculado.", StandardCharsets.UTF_8));
            return;
        }

        String accion = request.getParameter("accion");

        if (!"actualizarPerfilEmpresa".equalsIgnoreCase(accion) && !"darDeBaja".equalsIgnoreCase(accion)) {
            Empresa empresaActual = empresaDAO.obtenerPorId(idEmpresa);
            String estEmp = empresaActual != null ? empresaActual.getEstado() : "PENDIENTE";
            String estCta = empresaActual != null ? empresaActual.getEstadoCuenta() : usuario.getEstado();

            if (!"APROBADA".equalsIgnoreCase(estEmp) || !"ACTIVO".equalsIgnoreCase(estCta)) {
                String msg;
                if ("PENDIENTE".equalsIgnoreCase(estEmp)) {
                    msg = "Tu empresa está pendiente de aprobación por el administrador de PROPATH. No puedes publicar ofertas ni gestionar postulaciones hasta que tu empresa sea aprobada.";
                } else if ("RECHAZADA".equalsIgnoreCase(estEmp)) {
                    msg = "Tu empresa no ha sido aprobada. No puedes publicar ofertas ni gestionar postulaciones.";
                } else if ("SUSPENDIDA".equalsIgnoreCase(estEmp)) {
                    msg = "Tu empresa se encuentra suspendida. Las funciones de publicación y gestión de postulaciones están deshabilitadas.";
                } else {
                    msg = "Tu cuenta se encuentra inactiva. Las funciones de gestión empresarial están deshabilitadas.";
                }
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                        URLEncoder.encode(msg, StandardCharsets.UTF_8));
                return;
            }
        }

        if ("actualizar".equalsIgnoreCase(accion)) {
            String idStr = request.getParameter("id");
            int id = 0;
            try {
                if (idStr != null) {
                    id = Integer.parseInt(idStr);
                }
            } catch (NumberFormatException ignored) {
            }

            String titulo = request.getParameter("titulo");
            String descripcion = request.getParameter("descripcion");
            String requisitos = request.getParameter("requisitos");
            String carrera = request.getParameter("carrera");
            String modalidad = request.getParameter("modalidad");
            String vacantesStr = request.getParameter("vacantes");
            String fechaLimite = request.getParameter("fechaLimite");
            String ubicacion = request.getParameter("ubicacion");
            String remuneracion = request.getParameter("remuneracion");

            int vacantes = 1;
            try {
                if (vacantesStr != null) {
                    vacantes = Integer.parseInt(vacantesStr);
                }
            } catch (NumberFormatException ignored) {
            }

            if (id <= 0 || titulo == null || titulo.trim().isEmpty() || carrera == null || carrera.trim().isEmpty()
                    || fechaLimite == null || fechaLimite.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                        URLEncoder.encode("Campos obligatorios incompletos para actualizar la oferta.",
                                StandardCharsets.UTF_8));
                return;
            }

            OfertaPractica existente = ofertaDAO.obtenerPorId(id);
            if (existente == null || existente.getIdEmpresa() != idEmpresa) {
                response.sendError(403, "No estás autorizado para modificar esta oferta.");
                return;
            }

            existente.setTitulo(titulo.trim());
            existente.setDescripcion(descripcion != null ? descripcion.trim() : "");
            existente.setRequisitos(requisitos != null ? requisitos.trim() : "");
            existente.setCarrera(carrera.trim());
            existente.setModalidad(modalidad != null ? modalidad.trim() : "PRESENCIAL");
            existente.setVacantes(vacantes > 0 ? vacantes : 1);
            existente.setFechaLimite(fechaLimite.trim());
            existente.setUbicacion(ubicacion != null ? ubicacion.trim() : "");
            existente.setRemuneracion(remuneracion != null ? remuneracion.trim() : "");

            boolean ok = ofertaDAO.actualizar(existente);
            if (ok) {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?exito=actualizada");
            } else {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                        URLEncoder.encode("Error al actualizar la oferta.", StandardCharsets.UTF_8));
            }
            return;
        } else if ("eliminar".equalsIgnoreCase(accion)) {
            String idStr = request.getParameter("id");
            int id = 0;
            try {
                if (idStr != null) {
                    id = Integer.parseInt(idStr);
                }
            } catch (NumberFormatException ignored) {
            }

            if (id > 0) {
                OfertaPractica existente = ofertaDAO.obtenerPorId(id);
                if (existente == null || existente.getIdEmpresa() != idEmpresa) {
                    response.sendError(403, "No estás autorizado para eliminar esta oferta.");
                    return;
                }
                ofertaDAO.eliminar(id);
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?exito=eliminada");
                return;
            }
        } else if ("actualizarPerfilEmpresa".equalsIgnoreCase(accion)) {
            String repNombre = request.getParameter("representanteNombre");
            String repApellidos = request.getParameter("representanteApellidos");
            String correo = request.getParameter("correo");
            String contrasena = request.getParameter("contrasena");
            String razonSocial = request.getParameter("razonSocial");
            String rucParam = request.getParameter("ruc");
            String telefono = request.getParameter("telefono");
            String direccion = request.getParameter("direccion");
            String descripcion = request.getParameter("descripcion");

            Empresa curEmp = empresaDAO.obtenerPorId(idEmpresa);

            if (rucParam == null || !rucParam.trim().matches("\\d{11}")) {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                        URLEncoder.encode("El RUC debe contener exactamente 11 dígitos numéricos.",
                                StandardCharsets.UTF_8));
                return;
            }

            String ruc = rucParam.trim();
            String repNombreFinal = (repNombre != null && !repNombre.trim().isEmpty()) ? repNombre.trim()
                    : usuario.getNombre();
            String repApellidosFinal = (repApellidos != null && !repApellidos.trim().isEmpty()) ? repApellidos.trim()
                    : (usuario.getApellidos() != null ? usuario.getApellidos() : "");
            String correoFinal = (correo != null && !correo.trim().isEmpty()) ? correo.trim() : usuario.getCorreo();
            String razonFinal = (razonSocial != null && !razonSocial.trim().isEmpty()) ? razonSocial.trim()
                    : (curEmp != null ? curEmp.getRazonSocial() : usuario.getRazonSocial());

            if (curEmp != null) {
                curEmp.setRazonSocial(razonFinal);
                curEmp.setRuc(ruc);
                curEmp.setTelefono(telefono != null ? telefono.trim() : "");
                curEmp.setDireccion(direccion != null ? direccion.trim() : "");
                curEmp.setDescripcion(descripcion != null ? descripcion.trim() : "");
                empresaDAO.actualizar(curEmp);
            }

            usuario.setNombre(repNombreFinal);
            usuario.setApellidos(repApellidosFinal);
            usuario.setCorreo(correoFinal);
            usuario.setRazonSocial(razonFinal);
            if (contrasena != null && !contrasena.trim().isEmpty()) {
                usuario.setContrasena(com.propath.conexion.SeguridadUtil.encriptar(contrasena.trim()));
            }
            usuarioDAO.actualizar(usuario);

            session.setAttribute("usuarioLogueado", usuario);
            session.setAttribute("nombreUsuario", razonFinal);
            session.setAttribute("correoUsuario", correoFinal);
            response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?exito=perfil_actualizado");
            return;
        } else if ("darDeBaja".equalsIgnoreCase(accion)) {
            usuarioDAO.actualizarEstado(usuario.getId(), "INACTIVO");
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login.jsp?exito=cuenta_desactivada");
            return;
        } else if ("cambiarEstadoPostulacion".equalsIgnoreCase(accion)) {
            String idStr = request.getParameter("idPostulacion");
            String estado = request.getParameter("estado");

            int id = 0;
            try {
                if (idStr != null) {
                    id = Integer.parseInt(idStr);
                }
            } catch (NumberFormatException ignored) {
            }

            if (id <= 0 || estado == null
                    || (!"SELECCIONADO".equalsIgnoreCase(estado)
                            && !"NO_SELECCIONADO".equalsIgnoreCase(estado))) {
                response.sendError(400, "Datos de evaluación inválidos.");
                return;
            }

            Postulacion p = postulacionDAO.obtenerPorId(id);
            if (p == null) {
                response.sendError(404, "Postulación no encontrada.");
                return;
            }

            OfertaPractica of = ofertaDAO.obtenerPorId(p.getIdOferta());
            if (of == null || of.getIdEmpresa() != idEmpresa) {
                response.sendError(403, "La postulación no pertenece a tu empresa.");
                return;
            }

            String estadoActual = p.getEstado();

            if ("NO_SELECCIONADO".equalsIgnoreCase(estadoActual)) {
                response.sendError(409, "Esta postulación ya fue rechazada.");
                return;
            }

            if ("SELECCIONADO".equalsIgnoreCase(estadoActual)) {
                if (!"NO_SELECCIONADO".equalsIgnoreCase(estado)) {
                    response.sendError(409, "No puedes seleccionar nuevamente al postulante.");
                    return;
                }

                List<Entrevista> entrevistas = entrevistaDAO.listarPorPostulacion(id);
                if (entrevistas != null && !entrevistas.isEmpty()) {
                    response.sendError(409, "No puedes rechazar al postulante porque ya tiene una entrevista registrada.");
                    return;
                }
            }

            String motivoRechazo = request.getParameter("motivoRechazo");

            if ("NO_SELECCIONADO".equalsIgnoreCase(estado)) {
                if (motivoRechazo == null
                        || motivoRechazo.trim().isEmpty()
                        || motivoRechazo.trim().length() > 1000) {
                    response.sendError(400, "Debes escribir un motivo de rechazo.");
                    return;
                }
            }

            boolean actualizado = postulacionDAO.evaluar(id, estado, motivoRechazo);

            if (actualizado) {
                response.sendRedirect(request.getContextPath()
                        + "/empresa/dashboard.jsp?exito=estado_actualizado");
            } else {
                response.sendRedirect(request.getContextPath()
                        + "/empresa/dashboard.jsp?error="
                        + URLEncoder.encode("No se pudo actualizar la postulación.",
                                StandardCharsets.UTF_8));
            }
            return;
        } else {
            String titulo = request.getParameter("titulo");
            String descripcion = request.getParameter("descripcion");
            String requisitos = request.getParameter("requisitos");
            String carrera = request.getParameter("carrera");
            String modalidad = request.getParameter("modalidad");
            String vacantesStr = request.getParameter("vacantes");
            String fechaLimite = request.getParameter("fechaLimite");
            String ubicacion = request.getParameter("ubicacion");
            String remuneracion = request.getParameter("remuneracion");

            int vacantes = 1;
            try {
                if (vacantesStr != null) {
                    vacantes = Integer.parseInt(vacantesStr);
                }
            } catch (NumberFormatException ignored) {
            }

            if (titulo == null || titulo.trim().isEmpty() || carrera == null || carrera.trim().isEmpty()
                    || fechaLimite == null || fechaLimite.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                        URLEncoder.encode("Título, carrera y fecha límite son obligatorios.", StandardCharsets.UTF_8));
                return;
            }

            OfertaPractica nuevaOferta = new OfertaPractica();
            nuevaOferta.setIdEmpresa(idEmpresa);
            nuevaOferta.setTitulo(titulo.trim());
            nuevaOferta.setDescripcion(descripcion != null ? descripcion.trim() : "");
            nuevaOferta.setRequisitos(requisitos != null ? requisitos.trim() : "");
            nuevaOferta.setCarrera(carrera.trim());
            nuevaOferta.setModalidad(modalidad != null ? modalidad.trim() : "PRESENCIAL");
            nuevaOferta.setVacantes(vacantes > 0 ? vacantes : 1);
            nuevaOferta.setFechaLimite(fechaLimite.trim());
            nuevaOferta.setUbicacion(ubicacion != null ? ubicacion.trim() : "");
            nuevaOferta.setRemuneracion(remuneracion != null ? remuneracion.trim() : "");
            nuevaOferta.setEstado("ACTIVA");

            int idGenerado = ofertaDAO.crear(nuevaOferta);

            if (idGenerado > 0) {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?exito=publicada");
                return;
            } else {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                        URLEncoder.encode("No se pudo publicar la oferta.", StandardCharsets.UTF_8));
                return;
            }
        }
    }
}