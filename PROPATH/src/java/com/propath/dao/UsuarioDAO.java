package com.propath.dao;

import com.propath.conexion.ConexionBD;
import com.propath.conexion.SeguridadUtil;
import com.propath.modelo.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public Usuario autenticar(String correo, String contrasenaPlana) {
        if (correo == null || contrasenaPlana == null || correo.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT id_usuario, nombre, apellidos, correo, contrasena, rol, estado, fecha_registro FROM usuarios WHERE correo = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, correo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                
                if (rs.next()) {
    String hash = rs.getString("contrasena");

    System.out.println("PROPATH LOGIN - Usuario encontrado: SI");
    System.out.println("PROPATH LOGIN - ID: " + rs.getInt("id_usuario"));
    System.out.println("PROPATH LOGIN - Hash presente: " + (hash != null));
    System.out.println("PROPATH LOGIN - Hash BCrypt formato esperado: "
            + (hash != null && hash.matches("\\$2[aby]\\$\\d{2}\\$.{53}")));

    boolean passwordValida =
            SeguridadUtil.verificarPassword(contrasenaPlana, hash);

    System.out.println("PROPATH LOGIN - BCrypt coincide: " + passwordValida);

    if (passwordValida) {
        Usuario usuario = mapearUsuario(rs);
        cargarDatosRol(conn, usuario);
        return usuario;
    }
} else {
    System.out.println("PROPATH LOGIN - Usuario encontrado: NO");
}
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Usuario obtenerPorId(int id) {
        String sql = "SELECT id_usuario, nombre, apellidos, correo, contrasena, rol, estado, fecha_registro FROM usuarios WHERE id_usuario = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapearUsuario(rs);
                    cargarDatosRol(conn, usuario);
                    return usuario;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Usuario obtenerPorCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT id_usuario, nombre, apellidos, correo, contrasena, rol, estado, fecha_registro FROM usuarios WHERE correo = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, correo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapearUsuario(rs);
                    cargarDatosRol(conn, usuario);
                    return usuario;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT u.id_usuario, u.nombre, u.apellidos, u.correo, u.contrasena, u.rol, u.estado, u.fecha_registro, "
                   + "e.id_estudiante, emp.id_empresa, emp.razon_social "
                   + "FROM usuarios u "
                   + "LEFT JOIN estudiantes e ON u.id_usuario = e.id_usuario "
                   + "LEFT JOIN empresas emp ON u.id_usuario = emp.id_usuario "
                   + "ORDER BY u.id_usuario DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Usuario u = mapearUsuario(rs);
                int idEst = rs.getInt("id_estudiante");
                if (!rs.wasNull()) {
                    u.setIdEstudiante(idEst);
                }
                int idEmp = rs.getInt("id_empresa");
                if (!rs.wasNull()) {
                    u.setIdEmpresa(idEmp);
                    u.setRazonSocial(rs.getString("razon_social"));
                }
                lista.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public int crear(Usuario usuario, String contrasenaPlana) {
        if (usuario == null || usuario.getCorreo() == null || contrasenaPlana == null) {
            return -1;
        }

        String hash = SeguridadUtil.encriptarPassword(contrasenaPlana);
        String sql = "INSERT INTO usuarios (nombre, apellidos, correo, contrasena, rol, estado) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, usuario.getNombre() != null ? usuario.getNombre().trim() : "");
            ps.setString(2, usuario.getApellido() != null ? usuario.getApellido().trim() : "");
            ps.setString(3, usuario.getCorreo().trim());
            ps.setString(4, hash);
            ps.setString(5, usuario.getRol() != null ? usuario.getRol().trim().toUpperCase() : "ESTUDIANTE");
            ps.setString(6, usuario.getEstado() != null ? usuario.getEstado().trim().toUpperCase() : "ACTIVO");

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int idGenerado = rs.getInt(1);
                        usuario.setId(idGenerado);
                        return idGenerado;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int crear(Usuario usuario) {
        if (usuario == null) {
            return -1;
        }
        String pass = usuario.getContrasena();
        return crear(usuario, pass != null ? pass : "");
    }

    public boolean actualizar(Usuario usuario) {
        if (usuario == null || usuario.getId() <= 0) {
            return false;
        }

        String sql = "UPDATE usuarios SET nombre = ?, apellidos = ?, estado = ? WHERE id_usuario = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombre() != null ? usuario.getNombre().trim() : "");
            ps.setString(2, usuario.getApellido() != null ? usuario.getApellido().trim() : "");
            ps.setString(3, usuario.getEstado() != null ? usuario.getEstado().trim().toUpperCase() : "ACTIVO");
            ps.setInt(4, usuario.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarEstado(int idUsuario, String nuevoEstado) {
        if (idUsuario <= 0 || nuevoEstado == null) {
            return false;
        }

        String sql = "UPDATE usuarios SET estado = ? WHERE id_usuario = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado.trim().toUpperCase());
            ps.setInt(2, idUsuario);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarContrasena(int idUsuario, String nuevaContrasenaPlana) {
        if (idUsuario <= 0 || nuevaContrasenaPlana == null || nuevaContrasenaPlana.isEmpty()) {
            return false;
        }

        String hash = SeguridadUtil.encriptarPassword(nuevaContrasenaPlana);
        String sql = "UPDATE usuarios SET contrasena = ? WHERE id_usuario = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hash);
            ps.setInt(2, idUsuario);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean darDeBaja(int idUsuario) {
        return actualizarEstado(idUsuario, "INACTIVO");
    }

    public boolean eliminar(int idUsuario) {
        if (idUsuario <= 0) {
            return false;
        }

        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id_usuario"));
        u.setNombre(rs.getString("nombre"));
        u.setApellido(rs.getString("apellidos"));
        u.setCorreo(rs.getString("correo"));
        u.setRol(rs.getString("rol"));
        u.setEstado(rs.getString("estado"));
        try {
            u.setContrasena(rs.getString("contrasena"));
        } catch (SQLException ignored) {
        }
        try {
            java.sql.Timestamp ts = rs.getTimestamp("fecha_registro");
            if (ts != null) {
                u.setFechaRegistro(ts.toString());
            }
        } catch (SQLException ignored) {
        }
        return u;
    }

    private void cargarDatosRol(Connection conn, Usuario usuario) {
        if (usuario == null) {
            return;
        }

        if ("ESTUDIANTE".equalsIgnoreCase(usuario.getRol())) {
            String sql = "SELECT id_estudiante FROM estudiantes WHERE id_usuario = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, usuario.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        usuario.setIdEstudiante(rs.getInt("id_estudiante"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if ("EMPRESA".equalsIgnoreCase(usuario.getRol())) {
            String sql = "SELECT id_empresa, razon_social FROM empresas WHERE id_usuario = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, usuario.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        usuario.setIdEmpresa(rs.getInt("id_empresa"));
                        usuario.setRazonSocial(rs.getString("razon_social"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
