package com.propath.controlador;

import com.propath.servicio.BackendCliente;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "AdminAccionServlet", urlPatterns = {"/admin/accion", "/AdminAccionServlet"})
public class AdminAccionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"exito\":false,\"mensaje\":\"Sesión no válida o expirada.\"}");
            return;
        }

        com.propath.modelo.Usuario usuarioLogueado = (com.propath.modelo.Usuario) session.getAttribute("usuarioLogueado");
        if (!"ADMIN".equalsIgnoreCase(usuarioLogueado.getRol())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"exito\":false,\"mensaje\":\"Permisos insuficientes.\"}");
            return;
        }

        String accion = request.getParameter("accion");
        String idStr = request.getParameter("id");
        String estado = request.getParameter("estado");
        String recurso = request.getParameter("recurso");

        int id = 0;
        try {
            if (idStr != null) id = Integer.parseInt(idStr);
        } catch (NumberFormatException ignored) {}

        boolean ok = false;
        String mensaje = null;
        boolean esProhibido = false;

        if ("crearUsuario".equalsIgnoreCase(accion)) {
            String nombre = request.getParameter("nombre");
            String apellidos = request.getParameter("apellidos");
            String correo = request.getParameter("correo");
            String contrasena = request.getParameter("contrasena");
            String rol = request.getParameter("rol");

            if ("ADMIN".equalsIgnoreCase(rol)) {
                ok = false;
                mensaje = "No está permitido crear cuentas de administrador adicionales.";
            } else if ("ESTUDIANTE".equalsIgnoreCase(rol)) {
                String universidad = request.getParameter("universidad");
                String carrera = request.getParameter("carrera");
                String cicloStr = request.getParameter("ciclo");
                String telefono = request.getParameter("telefono");
                int ciclo = 1;
                try {
                    if (cicloStr != null) ciclo = Integer.parseInt(cicloStr);
                } catch (NumberFormatException ignored) {}

                if (universidad == null || universidad.trim().isEmpty() || carrera == null || carrera.trim().isEmpty()) {
                    ok = false;
                    mensaje = "Debe completar la universidad y carrera para el perfil de estudiante.";
                } else {
                    String error = BackendCliente.registrarEstudiante(nombre, apellidos != null ? apellidos : "", correo, contrasena, universidad, carrera, ciclo, telefono);
                    ok = (error == null);
                    mensaje = error != null ? error : "Estudiante registrado exitosamente.";
                }
            } else if ("EMPRESA".equalsIgnoreCase(rol)) {
                String razonSocial = request.getParameter("razonSocial");
                String ruc = request.getParameter("ruc");
                String telefono = request.getParameter("telefono");
                String direccion = request.getParameter("direccion");
                String descripcion = request.getParameter("descripcion");

                if (razonSocial == null || razonSocial.trim().isEmpty() || ruc == null || ruc.trim().length() != 11) {
                    ok = false;
                    mensaje = "Debe proporcionar una razón social válida y un RUC de 11 dígitos.";
                } else {
                    String error = BackendCliente.registrarEmpresa(nombre, razonSocial, ruc, correo, contrasena, telefono, direccion, descripcion);
                    ok = (error == null);
                    mensaje = error != null ? error : "Empresa registrada exitosamente.";
                }
            } else {
                ok = false;
                mensaje = "Debe seleccionar un rol válido (ESTUDIANTE o EMPRESA).";
            }
        } else if ("actualizarUsuario".equalsIgnoreCase(accion) && id > 0) {
            if (id != usuarioLogueado.getId()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"exito\":false,\"prohibido\":true,\"mensaje\":\"No está permitido modificar datos personales de otros usuarios. Cada cuenta debe ser gestionada por su propietario.\"}");
                return;
            }

            String nombre = request.getParameter("nombre");
            String apellidos = request.getParameter("apellidos");
            String correo = request.getParameter("correo");
            String contrasena = request.getParameter("contrasena");
            String est = request.getParameter("estado");

            if (id == 60 || "admin@propath.com".equalsIgnoreCase(correo)) {
                if (est != null && !est.equalsIgnoreCase("ACTIVO")) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"exito\":false,\"prohibido\":true,\"mensaje\":\"No está permitido desactivar o bloquear la cuenta del administrador principal del sistema.\"}");
                    return;
                }
            }

            String error = BackendCliente.actualizarUsuario(id, nombre, apellidos != null ? apellidos : "", correo, contrasena, est != null ? est : "ACTIVO");
            ok = (error == null);
            mensaje = error != null ? error : "Usuario actualizado exitosamente.";
        } else if ("crearEmpresa".equalsIgnoreCase(accion)) {
            String nombre = request.getParameter("nombre");
            String razonSocial = request.getParameter("razonSocial");
            String ruc = request.getParameter("ruc");
            String correo = request.getParameter("correo");
            String contrasena = request.getParameter("contrasena");
            String telefono = request.getParameter("telefono");
            String direccion = request.getParameter("direccion");
            String descripcion = request.getParameter("descripcion");
            String est = request.getParameter("estado");

            String error = BackendCliente.crearEmpresaAdmin(nombre, razonSocial, ruc, correo, contrasena, telefono, direccion, descripcion, est);
            ok = (error == null);
            mensaje = error != null ? error : "Empresa registrada exitosamente.";
        } else if ("actualizarEmpresa".equalsIgnoreCase(accion) && id > 0) {
            String razonSocial = request.getParameter("razonSocial");
            String ruc = request.getParameter("ruc");
            String telefono = request.getParameter("telefono");
            String direccion = request.getParameter("direccion");
            String descripcion = request.getParameter("descripcion");
            String est = request.getParameter("estado");

            String error = BackendCliente.actualizarEmpresa(id, razonSocial, ruc, telefono, direccion, descripcion, est);
            ok = (error == null);
            mensaje = error != null ? error : "Empresa actualizada exitosamente.";
        } else if ("aprobarEmpresa".equalsIgnoreCase(accion) && id > 0) {
            ok = BackendCliente.actualizarEstadoEmpresa(id, "APROBADA");
            mensaje = ok ? "Empresa aprobada exitosamente." : "No se pudo aprobar la empresa.";
        } else if ("cambiarEstadoEmpresa".equalsIgnoreCase(accion) && id > 0 && estado != null) {
            ok = BackendCliente.actualizarEstadoEmpresa(id, estado);
            mensaje = ok ? "Estado de empresa actualizado." : "No se pudo actualizar el estado de la empresa.";
        } else if ("cambiarEstadoEstudiante".equalsIgnoreCase(accion) && id > 0 && estado != null) {
            ok = BackendCliente.actualizarEstadoEstudiante(id, estado);
            mensaje = ok ? "Estado de estudiante actualizado." : "No se pudo actualizar el estado del estudiante.";
        } else if ("cambiarEstadoOferta".equalsIgnoreCase(accion) && id > 0 && estado != null) {
            ok = BackendCliente.actualizarEstadoOferta(id, estado);
            mensaje = ok ? "Estado de oferta actualizado." : "No se pudo actualizar el estado de la oferta.";
        } else if ("cambiarEstadoPostulacion".equalsIgnoreCase(accion) && id > 0 && estado != null) {
            ok = BackendCliente.actualizarEstadoPostulacion(id, estado);
            mensaje = ok ? "Estado de postulación actualizado." : "No se pudo actualizar el estado de la postulación.";
        } else if ("activarCuentaEmpresa".equalsIgnoreCase(accion) && id > 0) {
            String err = BackendCliente.activarCuentaEmpresa(id);
            ok = (err == null);
            mensaje = ok ? "Cuenta de empresa reactivada exitosamente." : err;
        } else if ("eliminar".equalsIgnoreCase(accion) && id > 0 && recurso != null) {
            if (("usuario".equalsIgnoreCase(recurso) || "usuarios".equalsIgnoreCase(recurso)) && id == 60) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"exito\":false,\"prohibido\":true,\"mensaje\":\"No está permitido eliminar la cuenta del administrador principal del sistema.\"}");
                return;
            }
            String err = BackendCliente.eliminarRegistroConDetalle(recurso, id);
            ok = (err == null);
            mensaje = ok ? "Registro eliminado exitosamente." : err;
        }

        String jsonResp = String.format("{\"exito\":%b,\"prohibido\":%b,\"mensaje\":\"%s\"}",
                ok, esProhibido, mensaje != null ? mensaje.replace("\"", "\\\"") : "");
        response.getWriter().write(jsonResp);
    }
}
