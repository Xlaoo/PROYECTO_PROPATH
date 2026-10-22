package com.propath.controlador;

import com.propath.servicio.BackendCliente;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "RegistroServlet", urlPatterns = { "/RegistroServlet", "/registro" })
public class RegistroServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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

        String tipo = request.getParameter("tipo");

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

            if (nombre == null || nombre.trim().isEmpty() ||
                    apellido == null || apellido.trim().isEmpty() ||
                    razonSocial == null || razonSocial.trim().isEmpty() ||
                    ruc == null || ruc.trim().length() != 11 ||
                    correo == null || correo.trim().isEmpty() ||
                    contrasena == null || contrasena.trim().length() < 6) {

                request.setAttribute("error",
                        "Por favor completa todos los campos obligatorios. El RUC debe tener 11 dígitos y la contraseña al menos 6 caracteres.");
                request.setAttribute("nombre", nombre);
                request.setAttribute("apellido", apellido);
                request.setAttribute("razonSocial", razonSocial);
                request.setAttribute("ruc", ruc);
                request.setAttribute("correo", correo);
                request.setAttribute("telefono", telefono);
                request.setAttribute("direccion", direccion);
                request.setAttribute("descripcion", descripcion);
                request.getRequestDispatcher("/registro-empresa.jsp").forward(request, response);
                return;
            }

            String error = BackendCliente.registrarEmpresa(
                    nombre.trim(),
                    apellido.trim(),
                    razonSocial.trim(),
                    ruc.trim(),
                    correo.trim(),
                    contrasena,
                    telefono != null ? telefono.trim() : "",
                    direccion != null ? direccion.trim() : "",
                    descripcion != null ? descripcion.trim() : "");

            if (error == null) {
                response.sendRedirect(request.getContextPath() + "/login.jsp?registro=exito");
            } else {
                request.setAttribute("error", error);
                request.setAttribute("nombre", nombre);
                request.setAttribute("apellido", apellido);
                request.setAttribute("razonSocial", razonSocial);
                request.setAttribute("ruc", ruc);
                request.setAttribute("correo", correo);
                request.setAttribute("telefono", telefono);
                request.setAttribute("direccion", direccion);
                request.setAttribute("descripcion", descripcion);
                request.getRequestDispatcher("/registro-empresa.jsp").forward(request, response);
            }

        } else {
            String nombre = request.getParameter("nombre");
            String apellidos = request.getParameter("apellidos");
            String correo = request.getParameter("correo");
            String contrasena = request.getParameter("contrasena");
            String universidad = request.getParameter("universidad");
            String carrera = request.getParameter("carrera");
            String cicloStr = request.getParameter("ciclo");
            String telefono = request.getParameter("telefono");

            int ciclo = 1;
            try {
                if (cicloStr != null && !cicloStr.trim().isEmpty()) {
                    ciclo = Integer.parseInt(cicloStr.trim());
                }
            } catch (NumberFormatException ignored) {
            }

            if (nombre == null || nombre.trim().isEmpty() ||
                    apellidos == null || apellidos.trim().isEmpty() ||
                    correo == null || correo.trim().isEmpty() ||
                    contrasena == null || contrasena.trim().length() < 6 ||
                    universidad == null || universidad.trim().isEmpty() ||
                    carrera == null || carrera.trim().isEmpty()) {

                request.setAttribute("error",
                        "Por favor completa todos los campos obligatorios. La contraseña debe tener al menos 6 caracteres.");
                request.setAttribute("nombre", nombre);
                request.setAttribute("apellidos", apellidos);
                request.setAttribute("correo", correo);
                request.setAttribute("universidad", universidad);
                request.setAttribute("carrera", carrera);
                request.setAttribute("ciclo", ciclo);
                request.setAttribute("telefono", telefono);
                request.getRequestDispatcher("/registro-estudiante.jsp").forward(request, response);
                return;
            }

            String error = BackendCliente.registrarEstudiante(nombre.trim(), apellidos.trim(), correo.trim(),
                    contrasena, universidad.trim(), carrera.trim(), ciclo, telefono != null ? telefono.trim() : "");

            if (error == null) {
                response.sendRedirect(request.getContextPath() + "/login.jsp?registro=exito");
            } else {
                request.setAttribute("error", error);
                request.setAttribute("nombre", nombre);
                request.setAttribute("apellidos", apellidos);
                request.setAttribute("correo", correo);
                request.setAttribute("universidad", universidad);
                request.setAttribute("carrera", carrera);
                request.setAttribute("ciclo", ciclo);
                request.setAttribute("telefono", telefono);
                request.getRequestDispatcher("/registro-estudiante.jsp").forward(request, response);
            }
        }
    }
}
