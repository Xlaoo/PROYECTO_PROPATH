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
                response.getWriter().write("{\"exito\":false,\"mensaje\":\"PostulaciÃ³n no encontrada.\"}");
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
        response.getWriter().write("{\"exito\":false,\"mensaje\":\"AcciÃ³n no reconocida.\"}");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"exito\":false,\"mensaje\":\"SesiÃ³n no vÃ¡lida o expirada.\"}");
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

            if (correo == null || correo.trim().isEmpty() || contrasena == null || contrasena.trim().length() < 6) {
                ok = false;
                mensaje = "Debe ingresar un correo válido y una contraseña de al menos 6 caracteres.";
            } else if ("ADMIN".equalsIgnoreCase(rol)) {
                ok = false;
                mensaje = "No está permitido crear cuentas de administrador adicionales.";
            } else if ("ESTUDIANTE".equalsIgnoreCase(rol)) {
                String dni = request.getParameter("dni");
                String universidad = request.getParameter("universidad");
                String carrera = request.getParameter("carrera");
                String cicloStr = request.getParameter("ciclo");
                String telefono = request.getParameter("telefono");
                int ciclo = 1;
                try {
                    if (cicloStr != null)
                        ciclo = Math.max(1, Math.min(14, Integer.parseInt(cicloStr.trim())));
                } catch (NumberFormatException ignored) {
                }

                String dniTrim = dni != null ? dni.trim() : "";
                if (nombre == null || nombre.trim().isEmpty() || apellidos == null || apellidos.trim().isEmpty()) {
                    ok = false;
                    mensaje = "Debe completar nombres y apellidos del estudiante.";
                } else if (!dniTrim.isEmpty() && !dniTrim.matches("^[0-9]{8}$")) {
                    ok = false;
                    mensaje = "El DNI debe contener exactamente 8 dígitos numéricos.";
                } else if (!dniTrim.isEmpty() && estudianteDAO.obtenerPorDni(dniTrim) != null) {
                    ok = false;
                    mensaje = "El DNI ya se encuentra registrado por otro estudiante.";
                } else if (universidad == null || universidad.trim().isEmpty() || carrera == null
                        || carrera.trim().isEmpty()) {
                    ok = false;
                    mensaje = "Debe completar la universidad y carrera para el perfil de estudiante.";
                } else if (usuarioDAO.obtenerPorCorreo(correo) != null) {
                    ok = false;
                    mensaje = "El correo electrónico ya se encuentra registrado.";
                } else {
                    Estudiante est = new Estudiante();
                    est.setDni(dniTrim);
                    est.setNombre(nombre.trim());
                    est.setApellido(apellidos.trim());
                    est.setUniversidad(universidad.trim());
                    est.setCarrera(carrera.trim());
                    est.setCiclo(ciclo);
                    est.setTelefono(telefono != null ? telefono.trim() : "");
                    int idGen = estudianteDAO.registrarConUsuario(est, correo.trim().toLowerCase(), contrasena);
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
            ok = false;
            mensaje = "El alta administrativa de empresas no estÃ¡ permitida. Las empresas deben registrarse a travÃ©s del formulario pÃºblico.";
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

            Integer cicloValidado = validarCicloEstudiante(cicloStr);

            String dni = request.getParameter("dni");
            if (cicloValidado == null) {
                ok = false;
                mensaje = "Debe seleccionar un ciclo académico válido (número entero del 1 al 12).";
            } else {
                Estudiante estudiante = estudianteDAO.obtenerPorId(id);
                if (estudiante != null) {
                    String dniTrim = dni != null ? dni.trim() : "";
                    if (!dniTrim.isEmpty() && !dniTrim.matches("^[0-9]{8}$")) {
                        ok = false;
                        mensaje = "El DNI debe contener exactamente 8 dígitos numéricos.";
                    } else {
                        Estudiante porDni = !dniTrim.isEmpty() ? estudianteDAO.obtenerPorDni(dniTrim) : null;
                        if (porDni != null && porDni.getId() != estudiante.getId()) {
                            ok = false;
                            mensaje = "El DNI ya pertenece a otro estudiante.";
                        } else {
                            if (!dniTrim.isEmpty())
                                estudiante.setDni(dniTrim);
                            if (nombre != null && !nombre.trim().isEmpty())
                                estudiante.setNombre(nombre.trim());
                            if (apellido != null)
                                estudiante.setApellido(apellido.trim());
                            if (universidad != null && !universidad.trim().isEmpty())
                                estudiante.setUniversidad(universidad.trim());
                            if (carrera != null && !carrera.trim().isEmpty())
                                estudiante.setCarrera(carrera.trim());
                            estudiante.setCiclo(cicloValidado);
                            if (telefono != null)
                                estudiante.setTelefono(telefono.trim());

                            ok = estudianteDAO.actualizarConEstado(estudiante,
                                    est != null ? est.trim().toUpperCase() : "ACTIVO");
                            mensaje = ok ? "Estudiante actualizado exitosamente."
                                    : "Error al actualizar los datos del estudiante.";
                        }
                    }
                } else {
                    ok = false;
                    mensaje = "Estudiante no encontrado.";
                }
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
                mensaje = "El RUC debe contener exactamente 11 dÃ­gitos numÃ©ricos.";
            } else {
                Empresa emp = empresaDAO.obtenerPorId(id);
                if (emp != null) {
                    if (ruc != null && !ruc.trim().isEmpty() && !ruc.trim().equalsIgnoreCase(emp.getRuc())) {
                        Empresa empRucExistente = empresaDAO.obtenerPorRuc(ruc.trim());
                        if (empRucExistente != null && empRucExistente.getId() != id) {
                            ok = false;
                            mensaje = "El RUC ingresado ya pertenece a otra empresa registrada.";
                        }
                    }

                    if (mensaje == null) {
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
                    }
                } else {
                    ok = false;
                    mensaje = "Empresa no encontrada.";
                }
            }
        } else if ("crearOferta".equalsIgnoreCase(accion)) {
            int idEmpresa = 0;
            try {
                idEmpresa = Integer.parseInt(request.getParameter("idEmpresa"));
            } catch (Exception ignored) {
            }
            String titulo = request.getParameter("titulo");
            String carrera = request.getParameter("carrera");
            String modalidad = request.getParameter("modalidad");
            String fechaLimite = request.getParameter("fechaLimite");
            String ubicacion = request.getParameter("ubicacion");
            String remuneracion = request.getParameter("remuneracion");
            String descripcion = request.getParameter("descripcion");
            String requisitos = request.getParameter("requisitos");
            String estOferta = request.getParameter("estado");
            int vacantes = 1;
            try {
                vacantes = Integer.parseInt(request.getParameter("vacantes"));
            } catch (Exception ignored) {
            }

            if (idEmpresa <= 0 || titulo == null || titulo.trim().isEmpty()
                    || carrera == null || carrera.trim().isEmpty()
                    || fechaLimite == null || fechaLimite.trim().isEmpty()) {
                ok = false;
                mensaje = "Debe seleccionar una empresa e ingresar título, carrera y fecha límite.";
            } else if (vacantes < 1 || vacantes > 100) {
                ok = false;
                mensaje = "El número de vacantes debe estar entre 1 y 100.";
            } else {
                OfertaPractica of = new OfertaPractica();
                of.setIdEmpresa(idEmpresa);
                of.setTitulo(titulo.trim());
                of.setCarrera(carrera.trim());
                of.setModalidad(modalidad != null ? modalidad.trim() : "PRESENCIAL");
                of.setVacantes(vacantes);
                of.setFechaLimite(fechaLimite.trim());
                of.setUbicacion(ubicacion != null ? ubicacion.trim() : "");
                of.setRemuneracion(remuneracion != null ? remuneracion.trim() : "");
                of.setDescripcion(descripcion != null ? descripcion.trim() : "");
                of.setRequisitos(requisitos != null ? requisitos.trim() : "");
                of.setEstado(estOferta != null && !estOferta.trim().isEmpty() ? estOferta.trim().toUpperCase() : "ACTIVA");
                int idGen = ofertaDAO.crear(of);
                ok = (idGen > 0);
                mensaje = ok ? "Oferta publicada exitosamente." : "No se pudo registrar la oferta.";
            }
        } else if ("actualizarOferta".equalsIgnoreCase(accion) && id > 0) {
            OfertaPractica of = ofertaDAO.obtenerPorId(id);
            String titulo = request.getParameter("titulo");
            String carrera = request.getParameter("carrera");
            String modalidad = request.getParameter("modalidad");
            String fechaLimite = request.getParameter("fechaLimite");
            String ubicacion = request.getParameter("ubicacion");
            String remuneracion = request.getParameter("remuneracion");
            String descripcion = request.getParameter("descripcion");
            String requisitos = request.getParameter("requisitos");
            String estOferta = request.getParameter("estado");
            int vacantes = 1;
            try {
                vacantes = Integer.parseInt(request.getParameter("vacantes"));
            } catch (Exception ignored) {
            }

            if (of == null) {
                ok = false;
                mensaje = "Oferta no encontrada.";
            } else if (titulo == null || titulo.trim().isEmpty()
                    || carrera == null || carrera.trim().isEmpty()
                    || fechaLimite == null || fechaLimite.trim().isEmpty()) {
                ok = false;
                mensaje = "Título, carrera y fecha límite son obligatorios.";
            } else if (vacantes < 1 || vacantes > 100) {
                ok = false;
                mensaje = "El número de vacantes debe estar entre 1 y 100.";
            } else {
                of.setTitulo(titulo.trim());
                of.setCarrera(carrera.trim());
                of.setModalidad(modalidad != null ? modalidad.trim() : of.getModalidad());
                of.setVacantes(vacantes);
                of.setFechaLimite(fechaLimite.trim());
                of.setUbicacion(ubicacion != null ? ubicacion.trim() : "");
                of.setRemuneracion(remuneracion != null ? remuneracion.trim() : "");
                of.setDescripcion(descripcion != null ? descripcion.trim() : "");
                of.setRequisitos(requisitos != null ? requisitos.trim() : "");
                of.setEstado(estOferta != null && !estOferta.trim().isEmpty() ? estOferta.trim().toUpperCase() : of.getEstado());
                ok = ofertaDAO.actualizar(of);
                mensaje = ok ? "Oferta actualizada exitosamente." : "No se pudo actualizar la oferta.";
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
        } else if ("cambiarEstadoUsuario".equalsIgnoreCase(accion) && id > 0 && estado != null) {
            if (id == 60 || id == usuarioLogueado.getId()) {
                esProhibido = true;
                ok = false;
                mensaje = "No está permitido cambiar el estado del administrador principal o de tu propia sesión.";
            } else {
                String estNorm = estado.trim().toUpperCase();
                if (!estNorm.equals("ACTIVO") && !estNorm.equals("BLOQUEADO") && !estNorm.equals("INACTIVO")) {
                    ok = false;
                    mensaje = "Estado de usuario no válido.";
                } else {
                    ok = usuarioDAO.actualizarEstado(id, estNorm);
                    mensaje = ok ? "Estado de usuario actualizado a " + estNorm + "." : "No se pudo actualizar el estado del usuario.";
                }
            }
        } else if ("cambiarEstadoOferta".equalsIgnoreCase(accion) && id > 0 && estado != null) {
            ok = ofertaDAO.actualizarEstado(id, estado);
            mensaje = ok ? "Estado de oferta actualizado." : "No se pudo actualizar el estado de la oferta.";
        } else if ("cambiarEstadoPostulacion".equalsIgnoreCase(accion) && id > 0 && estado != null) {
            String estNorm = estado.trim().toUpperCase();
            String motivo = request.getParameter("motivo");
            if (!estNorm.equals("POSTULADO") && !estNorm.equals("EN_REVISION")
                    && !estNorm.equals("SELECCIONADO") && !estNorm.equals("NO_SELECCIONADO")) {
                ok = false;
                mensaje = "Estado de postulación no válido.";
            } else if ("NO_SELECCIONADO".equals(estNorm) && (motivo == null || motivo.trim().isEmpty())) {
                ok = false;
                mensaje = "Debe indicar el motivo cuando el estado es NO_SELECCIONADO.";
            } else {
                ok = postulacionDAO.evaluar(id, estNorm, "NO_SELECCIONADO".equals(estNorm) ? motivo.trim() : null);
                mensaje = ok ? "Estado de postulación actualizado." : "No se pudo actualizar el estado de la postulación.";
            }
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
            if (("usuario".equalsIgnoreCase(recurso) || "usuarios".equalsIgnoreCase(recurso))
                    && (id == 60 || id == usuarioLogueado.getId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write(
                        "{\"exito\":false,\"prohibido\":true,\"mensaje\":\"No estÃ¡ permitido eliminar la cuenta del administrador en sesiÃ³n o principal del sistema.\"}");
                return;
            }
            if ("usuario".equalsIgnoreCase(recurso) || "usuarios".equalsIgnoreCase(recurso)) {
                Usuario uElim = usuarioDAO.obtenerPorId(id);
                if (uElim == null) {
                    ok = false;
                    mensaje = "Usuario no encontrado.";
                } else if ("ADMIN".equalsIgnoreCase(uElim.getRol())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write(
                            "{\"exito\":false,\"prohibido\":true,\"mensaje\":\"No estÃ¡ permitido eliminar cuentas de administrador del sistema.\"}");
                    return;
                } else if ("ESTUDIANTE".equalsIgnoreCase(uElim.getRol())) {
                    Estudiante est = estudianteDAO.obtenerPorUsuarioId(id);
                    if (est != null && estudianteDAO.contarPostulaciones(est.getId()) > 0) {
                        ok = estudianteDAO.actualizarEstado(est.getId(), "INACTIVO");
                        if (ok) {
                            mensaje = "El estudiante tiene postulaciones o registros asociados. Se desactivÃ³ su cuenta (estado INACTIVO) para preservar el historial.";
                        } else {
                            mensaje = "No se pudo desactivar la cuenta del estudiante.";
                        }
                    } else if (est != null) {
                        ok = estudianteDAO.eliminar(est.getId());
                        mensaje = ok ? "Usuario estudiante eliminado exitosamente."
                                : "No se pudo eliminar el estudiante.";
                    } else {
                        ok = usuarioDAO.eliminar(id);
                        mensaje = ok ? "Usuario eliminado exitosamente." : "No se pudo eliminar el usuario.";
                    }
                } else if ("EMPRESA".equalsIgnoreCase(uElim.getRol())) {
                    Empresa emp = empresaDAO.obtenerPorUsuarioId(id);
                    if (emp != null && empresaDAO.contarOfertas(emp.getId()) > 0) {
                        ok = empresaDAO.darDeBaja(emp.getId());
                        if (ok) {
                            mensaje = "La empresa tiene ofertas o registros asociados. Se procediÃ³ a desactivar su cuenta (estado INACTIVO) para preservar el historial.";
                        } else {
                            mensaje = "No se pudo desactivar la cuenta de la empresa.";
                        }
                    } else if (emp != null) {
                        ok = empresaDAO.eliminar(emp.getId());
                        mensaje = ok ? "Usuario empresa eliminado exitosamente." : "No se pudo eliminar la empresa.";
                    } else {
                        ok = usuarioDAO.eliminar(id);
                        mensaje = ok ? "Usuario eliminado exitosamente." : "No se pudo eliminar el usuario.";
                    }
                } else {
                    ok = usuarioDAO.eliminar(id);
                    mensaje = ok ? "Usuario eliminado exitosamente." : "No se pudo eliminar el usuario.";
                }
            } else if ("empresa".equalsIgnoreCase(recurso) || "empresas".equalsIgnoreCase(recurso)) {
                Empresa empExistente = empresaDAO.obtenerPorId(id);
                if (empExistente != null && "INACTIVO".equalsIgnoreCase(empExistente.getEstadoCuenta())) {
                    ok = true;
                    mensaje = "El registro ya se encuentra inactivo.";
                } else {
                    int cantOfertas = empresaDAO.contarOfertas(id);
                    if (cantOfertas > 0) {
                        ok = empresaDAO.darDeBaja(id);
                        if (ok) {
                            mensaje = "La empresa tiene ofertas o registros asociados. Se procediÃ³ a desactivar su cuenta (estado INACTIVO) para preservar el historial.";
                        } else {
                            mensaje = "No se pudo desactivar la cuenta de la empresa.";
                        }
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
                        if (ok) {
                            mensaje = "El estudiante tiene postulaciones o registros asociados. Se desactivÃ³ su cuenta (estado INACTIVO) para preservar el historial.";
                        } else {
                            mensaje = "No se pudo desactivar la cuenta del estudiante.";
                        }
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
                    mensaje = "La oferta tiene postulaciones registradas. Se cambiÃ³ su estado a CERRADA para preservar el historial de postulantes.";
                } else {
                    ok = ofertaDAO.eliminar(id);
                    mensaje = ok ? "Oferta eliminada exitosamente." : "No se pudo eliminar la oferta.";
                }
            } else if ("postulacion".equalsIgnoreCase(recurso) || "postulaciones".equalsIgnoreCase(recurso)) {
                ok = postulacionDAO.eliminar(id);
                mensaje = ok ? "PostulaciÃ³n eliminada exitosamente." : "No se pudo eliminar la postulaciÃ³n.";
            } else if ("entrevista".equalsIgnoreCase(recurso) || "entrevistas".equalsIgnoreCase(recurso)) {
                ok = entrevistaDAO.eliminar(id);
                mensaje = ok ? "Entrevista eliminada exitosamente." : "No se pudo eliminar la entrevista.";
            }
        }

        String jsonResp = String.format("{\"exito\":%b,\"prohibido\":%b,\"mensaje\":\"%s\"}",
                ok, esProhibido, mensaje != null ? mensaje.replace("\"", "\\\"") : "");
        response.getWriter().write(jsonResp);
    }

    /**
     * Valida que el parÃ¡metro ciclo sea un entero vÃ¡lido dentro del rango de ciclos
     * acadÃ©micos admitidos en los formularios de Estudiantes (ciclos 1 al 12).
     * Devuelve el entero validado, o null si falta, estÃ¡ vacÃ­o, no es numÃ©rico o
     * estÃ¡ fuera de rango.
     */
    private Integer validarCicloEstudiante(String cicloStr) {
        if (cicloStr == null || cicloStr.trim().isEmpty()) {
            return null;
        }
        try {
            int c = Integer.parseInt(cicloStr.trim());
            if (c >= 1 && c <= 12) {
                return c;
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }
}
