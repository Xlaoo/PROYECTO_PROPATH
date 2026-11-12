package com.propath.dao;

import com.propath.conexion.ConexionBD;
import com.propath.modelo.DocumentoEstudiante;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DocumentoEstudianteDAO {

    public List<DocumentoEstudiante> listarPorEstudiante(int idEstudiante) {
        List<DocumentoEstudiante> lista = new ArrayList<>();
        String sql = "SELECT id_documento, id_estudiante, tipo_documento, nombre_archivo, archivo_ruta, "
                   + "tamanio_bytes, content_type, fecha_subida "
                   + "FROM documentos_estudiante WHERE id_estudiante = ? ORDER BY id_documento DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearDocumento(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public DocumentoEstudiante obtenerCvEstudiante(int idEstudiante) {
        String sql = "SELECT id_documento, id_estudiante, tipo_documento, nombre_archivo, archivo_ruta, "
                   + "tamanio_bytes, content_type, fecha_subida "
                   + "FROM documentos_estudiante WHERE id_estudiante = ? AND tipo_documento = 'CV' "
                   + "ORDER BY id_documento DESC LIMIT 1";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearDocumento(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public DocumentoEstudiante obtenerCvPorEstudiante(int idEstudiante) {
        return obtenerCvEstudiante(idEstudiante);
    }

    public DocumentoEstudiante obtenerPorId(int idDocumento) {
        String sql = "SELECT id_documento, id_estudiante, tipo_documento, nombre_archivo, archivo_ruta, "
                   + "tamanio_bytes, content_type, fecha_subida "
                   + "FROM documentos_estudiante WHERE id_documento = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idDocumento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearDocumento(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int guardar(DocumentoEstudiante doc) {
        if (doc == null || doc.getIdEstudiante() <= 0) {
            return -1;
        }

        String sql = "INSERT INTO documentos_estudiante (id_estudiante, tipo_documento, nombre_archivo, archivo_ruta, tamanio_bytes, content_type) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, doc.getIdEstudiante());
            ps.setString(2, doc.getTipoDocumento() != null ? doc.getTipoDocumento() : "CV");
            ps.setString(3, doc.getNombreArchivo() != null ? doc.getNombreArchivo() : "archivo.pdf");
            ps.setString(4, doc.getArchivoRuta() != null ? doc.getArchivoRuta() : "");
            ps.setLong(5, doc.getTamanioBytes());
            ps.setString(6, doc.getContentType() != null ? doc.getContentType() : "application/pdf");

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int idGen = rs.getInt(1);
                        doc.setId(idGen);
                        return idGen;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean eliminar(int idDocumento) {
        if (idDocumento <= 0) {
            return false;
        }

        String sql = "DELETE FROM documentos_estudiante WHERE id_documento = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idDocumento);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String guardarCv(int idEstudiante, String nombreOriginal, byte[] contenidoPdf) {
        if (idEstudiante <= 0) {
            return "ID de estudiante no válido.";
        }
        if (contenidoPdf == null || contenidoPdf.length < 4 ||
            contenidoPdf[0] != 0x25 || contenidoPdf[1] != 0x50 ||
            contenidoPdf[2] != 0x44 || contenidoPdf[3] != 0x46) {
            return "El archivo seleccionado no es un documento PDF válido.";
        }
        if (contenidoPdf.length > 5 * 1024 * 1024) {
            return "El archivo supera el tamaño máximo permitido de 5 MB.";
        }

        String storageDir = obtenerDirectorioAlmacenamiento();
        java.io.File dir = new java.io.File(storageDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        DocumentoEstudiante docExistente = obtenerCvEstudiante(idEstudiante);
        String oldFile = docExistente != null ? docExistente.getArchivoRuta() : null;
        String safeFileName = "cv_estudiante_" + idEstudiante + "_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 10) + ".pdf";
        java.io.File targetFile = new java.io.File(dir, safeFileName);

        try {
            java.nio.file.Files.write(targetFile.toPath(), contenidoPdf);
        } catch (java.io.IOException e) {
            return "Error al escribir el archivo en almacenamiento: " + e.getMessage();
        }

        String nombreLimpio = nombreOriginal != null ? new java.io.File(nombreOriginal).getName() : "CV.pdf";
        if (!nombreLimpio.toLowerCase().endsWith(".pdf")) {
            nombreLimpio += ".pdf";
        }

        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);

            if (docExistente != null) {
                String sqlDoc = "UPDATE documentos_estudiante SET nombre_archivo = ?, archivo_ruta = ?, tamanio_bytes = ?, content_type = 'application/pdf', fecha_subida = CURRENT_TIMESTAMP WHERE id_documento = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlDoc)) {
                    ps.setString(1, nombreLimpio);
                    ps.setString(2, safeFileName);
                    ps.setLong(3, contenidoPdf.length);
                    ps.setInt(4, docExistente.getId());
                    ps.executeUpdate();
                }
            } else {
                String sqlDoc = "INSERT INTO documentos_estudiante (id_estudiante, tipo_documento, nombre_archivo, archivo_ruta, tamanio_bytes, content_type) VALUES (?, 'CV', ?, ?, ?, 'application/pdf')";
                try (PreparedStatement ps = conn.prepareStatement(sqlDoc)) {
                    ps.setInt(1, idEstudiante);
                    ps.setString(2, nombreLimpio);
                    ps.setString(3, safeFileName);
                    ps.setLong(4, contenidoPdf.length);
                    ps.executeUpdate();
                }
            }

            String sqlEst = "UPDATE estudiantes SET cv = ? WHERE id_estudiante = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlEst)) {
                ps.setString(1, nombreLimpio);
                ps.setInt(2, idEstudiante);
                ps.executeUpdate();
            }

            conn.commit();

            if (oldFile != null && !oldFile.equals(safeFileName)) {
                java.io.File fOld = new java.io.File(dir, oldFile);
                if (fOld.exists()) {
                    fOld.delete();
                }
            }
            return null;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            targetFile.delete();
            return "Error en la base de datos al guardar CV: " + e.getMessage();
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public byte[] descargarCv(int idEstudiante) {
        DocumentoEstudiante doc = obtenerCvEstudiante(idEstudiante);
        if (doc == null || doc.getArchivoRuta() == null) {
            return null;
        }
        return descargarCv(doc.getArchivoRuta());
    }

    public byte[] descargarCv(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            return null;
        }
        String storageDir = obtenerDirectorioAlmacenamiento();
        java.io.File file = new java.io.File(storageDir, nombreArchivo.trim());
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        try {
            return java.nio.file.Files.readAllBytes(file.toPath());
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean eliminarCv(int idEstudiante) {
        DocumentoEstudiante doc = obtenerCvEstudiante(idEstudiante);
        if (doc == null) {
            return false;
        }
        String storageDir = obtenerDirectorioAlmacenamiento();
        java.io.File file = new java.io.File(storageDir, doc.getArchivoRuta());
        if (file.exists()) {
            file.delete();
        }
        eliminar(doc.getId());
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement("UPDATE estudiantes SET cv = NULL WHERE id_estudiante = ?")) {
            ps.setInt(1, idEstudiante);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
        return true;
    }

    public String guardarDocumento(int idEstudiante, String tipoDocumento, String nombreOriginal, byte[] contenido) {
        if (idEstudiante <= 0) {
            return "ID de estudiante no válido.";
        }
        String tipoNorm = normalizarTipoDocumento(tipoDocumento);
        if ("CV".equals(tipoNorm)) {
            return guardarCv(idEstudiante, nombreOriginal, contenido);
        }
        if (contenido == null || contenido.length == 0) {
            return "El archivo seleccionado está vacío.";
        }
        if (contenido.length > 5 * 1024 * 1024) {
            return "El archivo supera el tamaño máximo permitido de 5 MB.";
        }

        String nombreLimpio = nombreOriginal != null ? new java.io.File(nombreOriginal).getName().trim() : "documento.pdf";
        String lower = nombreLimpio.toLowerCase();
        String detectedContentType = detectarTipoContenidoSeguro(contenido, lower);
        if (detectedContentType == null) {
            return "Formato no permitido o archivo corrupto. Solo se aceptan PDF, PNG o JPG válidos (máx. 5 MB).";
        }

        List<DocumentoEstudiante> actuales = listarPorEstudiante(idEstudiante);
        if (actuales.size() >= 10) {
            return "Has alcanzado el límite máximo de 10 documentos almacenados.";
        }

        String ext = "application/pdf".equals(detectedContentType) ? ".pdf"
                : ("image/png".equals(detectedContentType) ? ".png" : ".jpg");
        String storageDir = obtenerDirectorioAlmacenamiento();
        java.io.File dir = new java.io.File(storageDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String safeFileName = "doc_" + tipoNorm.toLowerCase() + "_" + idEstudiante + "_"
                + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 10) + ext;
        java.io.File targetFile = new java.io.File(dir, safeFileName);
        try {
            java.nio.file.Files.write(targetFile.toPath(), contenido);
        } catch (java.io.IOException e) {
            return "Error al guardar el documento en almacenamiento: " + e.getMessage();
        }

        DocumentoEstudiante doc = new DocumentoEstudiante();
        doc.setIdEstudiante(idEstudiante);
        doc.setTipoDocumento(tipoNorm);
        doc.setNombreArchivo(nombreLimpio);
        doc.setArchivoRuta(safeFileName);
        doc.setTamanioBytes(contenido.length);
        doc.setContentType(detectedContentType);
        int idGen = guardar(doc);
        if (idGen <= 0) {
            targetFile.delete();
            return "No se pudo registrar el documento en la base de datos.";
        }
        return null;
    }

    public byte[] descargarPorId(int idDocumento) {
        DocumentoEstudiante doc = obtenerPorId(idDocumento);
        if (doc == null || doc.getArchivoRuta() == null) {
            return null;
        }
        return descargarCv(doc.getArchivoRuta());
    }

    public boolean eliminarPorId(int idDocumento, int idEstudiante) {
        DocumentoEstudiante doc = obtenerPorId(idDocumento);
        if (doc == null || doc.getIdEstudiante() != idEstudiante) {
            return false;
        }
        if ("CV".equalsIgnoreCase(doc.getTipoDocumento())) {
            return eliminarCv(idEstudiante);
        }
        String storageDir = obtenerDirectorioAlmacenamiento();
        java.io.File file = new java.io.File(storageDir, doc.getArchivoRuta());
        if (file.exists()) {
            file.delete();
        }
        return eliminar(idDocumento);
    }

    private String normalizarTipoDocumento(String tipo) {
        if (tipo == null) return "OTRO";
        String t = tipo.trim().toUpperCase();
        if ("CV".equals(t)) return "CV";
        if ("CARTA_PRESENTACION".equals(t)) return "CARTA_PRESENTACION";
        if ("CERTIFICADO".equals(t)) return "CERTIFICADO";
        if ("CONSTANCIA".equals(t)) return "CONSTANCIA";
        if ("PORTAFOLIO".equals(t)) return "PORTAFOLIO";
        return "OTRO";
    }

    private String detectarTipoContenidoSeguro(byte[] bytes, String fileNameLower) {
        if (bytes.length >= 4 && bytes[0] == 0x25 && bytes[1] == 0x50 && bytes[2] == 0x44 && bytes[3] == 0x46
                && fileNameLower.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (bytes.length >= 8 && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47
                && fileNameLower.endsWith(".png")) {
            return "image/png";
        }
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF
                && (fileNameLower.endsWith(".jpg") || fileNameLower.endsWith(".jpeg"))) {
            return "image/jpeg";
        }
        return null;
    }

    private String obtenerDirectorioAlmacenamiento() {
        String customDir = System.getProperty("PROPATH_STORAGE_DIR");
        if (customDir != null && !customDir.trim().isEmpty()) {
            return customDir.trim();
        }
        java.io.File legacyDir = new java.io.File("D:\\PROYECTO_PROPATH");
        if (legacyDir.exists() && legacyDir.isDirectory()) {
            return "D:\\PROYECTO_PROPATH\\PROPATH\\storage\\documentos";
        }
        return java.nio.file.Paths.get(System.getProperty("user.home"), "propath_storage", "documentos").toString();
    }

    private DocumentoEstudiante mapearDocumento(ResultSet rs) throws SQLException {
        DocumentoEstudiante d = new DocumentoEstudiante();
        d.setId(rs.getInt("id_documento"));
        d.setIdEstudiante(rs.getInt("id_estudiante"));
        d.setTipoDocumento(rs.getString("tipo_documento"));
        d.setNombreArchivo(rs.getString("nombre_archivo"));
        d.setArchivoRuta(rs.getString("archivo_ruta"));
        d.setTamanioBytes(rs.getLong("tamanio_bytes"));
        d.setContentType(rs.getString("content_type"));
        try {
            java.sql.Timestamp ts = rs.getTimestamp("fecha_subida");
            if (ts != null) {
                d.setFechaSubida(ts.toString());
            }
        } catch (SQLException ignored) {
        }
        return d;
    }
}
