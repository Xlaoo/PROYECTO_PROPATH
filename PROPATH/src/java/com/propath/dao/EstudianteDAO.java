package com.propath.dao;

import com.propath.conexion.ConexionBD;
import com.propath.conexion.SeguridadUtil;
import com.propath.modelo.Estudiante;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAO {

    public List<Estudiante> listarTodos() {
        List<Estudiante> lista = new ArrayList<>();
        String sql = "SELECT e.id_estudiante, e.id_usuario, e.nombres, e.apellidos, u.correo, "
                   + "e.universidad, e.carrera, e.ciclo, e.telefono, e.cv, u.estado "
                   + "FROM estudiantes e "
                   + "INNER JOIN usuarios u ON e.id_usuario = u.id_usuario "
                   + "ORDER BY e.id_estudiante DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearEstudiante(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Estudiante obtenerPorId(int idEstudiante) {
        String sql = "SELECT e.id_estudiante, e.id_usuario, e.nombres, e.apellidos, u.correo, "
                   + "e.universidad, e.carrera, e.ciclo, e.telefono, e.cv, u.estado "
                   + "FROM estudiantes e "
                   + "INNER JOIN usuarios u ON e.id_usuario = u.id_usuario "
                   + "WHERE e.id_estudiante = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEstudiante(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Estudiante obtenerPorUsuarioId(int idUsuario) {
        String sql = "SELECT e.id_estudiante, e.id_usuario, e.nombres, e.apellidos, u.correo, "
                   + "e.universidad, e.carrera, e.ciclo, e.telefono, e.cv, u.estado "
                   + "FROM estudiantes e "
                   + "INNER JOIN usuarios u ON e.id_usuario = u.id_usuario "
                   + "WHERE e.id_usuario = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEstudiante(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Estudiante obtenerPorCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT e.id_estudiante, e.id_usuario, e.nombres, e.apellidos, u.correo, "
                   + "e.universidad, e.carrera, e.ciclo, e.telefono, e.cv, u.estado "
                   + "FROM estudiantes e "
                   + "INNER JOIN usuarios u ON e.id_usuario = u.id_usuario "
                   + "WHERE u.correo = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, correo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEstudiante(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int crear(Estudiante estudiante) {
        if (estudiante == null || estudiante.getIdUsuario() <= 0) {
            return -1;
        }

        String sql = "INSERT INTO estudiantes (id_usuario, nombres, apellidos, universidad, carrera, ciclo, telefono, cv) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, estudiante.getIdUsuario());
            ps.setString(2, estudiante.getNombre() != null ? estudiante.getNombre().trim() : "");
            ps.setString(3, estudiante.getApellido() != null ? estudiante.getApellido().trim() : "");
            ps.setString(4, estudiante.getUniversidad() != null ? estudiante.getUniversidad().trim() : "");
            ps.setString(5, estudiante.getCarrera() != null ? estudiante.getCarrera().trim() : "");
            ps.setInt(6, estudiante.getCiclo());
            ps.setString(7, estudiante.getTelefono() != null ? estudiante.getTelefono().trim() : "");
            ps.setString(8, estudiante.getCv() != null ? estudiante.getCv().trim() : "");

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int idGen = rs.getInt(1);
                        estudiante.setId(idGen);
                        return idGen;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int registrarConUsuario(Estudiante estudiante, String correo, String contrasenaPlana) {
        if (estudiante == null || correo == null || contrasenaPlana == null) {
            return -1;
        }

        String hash = SeguridadUtil.encriptarPassword(contrasenaPlana);
        String sqlUsuario = "INSERT INTO usuarios (nombre, apellidos, correo, contrasena, rol, estado) VALUES (?, ?, ?, ?, 'ESTUDIANTE', 'ACTIVO')";
        String sqlEstudiante = "INSERT INTO estudiantes (id_usuario, nombres, apellidos, universidad, carrera, ciclo, telefono, cv) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);

            int idUsuarioGenerado = -1;
            try (PreparedStatement psUser = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, estudiante.getNombre() != null ? estudiante.getNombre().trim() : "");
                psUser.setString(2, estudiante.getApellido() != null ? estudiante.getApellido().trim() : "");
                psUser.setString(3, correo.trim());
                psUser.setString(4, hash);

                psUser.executeUpdate();
                try (ResultSet rsUser = psUser.getGeneratedKeys()) {
                    if (rsUser.next()) {
                        idUsuarioGenerado = rsUser.getInt(1);
                    }
                }
            }

            if (idUsuarioGenerado <= 0) {
                conn.rollback();
                return -1;
            }

            int idEstudianteGenerado = -1;
            try (PreparedStatement psEst = conn.prepareStatement(sqlEstudiante, Statement.RETURN_GENERATED_KEYS)) {
                psEst.setInt(1, idUsuarioGenerado);
                psEst.setString(2, estudiante.getNombre() != null ? estudiante.getNombre().trim() : "");
                psEst.setString(3, estudiante.getApellido() != null ? estudiante.getApellido().trim() : "");
                psEst.setString(4, estudiante.getUniversidad() != null ? estudiante.getUniversidad().trim() : "");
                psEst.setString(5, estudiante.getCarrera() != null ? estudiante.getCarrera().trim() : "");
                psEst.setInt(6, estudiante.getCiclo());
                psEst.setString(7, estudiante.getTelefono() != null ? estudiante.getTelefono().trim() : "");
                psEst.setString(8, estudiante.getCv() != null ? estudiante.getCv().trim() : "");

                psEst.executeUpdate();
                try (ResultSet rsEst = psEst.getGeneratedKeys()) {
                    if (rsEst.next()) {
                        idEstudianteGenerado = rsEst.getInt(1);
                    }
                }
            }

            if (idEstudianteGenerado > 0) {
                conn.commit();
                estudiante.setId(idEstudianteGenerado);
                estudiante.setIdUsuario(idUsuarioGenerado);
                return idEstudianteGenerado;
            } else {
                conn.rollback();
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
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
        return -1;
    }

    public boolean actualizar(Estudiante estudiante) {
        if (estudiante == null || estudiante.getId() <= 0) {
            return false;
        }

        String sqlEst = "UPDATE estudiantes SET nombres = ?, apellidos = ?, universidad = ?, carrera = ?, ciclo = ?, telefono = ?, cv = ? WHERE id_estudiante = ?";
        String sqlUser = "UPDATE usuarios SET nombre = ?, apellidos = ? WHERE id_usuario = ?";

        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlEst)) {
                ps.setString(1, estudiante.getNombre() != null ? estudiante.getNombre().trim() : "");
                ps.setString(2, estudiante.getApellido() != null ? estudiante.getApellido().trim() : "");
                ps.setString(3, estudiante.getUniversidad() != null ? estudiante.getUniversidad().trim() : "");
                ps.setString(4, estudiante.getCarrera() != null ? estudiante.getCarrera().trim() : "");
                ps.setInt(5, estudiante.getCiclo());
                ps.setString(6, estudiante.getTelefono() != null ? estudiante.getTelefono().trim() : "");
                ps.setString(7, estudiante.getCv() != null ? estudiante.getCv().trim() : "");
                ps.setInt(8, estudiante.getId());

                int filas = ps.executeUpdate();
                if (filas <= 0) {
                    conn.rollback();
                    return false;
                }
            }

            if (estudiante.getIdUsuario() > 0) {
                try (PreparedStatement psUser = conn.prepareStatement(sqlUser)) {
                    psUser.setString(1, estudiante.getNombre() != null ? estudiante.getNombre().trim() : "");
                    psUser.setString(2, estudiante.getApellido() != null ? estudiante.getApellido().trim() : "");
                    psUser.setInt(3, estudiante.getIdUsuario());
                    psUser.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
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

    public boolean darDeBaja(int idEstudiante) {
        if (idEstudiante <= 0) {
            return false;
        }

        String sql = "UPDATE usuarios u INNER JOIN estudiantes e ON u.id_usuario = e.id_usuario "
                   + "SET u.estado = 'INACTIVO' WHERE e.id_estudiante = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEstudiante);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarEstado(int idEstudiante, String nuevoEstado) {
        if (idEstudiante <= 0 || nuevoEstado == null) {
            return false;
        }

        String sql = "UPDATE usuarios u INNER JOIN estudiantes e ON u.id_usuario = e.id_usuario "
                   + "SET u.estado = ? WHERE e.id_estudiante = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado.trim().toUpperCase());
            ps.setInt(2, idEstudiante);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(int idEstudiante) {
        if (idEstudiante <= 0) {
            return false;
        }

        Estudiante est = obtenerPorId(idEstudiante);
        if (est == null) {
            return false;
        }

        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM estudiantes WHERE id_estudiante = ?")) {
                ps.setInt(1, idEstudiante);
                ps.executeUpdate();
            }

            if (est.getIdUsuario() > 0) {
                try (PreparedStatement psUser = conn.prepareStatement("DELETE FROM usuarios WHERE id_usuario = ?")) {
                    psUser.setInt(1, est.getIdUsuario());
                    psUser.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
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

    private Estudiante mapearEstudiante(ResultSet rs) throws SQLException {
        Estudiante e = new Estudiante();
        e.setId(rs.getInt("id_estudiante"));
        e.setIdUsuario(rs.getInt("id_usuario"));
        e.setNombre(rs.getString("nombres"));
        e.setApellido(rs.getString("apellidos"));
        e.setCorreo(rs.getString("correo"));
        e.setUniversidad(rs.getString("universidad"));
        e.setCarrera(rs.getString("carrera"));
        e.setCiclo(rs.getInt("ciclo"));
        e.setTelefono(rs.getString("telefono"));
        e.setCv(rs.getString("cv"));
        e.setEstado(rs.getString("estado"));
        return e;
    }
}
