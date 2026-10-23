package com.propath.dao;

import com.propath.conexion.ConexionBD;
import com.propath.modelo.Postulacion;
import com.propath.modelo.RespuestaPostulacion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PostulacionDAO {

    public List<Postulacion> listarTodas() {
        List<Postulacion> lista = new ArrayList<>();
        String sql = "SELECT p.id_postulacion, p.id_estudiante, p.id_oferta, p.fecha_postulacion, p.estado, p.motivo_rechazo, "
                   + "CONCAT(e.nombres, ' ', e.apellidos) AS estudiante, "
                   + "o.titulo AS oferta, "
                   + "emp.razon_social AS empresa "
                   + "FROM postulaciones p "
                   + "INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante "
                   + "INNER JOIN ofertas_practicas o ON p.id_oferta = o.id_oferta "
                   + "INNER JOIN empresas emp ON o.id_empresa = emp.id_empresa "
                   + "ORDER BY p.id_postulacion DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearPostulacion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Postulacion> listarPorEstudiante(int idEstudiante) {
        List<Postulacion> lista = new ArrayList<>();
        String sql = "SELECT p.id_postulacion, p.id_estudiante, p.id_oferta, p.fecha_postulacion, p.estado, p.motivo_rechazo, "
                   + "CONCAT(e.nombres, ' ', e.apellidos) AS estudiante, "
                   + "o.titulo AS oferta, "
                   + "emp.razon_social AS empresa "
                   + "FROM postulaciones p "
                   + "INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante "
                   + "INNER JOIN ofertas_practicas o ON p.id_oferta = o.id_oferta "
                   + "INNER JOIN empresas emp ON o.id_empresa = emp.id_empresa "
                   + "WHERE p.id_estudiante = ? "
                   + "ORDER BY p.id_postulacion DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearPostulacion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Postulacion> listarPorOferta(int idOferta) {
        List<Postulacion> lista = new ArrayList<>();
        String sql = "SELECT p.id_postulacion, p.id_estudiante, p.id_oferta, p.fecha_postulacion, p.estado, p.motivo_rechazo, "
                   + "CONCAT(e.nombres, ' ', e.apellidos) AS estudiante, "
                   + "o.titulo AS oferta, "
                   + "emp.razon_social AS empresa "
                   + "FROM postulaciones p "
                   + "INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante "
                   + "INNER JOIN ofertas_practicas o ON p.id_oferta = o.id_oferta "
                   + "INNER JOIN empresas emp ON o.id_empresa = emp.id_empresa "
                   + "WHERE p.id_oferta = ? "
                   + "ORDER BY p.id_postulacion DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idOferta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearPostulacion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Postulacion obtenerPorId(int idPostulacion) {
        String sql = "SELECT p.id_postulacion, p.id_estudiante, p.id_oferta, p.fecha_postulacion, p.estado, p.motivo_rechazo, "
                   + "CONCAT(e.nombres, ' ', e.apellidos) AS estudiante, "
                   + "o.titulo AS oferta, "
                   + "emp.razon_social AS empresa "
                   + "FROM postulaciones p "
                   + "INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante "
                   + "INNER JOIN ofertas_practicas o ON p.id_oferta = o.id_oferta "
                   + "INNER JOIN empresas emp ON o.id_empresa = emp.id_empresa "
                   + "WHERE p.id_postulacion = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPostulacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPostulacion(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean yaPostulo(int idEstudiante, int idOferta) {
        String sql = "SELECT 1 FROM postulaciones WHERE id_estudiante = ? AND id_oferta = ? LIMIT 1";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEstudiante);
            ps.setInt(2, idOferta);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int registrarConRespuestas(int idEstudiante, int idOferta, List<RespuestaPostulacion> respuestas) {
        if (idEstudiante <= 0 || idOferta <= 0) {
            return -1;
        }

        String sqlPostulacion = "INSERT INTO postulaciones (id_estudiante, id_oferta, estado) VALUES (?, ?, 'POSTULADO')";
        String sqlRespuesta = "INSERT INTO respuestas_postulacion (id_postulacion, numero_pregunta, pregunta, respuesta) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);

            int idPostulacionGenerada = -1;
            try (PreparedStatement psPost = conn.prepareStatement(sqlPostulacion, Statement.RETURN_GENERATED_KEYS)) {
                psPost.setInt(1, idEstudiante);
                psPost.setInt(2, idOferta);
                psPost.executeUpdate();

                try (ResultSet rs = psPost.getGeneratedKeys()) {
                    if (rs.next()) {
                        idPostulacionGenerada = rs.getInt(1);
                    }
                }
            }

            if (idPostulacionGenerada <= 0) {
                conn.rollback();
                return -1;
            }

            if (respuestas != null && !respuestas.isEmpty()) {
                try (PreparedStatement psResp = conn.prepareStatement(sqlRespuesta)) {
                    for (RespuestaPostulacion r : respuestas) {
                        psResp.setInt(1, idPostulacionGenerada);
                        psResp.setInt(2, r.getNumeroPregunta());
                        psResp.setString(3, r.getPregunta() != null ? r.getPregunta() : "");
                        psResp.setString(4, r.getRespuesta() != null ? r.getRespuesta() : "");
                        psResp.addBatch();
                    }
                    psResp.executeBatch();
                }
            }

            conn.commit();
            return idPostulacionGenerada;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean actualizarEstado(int idPostulacion, String nuevoEstado) {
        if (idPostulacion <= 0 || nuevoEstado == null) {
            return false;
        }

        String sql = "UPDATE postulaciones SET estado = ? WHERE id_postulacion = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, normalizarEstado(nuevoEstado));
            ps.setInt(2, idPostulacion);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean evaluar(int idPostulacion, String nuevoEstado, String comentario) {
        if (idPostulacion <= 0 || nuevoEstado == null) {
            return false;
        }

        String sql = "UPDATE postulaciones SET estado = ?, motivo_rechazo = ? WHERE id_postulacion = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, normalizarEstado(nuevoEstado));
            ps.setString(2, comentario != null ? comentario.trim() : null);
            ps.setInt(3, idPostulacion);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean rechazar(int idPostulacion, String motivo) {
        return evaluar(idPostulacion, "NO_SELECCIONADO", motivo);
    }

    public boolean evaluarPostulacion(int idPostulacion, String nuevoEstado, String motivo, Integer puntuacion) {
        return evaluar(idPostulacion, nuevoEstado, motivo);
    }

    public boolean existePostulacion(int idEstudiante, int idOferta) {
        return yaPostulo(idEstudiante, idOferta);
    }

    public int crearConRespuestas(Postulacion post, String[] respuestas) {
        if (post == null) {
            return -1;
        }
        List<RespuestaPostulacion> lista = new ArrayList<>();
        if (respuestas != null) {
            for (int i = 0; i < respuestas.length; i++) {
                RespuestaPostulacion r = new RespuestaPostulacion();
                r.setNumeroPregunta(i + 1);
                r.setPregunta("Pregunta " + (i + 1));
                r.setRespuesta(respuestas[i] != null ? respuestas[i] : "");
                lista.add(r);
            }
        }
        return registrarConRespuestas(post.getIdEstudiante(), post.getIdOferta(), lista);
    }

    public boolean guardarRespuestas(int idPostulacion, String[] respuestas) {
        return actualizarRespuestas(idPostulacion, respuestas);
    }

    public boolean eliminar(int idPostulacion) {
        if (idPostulacion <= 0) {
            return false;
        }
        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);
            try (PreparedStatement psResp = conn.prepareStatement("DELETE FROM respuestas_postulacion WHERE id_postulacion = ?")) {
                psResp.setInt(1, idPostulacion);
                psResp.executeUpdate();
            }
            try (PreparedStatement psEnt = conn.prepareStatement("DELETE FROM entrevistas WHERE id_postulacion = ?")) {
                psEnt.setInt(1, idPostulacion);
                psEnt.executeUpdate();
            }
            boolean ok;
            try (PreparedStatement psPost = conn.prepareStatement("DELETE FROM postulaciones WHERE id_postulacion = ?")) {
                psPost.setInt(1, idPostulacion);
                ok = psPost.executeUpdate() > 0;
            }
            conn.commit();
            return ok;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public boolean retirar(int idPostulacion) {
        return eliminar(idPostulacion);
    }

    public boolean actualizarRespuestas(int idPostulacion, String[] respuestas) {
        if (idPostulacion <= 0 || respuestas == null || respuestas.length == 0) {
            return false;
        }
        String sqlDelete = "DELETE FROM respuestas_postulacion WHERE id_postulacion = ?";
        String sqlInsert = "INSERT INTO respuestas_postulacion (id_postulacion, numero_pregunta, pregunta, respuesta) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);
            try (PreparedStatement psDel = conn.prepareStatement(sqlDelete)) {
                psDel.setInt(1, idPostulacion);
                psDel.executeUpdate();
            }
            try (PreparedStatement psIns = conn.prepareStatement(sqlInsert)) {
                for (int i = 0; i < respuestas.length; i++) {
                    psIns.setInt(1, idPostulacion);
                    psIns.setInt(2, i + 1);
                    psIns.setString(3, "Pregunta " + (i + 1));
                    psIns.setString(4, respuestas[i] != null ? respuestas[i] : "");
                    psIns.addBatch();
                }
                psIns.executeBatch();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public List<RespuestaPostulacion> obtenerRespuestas(int idPostulacion) {
        List<RespuestaPostulacion> lista = new ArrayList<>();
        String sql = "SELECT id_respuesta, id_postulacion, numero_pregunta, pregunta, respuesta "
                   + "FROM respuestas_postulacion WHERE id_postulacion = ? ORDER BY numero_pregunta ASC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPostulacion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RespuestaPostulacion r = new RespuestaPostulacion();
                    r.setId(rs.getInt("id_respuesta"));
                    r.setIdPostulacion(rs.getInt("id_postulacion"));
                    r.setNumeroPregunta(rs.getInt("numero_pregunta"));
                    r.setPregunta(rs.getString("pregunta"));
                    r.setRespuesta(rs.getString("respuesta"));
                    lista.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private Postulacion mapearPostulacion(ResultSet rs) throws SQLException {
        Postulacion p = new Postulacion();
        p.setId(rs.getInt("id_postulacion"));
        p.setIdEstudiante(rs.getInt("id_estudiante"));
        p.setIdOferta(rs.getInt("id_oferta"));
        p.setEstudiante(rs.getString("estudiante"));
        p.setOferta(rs.getString("oferta"));
        p.setEmpresa(rs.getString("empresa"));
        try {
            java.sql.Timestamp ts = rs.getTimestamp("fecha_postulacion");
            if (ts != null) {
                p.setFechaPostulacion(ts.toString());
            }
        } catch (SQLException ignored) {
        }
        p.setEstado(rs.getString("estado"));
        p.setMotivoRechazo(rs.getString("motivo_rechazo"));
        return p;
    }

    private String normalizarEstado(String estado) {
        if (estado == null) return "POSTULADO";
        String e = estado.trim().toUpperCase();
        if (e.contains("PENDIENTE") || e.contains("POSTULADO")) return "POSTULADO";
        if (e.contains("REVISION") || e.contains("EVALUACION")) return "EN_REVISION";
        if (e.contains("ACEPTAD") || e.contains("SELECCIONADO") || e.contains("APROBAD")) return "SELECCIONADO";
        if (e.contains("RECHAZAD") || e.contains("NO_SELECCIONADO")) return "NO_SELECCIONADO";
        if (e.contains("RETIRAD") || e.contains("CANCELAD")) return "RETIRADO";
        return "POSTULADO";
    }
}
