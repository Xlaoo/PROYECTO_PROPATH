package com.propath.controlador;

import com.propath.dao.EmpresaDAO;
import com.propath.dao.EntrevistaDAO;
import com.propath.dao.EstudianteDAO;
import com.propath.dao.OfertaPracticaDAO;
import com.propath.dao.PostulacionDAO;
import com.propath.dao.UsuarioDAO;
import com.propath.modelo.Empresa;
import com.propath.modelo.Estudiante;
import com.propath.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "AdminAccionServlet", urlPatterns = {"/admin/accion", "/AdminAccionServlet"})
public class AdminAccionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"exito\":false,\"mensaje\":\"Sesión no válida o expirada.\"}");
            return;
        }

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
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

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        EmpresaDAO empresaDAO = new EmpresaDAO();
        EstudianteDAO estudianteDAO = new EstudianteDAO();
        OfertaPracticaDAO ofertaDAO = new OfertaPracticaDAO();
        PostulacionDAO postulacionDAO = new PostulacionDAO();
        EntrevistaDAO entrevistaDAO = new EntrevistaDAO();

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
                } else if (usuarioDAO.obtenerPorCorreo(correo) != null) {
                    ok = false;
                    mensaje = "El correo electrónico ya se encuentra registrado.";
                } else {
                    Estudiante est = new Estudiante();
                    est.setNombre(nombre != null ? nombre.trim() : "");
                    est.setApellido(apellidos != null ? apellidos.trim() : "");
                    est.setUniversidad(universidad.trim());
                    est.setCarrera(carrera.trim());
                    est.setCiclo(ciclo);
                    est.setTelefono(telefono != null ? telefono.trim() : "");
                    int idGen = estudianteDAO.registrarConUsuario(est, correo.trim(), contrasena);
                    ok = (idGen > 0);
                    mensaje = ok ? "Estudiante registrado exitosamente." : "Error al registrar el estudiante.";
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
                } else if (usuarioDAO.obtenerPorCorreo(correo) != null) {
                    ok = false;
                    mensaje = "El correo electrónico ya se encuentra registrado.";
                } else if (empresaDAO.obtenerPorRuc(ruc) != null) {
                    ok = false;
                    mensaje = "El RUC ya se encuentra registrado.";
                } else {
                    Empresa emp = new Empresa();
                    emp.setRazonSocial(razonSocial.trim());
                    emp.setRuc(ruc.trim());
                    emp.setTelefono(telefono != null ? telefono.trim() : "");
                    emp.setDireccion(direccion != null ? direccion.trim() : "");
                    emp.setDescripcion(descripcion != null ? descripcion.trim() : "");
                    emp.setEstado("APROBADA");
                    int idGen = empresaDAO.registrarConUsuario(emp, correo.trim(), contrasena, nombre != null ? nombre.trim() : "", apellidos != null ? apellidos.trim() : "");
                    ok = (idGen > 0);
                    mensaje = ok ? "Empresa registrada exitosamente." : "Error al registrar la empresa.";
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

            Usuario u = usuarioDAO.obtenerPorId(id);
            if (u != null) {
                if (nombre != null && !nombre.trim().isEmpty()) u.setNombre(nombre.trim());
                if (apellidos != null) u.setApellidos(apellidos.trim());
                if (correo != null && !correo.trim().isEmpty()) u.setCorreo(correo.trim());
                if (contrasena != null && !contrasena.trim().isEmpty()) u.setContrasena(contrasena.trim());
                if (est != null && !est.trim().isEmpty()) u.setEstado(est.trim().toUpperCase());
                ok = usuarioDAO.actualizar(u);
                mensaje = ok ? "Usuario actualizado exitosamente." : "Error al actualizar el usuario.";
            } else {
                ok = false;
                mensaje = "Usuario no encontrado.";
            }
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

            if (usuarioDAO.obtenerPorCorreo(correo) != null) {
                ok = false;
                mensaje = "El correo electrónico ya se encuentra registrado.";
            } else if (empresaDAO.obtenerPorRuc(ruc) != null) {
                ok = false;
                mensaje = "El RUC ya se encuentra registrado.";
            } else {
                Empresa emp = new Empresa();
                emp.setRazonSocial(razonSocial != null ? razonSocial.trim() : "");
                emp.setRuc(ruc != null ? ruc.trim() : "");
                emp.setTelefono(telefono != null ? telefono.trim() : "");
                emp.setDireccion(direccion != null ? direccion.trim() : "");
                emp.setDescripcion(descripcion != null ? descripcion.trim() : "");
                emp.setEstado(est != null && !est.trim().isEmpty() ? est.trim().toUpperCase() : "APROBADA");
                int idGen = empresaDAO.registrarConUsuario(emp, correo.trim(), contrasena, nombre != null ? nombre.trim() : "", "");
                ok = (idGen > 0);
                mensaje = ok ? "Empresa registrada exitosamente." : "Error al registrar la empresa.";
            }
        } else if ("actualizarEmpresa".equalsIgnoreCase(accion) && id > 0) {
            String razonSocial = request.getParameter("razonSocial");
            String ruc = request.getParameter("ruc");
            String telefono = request.getParameter("telefono");
            String direccion = request.getParameter("direccion");
            String descripcion = request.getParameter("descripcion");
            String est = request.getParameter("estado");

            Empresa emp = empresaDAO.obtenerPorId(id);
            if (emp != null) {
                if (razonSocial != null && !razonSocial.trim().isEmpty()) emp.setRazonSocial(razonSocial.trim());
                if (ruc != null && !ruc.trim().isEmpty()) emp.setRuc(ruc.trim());
                if (telefono != null) emp.setTelefono(telefono.trim());
                if (direccion != null) emp.setDireccion(direccion.trim());
                if (descripcion != null) emp.setDescripcion(descripcion.trim());
                if (est != null && !est.trim().isEmpty()) emp.setEstado(est.trim().toUpperCase());
                ok = empresaDAO.actualizar(emp);
                if (est != null && !est.trim().isEmpty()) {
                    empresaDAO.actualizarEstado(id, est.trim().toUpperCase());
                }
                mensaje = ok ? "Empresa actualizada exitosamente." : "No se pudo actualizar la empresa.";
            } else {
                ok = false;
                mensaje = "Empresa no encontrada.";
            }
        } else if ("aprobarEmpresa".equalsIgnoreCase(accion) && id > 0) {
            ok = empresaDAO.actualizarEstado(id, "APROBADA");
            mensaje = ok ? "Empresa aprobada exitosamente." : "No se pudo aprobar la empresa.";
        } else if ("cambiarEstadoEmpresa".equalsIgnoreCase(accion) && id > 0 && estado != null) {
            ok = empresaDAO.actualizarEstado(id, estado);
            mensaje = ok ? "Estado de empresa actualizado." : "No se pudo actualizar el estado de la empresa.";
        } else if ("cambiarEstadoEstudiante".equalsIgnoreCase(accion) && id > 0 && estado != null) {
            ok = estudianteDAO.actualizarEstado(id, estado);
            mensaje = ok ? "Estado de estudiante actualizado." : "No se pudo actualizar el estado del estudiante.";
        } else if ("cambiarEstadoOferta".equalsIgnoreCase(accion) && id > 0 && estado != null) {
            ok = ofertaDAO.actualizarEstado(id, estado);
            mensaje = ok ? "Estado de oferta actualizado." : "No se pudo actualizar el estado de la oferta.";
        } else if ("cambiarEstadoPostulacion".equalsIgnoreCase(accion) && id > 0 && estado != null) {
            ok = postulacionDAO.actualizarEstado(id, estado);
            mensaje = ok ? "Estado de postulación actualizado." : "No se pudo actualizar el estado de la postulación.";
        } else if ("activarCuentaEmpresa".equalsIgnoreCase(accion) && id > 0) {
            Empresa emp = empresaDAO.obtenerPorId(id);
            if (emp != null) {
                empresaDAO.actualizarEstado(id, "APROBADA");
                ok = usuarioDAO.actualizarEstado(emp.getIdUsuario(), "ACTIVO");
                mensaje = ok ? "Cuenta de empresa reactivada exitosamente." : "No se pudo reactivar la cuenta.";
            } else {
                ok = false;
                mensaje = "Empresa no encontrada.";
            }
        } else if ("eliminar".equalsIgnoreCase(accion) && id > 0 && recurso != null) {
            if (("usuario".equalsIgnoreCase(recurso) || "usuarios".equalsIgnoreCase(recurso)) && id == 60) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"exito\":false,\"prohibido\":true,\"mensaje\":\"No está permitido eliminar la cuenta del administrador principal del sistema.\"}");
                return;
            }
            if ("usuario".equalsIgnoreCase(recurso) || "usuarios".equalsIgnoreCase(recurso)) {
                ok = usuarioDAO.eliminar(id);
            } else if ("empresa".equalsIgnoreCase(recurso) || "empresas".equalsIgnoreCase(recurso)) {
                ok = empresaDAO.eliminar(id);
            } else if ("estudiante".equalsIgnoreCase(recurso) || "estudiantes".equalsIgnoreCase(recurso)) {
                ok = estudianteDAO.eliminar(id);
            } else if ("oferta".equalsIgnoreCase(recurso) || "ofertas".equalsIgnoreCase(recurso)) {
                ok = ofertaDAO.eliminar(id);
            } else if ("postulacion".equalsIgnoreCase(recurso) || "postulaciones".equalsIgnoreCase(recurso)) {
                ok = postulacionDAO.eliminar(id);
            } else if ("entrevista".equalsIgnoreCase(recurso) || "entrevistas".equalsIgnoreCase(recurso)) {
                ok = entrevistaDAO.eliminar(id);
            }
            mensaje = ok ? "Registro eliminado exitosamente." : "No se pudo eliminar el registro seleccionado.";
        }

        String jsonResp = String.format("{\"exito\":%b,\"prohibido\":%b,\"mensaje\":\"%s\"}",
                ok, esProhibido, mensaje != null ? mensaje.replace("\"", "\\\"") : "");
        response.getWriter().write(jsonResp);
    }
}
