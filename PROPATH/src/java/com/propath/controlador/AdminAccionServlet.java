package com.propath.controlador;

import com.propath.dao.EmpresaDAO;
import com.propath.dao.EntrevistaDAO;
import com.propath.dao.EstudianteDAO;
import com.propath.dao.OfertaPracticaDAO;
import com.propath.dao.PostulacionDAO;
import com.propath.dao.UsuarioDAO;
import com.propath.modelo.Empresa;
import com.propath.modelo.Estudiante;
import com.propath.modelo.OfertaPractica;
import com.propath.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "AdminAccionServlet", urlPatterns = { "/admin/accion", "/AdminAccionServlet" })
public class AdminAccionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"exito\":false,\"mensaje\":\"No autorizado.\"}");
            return;
        }

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (!"ADMIN".equalsIgnoreCase(usuarioLogueado.getRol())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"exito\":false,\"mensaje\":\"Permisos insuficientes.\"}");
            return;
        }

        String accion = request.getParameter("accion");
        if ("obtenerPostulacion".equalsIgnoreCase(accion)) {
            int idPostulacion = 0;
            try {
                idPostulacion = Integer.parseInt(request.getParameter("id"));
            } catch (Exception ignored) {
            }

            PostulacionDAO postulacionDAO = new PostulacionDAO();
            com.propath.modelo.Postulacion p = postulacionDAO.obtenerPorId(idPostulacion);
            if (p == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"exito\":false,\"mensaje\":\"Postulación no encontrada.\"}");
                return;
            }

            java.util.List<com.propath.modelo.RespuestaPostulacion> respuestas = postulacionDAO
                    .obtenerRespuestas(idPostulacion);
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"exito\":true,");
            sb.append("\"id\":").append(p.getId()).append(",");
            sb.append("\"estudiante\":\"")
                    .append(p.getEstudiante() != null ? p.getEstudiante().replace("\"", "\\\"") : "").append("\",");
            sb.append("\"oferta\":\"").append(p.getOferta() != null ? p.getOferta().replace("\"", "\\\"") : "")
                    .append("\",");
            sb.append("\"empresa\":\"").append(p.getEmpresa() != null ? p.getEmpresa().replace("\"", "\\\"") : "")
                    .append("\",");
            sb.append("\"fecha\":\"").append(p.getFechaPostulacion() != null ? p.getFechaPostulacion() : "")
                    .append("\",");
            sb.append("\"estado\":\"").append(p.getEstado() != null ? p.getEstado() : "").append("\",");
            sb.append("\"respuestas\":[");
            for (int i = 0; i < respuestas.size(); i++) {
                if (i > 0)
                    sb.append(",");
                com.propath.modelo.RespuestaPostulacion r = respuestas.get(i);
                String preg = r.getPregunta() != null ? r.getPregunta().replace("\"", "\\\"") : "";
                String resp = r.getRespuesta() != null ? r.getRespuesta().replace("\"", "\\\"") : "";
                sb.append("{\"numeroPregunta\":").append(r.getNumeroPregunta())
                        .append(",\"pregunta\":\"").append(preg).append("\"")
                        .append(",\"respuesta\":\"").append(resp).append("\"}");
            }
            sb.append("]}");
            response.getWriter().write(sb.toString());
            return;
        }

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"exito\":false,\"mensaje\":\"Acción no reconocida.\"}");
    }

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
            if (idStr != null)
                id = Integer.parseInt(idStr);
        } catch (NumberFormatException ignored) {
        }

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
                    if (cicloStr != null)
                        ciclo = Integer.parseInt(cicloStr);
                } catch (NumberFormatException ignored) {
                }

                if (universidad == null || universidad.trim().isEmpty() || carrera == null
                        || carrera.trim().isEmpty()) {
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
                    int idGen = empresaDAO.registrarConUsuario(emp, correo.trim(), contrasena,
                            nombre != null ? nombre.trim() : "", apellidos != null ? apellidos.trim() : "");
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
                response.getWriter().write(
                        "{\"exito\":false,\"prohibido\":true,\"mensaje\":\"No está permitido modificar datos personales de otros usuarios. Cada cuenta debe ser gestionada por su propietario.\"}");
                return;
            }

            String nombre = request.getParameter("nombre");
            String apellidos = request.getParameter("apellidos");
            String correo = request.getParameter("correo");
            String contrasena = request.getParameter("contrasena");
            String confirmarContrasena = request.getParameter("confirmarContrasena");
            String est = request.getParameter("estado");

            if (id == 60 || "admin@propath.com".equalsIgnoreCase(correo)) {
                if (est != null && !est.equalsIgnoreCase("ACTIVO")) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write(
                            "{\"exito\":false,\"prohibido\":true,\"mensaje\":\"No está permitido desactivar o bloquear la cuenta del administrador principal del sistema.\"}");
                    return;
                }
            }

            Usuario u = usuarioDAO.obtenerPorId(id);
            if (u != null) {
                if (nombre != null && !nombre.trim().isEmpty())
                    u.setNombre(nombre.trim());
                if (apellidos != null)
                    u.setApellidos(apellidos.trim());

                // Validación obligatoria en el servidor para cambio de correo
                if (correo != null && !correo.trim().isEmpty() && !correo.trim().equalsIgnoreCase(u.getCorreo())) {
                    HttpSession ses = request.getSession(false);
                    String verificado = (ses != null) ? (String) ses.getAttribute("CORREO_VERIFICADO_PERFIL") : null;
                    if (verificado != null && verificado.equalsIgnoreCase(correo.trim())) {
                        u.setCorreo(correo.trim().toLowerCase());
                        ses.removeAttribute("CORREO_VERIFICADO_PERFIL");
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write(
                                "{\"exito\":false,\"mensaje\":\"Para modificar tu correo electrónico debes verificarlo con el código de seguridad enviado al nuevo correo.\"}");
                        return;
                    }
                }

                boolean contrasenaCambiada = false;
                if (contrasena != null && !contrasena.trim().isEmpty()) {
                    if (contrasena.trim().length() < 8 || !contrasena.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write(
                                "{\"exito\":false,\"mensaje\":\"La nueva contraseña debe tener al menos 8 caracteres y combinar letras y números.\"}");
                        return;
                    }
                    if (!contrasena.trim().equals(confirmarContrasena != null ? confirmarContrasena.trim() : "")) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write(
                                "{\"exito\":false,\"mensaje\":\"La nueva contraseña y su confirmación no coinciden.\"}");
                        return;
                    }
                    usuarioDAO.actualizarContrasena(id, contrasena.trim());
                    u.setContrasena(com.propath.conexion.SeguridadUtil.encriptarPassword(contrasena.trim()));
                    contrasenaCambiada = true;
                }
                if (est != null && !est.trim().isEmpty())
                    u.setEstado(est.trim().toUpperCase());
                ok = usuarioDAO.actualizar(u);
                if (contrasenaCambiada) {
                    com.propath.servicio.DesafioSeguridadServicio.invalidarTodoPorCorreo(u.getCorreo());
                    if (com.propath.servicio.ServicioCorreo.isConfigurado()) {
                        try {
                            com.propath.servicio.ServicioCorreo.enviarConfirmacionCambioPassword(u.getCorreo(), u.getNombre());
                        } catch (Exception ignored) {
                        }
                    }
                }
                if (ok && id == usuarioLogueado.getId()) {
                    HttpSession ses = request.getSession(false);
                    if (ses != null) {
                        ses.setAttribute("usuarioLogueado", u);
                        ses.setAttribute("correoUsuario", u.getCorreo());
                        ses.setAttribute("nombreUsuario", u.getNombre());
                    }
                }
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
                int idGen = empresaDAO.registrarConUsuario(emp, correo.trim(), contrasena,
                        nombre != null ? nombre.trim() : "", "");
                ok = (idGen > 0);
                mensaje = ok ? "Empresa registrada exitosamente." : "Error al registrar la empresa.";
            }
        } else if ("actualizarEstudiante".equalsIgnoreCase(accion) && id > 0) {
            String nombre = request.getParameter("nombre");
            String apellido = request.getParameter("apellido");
            if (apellido == null)
                apellido = request.getParameter("apellidos");
            String universidad = request.getParameter("universidad");
            String carrera = request.getParameter("carrera");
            String cicloStr = request.getParameter("ciclo");
            String telefono = request.getParameter("telefono");
            String est = request.getParameter("estado");

            int ciclo = 1;
            try {
                if (cicloStr != null)
                    ciclo = Integer.parseInt(cicloStr.trim());
            } catch (NumberFormatException ignored) {
            }

            Estudiante estudiante = estudianteDAO.obtenerPorId(id);
            if (estudiante != null) {
                if (nombre != null && !nombre.trim().isEmpty())
                    estudiante.setNombre(nombre.trim());
                if (apellido != null)
                    estudiante.setApellido(apellido.trim());
                if (universidad != null && !universidad.trim().isEmpty())
                    estudiante.setUniversidad(universidad.trim());
                if (carrera != null && !carrera.trim().isEmpty())
                    estudiante.setCarrera(carrera.trim());
                estudiante.setCiclo(ciclo);
                if (telefono != null)
                    estudiante.setTelefono(telefono.trim());

                ok = estudianteDAO.actualizarConEstado(estudiante, est != null ? est.trim().toUpperCase() : "ACTIVO");
                mensaje = ok ? "Estudiante actualizado exitosamente." : "Error al actualizar los datos del estudiante.";
            } else {
                ok = false;
                mensaje = "Estudiante no encontrado.";
            }
        } else if ("actualizarEmpresa".equalsIgnoreCase(accion) && id > 0) {
            String razonSocial = request.getParameter("razonSocial");
            String ruc = request.getParameter("ruc");
            String telefono = request.getParameter("telefono");
            String direccion = request.getParameter("direccion");
            String descripcion = request.getParameter("descripcion");
            String estadoEmpresa = request.getParameter("estadoEmpresa");
            if (estadoEmpresa == null)
                estadoEmpresa = request.getParameter("estado");
            String estadoCuenta = request.getParameter("estadoCuenta");

            if (ruc != null && !ruc.trim().matches("\\d{11}")) {
                ok = false;
                mensaje = "El RUC debe contener exactamente 11 dígitos numéricos.";
            } else {
                Empresa emp = empresaDAO.obtenerPorId(id);
                if (emp != null) {
                    if (razonSocial != null && !razonSocial.trim().isEmpty())
                        emp.setRazonSocial(razonSocial.trim());
                    if (ruc != null && !ruc.trim().isEmpty())
                        emp.setRuc(ruc.trim());
                    if (telefono != null)
                        emp.setTelefono(telefono.trim());
                    if (direccion != null)
                        emp.setDireccion(direccion.trim());
                    if (descripcion != null)
                        emp.setDescripcion(descripcion.trim());

                    String estEmpFinal = (estadoEmpresa != null && !estadoEmpresa.trim().isEmpty())
                            ? estadoEmpresa.trim().toUpperCase()
                            : emp.getEstado();
                    String estCtaFinal = (estadoCuenta != null && !estadoCuenta.trim().isEmpty())
                            ? estadoCuenta.trim().toUpperCase()
                            : emp.getEstadoCuenta();

                    ok = empresaDAO.actualizarConEstadoCuenta(emp, estEmpFinal, estCtaFinal);
                    mensaje = ok ? "Empresa actualizada exitosamente." : "No se pudo actualizar la empresa.";
                } else {
                    ok = false;
                    mensaje = "Empresa no encontrada.";
                }
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
                response.getWriter().write(
                        "{\"exito\":false,\"prohibido\":true,\"mensaje\":\"No está permitido eliminar la cuenta del administrador principal del sistema.\"}");
                return;
            }
            if ("usuario".equalsIgnoreCase(recurso) || "usuarios".equalsIgnoreCase(recurso)) {
                ok = usuarioDAO.eliminar(id);
                mensaje = ok ? "Usuario eliminado exitosamente." : "No se pudo eliminar el usuario.";
            } else if ("empresa".equalsIgnoreCase(recurso) || "empresas".equalsIgnoreCase(recurso)) {
                Empresa empExistente = empresaDAO.obtenerPorId(id);
                if (empExistente != null && "INACTIVO".equalsIgnoreCase(empExistente.getEstadoCuenta())) {
                    ok = true;
                    mensaje = "El registro ya se encuentra inactivo.";
                } else {
                    int cantOfertas = empresaDAO.contarOfertas(id);
                    if (cantOfertas > 0) {
                        ok = empresaDAO.darDeBaja(id);
                        mensaje = "La empresa tiene ofertas o registros asociados. Se procedió a desactivar su cuenta (estado INACTIVO) para preservar el historial.";
                    } else {
                        ok = empresaDAO.eliminar(id);
                        mensaje = ok ? "Empresa eliminada exitosamente." : "No se pudo eliminar la empresa.";
                    }
                }
            } else if ("estudiante".equalsIgnoreCase(recurso) || "estudiantes".equalsIgnoreCase(recurso)) {
                Estudiante estExistente = estudianteDAO.obtenerPorId(id);
                if (estExistente != null && "INACTIVO".equalsIgnoreCase(estExistente.getEstado())) {
                    ok = true;
                    mensaje = "El registro ya se encuentra inactivo.";
                } else {
                    int cantPost = estudianteDAO.contarPostulaciones(id);
                    if (cantPost > 0) {
                        ok = estudianteDAO.actualizarEstado(id, "INACTIVO");
                        mensaje = "El estudiante tiene postulaciones o registros asociados. Se desactivó su cuenta (estado INACTIVO) para preservar el historial.";
                    } else {
                        ok = estudianteDAO.eliminar(id);
                        mensaje = ok ? "Estudiante eliminado exitosamente." : "No se pudo eliminar el estudiante.";
                    }
                }
            } else if ("oferta".equalsIgnoreCase(recurso) || "ofertas".equalsIgnoreCase(recurso)) {
                OfertaPractica ofExistente = ofertaDAO.obtenerPorId(id);
                int cantPost = ofertaDAO.contarPostulaciones(id);
                if (ofExistente != null && "CERRADA".equalsIgnoreCase(ofExistente.getEstado()) && cantPost > 0) {
                    ok = true;
                    mensaje = "El registro ya se encuentra inactivo.";
                } else if (cantPost > 0) {
                    ok = ofertaDAO.actualizarEstado(id, "CERRADA");
                    mensaje = "La oferta tiene postulaciones registradas. Se cambió su estado a CERRADA para preservar el historial de postulantes.";
                } else {
                    ok = ofertaDAO.eliminar(id);
                    mensaje = ok ? "Oferta eliminada exitosamente." : "No se pudo eliminar la oferta.";
                }
            } else if ("postulacion".equalsIgnoreCase(recurso) || "postulaciones".equalsIgnoreCase(recurso)) {
                ok = postulacionDAO.eliminar(id);
                mensaje = ok ? "Postulación eliminada exitosamente." : "No se pudo eliminar la postulación.";
            } else if ("entrevista".equalsIgnoreCase(recurso) || "entrevistas".equalsIgnoreCase(recurso)) {
                ok = entrevistaDAO.eliminar(id);
                mensaje = ok ? "Entrevista eliminada exitosamente." : "No se pudo eliminar la entrevista.";
            }
        }

        String jsonResp = String.format("{\"exito\":%b,\"prohibido\":%b,\"mensaje\":\"%s\"}",
                ok, esProhibido, mensaje != null ? mensaje.replace("\"", "\\\"") : "");
        response.getWriter().write(jsonResp);
    }
}
