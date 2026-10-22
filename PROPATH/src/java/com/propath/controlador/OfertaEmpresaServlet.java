package com.propath.controlador;

import com.propath.modelo.Empresa;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "OfertaEmpresaServlet", urlPatterns = { "/empresa/oferta", "/OfertaEmpresaServlet" })
public class OfertaEmpresaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private int idEstudiantePostulacion(int idPostulacion, int idEmpresa) {
        Empresa empresa = BackendCliente.obtenerEmpresaPorId(idEmpresa);
        if (empresa == null || empresa.getRazonSocial() == null)
            return 0;
        boolean pertenece = false;
        for (com.propath.modelo.Postulacion p : BackendCliente.listarPostulaciones()) {
            if (p.getId() == idPostulacion && p.getEmpresa() != null
                    && empresa.getRazonSocial().trim().equalsIgnoreCase(p.getEmpresa().trim())) {
                pertenece = true;
                break;
            }
        }
        if (!pertenece)
            return 0;
        String detalle = BackendCliente.obtenerDetallePostulacion(idPostulacion);
        if (detalle == null)
            return 0;
        Matcher estudiante = Pattern.compile("\"estudiante\"\\s*:\\s*\\{[^{}]*\"id\"\\s*:\\s*(\\d+)", Pattern.DOTALL)
                .matcher(detalle);
        if (!estudiante.find())
            return 0;
        return Integer.parseInt(estudiante.group(1));
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
        Empresa empresa = BackendCliente.obtenerEmpresaPorId(usuario.getIdEmpresa());
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
            byte[] pdf = BackendCliente.descargarCvEstudiante(idEstudiante, true);
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
        String respuestas = BackendCliente.obtenerRespuestasPostulacion(idPostulacion, idEstudiante);
        if (respuestas == null || !respuestas.trim().startsWith("[")) {
            response.sendError(502, "No se pudieron obtener las respuestas");
            return;
        }
        boolean cvDisponible = BackendCliente.obtenerCvEstudiante(idEstudiante) != null;
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"cvDisponible\":" + cvDisponible
                + ",\"respuestas\":" + respuestas + "}");
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
            List<Empresa> empresas = BackendCliente.listarEmpresas();
            for (Empresa emp : empresas) {
                if (emp.getCorreo() != null && emp.getCorreo().equalsIgnoreCase(usuario.getCorreo())) {
                    idEmpresa = emp.getId();
                    usuario.setIdEmpresa(idEmpresa);
                    usuario.setRazonSocial(emp.getRazonSocial());
                    break;
                }
            }
        }

        if (idEmpresa == null || idEmpresa == 0) {
            response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                    URLEncoder.encode("Perfil de empresa no vinculado.", StandardCharsets.UTF_8));
            return;
        }

        String accion = request.getParameter("accion");

        if (!"actualizarPerfilEmpresa".equalsIgnoreCase(accion) && !"darDeBaja".equalsIgnoreCase(accion)) {
            Empresa empresaActual = BackendCliente.obtenerEmpresaPorId(idEmpresa);
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
                if (idStr != null)
                    id = Integer.parseInt(idStr);
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
                if (vacantesStr != null)
                    vacantes = Integer.parseInt(vacantesStr);
            } catch (NumberFormatException ignored) {
            }

            if (id <= 0 || titulo == null || titulo.trim().isEmpty() || carrera == null || carrera.trim().isEmpty()
                    || fechaLimite == null || fechaLimite.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                        URLEncoder.encode("Campos obligatorios incompletos para actualizar la oferta.",
                                StandardCharsets.UTF_8));
                return;
            }
            String error = BackendCliente.actualizarOferta(
                    id, idEmpresa, titulo.trim(), descripcion, requisitos,
                    carrera.trim(), modalidad, vacantes, fechaLimite.trim(),
                    ubicacion, remuneracion, null);

            if (error == null) {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?exito=actualizada");
            } else {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                        URLEncoder.encode(error, StandardCharsets.UTF_8));
            }
            return;
        } else if ("eliminar".equalsIgnoreCase(accion)) {
            String idStr = request.getParameter("id");
            int id = 0;
            try {
                if (idStr != null)
                    id = Integer.parseInt(idStr);
            } catch (NumberFormatException ignored) {
            }

            if (id > 0) {
                BackendCliente.eliminarRegistro("ofertas", id);
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
            Empresa curEmp = BackendCliente.obtenerEmpresaPorId(idEmpresa);
            if (curEmp == null) {
                List<Empresa> emps = BackendCliente.listarEmpresas();
                for (Empresa emp : emps) {
                    if (emp.getId() == idEmpresa) {
                        curEmp = emp;
                        break;
                    }
                }
            }
            if (rucParam == null || !rucParam.trim().matches("\\d{11}")) {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                        URLEncoder.encode("El RUC debe contener exactamente 11 dígitos numéricos.",
                                StandardCharsets.UTF_8));
                return;
            }

            String ruc = rucParam.trim();
            String estado = curEmp != null ? curEmp.getEstado() : null;

            String repNombreFinal = (repNombre != null && !repNombre.trim().isEmpty()) ? repNombre.trim()
                    : usuario.getNombre();
            String repApellidosFinal = (repApellidos != null && !repApellidos.trim().isEmpty()) ? repApellidos.trim()
                    : (usuario.getApellidos() != null ? usuario.getApellidos() : "");
            String correoFinal = (correo != null && !correo.trim().isEmpty()) ? correo.trim() : usuario.getCorreo();
            String razonFinal = (razonSocial != null && !razonSocial.trim().isEmpty()) ? razonSocial.trim()
                    : (curEmp != null ? curEmp.getRazonSocial() : usuario.getRazonSocial());

            String error = BackendCliente.actualizarEmpresa(idEmpresa,
                    razonFinal,
                    ruc,
                    telefono != null ? telefono.trim() : "",
                    direccion != null ? direccion.trim() : "",
                    descripcion != null ? descripcion.trim() : "",
                    estado,
                    repNombreFinal,
                    repApellidosFinal,
                    correoFinal,
                    contrasena != null ? contrasena.trim() : "");

            if (error == null) {
                usuario.setNombre(repNombreFinal);
                usuario.setApellidos(repApellidosFinal);
                usuario.setCorreo(correoFinal);
                usuario.setRazonSocial(razonFinal);
                session.setAttribute("usuarioLogueado", usuario);
                session.setAttribute("nombreUsuario", razonFinal);
                session.setAttribute("correoUsuario", correoFinal);
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?exito=perfil_actualizado");
            } else {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                        URLEncoder.encode(error, StandardCharsets.UTF_8));
            }
            return;
        } else if ("darDeBaja".equalsIgnoreCase(accion)) {
            String error = BackendCliente.actualizarUsuario(usuario.getId(), usuario.getNombre(),
                    usuario.getApellidos(), usuario.getCorreo(), "", "INACTIVO");
            if (error == null) {
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login.jsp?exito=cuenta_desactivada");
            } else {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                        URLEncoder.encode(error, StandardCharsets.UTF_8));
            }
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

            Empresa empresa = BackendCliente.obtenerEmpresaPorId(idEmpresa);

            if (empresa == null || empresa.getRazonSocial() == null) {
                response.sendError(403);
                return;
            }

            boolean pertenece = false;

            for (com.propath.modelo.Postulacion p : BackendCliente.listarPostulaciones()) {
                if (p.getId() != id) {
                    continue;
                }
                if (p.getEmpresa() == null
                        || !empresa.getRazonSocial().trim()
                                .equalsIgnoreCase(p.getEmpresa().trim())) {
                    response.sendError(403, "La postulación no pertenece a tu empresa.");
                    return;
                }

                pertenece = true;
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

                    String entrevistas = BackendCliente.consultarEntrevistas(idEmpresa);

                    if (entrevistas == null || !entrevistas.trim().startsWith("[")) {
                        response.sendError(502, "No se pudo verificar si existen entrevistas.");
                        return;
                    }

                    Matcher matcher = Pattern.compile("\\{[^{}]*\\}").matcher(entrevistas);

                    while (matcher.find()) {
                        String objeto = matcher.group();

                        Matcher postulacionEntrevista = Pattern.compile(
                                "\"idPostulacion\"\\s*:\\s*\"?" + id + "\"?(?=\\s*[,}])").matcher(objeto);

                        if (postulacionEntrevista.find()) {
                            response.sendError(409,
                                    "No puedes rechazar al postulante porque ya tiene una entrevista registrada.");
                            return;
                        }
                    }
                }
            }

            if (!pertenece) {
                response.sendError(403, "La postulación no pertenece a tu empresa.");
                return;
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

            boolean actualizado = BackendCliente.actualizarEstadoPostulacion(
                    id, estado, motivoRechazo);

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
                if (vacantesStr != null)
                    vacantes = Integer.parseInt(vacantesStr);
            } catch (NumberFormatException ignored) {
            }

            if (titulo == null || titulo.trim().isEmpty() || carrera == null || carrera.trim().isEmpty()
                    || fechaLimite == null || fechaLimite.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                        URLEncoder.encode("Título, carrera y fecha límite son obligatorios.", StandardCharsets.UTF_8));
                return;
            }

            String error = BackendCliente.publicarOferta(
                    idEmpresa, titulo.trim(), descripcion, requisitos, carrera.trim(),
                    modalidad, vacantes, fechaLimite.trim(), ubicacion, remuneracion);

            if (error == null) {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?exito=publicada");
                return;
            } else {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                        URLEncoder.encode(error, StandardCharsets.UTF_8));
                return;
            }
        }

        response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp");
    }
}