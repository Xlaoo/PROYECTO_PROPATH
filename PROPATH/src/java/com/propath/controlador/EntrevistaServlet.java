package com.propath.controlador;

import com.propath.dao.EmpresaDAO;
import com.propath.dao.EntrevistaDAO;
import com.propath.dao.OfertaPracticaDAO;
import com.propath.dao.PostulacionDAO;
import com.propath.modelo.Empresa;
import com.propath.modelo.Entrevista;
import com.propath.modelo.OfertaPractica;
import com.propath.modelo.Postulacion;
import com.propath.modelo.Usuario;
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

@WebServlet("/empresa/entrevista")
public class EntrevistaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final transient EntrevistaDAO entrevistaDAO = new EntrevistaDAO();
    private final transient PostulacionDAO postulacionDAO = new PostulacionDAO();
    private final transient OfertaPracticaDAO ofertaDAO = new OfertaPracticaDAO();
    private final transient EmpresaDAO empresaDAO = new EmpresaDAO();

    private void volver(HttpServletRequest req, HttpServletResponse res,
            String tipo, String mensaje) throws IOException {
        String tab = req.getParameter("redirectTab");
        if (tab == null || tab.trim().isEmpty()) {
            tab = "entrevistas";
        }
        res.sendRedirect(req.getContextPath()
                + "/empresa/dashboard.jsp?" + tipo + "="
                + URLEncoder.encode(mensaje, StandardCharsets.UTF_8)
                + "&tab=" + URLEncoder.encode(tab.trim(), StandardCharsets.UTF_8));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        HttpSession sesion = req.getSession(false);

        if (sesion == null || !(sesion.getAttribute("usuarioLogueado") instanceof Usuario)) {
            res.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        Usuario usuario = (Usuario) sesion.getAttribute("usuarioLogueado");

        if (!"EMPRESA".equalsIgnoreCase(usuario.getRol())) {
            res.sendError(403);
            return;
        }

        Integer idEmpresaObj = usuario.getIdEmpresa();
        if (idEmpresaObj == null || idEmpresaObj <= 0) {
            Empresa emp = empresaDAO.obtenerPorUsuarioId(usuario.getId());
            if (emp != null) {
                idEmpresaObj = emp.getId();
                usuario.setIdEmpresa(idEmpresaObj);
            }
        }
        if (idEmpresaObj == null || idEmpresaObj <= 0) {
            res.sendError(403, "Empresa no identificada.");
            return;
        }

        Empresa empActual = empresaDAO.obtenerPorId(idEmpresaObj);
        if (empActual == null || !"APROBADA".equalsIgnoreCase(empActual.getEstado())
                || !"ACTIVO".equalsIgnoreCase(empActual.getEstadoCuenta())) {
            volver(req, res, "error", "Tu empresa debe estar aprobada y activa para gestionar entrevistas.");
            return;
        }

        int idEmpresa = idEmpresaObj;

        try {
            String accion = req.getParameter("accion");

            if ("eliminar".equalsIgnoreCase(accion)) {
                int id = Integer.parseInt(req.getParameter("idEntrevista"));

                Entrevista entrevista = entrevistaDAO.obtenerPorId(id);
                if (entrevista == null) {
                    volver(req, res, "error", "La entrevista seleccionada no existe.");
                    return;
                }

                Postulacion p = postulacionDAO.obtenerPorId(entrevista.getIdPostulacion());
                if (p == null) {
                    res.sendError(403);
                    return;
                }

                OfertaPractica o = ofertaDAO.obtenerPorId(p.getIdOferta());
                if (o == null || o.getIdEmpresa() != idEmpresa) {
                    res.sendError(403, "No estás autorizado para eliminar esta entrevista.");
                    return;
                }

                boolean ok = entrevistaDAO.eliminar(id);
                volver(req, res, ok ? "exito" : "error",
                        ok ? "entrevista_eliminada" : "No se pudo eliminar la entrevista.");
                return;
            }

            if ("cambiarEstado".equalsIgnoreCase(accion)) {
                int id = Integer.parseInt(req.getParameter("idEntrevista"));
                String nuevoEstado = req.getParameter("estado");
                String obs = req.getParameter("observaciones");
                if (nuevoEstado == null || (!"PROGRAMADA".equalsIgnoreCase(nuevoEstado)
                        && !"REALIZADA".equalsIgnoreCase(nuevoEstado)
                        && !"CANCELADA".equalsIgnoreCase(nuevoEstado))) {
                    volver(req, res, "error", "Estado de entrevista no válido.");
                    return;
                }

                Entrevista entrevista = entrevistaDAO.obtenerPorId(id);
                if (entrevista == null) {
                    volver(req, res, "error", "Entrevista no encontrada.");
                    return;
                }

                Postulacion p = postulacionDAO.obtenerPorId(entrevista.getIdPostulacion());
                OfertaPractica o = p != null ? ofertaDAO.obtenerPorId(p.getIdOferta()) : null;
                if (o == null || o.getIdEmpresa() != idEmpresa) {
                    res.sendError(403, "No estás autorizado para modificar esta entrevista.");
                    return;
                }

                boolean ok = entrevistaDAO.actualizarEstado(id, nuevoEstado.toUpperCase(), obs);
                volver(req, res, ok ? "exito" : "error",
                        ok ? "entrevista_estado_actualizado" : "No se pudo actualizar el estado de la entrevista.");
                return;
            }

            int idPostulacion = Integer.parseInt(req.getParameter("idPostulacion"));

            String fecha = req.getParameter("fecha");
            String hora = req.getParameter("hora");
            String modalidad = req.getParameter("modalidad");
            String lugar = req.getParameter("enlaceLugar");
            String estado = req.getParameter("estado");
            String observaciones = req.getParameter("observaciones");

            if (fecha == null || fecha.isBlank() || hora == null || hora.isBlank()
                    || modalidad == null || lugar == null || lugar.isBlank()) {
                volver(req, res, "error", "Completa los campos obligatorios de la entrevista.");
                return;
            }

            LocalDate fechaEnt;
            try {
                fechaEnt = LocalDate.parse(fecha.trim());
            } catch (DateTimeParseException ex) {
                volver(req, res, "error", "Formato de fecha inválido.");
                return;
            }

            if ("crear".equalsIgnoreCase(accion) && fechaEnt.isBefore(LocalDate.now())) {
                volver(req, res, "error", "No puedes programar una nueva entrevista en una fecha pasada.");
                return;
            }

            if ("VIRTUAL".equalsIgnoreCase(modalidad)
                    && !lugar.trim().matches("^(https?://)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$")) {
                volver(req, res, "error", "Ingresa un enlace de reunión válido (ej. Google Meet, Zoom o Teams).");
                return;
            }

            Postulacion p = postulacionDAO.obtenerPorId(idPostulacion);
            if (p == null) {
                volver(req, res, "error", "Postulación no encontrada.");
                return;
            }

            OfertaPractica o = ofertaDAO.obtenerPorId(p.getIdOferta());
            if (o == null || o.getIdEmpresa() != idEmpresa) {
                res.sendError(403, "La postulación no pertenece a tu empresa.");
                return;
            }

            if ("NO_SELECCIONADO".equalsIgnoreCase(p.getEstado()) || "RETIRADO".equalsIgnoreCase(p.getEstado())) {
                volver(req, res, "error",
                        "No se puede programar entrevista a un postulante no seleccionado o retirado.");
                return;
            }

            // Si estaba en revisión o postulado, promover automáticamente a SELECCIONADO
            if (!"SELECCIONADO".equalsIgnoreCase(p.getEstado())) {
                postulacionDAO.actualizarEstado(idPostulacion, "SELECCIONADO");
            }

            if ("actualizar".equalsIgnoreCase(accion)) {
                int idEntrevista = Integer.parseInt(req.getParameter("idEntrevista"));

                Entrevista existente = entrevistaDAO.obtenerPorId(idEntrevista);
                if (existente == null || existente.getIdPostulacion() != idPostulacion) {
                    res.sendError(403, "Entrevista no autorizada.");
                    return;
                }

                existente.setFecha(fecha.trim());
                existente.setHora(hora.trim());
                existente.setModalidad(modalidad.trim());
                existente.setEnlaceLugar(lugar.trim());
                existente.setEstado(estado == null ? "PROGRAMADA" : estado.trim());
                existente.setObservaciones(observaciones == null ? "" : observaciones.trim());

                boolean ok = entrevistaDAO.actualizar(existente);
                volver(req, res, ok ? "exito" : "error",
                        ok ? "entrevista_guardada" : "Error al actualizar la entrevista.");
            } else if ("crear".equalsIgnoreCase(accion)) {
                if (entrevistaDAO.tieneEntrevistaActiva(idPostulacion)) {
                    volver(req, res, "error", "Este postulante ya tiene una entrevista activa programada.");
                    return;
                }
                Entrevista nueva = new Entrevista();
                nueva.setIdPostulacion(idPostulacion);
                nueva.setFecha(fecha.trim());
                nueva.setHora(hora.trim());
                nueva.setModalidad(modalidad.trim());
                nueva.setEnlaceLugar(lugar.trim());
                nueva.setEstado(estado == null ? "PROGRAMADA" : estado.trim());
                nueva.setObservaciones(observaciones == null ? "" : observaciones.trim());

                int idGen = entrevistaDAO.crear(nueva);
                volver(req, res, idGen > 0 ? "exito" : "error",
                        idGen > 0 ? "entrevista_guardada" : "Error al crear la entrevista.");
            } else {
                res.sendError(400);
            }

        } catch (NumberFormatException e) {
            volver(req, res, "error", "Identificador inválido.");
        } catch (Exception e) {
            volver(req, res, "error", "Error al gestionar entrevistas.");
        }
    }
}
