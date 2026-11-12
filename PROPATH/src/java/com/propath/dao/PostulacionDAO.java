package com.propath.dao;

import com.propath.conexion.ConexionBD;
import com.propath.modelo.Postulacion;
import com.propath.modelo.RespuestaPostulacion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostulacionDAO {

    private static final String BASE_SELECT =
            "SELECT p.id_postulacion, p.id_estudiante, p.id_oferta, p.fecha_postulacion, p.estado, p.motivo_rechazo, "
            + "CONCAT(e.nombres, ' ', e.apellidos) AS estudiante, "
            + "e.carrera AS carrera_estudiante, e.universidad AS universidad_estudiante, "
            + "o.id_empresa, o.titulo AS oferta, emp.razon_social AS empresa, "
            + "EXISTS(SELECT 1 FROM documentos_estudiante d WHERE d.id_estudiante = e.id_estudiante AND d.tipo_documento = 'CV') AS cv_disponible "
            + "FROM postulaciones p "
            + "INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante "
            + "INNER JOIN ofertas_practicas o ON p.id_oferta = o.id_oferta "
            + "INNER JOIN empresas emp ON o.id_empresa = emp.id_empresa ";

    public List<Postulacion> listarTodas() {
        List<Postulacion> lista = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY p.id_postulacion DESC";

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
        String sql = BASE_SELECT + "WHERE p.id_estudiante = ? ORDER BY p.id_postulacion DESC";

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

    public List<Postulacion> listarPorEmpresa(int idEmpresa) {
        List<Postulacion> lista = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE o.id_empresa = ? ORDER BY p.id_postulacion DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpresa);
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
        String sql = BASE_SELECT + "WHERE p.id_oferta = ? ORDER BY p.id_postulacion DESC";

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
        String sql = BASE_SELECT + "WHERE p.id_postulacion = ?";

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

        } catch (SQLIntegrityConstraintViolationException dup) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            return -2;
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

    public java.util.Map<String, Integer> obtenerEstadisticasSeleccionEmpresa(int idEmpresa) {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        stats.put("totalPostulantes", 0);
        stats.put("postulados", 0);
        stats.put("enRevision", 0);
        stats.put("seleccionados", 0);
        stats.put("noSeleccionados", 0);
        stats.put("retirados", 0);

        String sql = "SELECT p.estado, COUNT(*) AS cantidad "
                   + "FROM postulaciones p "
                   + "INNER JOIN ofertas_practicas o ON p.id_oferta = o.id_oferta "
                   + "WHERE o.id_empresa = ? "
                   + "GROUP BY p.estado";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                int total = 0;
                while (rs.next()) {
                    String estado = rs.getString("estado");
                    int cant = rs.getInt("cantidad");
                    total += cant;

                    if ("POSTULADO".equalsIgnoreCase(estado)) {
                        stats.put("postulados", cant);
                    } else if ("EN_REVISION".equalsIgnoreCase(estado)) {
                        stats.put("enRevision", cant);
                    } else if ("SELECCIONADO".equalsIgnoreCase(estado)) {
                        stats.put("seleccionados", cant);
                    } else if ("NO_SELECCIONADO".equalsIgnoreCase(estado)) {
                        stats.put("noSeleccionados", cant);
                    } else if ("RETIRADO".equalsIgnoreCase(estado)) {
                        stats.put("retirados", cant);
                    }
                }
                stats.put("totalPostulantes", total);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
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
        try {
            p.setIdEmpresa(rs.getInt("id_empresa"));
            p.setCarreraEstudiante(rs.getString("carrera_estudiante"));
            p.setUniversidadEstudiante(rs.getString("universidad_estudiante"));
            p.setCvDisponible(rs.getBoolean("cv_disponible"));
        } catch (SQLException ignored) {
        }
        return p;
    }

    public Map<String, Object> obtenerEstadisticasEmpresa(int idEmpresa) {
        Map<String, Object> stats = new HashMap<>();
        long totalOfertas = 0, ofertasActivas = 0, ofertasCerradas = 0, ofertasPendientes = 0, ofertasRechazadas = 0, totalVacantes = 0;
        long totalPost = 0, postulados = 0, enRevision = 0, seleccionados = 0, noSeleccionados = 0;
        long totalEnt = 0, entProgramadas = 0, entRealizadas = 0, entCanceladas = 0;
        List<Map<String, Object>> resumenOfertas = new ArrayList<>();

        String sqlOfertas = "SELECT estado, COUNT(*) AS cant, COALESCE(SUM(vacantes), 0) AS vac FROM ofertas_practicas WHERE id_empresa = ? GROUP BY estado";
        String sqlPost = "SELECT p.estado, COUNT(*) AS cant FROM postulaciones p INNER JOIN ofertas_practicas o ON p.id_oferta = o.id_oferta WHERE o.id_empresa = ? GROUP BY p.estado";
        String sqlEnt = "SELECT en.estado, COUNT(*) AS cant FROM entrevistas en INNER JOIN postulaciones p ON en.id_postulacion = p.id_postulacion INNER JOIN ofertas_practicas o ON p.id_oferta = o.id_oferta WHERE o.id_empresa = ? GROUP BY en.estado";
        String sqlPorOferta =
                "SELECT o.id_oferta, o.titulo, o.estado, o.vacantes, o.fecha_limite, "
                + "COUNT(DISTINCT p.id_postulacion) AS total_post, "
                + "SUM(CASE WHEN p.estado = 'POSTULADO' THEN 1 ELSE 0 END) AS postulados, "
                + "SUM(CASE WHEN p.estado = 'EN_REVISION' THEN 1 ELSE 0 END) AS en_revision, "
                + "SUM(CASE WHEN p.estado = 'SELECCIONADO' THEN 1 ELSE 0 END) AS seleccionados, "
                + "SUM(CASE WHEN p.estado = 'NO_SELECCIONADO' THEN 1 ELSE 0 END) AS no_seleccionados, "
                + "(SELECT COUNT(*) FROM entrevistas en2 INNER JOIN postulaciones p2 ON en2.id_postulacion = p2.id_postulacion WHERE p2.id_oferta = o.id_oferta AND en2.estado = 'PROGRAMADA') AS ent_programadas, "
                + "(SELECT COUNT(*) FROM entrevistas en3 INNER JOIN postulaciones p3 ON en3.id_postulacion = p3.id_postulacion WHERE p3.id_oferta = o.id_oferta AND en3.estado = 'REALIZADA') AS ent_realizadas "
                + "FROM ofertas_practicas o "
                + "LEFT JOIN postulaciones p ON p.id_oferta = o.id_oferta "
                + "WHERE o.id_empresa = ? "
                + "GROUP BY o.id_oferta, o.titulo, o.estado, o.vacantes, o.fecha_limite "
                + "ORDER BY o.id_oferta DESC";

        try (Connection conn = ConexionBD.getConexion()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlOfertas)) {
                ps.setInt(1, idEmpresa);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String est = rs.getString("estado");
                        long c = rs.getLong("cant");
                        long v = rs.getLong("vac");
                        totalOfertas += c;
                        if ("ACTIVA".equalsIgnoreCase(est)) {
                            ofertasActivas += c;
                            totalVacantes += v;
                        } else if ("CERRADA".equalsIgnoreCase(est) || "INACTIVA".equalsIgnoreCase(est)) {
                            ofertasCerradas += c;
                        } else if ("PENDIENTE".equalsIgnoreCase(est)) {
                            ofertasPendientes += c;
                        } else if ("RECHAZADA".equalsIgnoreCase(est)) {
                            ofertasRechazadas += c;
                        }
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlPost)) {
                ps.setInt(1, idEmpresa);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String est = rs.getString("estado");
                        long c = rs.getLong("cant");
                        totalPost += c;
                        if ("POSTULADO".equalsIgnoreCase(est)) postulados += c;
                        else if ("EN_REVISION".equalsIgnoreCase(est)) enRevision += c;
                        else if ("SELECCIONADO".equalsIgnoreCase(est)) seleccionados += c;
                        else if ("NO_SELECCIONADO".equalsIgnoreCase(est)) noSeleccionados += c;
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlEnt)) {
                ps.setInt(1, idEmpresa);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String est = rs.getString("estado");
                        long c = rs.getLong("cant");
                        totalEnt += c;
                        if ("PROGRAMADA".equalsIgnoreCase(est)) entProgramadas += c;
                        else if ("REALIZADA".equalsIgnoreCase(est)) entRealizadas += c;
                        else if ("CANCELADA".equalsIgnoreCase(est)) entCanceladas += c;
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlPorOferta)) {
                ps.setInt(1, idEmpresa);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("idOferta", rs.getInt("id_oferta"));
                        row.put("titulo", rs.getString("titulo"));
                        row.put("estado", rs.getString("estado"));
                        row.put("vacantes", rs.getInt("vacantes"));
                        row.put("fechaLimite", rs.getDate("fecha_limite") != null ? rs.getDate("fecha_limite").toString() : "—");
                        row.put("totalPostulaciones", rs.getLong("total_post"));
                        row.put("postulados", rs.getLong("postulados"));
                        row.put("enRevision", rs.getLong("en_revision"));
                        row.put("seleccionados", rs.getLong("seleccionados"));
                        row.put("noSeleccionados", rs.getLong("no_seleccionados"));
                        row.put("entProgramadas", rs.getLong("ent_programadas"));
                        row.put("entRealizadas", rs.getLong("ent_realizadas"));
                        resumenOfertas.add(row);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        double tasaSeleccion = totalPost > 0 ? Math.round((seleccionados * 1000.0) / totalPost) / 10.0 : 0.0;
        double tasaRevision = totalPost > 0 ? Math.round(((enRevision + seleccionados + noSeleccionados) * 1000.0) / totalPost) / 10.0 : 0.0;
        double tasaEntRealizada = (entProgramadas + entRealizadas) > 0 ? Math.round((entRealizadas * 1000.0) / (entProgramadas + entRealizadas)) / 10.0 : 0.0;

        stats.put("totalOfertas", totalOfertas);
        stats.put("ofertasActivas", ofertasActivas);
        stats.put("ofertasCerradas", ofertasCerradas);
        stats.put("ofertasPendientes", ofertasPendientes);
        stats.put("ofertasRechazadas", ofertasRechazadas);
        stats.put("totalVacantesActivas", totalVacantes);
        stats.put("totalPostulaciones", totalPost);
        stats.put("postulados", postulados);
        stats.put("enRevision", enRevision);
        stats.put("seleccionados", seleccionados);
        stats.put("noSeleccionados", noSeleccionados);
        stats.put("tasaSeleccion", tasaSeleccion);
        stats.put("tasaRevision", tasaRevision);
        stats.put("totalEntrevistas", totalEnt);
        stats.put("entrevistasProgramadas", entProgramadas);
        stats.put("entrevistasRealizadas", entRealizadas);
        stats.put("entrevistasCanceladas", entCanceladas);
        stats.put("tasaEntrevistaRealizada", tasaEntRealizada);
        stats.put("resumenOfertas", resumenOfertas);
        return stats;
    }

    public Map<String, Object> obtenerEstadisticasEstudiante(int idEstudiante) {
        Map<String, Object> stats = new HashMap<>();
        long totalPost = 0, postulados = 0, enRevision = 0, seleccionados = 0, noSeleccionados = 0;
        long totalEnt = 0, entProgramadas = 0, entRealizadas = 0, entCanceladas = 0;
        long totalDocs = 0;

        String sqlPost = "SELECT estado, COUNT(*) AS cant FROM postulaciones WHERE id_estudiante = ? GROUP BY estado";
        String sqlEnt = "SELECT en.estado, COUNT(*) AS cant FROM entrevistas en INNER JOIN postulaciones p ON en.id_postulacion = p.id_postulacion WHERE p.id_estudiante = ? GROUP BY en.estado";
        String sqlDocs = "SELECT COUNT(*) FROM documentos_estudiante WHERE id_estudiante = ?";

        try (Connection conn = ConexionBD.getConexion()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlPost)) {
                ps.setInt(1, idEstudiante);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String est = rs.getString("estado");
                        long c = rs.getLong("cant");
                        totalPost += c;
                        if ("POSTULADO".equalsIgnoreCase(est)) postulados += c;
                        else if ("EN_REVISION".equalsIgnoreCase(est)) enRevision += c;
                        else if ("SELECCIONADO".equalsIgnoreCase(est)) seleccionados += c;
                        else if ("NO_SELECCIONADO".equalsIgnoreCase(est)) noSeleccionados += c;
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlEnt)) {
                ps.setInt(1, idEstudiante);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String est = rs.getString("estado");
                        long c = rs.getLong("cant");
                        totalEnt += c;
                        if ("PROGRAMADA".equalsIgnoreCase(est)) entProgramadas += c;
                        else if ("REALIZADA".equalsIgnoreCase(est)) entRealizadas += c;
                        else if ("CANCELADA".equalsIgnoreCase(est)) entCanceladas += c;
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlDocs)) {
                ps.setInt(1, idEstudiante);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) totalDocs = rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        double tasaRespuesta = totalPost > 0 ? Math.round(((enRevision + seleccionados + noSeleccionados) * 1000.0) / totalPost) / 10.0 : 0.0;
        double tasaSeleccion = totalPost > 0 ? Math.round((seleccionados * 1000.0) / totalPost) / 10.0 : 0.0;

        stats.put("totalPostulaciones", totalPost);
        stats.put("postulados", postulados);
        stats.put("enRevision", enRevision);
        stats.put("seleccionados", seleccionados);
        stats.put("noSeleccionados", noSeleccionados);
        stats.put("tasaRespuesta", tasaRespuesta);
        stats.put("tasaSeleccion", tasaSeleccion);
        stats.put("totalEntrevistas", totalEnt);
        stats.put("entrevistasProgramadas", entProgramadas);
        stats.put("entrevistasRealizadas", entRealizadas);
        stats.put("entrevistasCanceladas", entCanceladas);
        stats.put("totalDocumentos", totalDocs);
        return stats;
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
