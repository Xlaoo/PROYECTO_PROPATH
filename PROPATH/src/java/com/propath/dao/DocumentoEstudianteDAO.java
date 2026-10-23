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

        String storageDir = System.getProperty("PROPATH_STORAGE_DIR", "D:\\PROYECTO_PROPATH\\PROPATH\\storage\\documentos");
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
        String storageDir = System.getProperty("PROPATH_STORAGE_DIR", "D:\\PROYECTO_PROPATH\\PROPATH\\storage\\documentos");
        java.io.File file = new java.io.File(storageDir, doc.getArchivoRuta());
        if (!file.exists()) {
            return null;
        }
        try {
            return java.nio.file.Files.readAllBytes(file.toPath());
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] descargarCv(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            return null;
        }
        String storageDir = System.getProperty("PROPATH_STORAGE_DIR", "D:\\PROYECTO_PROPATH\\PROPATH\\storage\\documentos");
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
        String storageDir = System.getProperty("PROPATH_STORAGE_DIR", "D:\\PROYECTO_PROPATH\\PROPATH\\storage\\documentos");
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
