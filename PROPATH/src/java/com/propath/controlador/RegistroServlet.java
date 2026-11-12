package com.propath.controlador;

import com.propath.dao.EmpresaDAO;
import com.propath.dao.EstudianteDAO;
import com.propath.dao.UsuarioDAO;
import com.propath.modelo.Empresa;
import com.propath.modelo.Estudiante;
import com.propath.servicio.ServicioCorreo;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Locale;

@WebServlet(name = "RegistroServlet", urlPatterns = { "/RegistroServlet", "/registro" })
public class RegistroServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        String tipo = request.getParameter("tipo");
        if ("empresa".equalsIgnoreCase(tipo)) {
            request.getRequestDispatcher("/registro-empresa.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/registro-estudiante.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        String tipo = request.getParameter("tipo");
        HttpSession session = request.getSession(true);

        if ("empresa".equalsIgnoreCase(tipo)) {
            String nombre = request.getParameter("nombre");
            String apellido = request.getParameter("apellido");
            String razonSocial = request.getParameter("razonSocial");
            String ruc = request.getParameter("ruc");
            String correo = request.getParameter("correo");
            String contrasena = request.getParameter("contrasena");
            String telefono = request.getParameter("telefono");
            String direccion = request.getParameter("direccion");
            String descripcion = request.getParameter("descripcion");

            if (correo != null) {
                correo = correo.trim().toLowerCase(Locale.ROOT);
            }

            if (nombre == null || nombre.trim().isEmpty() ||
                    apellido == null || apellido.trim().isEmpty() ||
                    razonSocial == null || razonSocial.trim().isEmpty() ||
                    ruc == null || !ruc.trim().matches("^[0-9]{11}$") ||
                    correo == null || correo.trim().isEmpty() ||
                    contrasena == null || contrasena.trim().length() < 8) {

                request.setAttribute("error",
                        "Por favor completa todos los campos obligatorios. El RUC debe tener exactamente 11 dígitos numéricos y la contraseña al menos 8 caracteres.");
                preservarCamposEmpresa(request, nombre, apellido, razonSocial, ruc, correo, telefono, direccion, descripcion);
                request.getRequestDispatcher("/registro-empresa.jsp").forward(request, response);
                return;
            }

            if (telefono != null && !telefono.trim().isEmpty() && !telefono.trim().matches("^[0-9]{7,9}$")) {
                request.setAttribute("error", "El número telefónico debe contener entre 7 y 9 dígitos numéricos.");
                preservarCamposEmpresa(request, nombre, apellido, razonSocial, ruc, correo, telefono, direccion, descripcion);
                request.getRequestDispatcher("/registro-empresa.jsp").forward(request, response);
                return;
            }

            if (!contrasena.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
                request.setAttribute("error", "La contraseña debe contener al menos una letra y un número para garantizar la seguridad.");
                preservarCamposEmpresa(request, nombre, apellido, razonSocial, ruc, correo, telefono, direccion, descripcion);
                request.getRequestDispatcher("/registro-empresa.jsp").forward(request, response);
                return;
            }

            // Validación de verificación previa de correo electrónico (solo obligatoria si SMTP está activo)
            if (ServicioCorreo.isConfigurado()) {
                String correoVerificado = (String) session.getAttribute("CORREO_VERIFICADO_REGISTRO");
                if (correoVerificado == null || !correoVerificado.equalsIgnoreCase(correo)) {
                    request.setAttribute("error", "Debes verificar tu correo electrónico antes de registrarte. Haz clic en 'Verificar Correo'.");
                    preservarCamposEmpresa(request, nombre, apellido, razonSocial, ruc, correo, telefono, direccion, descripcion);
                    request.getRequestDispatcher("/registro-empresa.jsp").forward(request, response);
                    return;
                }
            }

            UsuarioDAO usuarioDAO = new UsuarioDAO();
            EmpresaDAO empresaDAO = new EmpresaDAO();

            if (usuarioDAO.obtenerPorCorreo(correo) != null) {
                request.setAttribute("error", "El correo electrónico ya se encuentra registrado.");
                preservarCamposEmpresa(request, nombre, apellido, razonSocial, ruc, correo, telefono, direccion, descripcion);
                request.getRequestDispatcher("/registro-empresa.jsp").forward(request, response);
                return;
            }

            if (empresaDAO.obtenerPorRuc(ruc.trim()) != null) {
                request.setAttribute("error", "El RUC ya se encuentra registrado por otra empresa.");
                preservarCamposEmpresa(request, nombre, apellido, razonSocial, ruc, correo, telefono, direccion, descripcion);
                request.getRequestDispatcher("/registro-empresa.jsp").forward(request, response);
                return;
            }

            Empresa emp = new Empresa();
            emp.setRazonSocial(razonSocial.trim());
            emp.setRuc(ruc.trim());
            emp.setTelefono(telefono != null ? telefono.trim() : "");
            emp.setDireccion(direccion != null ? direccion.trim() : "");
            emp.setDescripcion(descripcion != null ? descripcion.trim() : "");
            emp.setEstado("PENDIENTE");

            int idGenerado = empresaDAO.registrarConUsuario(emp, correo, contrasena, nombre.trim(), apellido.trim());

            if (idGenerado > 0) {
                session.removeAttribute("CORREO_VERIFICADO_REGISTRO");
                session.removeAttribute("TOKEN_VERIFICADO_REGISTRO");

                if (ServicioCorreo.isConfigurado()) {
                    try {
                        ServicioCorreo.enviarBienvenidaRegistro(correo, razonSocial.trim(), "EMPRESA");
                    } catch (Exception e) {
                        getServletContext().log("Fallo al enviar correo de bienvenida de empresa", e);
                    }
                }
                response.sendRedirect(request.getContextPath() + "/login.jsp?registro=exito");
            } else {
                request.setAttribute("error", "Ocurrió un error al registrar la empresa. Por favor verifica los datos o intenta nuevamente.");
                preservarCamposEmpresa(request, nombre, apellido, razonSocial, ruc, correo, telefono, direccion, descripcion);
                request.getRequestDispatcher("/registro-empresa.jsp").forward(request, response);
            }

        } else {
            String dni = request.getParameter("dni");
            String nombre = request.getParameter("nombre");
            String apellidos = request.getParameter("apellidos");
            String correo = request.getParameter("correo");
            String contrasena = request.getParameter("contrasena");
            String universidad = request.getParameter("universidad");
            String carrera = request.getParameter("carrera");
            String cicloStr = request.getParameter("ciclo");
            String telefono = request.getParameter("telefono");

            if (correo != null) {
                correo = correo.trim().toLowerCase(Locale.ROOT);
            }

            int ciclo = 1;
            try {
                if (cicloStr != null && !cicloStr.trim().isEmpty()) {
                    ciclo = Integer.parseInt(cicloStr.trim());
                }
            } catch (NumberFormatException ignored) {
            }

            if (dni == null || !dni.trim().matches("^[0-9]{8}$")) {
                request.setAttribute("error", "El DNI es obligatorio y debe contener exactamente 8 dígitos numéricos.");
                preservarCamposEstudiante(request, dni, nombre, apellidos, correo, universidad, carrera, ciclo, telefono);
                request.getRequestDispatcher("/registro-estudiante.jsp").forward(request, response);
                return;
            }

            if (nombre == null || nombre.trim().isEmpty() ||
                    apellidos == null || apellidos.trim().isEmpty() ||
                    correo == null || correo.trim().isEmpty() ||
                    contrasena == null || contrasena.trim().length() < 8 ||
                    universidad == null || universidad.trim().isEmpty() ||
                    carrera == null || carrera.trim().isEmpty()) {

                request.setAttribute("error",
                        "Por favor completa todos los campos obligatorios. La contraseña debe tener al menos 8 caracteres.");
                preservarCamposEstudiante(request, dni, nombre, apellidos, correo, universidad, carrera, ciclo, telefono);
                request.getRequestDispatcher("/registro-estudiante.jsp").forward(request, response);
                return;
            }

            if (telefono != null && !telefono.trim().isEmpty() && !telefono.trim().matches("^[0-9]{9}$")) {
                request.setAttribute("error", "El teléfono celular debe contener exactamente 9 dígitos numéricos.");
                preservarCamposEstudiante(request, dni, nombre, apellidos, correo, universidad, carrera, ciclo, telefono);
                request.getRequestDispatcher("/registro-estudiante.jsp").forward(request, response);
                return;
            }

            if (!contrasena.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
                request.setAttribute("error", "La contraseña debe contener al menos una letra y un número para garantizar la seguridad.");
                preservarCamposEstudiante(request, dni, nombre, apellidos, correo, universidad, carrera, ciclo, telefono);
                request.getRequestDispatcher("/registro-estudiante.jsp").forward(request, response);
                return;
            }

            // Validación de verificación previa de correo electrónico (solo obligatoria si SMTP está activo)
            if (ServicioCorreo.isConfigurado()) {
                String correoVerificado = (String) session.getAttribute("CORREO_VERIFICADO_REGISTRO");
                if (correoVerificado == null || !correoVerificado.equalsIgnoreCase(correo)) {
                    request.setAttribute("error", "Debes verificar tu correo electrónico antes de registrarte. Haz clic en 'Verificar Correo'.");
                    preservarCamposEstudiante(request, dni, nombre, apellidos, correo, universidad, carrera, ciclo, telefono);
                    request.getRequestDispatcher("/registro-estudiante.jsp").forward(request, response);
                    return;
                }
            }

            UsuarioDAO usuarioDAO = new UsuarioDAO();
            EstudianteDAO estudianteDAO = new EstudianteDAO();

            if (usuarioDAO.obtenerPorCorreo(correo) != null) {
                request.setAttribute("error", "El correo electrónico ya se encuentra registrado.");
                preservarCamposEstudiante(request, dni, nombre, apellidos, correo, universidad, carrera, ciclo, telefono);
                request.getRequestDispatcher("/registro-estudiante.jsp").forward(request, response);
                return;
            }

            if (estudianteDAO.obtenerPorDni(dni.trim()) != null) {
                request.setAttribute("error", "El DNI ingresado ya se encuentra registrado en el sistema.");
                preservarCamposEstudiante(request, dni, nombre, apellidos, correo, universidad, carrera, ciclo, telefono);
                request.getRequestDispatcher("/registro-estudiante.jsp").forward(request, response);
                return;
            }

            Estudiante est = new Estudiante();
            est.setDni(dni.trim());
            est.setNombre(nombre.trim());
            est.setApellido(apellidos.trim());
            est.setUniversidad(universidad.trim());
            est.setCarrera(carrera.trim());
            est.setCiclo(ciclo);
            est.setTelefono(telefono != null ? telefono.trim() : "");
            est.setCv("");

            int idGenerado = estudianteDAO.registrarConUsuario(est, correo, contrasena);

            if (idGenerado > 0) {
                session.removeAttribute("CORREO_VERIFICADO_REGISTRO");
                session.removeAttribute("TOKEN_VERIFICADO_REGISTRO");

                if (ServicioCorreo.isConfigurado()) {
                    try {
                        String nombreCompleto = nombre.trim() + " " + apellidos.trim();
                        ServicioCorreo.enviarBienvenidaRegistro(correo, nombreCompleto, "ESTUDIANTE");
                    } catch (Exception e) {
                        getServletContext().log("Fallo al enviar correo de bienvenida de estudiante", e);
                    }
                }
                response.sendRedirect(request.getContextPath() + "/login.jsp?registro=exito");
            } else {
                request.setAttribute("error", "Ocurrió un error al registrar el estudiante en la base de datos. Por favor verifica que el servidor de base de datos esté activo e intenta nuevamente.");
                preservarCamposEstudiante(request, dni, nombre, apellidos, correo, universidad, carrera, ciclo, telefono);
                request.getRequestDispatcher("/registro-estudiante.jsp").forward(request, response);
            }
        }
    }

    private void preservarCamposEmpresa(HttpServletRequest request, String nombre, String apellido,
                                        String razonSocial, String ruc, String correo, String telefono,
                                        String direccion, String descripcion) {
        request.setAttribute("nombre", nombre);
        request.setAttribute("apellido", apellido);
        request.setAttribute("razonSocial", razonSocial);
        request.setAttribute("ruc", ruc);
        request.setAttribute("correo", correo);
        request.setAttribute("telefono", telefono);
        request.setAttribute("direccion", direccion);
        request.setAttribute("descripcion", descripcion);
    }

    private void preservarCamposEstudiante(HttpServletRequest request, String dni, String nombre, String apellidos,
                                           String correo, String universidad, String carrera,
                                           int ciclo, String telefono) {
        request.setAttribute("dni", dni);
        request.setAttribute("nombre", nombre);
        request.setAttribute("apellidos", apellidos);
        request.setAttribute("correo", correo);
        request.setAttribute("universidad", universidad);
        request.setAttribute("carrera", carrera);
        request.setAttribute("ciclo", ciclo);
        request.setAttribute("telefono", telefono);
    }
}
