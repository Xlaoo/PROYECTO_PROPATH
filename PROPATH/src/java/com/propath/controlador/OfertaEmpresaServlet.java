package com.propath.controlador;

import com.propath.dao.DocumentoEstudianteDAO;
import com.propath.dao.EmpresaDAO;
import com.propath.dao.EstudianteDAO;
import com.propath.modelo.Estudiante;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet(name = "OfertaEmpresaServlet", urlPatterns = { "/empresa/oferta", "/OfertaEmpresaServlet" })
public class OfertaEmpresaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final transient OfertaPracticaDAO ofertaDAO = new OfertaPracticaDAO();
    private final transient EmpresaDAO empresaDAO = new EmpresaDAO();
    private final transient PostulacionDAO postulacionDAO = new PostulacionDAO();
    private final transient EntrevistaDAO entrevistaDAO = new EntrevistaDAO();
    private final transient DocumentoEstudianteDAO documentoDAO = new DocumentoEstudianteDAO();
    private final transient UsuarioDAO usuarioDAO = new UsuarioDAO();

    private String escaparJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void redirectDashboard(HttpServletRequest req, HttpServletResponse res,
            String key, String value, String tab) throws IOException {
        String url = req.getContextPath() + "/empresa/dashboard.jsp?" + key + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8);
        if (tab != null && !tab.trim().isEmpty()) {
            url += "&tab=" + URLEncoder.encode(tab.trim(), StandardCharsets.UTF_8);
        }
        res.sendRedirect(url);
    }

    private String validarFechaLimite(String fechaLimiteStr) {
        if (fechaLimiteStr == null || fechaLimiteStr.trim().isEmpty()) {
            return "La fecha límite de postulación es obligatoria.";
        }
        try {
            LocalDate fecha = LocalDate.parse(fechaLimiteStr.trim());
            if (fecha.isBefore(LocalDate.now())) {
                return "La fecha límite no puede ser anterior a la fecha actual.";
            }
            return null;
        } catch (DateTimeParseException e) {
            return "El formato de fecha límite no es válido.";
        }
    }

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
        if (!"EMPRESA".equalsIgnoreCase(usuario.getRol())) {
            response.sendError(403);
            return;
        }
        Integer idEmpresa = usuario.getIdEmpresa();
        if (idEmpresa == null || idEmpresa <= 0) {
            Empresa emp = empresaDAO.obtenerPorUsuarioId(usuario.getId());
            if (emp != null) {
                idEmpresa = emp.getId();
                usuario.setIdEmpresa(idEmpresa);
            }
        }
        if (idEmpresa == null || idEmpresa <= 0) {
            response.sendError(403, "Empresa no identificada");
            return;
        }
        Empresa empresa = empresaDAO.obtenerPorId(idEmpresa);
        if (empresa == null || !"APROBADA".equalsIgnoreCase(empresa.getEstado())
                || !"ACTIVO".equalsIgnoreCase(empresa.getEstadoCuenta())) {
            response.sendError(403, "Empresa no aprobada o cuenta inactiva");
            return;
        }
        int idPostulacion;
        try {
            idPostulacion = Integer.parseInt(request.getParameter("idPostulacion"));
        } catch (Exception e) {
            response.sendError(400, "ID de postulación inválido");
            return;
        }
        int idEstudiante = idEstudiantePostulacion(idPostulacion, idEmpresa);
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
            boolean download = "1".equals(request.getParameter("download")) || "true".equalsIgnoreCase(request.getParameter("download"));
            String nombre = doc.getNombreArchivo() != null ? doc.getNombreArchivo() : "CV.pdf";
            String encodedName = URLEncoder.encode(nombre, StandardCharsets.UTF_8).replace("+", "%20");
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    (download ? "attachment" : "inline") + "; filename=\"" + nombre + "\"; filename*=UTF-8''" + encodedName);
            response.setContentLength(pdf.length);
            response.getOutputStream().write(pdf);
            return;
        }
        if ("documento".equals(accion)) {
            int idDoc = 0;
            try {
                idDoc = Integer.parseInt(request.getParameter("idDocumento"));
            } catch (Exception ignored) {
            }
            DocumentoEstudiante doc = documentoDAO.obtenerPorId(idDoc);
            if (doc == null || doc.getIdEstudiante() != idEstudiante) {
                response.sendError(404, "Documento no encontrado");
                return;
            }
            byte[] bytes = documentoDAO.descargarCv(doc.getArchivoRuta());
            if (bytes == null || bytes.length == 0) {
                response.sendError(404, "Archivo no disponible");
                return;
            }
            boolean download = "1".equals(request.getParameter("download")) || "true".equalsIgnoreCase(request.getParameter("download"));
            String ctype = doc.getContentType() != null && !doc.getContentType().isBlank() ? doc.getContentType() : "application/pdf";
            String nombre = doc.getNombreArchivo() != null ? doc.getNombreArchivo() : "documento";
            String encodedName = URLEncoder.encode(nombre, StandardCharsets.UTF_8).replace("+", "%20");
            response.setContentType(ctype);
            response.setHeader("Content-Disposition",
                    (download ? "attachment" : "inline") + "; filename=\"" + nombre + "\"; filename*=UTF-8''" + encodedName);
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
            return;
        }
        if (!"evaluacion".equals(accion)) {
            response.sendError(400);
            return;
        }
        Postulacion post = postulacionDAO.obtenerPorId(idPostulacion);
        EstudianteDAO estDAO = new EstudianteDAO();
        Estudiante est = estDAO.obtenerPorId(idEstudiante);
        List<RespuestaPostulacion> lista = postulacionDAO.obtenerRespuestas(idPostulacion);
        List<DocumentoEstudiante> docs = documentoDAO.listarPorEstudiante(idEstudiante);
        DocumentoEstudiante docCv = documentoDAO.obtenerCvPorEstudiante(idEstudiante);
        boolean cvDisponible = (docCv != null && docCv.getRutaArchivo() != null && !docCv.getRutaArchivo().trim().isEmpty());

        StringBuilder sb = new StringBuilder("{");
        sb.append("\"cvDisponible\":").append(cvDisponible).append(",");
        sb.append("\"idPostulacion\":").append(idPostulacion).append(",");
        sb.append("\"idEstudiante\":").append(idEstudiante).append(",");
        sb.append("\"estudiante\":\"").append(escaparJson(est != null && est.getNombreCompleto() != null ? est.getNombreCompleto() : (post != null ? post.getEstudiante() : ""))).append("\",");
        sb.append("\"correo\":\"").append(escaparJson(est != null ? est.getCorreo() : "")).append("\",");
        sb.append("\"universidad\":\"").append(escaparJson(est != null ? est.getUniversidad() : "")).append("\",");
        sb.append("\"carrera\":\"").append(escaparJson(est != null ? est.getCarrera() : "")).append("\",");
        sb.append("\"ciclo\":").append(est != null ? est.getCiclo() : 0).append(",");
        sb.append("\"telefono\":\"").append(escaparJson(est != null ? est.getTelefono() : "")).append("\",");
        sb.append("\"oferta\":\"").append(escaparJson(post != null ? post.getOferta() : "")).append("\",");
        sb.append("\"fecha\":\"").append(escaparJson(post != null ? post.getFechaPostulacion() : "")).append("\",");
        sb.append("\"estado\":\"").append(escaparJson(post != null ? post.getEstado() : "")).append("\",");
        sb.append("\"motivoRechazo\":\"").append(escaparJson(post != null ? post.getMotivoRechazo() : "")).append("\",");
        sb.append("\"documentos\":[");
        for (int i = 0; i < docs.size(); i++) {
            if (i > 0) sb.append(",");
            DocumentoEstudiante d = docs.get(i);
            sb.append("{\"id\":").append(d.getId())
              .append(",\"tipoDocumento\":\"").append(escaparJson(d.getTipoDocumento())).append("\"")
              .append(",\"nombreArchivo\":\"").append(escaparJson(d.getNombreArchivo())).append("\"")
              .append(",\"tamanio\":\"").append(escaparJson(d.getTamanioFormateado())).append("\"")
              .append(",\"contentType\":\"").append(escaparJson(d.getContentType())).append("\"")
              .append(",\"fechaSubida\":\"").append(escaparJson(d.getFechaSubida())).append("\"}");
        }
        sb.append("],");
        sb.append("\"respuestas\":[");
        for (int i = 0; i < lista.size(); i++) {
            if (i > 0) sb.append(",");
            RespuestaPostulacion r = lista.get(i);
            sb.append("{\"numeroPregunta\":").append(r.getNumeroPregunta())
              .append(",\"pregunta\":\"").append(escaparJson(r.getPregunta())).append("\"")
              .append(",\"respuesta\":\"").append(escaparJson(r.getRespuesta())).append("\"}");
        }
        sb.append("]}");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(sb.toString());
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
                redirectDashboard(request, response, "error", "Campos obligatorios incompletos para actualizar la oferta.", "ofertas");
                return;
            }
            if (titulo.trim().length() > 150 || carrera.trim().length() > 150) {
                redirectDashboard(request, response, "error", "El título o carrera excede el máximo de 150 caracteres.", "ofertas");
                return;
            }
            if (vacantes < 1 || vacantes > 100) {
                redirectDashboard(request, response, "error", "El número de vacantes debe estar entre 1 y 100.", "ofertas");
                return;
            }
            if (remuneracion != null && !remuneracion.trim().isEmpty()) {
                try {
                    if (Double.parseDouble(remuneracion.trim()) < 0) {
                        redirectDashboard(request, response, "error", "La subvención económica no puede ser negativa.", "ofertas");
                        return;
                    }
                } catch (NumberFormatException nfe) {
                    redirectDashboard(request, response, "error", "Ingresa un monto numérico válido para la subvención.", "ofertas");
                    return;
                }
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
            existente.setVacantes(vacantes);
            existente.setFechaLimite(fechaLimite.trim());
            existente.setUbicacion(ubicacion != null ? ubicacion.trim() : "");
            existente.setRemuneracion(remuneracion != null ? remuneracion.trim() : "");

            boolean ok = ofertaDAO.actualizar(existente);
            if (ok) {
                redirectDashboard(request, response, "exito", "actualizada", "ofertas");
            } else {
                redirectDashboard(request, response, "error", "Error al actualizar la oferta.", "ofertas");
            }
            return;
        } else if ("cambiarEstadoOferta".equalsIgnoreCase(accion)) {
            int id = 0;
            try {
                id = Integer.parseInt(request.getParameter("id"));
            } catch (Exception ignored) {
            }
            String nuevoEstado = request.getParameter("nuevoEstado");
            if (id <= 0 || nuevoEstado == null
                    || (!"ACTIVA".equalsIgnoreCase(nuevoEstado) && !"CERRADA".equalsIgnoreCase(nuevoEstado))) {
                redirectDashboard(request, response, "error", "Operación de estado no válida.", "ofertas");
                return;
            }
            OfertaPractica existente = ofertaDAO.obtenerPorId(id);
            if (existente == null || existente.getIdEmpresa() != idEmpresa) {
                response.sendError(403, "No estás autorizado para modificar esta oferta.");
                return;
            }
            if ("RECHAZADA".equalsIgnoreCase(existente.getEstado())) {
                redirectDashboard(request, response, "error", "Una oferta rechazada por administración no puede reactivarse.", "ofertas");
                return;
            }
            if ("ACTIVA".equalsIgnoreCase(nuevoEstado)) {
                String errFecha = validarFechaLimite(existente.getFechaLimite());
                if (errFecha != null) {
                    redirectDashboard(request, response, "error", "Actualiza la fecha límite de la oferta antes de reactivarla.", "ofertas");
                    return;
                }
            }
            boolean ok = ofertaDAO.actualizarEstado(id, nuevoEstado.toUpperCase());
            if (ok) {
                redirectDashboard(request, response, "exito",
                        "ACTIVA".equalsIgnoreCase(nuevoEstado) ? "oferta_reactivada" : "oferta_cerrada", "ofertas");
            } else {
                redirectDashboard(request, response, "error", "No se pudo cambiar el estado de la oferta.", "ofertas");
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
                int cantPost = ofertaDAO.contarPostulaciones(id);
                if (cantPost > 0) {
                    ofertaDAO.actualizarEstado(id, "CERRADA");
                    redirectDashboard(request, response, "exito", "cerrada_por_postulaciones", "ofertas");
                } else {
                    ofertaDAO.eliminar(id);
                    redirectDashboard(request, response, "exito", "eliminada", "ofertas");
                }
                return;
            }
        } else if ("actualizarPerfilEmpresa".equalsIgnoreCase(accion)) {
            String repNombre = request.getParameter("representanteNombre");
            String repApellidos = request.getParameter("representanteApellidos");
            String correo = request.getParameter("correo");
            String contrasena = request.getParameter("contrasena");
            String confirmarContrasena = request.getParameter("confirmarContrasena");
            String razonSocial = request.getParameter("razonSocial");
            String rucParam = request.getParameter("ruc");
            String telefono = request.getParameter("telefono");
            String direccion = request.getParameter("direccion");
            String descripcion = request.getParameter("descripcion");

            Empresa curEmp = empresaDAO.obtenerPorId(idEmpresa);

            if (rucParam == null || !rucParam.trim().matches("^[0-9]{11}$")) {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                        URLEncoder.encode("El RUC debe contener exactamente 11 dígitos numéricos.",
                                StandardCharsets.UTF_8));
                return;
            }

            if (telefono != null && !telefono.trim().isEmpty() && !telefono.trim().matches("^[0-9]{7,9}$")) {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                        URLEncoder.encode("El teléfono corporativo debe contener entre 7 y 9 dígitos numéricos.",
                                StandardCharsets.UTF_8));
                return;
            }

            String ruc = rucParam.trim();
            String repNombreFinal = (repNombre != null && !repNombre.trim().isEmpty()) ? repNombre.trim()
                    : usuario.getNombre();
            String repApellidosFinal = (repApellidos != null && !repApellidos.trim().isEmpty()) ? repApellidos.trim()
                    : (usuario.getApellidos() != null ? usuario.getApellidos() : "");
            
            String correoFinal = usuario.getCorreo();
            if (correo != null && !correo.trim().isEmpty() && !correo.trim().equalsIgnoreCase(usuario.getCorreo())) {
                String correoVerificado = (String) session.getAttribute("CORREO_VERIFICADO_PERFIL");
                if (correoVerificado != null && correoVerificado.equalsIgnoreCase(correo.trim())) {
                    correoFinal = correo.trim().toLowerCase();
                    session.removeAttribute("CORREO_VERIFICADO_PERFIL");
                } else {
                    response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                            URLEncoder.encode("Para modificar tu correo corporativo debes verificarlo con el código de seguridad enviado al nuevo correo.", StandardCharsets.UTF_8));
                    return;
                }
            }

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
            boolean contrasenaCambiada = false;
            if (contrasena != null && !contrasena.trim().isEmpty()) {
                if (contrasena.trim().length() < 8 || !contrasena.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
                    response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                            URLEncoder.encode("La nueva contraseña debe tener al menos 8 caracteres y combinar letras y números.", StandardCharsets.UTF_8));
                    return;
                }
                if (!contrasena.trim().equals(confirmarContrasena != null ? confirmarContrasena.trim() : "")) {
                    response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                            URLEncoder.encode("La nueva contraseña y su confirmación no coinciden.", StandardCharsets.UTF_8));
                    return;
                }
                usuarioDAO.actualizarContrasena(usuario.getId(), contrasena.trim());
                usuario.setContrasena(com.propath.conexion.SeguridadUtil.encriptarPassword(contrasena.trim()));
                contrasenaCambiada = true;
            }
            usuarioDAO.actualizar(usuario);

            if (contrasenaCambiada) {
                com.propath.servicio.DesafioSeguridadServicio.invalidarTodoPorCorreo(usuario.getCorreo());
                if (com.propath.servicio.ServicioCorreo.isConfigurado()) {
                    try {
                        com.propath.servicio.ServicioCorreo.enviarConfirmacionCambioPassword(usuario.getCorreo(), repNombreFinal);
                    } catch (Exception ignored) {
                    }
                }
            }

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
                    || (!"EN_REVISION".equalsIgnoreCase(estado)
                            && !"SELECCIONADO".equalsIgnoreCase(estado)
                            && !"NO_SELECCIONADO".equalsIgnoreCase(estado))) {
                redirectDashboard(request, response, "error", "Datos de evaluación inválidos.", "postulantes");
                return;
            }

            Postulacion p = postulacionDAO.obtenerPorId(id);
            if (p == null) {
                redirectDashboard(request, response, "error", "Postulación no encontrada.", "postulantes");
                return;
            }

            OfertaPractica of = ofertaDAO.obtenerPorId(p.getIdOferta());
            if (of == null || of.getIdEmpresa() != idEmpresa) {
                response.sendError(403, "La postulación no pertenece a tu empresa.");
                return;
            }

            String estadoActual = p.getEstado();

            if ("NO_SELECCIONADO".equalsIgnoreCase(estadoActual)) {
                redirectDashboard(request, response, "error", "Esta postulación ya fue finalizada como No Seleccionado.", "postulantes");
                return;
            }

            if ("SELECCIONADO".equalsIgnoreCase(estadoActual)) {
                if ("EN_REVISION".equalsIgnoreCase(estado) || "SELECCIONADO".equalsIgnoreCase(estado)) {
                    redirectDashboard(request, response, "error", "El postulante ya se encuentra seleccionado.", "postulantes");
                    return;
                }
                if (entrevistaDAO.tieneEntrevistaActiva(id)) {
                    redirectDashboard(request, response, "error", "No puedes rechazar al postulante porque ya tiene una entrevista activa.", "postulantes");
                    return;
                }
            }

            String motivoRechazo = request.getParameter("motivoRechazo");
            if ("NO_SELECCIONADO".equalsIgnoreCase(estado)) {
                if (motivoRechazo == null
                        || motivoRechazo.trim().isEmpty()
                        || motivoRechazo.trim().length() > 1000) {
                    redirectDashboard(request, response, "error", "Debes escribir un motivo de rechazo válido (máx. 1000 caracteres).", "postulantes");
                    return;
                }
                motivoRechazo = motivoRechazo.trim();
            } else {
                motivoRechazo = null;
            }

            boolean actualizado = postulacionDAO.evaluar(id, estado.toUpperCase(), motivoRechazo);

            if (actualizado) {
                redirectDashboard(request, response, "exito", "estado_actualizado", "postulantes");
            } else {
                redirectDashboard(request, response, "error", "No se pudo actualizar la postulación.", "postulantes");
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
                    || descripcion == null || descripcion.trim().isEmpty()
                    || fechaLimite == null || fechaLimite.trim().isEmpty()) {
                redirectDashboard(request, response, "error", "Título, carrera, descripción y fecha límite son obligatorios.", "ofertas");
                return;
            }
            if (titulo.trim().length() > 150 || carrera.trim().length() > 150) {
                redirectDashboard(request, response, "error", "El título o la carrera supera el máximo de 150 caracteres.", "ofertas");
                return;
            }
            String errFecha = validarFechaLimite(fechaLimite);
            if (errFecha != null) {
                redirectDashboard(request, response, "error", errFecha, "ofertas");
                return;
            }
            if (vacantes < 1 || vacantes > 100) {
                redirectDashboard(request, response, "error", "El número de vacantes debe estar entre 1 y 100.", "ofertas");
                return;
            }
            if (remuneracion != null && !remuneracion.trim().isEmpty()) {
                try {
                    if (Double.parseDouble(remuneracion.trim()) < 0) {
                        redirectDashboard(request, response, "error", "La subvención económica no puede ser negativa.", "ofertas");
                        return;
                    }
                } catch (NumberFormatException nfe) {
                    redirectDashboard(request, response, "error", "Ingresa un monto numérico válido para la subvención.", "ofertas");
                    return;
                }
            }

            OfertaPractica nuevaOferta = new OfertaPractica();
            nuevaOferta.setIdEmpresa(idEmpresa);
            nuevaOferta.setTitulo(titulo.trim());
            nuevaOferta.setDescripcion(descripcion.trim());
            nuevaOferta.setRequisitos(requisitos != null ? requisitos.trim() : "");
            nuevaOferta.setCarrera(carrera.trim());
            nuevaOferta.setModalidad(modalidad != null ? modalidad.trim() : "PRESENCIAL");
            nuevaOferta.setVacantes(vacantes);
            nuevaOferta.setFechaLimite(fechaLimite.trim());
            nuevaOferta.setUbicacion(ubicacion != null ? ubicacion.trim() : "");
            nuevaOferta.setRemuneracion(remuneracion != null ? remuneracion.trim() : "");
            nuevaOferta.setEstado("ACTIVA");

            int idGenerado = ofertaDAO.crear(nuevaOferta);

            if (idGenerado > 0) {
                redirectDashboard(request, response, "exito", "publicada", "ofertas");
                return;
            } else {
                redirectDashboard(request, response, "error", "No se pudo publicar la oferta.", "ofertas");
                return;
            }
        }
    }
}