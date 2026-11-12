package com.propath.controlador;

import com.propath.dao.DocumentoEstudianteDAO;
import com.propath.modelo.DocumentoEstudiante;
import com.propath.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

@WebServlet(name = "EstudianteDocumentoServlet", urlPatterns = { "/estudiante/documentos",
        "/EstudianteDocumentoServlet" })
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
public class EstudianteDocumentoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario.getEstado() != null && !usuario.getEstado().equalsIgnoreCase("ACTIVO")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "La cuenta de usuario no está activa.");
            return;
        }

        int idEstudiante = 0;
        boolean esAdmin = "ADMIN".equalsIgnoreCase(usuario.getRol());
        boolean esEstudiante = "ESTUDIANTE".equalsIgnoreCase(usuario.getRol());

        if (esEstudiante) {
            idEstudiante = usuario.getIdEstudiante() != null ? usuario.getIdEstudiante() : 0;
        } else if (esAdmin) {
            String idParam = request.getParameter("idEstudiante");
            if (idParam != null) {
                try {
                    idEstudiante = Integer.parseInt(idParam.trim());
                } catch (NumberFormatException ignored) {
                }
            }
        } else if ("EMPRESA".equalsIgnoreCase(usuario.getRol())) {
            String idPostParam = request.getParameter("idPostulacion");
            if (idPostParam != null && usuario.getIdEmpresa() != null) {
                try {
                    int idPost = Integer.parseInt(idPostParam.trim());
                    com.propath.modelo.Postulacion post = new com.propath.dao.PostulacionDAO().obtenerPorId(idPost);
                    if (post != null) {
                        com.propath.modelo.OfertaPractica of = new com.propath.dao.OfertaPracticaDAO().obtenerPorId(post.getIdOferta());
                        if (of != null && of.getIdEmpresa() == usuario.getIdEmpresa().intValue()) {
                            idEstudiante = post.getIdEstudiante();
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        if (idEstudiante <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso no autorizado.");
            return;
        }

        DocumentoEstudianteDAO docDAO = new DocumentoEstudianteDAO();
        String accion = request.getParameter("accion");
        if ("ver".equalsIgnoreCase(accion) || "descargar".equalsIgnoreCase(accion)) {
            boolean inline = "ver".equalsIgnoreCase(accion);
            String idDocParam = request.getParameter("idDocumento");
            DocumentoEstudiante doc = null;
            byte[] fileBytes = null;

            if (idDocParam != null && !idDocParam.trim().isEmpty()) {
                try {
                    int idDoc = Integer.parseInt(idDocParam.trim());
                    DocumentoEstudiante encontrado = docDAO.obtenerPorId(idDoc);
                    if (encontrado != null && encontrado.getIdEstudiante() == idEstudiante) {
                        doc = encontrado;
                        fileBytes = docDAO.descargarPorId(idDoc);
                    }
                } catch (NumberFormatException ignored) {
                }
            } else {
                doc = docDAO.obtenerCvEstudiante(idEstudiante);
                fileBytes = docDAO.descargarCv(idEstudiante);
            }

            if (doc == null || fileBytes == null || fileBytes.length == 0) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Documento no encontrado.");
                return;
            }

            String disposition = inline ? "inline" : "attachment";
            String ctype = (doc.getContentType() != null && !doc.getContentType().isBlank())
                    ? doc.getContentType() : "application/pdf";
            String nombreDescarga = doc.getNombreArchivo() != null ? doc.getNombreArchivo() : "documento.pdf";
            String encodedName = URLEncoder.encode(nombreDescarga, StandardCharsets.UTF_8).replace("+", "%20");

            response.setContentType(ctype);
            response.setHeader("Content-Disposition",
                    disposition + "; filename=\"" + nombreDescarga + "\"; filename*=UTF-8''" + encodedName);
            response.setContentLength(fileBytes.length);
            response.getOutputStream().write(fileBytes);
            response.getOutputStream().flush();
            return;
        }

        response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?tab=documentos");
    }

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
        if (!"ESTUDIANTE".equalsIgnoreCase(usuario.getRol())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Solo los estudiantes pueden gestionar sus documentos.");
            return;
        }

        if (usuario.getEstado() != null && !usuario.getEstado().equalsIgnoreCase("ACTIVO")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Tu cuenta no se encuentra activa para realizar operaciones.");
            return;
        }

        int idEstudiante = usuario.getIdEstudiante() != null ? usuario.getIdEstudiante() : 0;
        if (idEstudiante <= 0) {
            response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                    URLEncoder.encode("Perfil de estudiante no vinculado.", StandardCharsets.UTF_8) + "&tab=documentos");
            return;
        }

        String accion = request.getParameter("accion");
        DocumentoEstudianteDAO docDAO = new DocumentoEstudianteDAO();

        if ("subir".equalsIgnoreCase(accion) || "reemplazar".equalsIgnoreCase(accion)) {
            Part filePart = null;
            try {
                filePart = request.getPart("archivoPdf");
            } catch (Exception e) {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                        URLEncoder.encode("El archivo supera el tamaño máximo permitido de 5 MB.",
                                StandardCharsets.UTF_8) + "&tab=documentos");
                return;
            }

            if (filePart == null || filePart.getSize() <= 0) {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                        URLEncoder.encode("Por favor selecciona un archivo PDF.", StandardCharsets.UTF_8) + "&tab=documentos");
                return;
            }

            if (filePart.getSize() > 5 * 1024 * 1024) {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                        URLEncoder.encode("El archivo supera el límite de 5 MB.", StandardCharsets.UTF_8) + "&tab=documentos");
                return;
            }

            String submittedFileName = filePart.getSubmittedFileName();
            if (submittedFileName == null || !submittedFileName.toLowerCase().endsWith(".pdf")) {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                        URLEncoder.encode("El CV debe ser un documento con extensión .pdf.",
                                StandardCharsets.UTF_8) + "&tab=documentos");
                return;
            }

            byte[] fileBytes = filePart.getInputStream().readAllBytes();

            if (fileBytes.length < 4 ||
                    fileBytes[0] != 0x25 || fileBytes[1] != 0x50 ||
                    fileBytes[2] != 0x44 || fileBytes[3] != 0x46) {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                        URLEncoder.encode("El archivo seleccionado no es un documento PDF válido.",
                                StandardCharsets.UTF_8) + "&tab=documentos");
                return;
            }

            String nombreLimpio = Paths.get(submittedFileName).getFileName().toString();
            String error = docDAO.guardarCv(idEstudiante, nombreLimpio, fileBytes);

            if (error == null) {
                String mensajeExito = "reemplazar".equalsIgnoreCase(accion) ? "cv_reemplazado" : "cv_guardado";
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?exito=" + mensajeExito + "&tab=documentos");
            } else {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                        URLEncoder.encode(error, StandardCharsets.UTF_8) + "&tab=documentos");
            }
            return;
        } else if ("subirDocumento".equalsIgnoreCase(accion)) {
            Part filePart = null;
            try {
                filePart = request.getPart("archivoDocumento");
                if (filePart == null) {
                    filePart = request.getPart("archivoPdf");
                }
            } catch (Exception e) {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                        URLEncoder.encode("El documento supera el tamaño máximo permitido de 5 MB.",
                                StandardCharsets.UTF_8) + "&tab=documentos");
                return;
            }

            if (filePart == null || filePart.getSize() <= 0) {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                        URLEncoder.encode("Por favor selecciona un archivo válido (PDF, PNG o JPG).", StandardCharsets.UTF_8) + "&tab=documentos");
                return;
            }

            if (filePart.getSize() > 5 * 1024 * 1024) {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                        URLEncoder.encode("El documento supera el límite máximo permitido de 5 MB.", StandardCharsets.UTF_8) + "&tab=documentos");
                return;
            }

            String tipoDoc = request.getParameter("tipoDocumento");
            String submittedFileName = filePart.getSubmittedFileName();
            String nombreLimpio = submittedFileName != null ? Paths.get(submittedFileName).getFileName().toString() : "documento.pdf";
            byte[] fileBytes = filePart.getInputStream().readAllBytes();

            String error = docDAO.guardarDocumento(idEstudiante, tipoDoc, nombreLimpio, fileBytes);
            if (error == null) {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?exito=documento_guardado&tab=documentos");
            } else {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                        URLEncoder.encode(error, StandardCharsets.UTF_8) + "&tab=documentos");
            }
            return;
        } else if ("eliminarDocumento".equalsIgnoreCase(accion)) {
            int idDocumento = 0;
            try {
                idDocumento = Integer.parseInt(request.getParameter("idDocumento"));
            } catch (Exception ignored) {
            }
            boolean ok = docDAO.eliminarPorId(idDocumento, idEstudiante);
            if (ok) {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?exito=documento_eliminado&tab=documentos");
            } else {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                        URLEncoder.encode("No se pudo eliminar el documento.", StandardCharsets.UTF_8) + "&tab=documentos");
            }
            return;
        } else if ("eliminar".equalsIgnoreCase(accion)) {
            boolean ok = docDAO.eliminarCv(idEstudiante);
            if (ok) {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?exito=cv_eliminado&tab=documentos");
            } else {
                response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?error=" +
                        URLEncoder.encode("No se pudo eliminar el currículum vitae.", StandardCharsets.UTF_8) + "&tab=documentos");
            }
            return;
        }

        response.sendRedirect(request.getContextPath() + "/estudiante/dashboard.jsp?tab=documentos");
    }
}
