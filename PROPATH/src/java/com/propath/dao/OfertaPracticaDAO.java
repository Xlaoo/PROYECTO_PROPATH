package com.propath.dao;

import com.propath.conexion.ConexionBD;
import com.propath.modelo.OfertaPractica;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OfertaPracticaDAO {

    public List<OfertaPractica> listarTodas() {
        List<OfertaPractica> lista = new ArrayList<>();
        String sql = "SELECT o.id_oferta, o.id_empresa, o.titulo, o.descripcion, o.requisitos, o.carrera, "
                   + "o.modalidad, o.vacantes, o.fecha_publicacion, o.fecha_limite, o.estado, o.ubicacion, o.remuneracion, "
                   + "e.razon_social AS empresa "
                   + "FROM ofertas_practicas o "
                   + "INNER JOIN empresas e ON o.id_empresa = e.id_empresa "
                   + "ORDER BY o.id_oferta DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearOferta(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<OfertaPractica> listarActivas() {
        List<OfertaPractica> lista = new ArrayList<>();
        String sql = "SELECT o.id_oferta, o.id_empresa, o.titulo, o.descripcion, o.requisitos, o.carrera, "
                   + "o.modalidad, o.vacantes, o.fecha_publicacion, o.fecha_limite, o.estado, o.ubicacion, o.remuneracion, "
                   + "e.razon_social AS empresa "
                   + "FROM ofertas_practicas o "
                   + "INNER JOIN empresas e ON o.id_empresa = e.id_empresa "
                   + "WHERE o.estado = 'ACTIVA' AND e.estado = 'APROBADA' "
                   + "ORDER BY o.id_oferta DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearOferta(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<OfertaPractica> listarPorEmpresa(int idEmpresa) {
        List<OfertaPractica> lista = new ArrayList<>();
        String sql = "SELECT o.id_oferta, o.id_empresa, o.titulo, o.descripcion, o.requisitos, o.carrera, "
                   + "o.modalidad, o.vacantes, o.fecha_publicacion, o.fecha_limite, o.estado, o.ubicacion, o.remuneracion, "
                   + "e.razon_social AS empresa "
                   + "FROM ofertas_practicas o "
                   + "INNER JOIN empresas e ON o.id_empresa = e.id_empresa "
                   + "WHERE o.id_empresa = ? "
                   + "ORDER BY o.id_oferta DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearOferta(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public OfertaPractica obtenerPorId(int idOferta) {
        String sql = "SELECT o.id_oferta, o.id_empresa, o.titulo, o.descripcion, o.requisitos, o.carrera, "
                   + "o.modalidad, o.vacantes, o.fecha_publicacion, o.fecha_limite, o.estado, o.ubicacion, o.remuneracion, "
                   + "e.razon_social AS empresa "
                   + "FROM ofertas_practicas o "
                   + "INNER JOIN empresas e ON o.id_empresa = e.id_empresa "
                   + "WHERE o.id_oferta = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idOferta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearOferta(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int crear(OfertaPractica oferta) {
        if (oferta == null || oferta.getIdEmpresa() <= 0) {
            return -1;
        }

        String sql = "INSERT INTO ofertas_practicas (id_empresa, titulo, descripcion, requisitos, carrera, "
                   + "modalidad, vacantes, fecha_limite, estado, ubicacion, remuneracion) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, oferta.getIdEmpresa());
            ps.setString(2, oferta.getTitulo() != null ? oferta.getTitulo().trim() : "");
            ps.setString(3, oferta.getDescripcion() != null ? oferta.getDescripcion().trim() : "");
            ps.setString(4, oferta.getRequisitos() != null ? oferta.getRequisitos().trim() : "");
            ps.setString(5, oferta.getCarrera() != null ? oferta.getCarrera().trim() : "");
            ps.setString(6, normalizarModalidad(oferta.getModalidad()));
            ps.setInt(7, oferta.getVacantes() > 0 ? oferta.getVacantes() : 1);
            
            if (oferta.getFechaLimite() != null && !oferta.getFechaLimite().isEmpty()) {
                ps.setString(8, oferta.getFechaLimite().trim());
            } else {
                ps.setDate(8, new java.sql.Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000));
            }

            ps.setString(9, oferta.getEstado() != null ? oferta.getEstado().trim().toUpperCase() : "ACTIVA");
            ps.setString(10, oferta.getUbicacion() != null ? oferta.getUbicacion().trim() : "");
            ps.setString(11, oferta.getRemuneracion() != null ? oferta.getRemuneracion().trim() : "");

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int idGen = rs.getInt(1);
                        oferta.setId(idGen);
                        return idGen;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean actualizar(OfertaPractica oferta) {
        if (oferta == null || oferta.getId() <= 0) {
            return false;
        }

        String sql = "UPDATE ofertas_practicas SET titulo = ?, descripcion = ?, requisitos = ?, carrera = ?, "
                   + "modalidad = ?, vacantes = ?, fecha_limite = ?, estado = ?, ubicacion = ?, remuneracion = ? "
                   + "WHERE id_oferta = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, oferta.getTitulo() != null ? oferta.getTitulo().trim() : "");
            ps.setString(2, oferta.getDescripcion() != null ? oferta.getDescripcion().trim() : "");
            ps.setString(3, oferta.getRequisitos() != null ? oferta.getRequisitos().trim() : "");
            ps.setString(4, oferta.getCarrera() != null ? oferta.getCarrera().trim() : "");
            ps.setString(5, normalizarModalidad(oferta.getModalidad()));
            ps.setInt(6, oferta.getVacantes());
            ps.setString(7, oferta.getFechaLimite());
            ps.setString(8, oferta.getEstado() != null ? oferta.getEstado().trim().toUpperCase() : "ACTIVA");
            ps.setString(9, oferta.getUbicacion() != null ? oferta.getUbicacion().trim() : "");
            ps.setString(10, oferta.getRemuneracion() != null ? oferta.getRemuneracion().trim() : "");
            ps.setInt(11, oferta.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cerrarOferta(int idOferta) {
        if (idOferta <= 0) {
            return false;
        }

        String sql = "UPDATE ofertas_practicas SET estado = 'CERRADA' WHERE id_oferta = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idOferta);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarEstado(int idOferta, String nuevoEstado) {
        if (idOferta <= 0 || nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            return false;
        }
        String sql = "UPDATE ofertas_practicas SET estado = ? WHERE id_oferta = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.trim().toUpperCase());
            ps.setInt(2, idOferta);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(int idOferta) {
        if (idOferta <= 0) {
            return false;
        }

        String sql = "DELETE FROM ofertas_practicas WHERE id_oferta = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idOferta);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private OfertaPractica mapearOferta(ResultSet rs) throws SQLException {
        OfertaPractica o = new OfertaPractica();
        o.setId(rs.getInt("id_oferta"));
        o.setIdEmpresa(rs.getInt("id_empresa"));
        o.setTitulo(rs.getString("titulo"));
        o.setDescripcion(rs.getString("descripcion"));
        o.setRequisitos(rs.getString("requisitos"));
        o.setCarrera(rs.getString("carrera"));
        o.setModalidad(rs.getString("modalidad"));
        o.setVacantes(rs.getInt("vacantes"));
        try {
            java.sql.Timestamp ts = rs.getTimestamp("fecha_publicacion");
            if (ts != null) {
                o.setFechaPublicacion(ts.toString());
            }
        } catch (SQLException ignored) {
        }
        try {
            java.sql.Date d = rs.getDate("fecha_limite");
            if (d != null) {
                o.setFechaLimite(d.toString());
            }
        } catch (SQLException ignored) {
        }
        o.setEstado(rs.getString("estado"));
        o.setUbicacion(rs.getString("ubicacion"));
        o.setRemuneracion(rs.getString("remuneracion"));
        o.setEmpresa(rs.getString("empresa"));
        return o;
    }

    private String normalizarModalidad(String mod) {
        if (mod == null) return "PRESENCIAL";
        String m = mod.trim().toUpperCase();
        if (m.contains("VIRTUAL") || m.contains("REMOT")) return "VIRTUAL";
        if (m.contains("HIBRID")) return "HIBRIDA";
        return "PRESENCIAL";
    }
}
