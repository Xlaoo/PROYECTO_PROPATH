
package com.propath.controlador;

import com.propath.modelo.Usuario;
import com.propath.servicio.BackendCliente;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/empresa/entrevista")
public class EntrevistaServlet extends HttpServlet {

    private String valor(String json, String clave) {
        Matcher m = Pattern.compile(
                "\"" + clave + "\"\\s*:\\s*(\\d+)")
                .matcher(json);
        return m.find() ? m.group(1) : "";
    }

    private boolean contieneRelacion(String json, String campo, int id) {
        return Pattern.compile(
                "\"" + campo + "\"\\s*:\\s*" + id + "(?!\\d)")
                .matcher(json).find();
    }

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

        if (sesion == null
                || !(sesion.getAttribute("usuarioLogueado") instanceof Usuario)) {
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
            String json = BackendCliente.consultarEntrevistas(idEmpresa);

            if ("eliminar".equals(accion)) {
                int id = Integer.parseInt(req.getParameter("idEntrevista"));

                String entrevista = buscarObjeto(json, id);

                if (entrevista == null
                        || !contieneRelacion(entrevista, "idEmpresa", idEmpresa)) {
                    res.sendError(403);
                    return;
                }

                String error = BackendCliente.eliminarEntrevista(id);
                volver(req, res, error == null ? "exito" : "error",
                        error == null ? "entrevista_eliminada" : error);
                return;
            }

            int idPostulacion = Integer.parseInt(
                    req.getParameter("idPostulacion"));

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

            Integer idEntrevista = null;

            if ("actualizar".equals(accion)) {
                idEntrevista = Integer.valueOf(
                        req.getParameter("idEntrevista"));

                String existente = buscarObjeto(json, idEntrevista);

                if (existente == null
                        || !contieneRelacion(existente, "idEmpresa", idEmpresa)
                        || !contieneRelacion(existente, "idPostulacion", idPostulacion)) {
                    res.sendError(403);
                    return;
                }
            } else if (!"crear".equals(accion)) {
                res.sendError(400);
                return;
            }

            String postulaciones = consultarPostulacionesEmpresa(idEmpresa);

            if (!contienePostulacion(postulaciones, idPostulacion, idEmpresa)) {
                res.sendError(403);
                return;
            }
            boolean seleccionado = false;

            for (com.propath.modelo.Postulacion p : BackendCliente.listarPostulaciones()) {
                if (p.getId() == idPostulacion
                        && "SELECCIONADO".equalsIgnoreCase(p.getEstado())) {
                    seleccionado = true;
                    break;
                }
            }

            if (!seleccionado) {
                volver(req, res, "error",
                        "Debes seleccionar al postulante antes de programar una entrevista.");
                return;
            }
            String error = BackendCliente.guardarEntrevista(
                    idEntrevista, idPostulacion, fecha, hora,
                    modalidad, lugar, estado == null ? "PROGRAMADA" : estado,
                    observaciones == null ? "" : observaciones);

            volver(req, res, error == null ? "exito" : "error",
                    error == null ? "entrevista_guardada" : error);

        } catch (NumberFormatException e) {
            volver(req, res, "error", "Identificador inválido.");
        } catch (Exception e) {
            volver(req, res, "error", "Error al gestionar entrevistas.");
        }
    }

    private String consultarPostulacionesEmpresa(int idEmpresa) {
        com.propath.modelo.Empresa empresa = BackendCliente.obtenerEmpresaPorId(idEmpresa);

        if (empresa == null || empresa.getRazonSocial() == null) {
            return "[]";
        }

        String razon = empresa.getRazonSocial();
        StringBuilder json = new StringBuilder("[");
        boolean primero = true;

        for (com.propath.modelo.Postulacion p : BackendCliente.listarPostulaciones()) {

            if (p.getEmpresa() == null
                    || !razon.equalsIgnoreCase(p.getEmpresa().trim())) {
                continue;
            }

            if (!primero) {
                json.append(",");
            }

            json.append("{\"idPostulacion\":")
                    .append(p.getId())
                    .append(",\"idEmpresa\":")
                    .append(idEmpresa)
                    .append("}");

            primero = false;
        }

        return json.append("]").toString();
    }

    private boolean contienePostulacion(
            String json, int idPostulacion, int idEmpresa) {
        if (json == null)
            return false;
        Matcher m = Pattern.compile(
                "\\{[^{}]*\"idPostulacion\"\\s*:\\s*"
                        + idPostulacion + "(?!\\d)[^{}]*\\}")
                .matcher(json);
        while (m.find()) {
            if (contieneRelacion(m.group(), "idEmpresa", idEmpresa)) {
                return true;
            }
        }
        return false;
    }

    private String buscarObjeto(String json, int id) {
        if (json == null)
            return null;
        Matcher m = Pattern.compile("\\{[^{}]*\\}").matcher(json);
        while (m.find()) {
            String objeto = m.group();
            if (contieneRelacion(objeto, "id", id))
                return objeto;
        }
        return null;
    }
}
