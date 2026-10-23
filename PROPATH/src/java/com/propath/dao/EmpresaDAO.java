package com.propath.dao;

import com.propath.conexion.ConexionBD;
import com.propath.conexion.SeguridadUtil;
import com.propath.modelo.Empresa;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EmpresaDAO {

    public List<Empresa> listarTodas() {
        List<Empresa> lista = new ArrayList<>();
        String sql = "SELECT e.id_empresa, e.id_usuario, e.razon_social, e.ruc, e.telefono, e.direccion, e.descripcion, e.estado, "
                   + "u.correo, u.nombre, u.apellidos, u.estado AS estado_cuenta "
                   + "FROM empresas e "
                   + "INNER JOIN usuarios u ON e.id_usuario = u.id_usuario "
                   + "ORDER BY e.id_empresa DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearEmpresa(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Empresa obtenerPorId(int idEmpresa) {
        String sql = "SELECT e.id_empresa, e.id_usuario, e.razon_social, e.ruc, e.telefono, e.direccion, e.descripcion, e.estado, "
                   + "u.correo, u.nombre, u.apellidos, u.estado AS estado_cuenta "
                   + "FROM empresas e "
                   + "INNER JOIN usuarios u ON e.id_usuario = u.id_usuario "
                   + "WHERE e.id_empresa = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEmpresa(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Empresa obtenerPorUsuarioId(int idUsuario) {
        String sql = "SELECT e.id_empresa, e.id_usuario, e.razon_social, e.ruc, e.telefono, e.direccion, e.descripcion, e.estado, "
                   + "u.correo, u.nombre, u.apellidos, u.estado AS estado_cuenta "
                   + "FROM empresas e "
                   + "INNER JOIN usuarios u ON e.id_usuario = u.id_usuario "
                   + "WHERE e.id_usuario = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEmpresa(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Empresa obtenerPorCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT e.id_empresa, e.id_usuario, e.razon_social, e.ruc, e.telefono, e.direccion, e.descripcion, e.estado, "
                   + "u.correo, u.nombre, u.apellidos, u.estado AS estado_cuenta "
                   + "FROM empresas e "
                   + "INNER JOIN usuarios u ON e.id_usuario = u.id_usuario "
                   + "WHERE u.correo = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, correo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEmpresa(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Empresa obtenerPorRuc(String ruc) {
        if (ruc == null || ruc.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT e.id_empresa, e.id_usuario, e.razon_social, e.ruc, e.telefono, e.direccion, e.descripcion, e.estado, "
                   + "u.correo, u.nombre, u.apellidos, u.estado AS estado_cuenta "
                   + "FROM empresas e "
                   + "INNER JOIN usuarios u ON e.id_usuario = u.id_usuario "
                   + "WHERE e.ruc = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ruc.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEmpresa(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int crear(Empresa empresa) {
        if (empresa == null || empresa.getIdUsuario() <= 0) {
            return -1;
        }

        String sql = "INSERT INTO empresas (id_usuario, razon_social, ruc, telefono, direccion, descripcion, estado) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, empresa.getIdUsuario());
            ps.setString(2, empresa.getRazonSocial() != null ? empresa.getRazonSocial().trim() : "");
            ps.setString(3, empresa.getRuc() != null ? empresa.getRuc().trim() : "");
            ps.setString(4, empresa.getTelefono() != null ? empresa.getTelefono().trim() : "");
            ps.setString(5, empresa.getDireccion() != null ? empresa.getDireccion().trim() : "");
            ps.setString(6, empresa.getDescripcion() != null ? empresa.getDescripcion().trim() : "");
            ps.setString(7, empresa.getEstado() != null ? empresa.getEstado().trim().toUpperCase() : "PENDIENTE");

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int idGen = rs.getInt(1);
                        empresa.setId(idGen);
                        return idGen;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int registrarConUsuario(Empresa empresa, String correo, String contrasenaPlana, String repNombre, String repApellidos) {
        if (empresa == null || correo == null || contrasenaPlana == null) {
            return -1;
        }

        String hash = SeguridadUtil.encriptarPassword(contrasenaPlana);
        String sqlUsuario = "INSERT INTO usuarios (nombre, apellidos, correo, contrasena, rol, estado) VALUES (?, ?, ?, ?, 'EMPRESA', 'ACTIVO')";
        String sqlEmpresa = "INSERT INTO empresas (id_usuario, razon_social, ruc, telefono, direccion, descripcion, estado) VALUES (?, ?, ?, ?, ?, ?, 'PENDIENTE')";

        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);

            int idUsuarioGenerado = -1;
            try (PreparedStatement psUser = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, repNombre != null ? repNombre.trim() : "");
                psUser.setString(2, repApellidos != null ? repApellidos.trim() : "");
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

            int idEmpresaGenerado = -1;
            try (PreparedStatement psEmp = conn.prepareStatement(sqlEmpresa, Statement.RETURN_GENERATED_KEYS)) {
                psEmp.setInt(1, idUsuarioGenerado);
                psEmp.setString(2, empresa.getRazonSocial() != null ? empresa.getRazonSocial().trim() : "");
                psEmp.setString(3, empresa.getRuc() != null ? empresa.getRuc().trim() : "");
                psEmp.setString(4, empresa.getTelefono() != null ? empresa.getTelefono().trim() : "");
                psEmp.setString(5, empresa.getDireccion() != null ? empresa.getDireccion().trim() : "");
                psEmp.setString(6, empresa.getDescripcion() != null ? empresa.getDescripcion().trim() : "");

                psEmp.executeUpdate();
                try (ResultSet rsEmp = psEmp.getGeneratedKeys()) {
                    if (rsEmp.next()) {
                        idEmpresaGenerado = rsEmp.getInt(1);
                    }
                }
            }

            if (idEmpresaGenerado > 0) {
                conn.commit();
                empresa.setId(idEmpresaGenerado);
                empresa.setIdUsuario(idUsuarioGenerado);
                return idEmpresaGenerado;
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

    public boolean actualizar(Empresa empresa) {
        if (empresa == null || empresa.getId() <= 0) {
            return false;
        }

        String sqlEmp = "UPDATE empresas SET razon_social = ?, ruc = ?, telefono = ?, direccion = ?, descripcion = ? WHERE id_empresa = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sqlEmp)) {

            ps.setString(1, empresa.getRazonSocial() != null ? empresa.getRazonSocial().trim() : "");
            ps.setString(2, empresa.getRuc() != null ? empresa.getRuc().trim() : "");
            ps.setString(3, empresa.getTelefono() != null ? empresa.getTelefono().trim() : "");
            ps.setString(4, empresa.getDireccion() != null ? empresa.getDireccion().trim() : "");
            ps.setString(5, empresa.getDescripcion() != null ? empresa.getDescripcion().trim() : "");
            ps.setInt(6, empresa.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarEstado(int idEmpresa, String nuevoEstado) {
        if (idEmpresa <= 0 || nuevoEstado == null) {
            return false;
        }

        String sqlEmp = "UPDATE empresas SET estado = ? WHERE id_empresa = ?";
        String sqlUsr = "UPDATE usuarios u INNER JOIN empresas e ON u.id_usuario = e.id_usuario SET u.estado = ? WHERE e.id_empresa = ?";
        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);
            try (PreparedStatement psEmp = conn.prepareStatement(sqlEmp);
                 PreparedStatement psUsr = conn.prepareStatement(sqlUsr)) {
                psEmp.setString(1, nuevoEstado.trim().toUpperCase());
                psEmp.setInt(2, idEmpresa);
                psEmp.executeUpdate();

                String estadoUsuario = "APROBADA".equalsIgnoreCase(nuevoEstado.trim()) || "ACTIVO".equalsIgnoreCase(nuevoEstado.trim()) ? "ACTIVO" : "INACTIVO";
                psUsr.setString(1, estadoUsuario);
                psUsr.setInt(2, idEmpresa);
                psUsr.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean darDeBaja(int idEmpresa) {
        if (idEmpresa <= 0) {
            return false;
        }

        String sql = "UPDATE usuarios u INNER JOIN empresas e ON u.id_usuario = e.id_usuario "
                   + "SET u.estado = 'INACTIVO' WHERE e.id_empresa = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpresa);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(int idEmpresa) {
        if (idEmpresa <= 0) {
            return false;
        }

        Empresa emp = obtenerPorId(idEmpresa);
        if (emp == null) {
            return false;
        }

        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM empresas WHERE id_empresa = ?")) {
                ps.setInt(1, idEmpresa);
                ps.executeUpdate();
            }

            if (emp.getIdUsuario() > 0) {
                try (PreparedStatement psUser = conn.prepareStatement("DELETE FROM usuarios WHERE id_usuario = ?")) {
                    psUser.setInt(1, emp.getIdUsuario());
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

    private Empresa mapearEmpresa(ResultSet rs) throws SQLException {
        Empresa e = new Empresa();
        e.setId(rs.getInt("id_empresa"));
        e.setIdUsuario(rs.getInt("id_usuario"));
        e.setRazonSocial(rs.getString("razon_social"));
        e.setRuc(rs.getString("ruc"));
        e.setTelefono(rs.getString("telefono"));
        e.setDireccion(rs.getString("direccion"));
        e.setDescripcion(rs.getString("descripcion"));
        e.setEstado(rs.getString("estado"));
        e.setCorreo(rs.getString("correo"));
        e.setRepresentanteNombre(rs.getString("nombre"));
        e.setRepresentanteApellidos(rs.getString("apellidos"));
        e.setEstadoCuenta(rs.getString("estado_cuenta"));
        return e;
    }
}
