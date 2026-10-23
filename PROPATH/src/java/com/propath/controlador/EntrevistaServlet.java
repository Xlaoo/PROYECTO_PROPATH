package com.propath.controlador;

import com.propath.dao.EntrevistaDAO;
import com.propath.dao.OfertaPracticaDAO;
import com.propath.dao.PostulacionDAO;
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

@WebServlet("/empresa/entrevista")
public class EntrevistaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final EntrevistaDAO entrevistaDAO = new EntrevistaDAO();
    private final PostulacionDAO postulacionDAO = new PostulacionDAO();
    private final OfertaPracticaDAO ofertaDAO = new OfertaPracticaDAO();

    private void volver(HttpServletRequest req, HttpServletResponse res,
            String tipo, String mensaje) throws IOException {
        res.sendRedirect(req.getContextPath()
                + "/empresa/dashboard.jsp?" + tipo + "="
                + URLEncoder.encode(mensaje, StandardCharsets.UTF_8));
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

        if (!"EMPRESA".equalsIgnoreCase(usuario.getRol())
                || usuario.getIdEmpresa() == null
                || usuario.getIdEmpresa() <= 0) {
            res.sendError(403);
            return;
        }

        int idEmpresa = usuario.getIdEmpresa();

        try {
            String accion = req.getParameter("accion");

            if ("eliminar".equals(accion)) {
                int id = Integer.parseInt(req.getParameter("idEntrevista"));

                Entrevista entrevista = entrevistaDAO.obtenerPorId(id);
                if (entrevista == null) {
                    res.sendError(404);
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

            int idPostulacion = Integer.parseInt(req.getParameter("idPostulacion"));

            String fecha = req.getParameter("fecha");
            String hora = req.getParameter("hora");
            String modalidad = req.getParameter("modalidad");
            String lugar = req.getParameter("enlaceLugar");
            String estado = req.getParameter("estado");
            String observaciones = req.getParameter("observaciones");

            if (fecha == null || hora == null || modalidad == null
                    || lugar == null || lugar.isBlank()) {
                volver(req, res, "error", "Completa los campos obligatorios.");
                return;
            }

            if ("VIRTUAL".equalsIgnoreCase(modalidad)
                    && !lugar.matches("https://[^\\s]+")) {
                volver(req, res, "error", "Ingresa un enlace HTTPS válido.");
                return;
            }

            Postulacion p = postulacionDAO.obtenerPorId(idPostulacion);
            if (p == null) {
                res.sendError(404, "Postulación no encontrada.");
                return;
            }

            OfertaPractica o = ofertaDAO.obtenerPorId(p.getIdOferta());
            if (o == null || o.getIdEmpresa() != idEmpresa) {
                res.sendError(403, "La postulación no pertenece a tu empresa.");
                return;
            }

            if (!"SELECCIONADO".equalsIgnoreCase(p.getEstado())) {
                volver(req, res, "error",
                        "Debes seleccionar al postulante antes de programar una entrevista.");
                return;
            }

            if ("actualizar".equals(accion)) {
                int idEntrevista = Integer.parseInt(req.getParameter("idEntrevista"));

                Entrevista existente = entrevistaDAO.obtenerPorId(idEntrevista);
                if (existente == null || existente.getIdPostulacion() != idPostulacion) {
                    res.sendError(403, "Entrevista no autorizada.");
                    return;
                }

                existente.setFecha(fecha);
                existente.setHora(hora);
                existente.setModalidad(modalidad);
                existente.setEnlaceLugar(lugar);
                existente.setEstado(estado == null ? "PROGRAMADA" : estado);
                existente.setObservaciones(observaciones == null ? "" : observaciones);

                boolean ok = entrevistaDAO.actualizar(existente);
                volver(req, res, ok ? "exito" : "error",
                        ok ? "entrevista_guardada" : "Error al actualizar la entrevista.");
            } else if ("crear".equals(accion)) {
                Entrevista nueva = new Entrevista();
                nueva.setIdPostulacion(idPostulacion);
                nueva.setFecha(fecha);
                nueva.setHora(hora);
                nueva.setModalidad(modalidad);
                nueva.setEnlaceLugar(lugar);
                nueva.setEstado(estado == null ? "PROGRAMADA" : estado);
                nueva.setObservaciones(observaciones == null ? "" : observaciones);

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
