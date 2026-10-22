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

@WebServlet(name = "OfertaEmpresaServlet", urlPatterns = {"/empresa/oferta", "/OfertaEmpresaServlet"})
public class OfertaEmpresaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

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
                if (idStr != null) id = Integer.parseInt(idStr);
            } catch (NumberFormatException ignored) {}

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
                if (vacantesStr != null) vacantes = Integer.parseInt(vacantesStr);
            } catch (NumberFormatException ignored) {}

            if (id <= 0 || titulo == null || titulo.trim().isEmpty() || carrera == null || carrera.trim().isEmpty() || fechaLimite == null || fechaLimite.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                        URLEncoder.encode("Campos obligatorios incompletos para actualizar la oferta.", StandardCharsets.UTF_8));
                return;
            }
String error = BackendCliente.actualizarOferta(
        id, idEmpresa, titulo.trim(), descripcion, requisitos,
        carrera.trim(), modalidad, vacantes, fechaLimite.trim(),
        ubicacion, remuneracion, null
);

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
                if (idStr != null) id = Integer.parseInt(idStr);
            } catch (NumberFormatException ignored) {}

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
            URLEncoder.encode("El RUC debe contener exactamente 11 dígitos numéricos.", StandardCharsets.UTF_8));
    return;
}

String ruc = rucParam.trim();
            String estado = curEmp != null ? curEmp.getEstado() : null;

            String repNombreFinal = (repNombre != null && !repNombre.trim().isEmpty()) ? repNombre.trim() : usuario.getNombre();
            String repApellidosFinal = (repApellidos != null && !repApellidos.trim().isEmpty()) ? repApellidos.trim() : (usuario.getApellidos() != null ? usuario.getApellidos() : "");
            String correoFinal = (correo != null && !correo.trim().isEmpty()) ? correo.trim() : usuario.getCorreo();
            String razonFinal = (razonSocial != null && !razonSocial.trim().isEmpty()) ? razonSocial.trim() : (curEmp != null ? curEmp.getRazonSocial() : usuario.getRazonSocial());

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
            String error = BackendCliente.actualizarUsuario(usuario.getId(), usuario.getNombre(), usuario.getApellidos(), usuario.getCorreo(), "", "INACTIVO");
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
                if (idStr != null) id = Integer.parseInt(idStr);
            } catch (NumberFormatException ignored) {}

            if (id > 0 && estado != null) {
                BackendCliente.actualizarEstadoPostulacion(id, estado);
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?exito=estado_actualizado");
                return;
            }
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
                if (vacantesStr != null) vacantes = Integer.parseInt(vacantesStr);
            } catch (NumberFormatException ignored) {}

            if (titulo == null || titulo.trim().isEmpty() || carrera == null || carrera.trim().isEmpty() || fechaLimite == null || fechaLimite.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/empresa/dashboard.jsp?error=" +
                        URLEncoder.encode("Título, carrera y fecha límite son obligatorios.", StandardCharsets.UTF_8));
                return;
            }

            String error = BackendCliente.publicarOferta(
        idEmpresa, titulo.trim(), descripcion, requisitos, carrera.trim(),
        modalidad, vacantes, fechaLimite.trim(), ubicacion, remuneracion
);

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
