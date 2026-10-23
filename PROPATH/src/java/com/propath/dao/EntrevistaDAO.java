package com.propath.dao;

import com.propath.conexion.ConexionBD;
import com.propath.modelo.Entrevista;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EntrevistaDAO {

    public List<Entrevista> listarTodas() {
        List<Entrevista> lista = new ArrayList<>();
        String sql = "SELECT en.id_entrevista, en.id_postulacion, en.fecha, en.hora, en.modalidad, "
                   + "en.enlace_lugar, en.estado, en.observaciones, en.fecha_creacion, "
                   + "CONCAT(est.nombres, ' ', est.apellidos) AS estudiante, "
                   + "o.titulo AS oferta, "
                   + "emp.razon_social AS empresa "
                   + "FROM entrevistas en "
                   + "INNER JOIN postulaciones p ON en.id_postulacion = p.id_postulacion "
                   + "INNER JOIN estudiantes est ON p.id_estudiante = est.id_estudiante "
                   + "INNER JOIN ofertas_practicas o ON p.id_oferta = o.id_oferta "
                   + "INNER JOIN empresas emp ON o.id_empresa = emp.id_empresa "
                   + "ORDER BY en.fecha DESC, en.hora DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearEntrevista(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Entrevista> listarPorEmpresa(int idEmpresa) {
        List<Entrevista> lista = new ArrayList<>();
        String sql = "SELECT en.id_entrevista, en.id_postulacion, en.fecha, en.hora, en.modalidad, "
                   + "en.enlace_lugar, en.estado, en.observaciones, en.fecha_creacion, "
                   + "CONCAT(est.nombres, ' ', est.apellidos) AS estudiante, "
                   + "o.titulo AS oferta, "
                   + "emp.razon_social AS empresa "
                   + "FROM entrevistas en "
                   + "INNER JOIN postulaciones p ON en.id_postulacion = p.id_postulacion "
                   + "INNER JOIN estudiantes est ON p.id_estudiante = est.id_estudiante "
                   + "INNER JOIN ofertas_practicas o ON p.id_oferta = o.id_oferta "
                   + "INNER JOIN empresas emp ON o.id_empresa = emp.id_empresa "
                   + "WHERE emp.id_empresa = ? "
                   + "ORDER BY en.fecha DESC, en.hora DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearEntrevista(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Entrevista> listarPorEstudiante(int idEstudiante) {
        List<Entrevista> lista = new ArrayList<>();
        String sql = "SELECT en.id_entrevista, en.id_postulacion, en.fecha, en.hora, en.modalidad, "
                   + "en.enlace_lugar, en.estado, en.observaciones, en.fecha_creacion, "
                   + "CONCAT(est.nombres, ' ', est.apellidos) AS estudiante, "
                   + "o.titulo AS oferta, "
                   + "emp.razon_social AS empresa "
                   + "FROM entrevistas en "
                   + "INNER JOIN postulaciones p ON en.id_postulacion = p.id_postulacion "
                   + "INNER JOIN estudiantes est ON p.id_estudiante = est.id_estudiante "
                   + "INNER JOIN ofertas_practicas o ON p.id_oferta = o.id_oferta "
                   + "INNER JOIN empresas emp ON o.id_empresa = emp.id_empresa "
                   + "WHERE est.id_estudiante = ? "
                   + "ORDER BY en.fecha DESC, en.hora DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearEntrevista(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Entrevista> listarPorPostulacion(int idPostulacion) {
        List<Entrevista> lista = new ArrayList<>();
        String sql = "SELECT en.id_entrevista, en.id_postulacion, en.fecha, en.hora, en.modalidad, "
                   + "en.enlace_lugar, en.estado, en.observaciones, en.fecha_creacion, "
                   + "CONCAT(est.nombres, ' ', est.apellidos) AS estudiante, "
                   + "o.titulo AS oferta, "
                   + "emp.razon_social AS empresa "
                   + "FROM entrevistas en "
                   + "INNER JOIN postulaciones p ON en.id_postulacion = p.id_postulacion "
                   + "INNER JOIN estudiantes est ON p.id_estudiante = est.id_estudiante "
                   + "INNER JOIN ofertas_practicas o ON p.id_oferta = o.id_oferta "
                   + "INNER JOIN empresas emp ON o.id_empresa = emp.id_empresa "
                   + "WHERE en.id_postulacion = ? "
                   + "ORDER BY en.fecha DESC, en.hora DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPostulacion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearEntrevista(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Entrevista obtenerPorId(int idEntrevista) {
        String sql = "SELECT en.id_entrevista, en.id_postulacion, en.fecha, en.hora, en.modalidad, "
                   + "en.enlace_lugar, en.estado, en.observaciones, en.fecha_creacion, "
                   + "CONCAT(est.nombres, ' ', est.apellidos) AS estudiante, "
                   + "o.titulo AS oferta, "
                   + "emp.razon_social AS empresa "
                   + "FROM entrevistas en "
                   + "INNER JOIN postulaciones p ON en.id_postulacion = p.id_postulacion "
                   + "INNER JOIN estudiantes est ON p.id_estudiante = est.id_estudiante "
                   + "INNER JOIN ofertas_practicas o ON p.id_oferta = o.id_oferta "
                   + "INNER JOIN empresas emp ON o.id_empresa = emp.id_empresa "
                   + "WHERE en.id_entrevista = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEntrevista);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEntrevista(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int crear(Entrevista entrevista) {
        if (entrevista == null || entrevista.getIdPostulacion() <= 0) {
            return -1;
        }

        String sql = "INSERT INTO entrevistas (id_postulacion, fecha, hora, modalidad, enlace_lugar, estado, observaciones) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, entrevista.getIdPostulacion());
            ps.setString(2, entrevista.getFecha() != null ? entrevista.getFecha().trim() : "");
            ps.setString(3, formatHora(entrevista.getHora()));
            ps.setString(4, normalizarModalidad(entrevista.getModalidad()));
            ps.setString(5, entrevista.getEnlaceLugar() != null ? entrevista.getEnlaceLugar().trim() : "");
            ps.setString(6, normalizarEstado(entrevista.getEstado()));
            ps.setString(7, entrevista.getObservaciones() != null ? entrevista.getObservaciones().trim() : "");

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int idGen = rs.getInt(1);
                        entrevista.setId(idGen);
                        return idGen;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean actualizar(Entrevista entrevista) {
        if (entrevista == null || entrevista.getId() <= 0) {
            return false;
        }

        String sql = "UPDATE entrevistas SET fecha = ?, hora = ?, modalidad = ?, enlace_lugar = ?, estado = ?, observaciones = ? "
                   + "WHERE id_entrevista = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, entrevista.getFecha());
            ps.setString(2, formatHora(entrevista.getHora()));
            ps.setString(3, normalizarModalidad(entrevista.getModalidad()));
            ps.setString(4, entrevista.getEnlaceLugar() != null ? entrevista.getEnlaceLugar().trim() : "");
            ps.setString(5, normalizarEstado(entrevista.getEstado()));
            ps.setString(6, entrevista.getObservaciones() != null ? entrevista.getObservaciones().trim() : "");
            ps.setInt(7, entrevista.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cancelar(int idEntrevista) {
        if (idEntrevista <= 0) {
            return false;
        }

        String sql = "UPDATE entrevistas SET estado = 'CANCELADA' WHERE id_entrevista = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEntrevista);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(int idEntrevista) {
        if (idEntrevista <= 0) {
            return false;
        }

        String sql = "DELETE FROM entrevistas WHERE id_entrevista = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEntrevista);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Entrevista mapearEntrevista(ResultSet rs) throws SQLException {
        Entrevista en = new Entrevista();
        en.setId(rs.getInt("id_entrevista"));
        en.setIdPostulacion(rs.getInt("id_postulacion"));
        try {
            java.sql.Date d = rs.getDate("fecha");
            if (d != null) {
                en.setFecha(d.toString());
            }
        } catch (SQLException ignored) {
        }
        try {
            java.sql.Time t = rs.getTime("hora");
            if (t != null) {
                en.setHora(t.toString());
            }
        } catch (SQLException ignored) {
        }
        en.setModalidad(rs.getString("modalidad"));
        en.setEnlaceLugar(rs.getString("enlace_lugar"));
        en.setEstado(rs.getString("estado"));
        en.setObservaciones(rs.getString("observaciones"));
        try {
            java.sql.Timestamp ts = rs.getTimestamp("fecha_creacion");
            if (ts != null) {
                en.setFechaCreacion(ts.toString());
            }
        } catch (SQLException ignored) {
        }
        en.setEstudiante(rs.getString("estudiante"));
        en.setOferta(rs.getString("oferta"));
        en.setEmpresa(rs.getString("empresa"));
        return en;
    }

    private String normalizarModalidad(String mod) {
        if (mod == null) return "VIRTUAL";
        String m = mod.trim().toUpperCase();
        if (m.contains("PRESENCIAL")) return "PRESENCIAL";
        return "VIRTUAL";
    }

    private String formatHora(String h) {
        if (h == null || h.isEmpty()) return "10:00:00";
        String s = h.trim();
        if (s.length() == 5) {
            return s + ":00";
        }
        return s;
    }

    private String normalizarEstado(String estado) {
        if (estado == null) return "PROGRAMADA";
        String e = estado.trim().toUpperCase();
        if (e.contains("CANCELAD")) return "CANCELADA";
        if (e.contains("REALIZAD") || e.contains("COMPLETAD")) return "REALIZADA";
        return "PROGRAMADA";
    }
}
