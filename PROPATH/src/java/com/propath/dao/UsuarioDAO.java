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
                    boolean passwordValida = SeguridadUtil.verificarPassword(contrasenaPlana, hash);
                    if (passwordValida) {
                        Usuario usuario = mapearUsuario(rs);
                        cargarDatosRol(conn, usuario);
                        return usuario;
                    }
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
        return buscarYFiltrar(null, null, null);
    }

    public List<Usuario> buscarYFiltrar(String busqueda, String rol, String estado) {
        List<Usuario> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT u.id_usuario, u.nombre, u.apellidos, u.correo, u.contrasena, u.rol, u.estado, u.fecha_registro, ")
           .append("e.id_estudiante, emp.id_empresa, emp.razon_social ")
           .append("FROM usuarios u ")
           .append("LEFT JOIN estudiantes e ON u.id_usuario = e.id_usuario ")
           .append("LEFT JOIN empresas emp ON u.id_usuario = emp.id_usuario ")
           .append("WHERE 1=1 ");

        List<Object> parametros = new ArrayList<>();

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            sql.append("AND (LOWER(u.nombre) LIKE ? OR LOWER(u.apellidos) LIKE ? OR LOWER(u.correo) LIKE ? OR LOWER(emp.razon_social) LIKE ?) ");
            String term = "%" + busqueda.trim().toLowerCase() + "%";
            parametros.add(term);
            parametros.add(term);
            parametros.add(term);
            parametros.add(term);
        }

        if (rol != null && !rol.trim().isEmpty() && !"TODOS".equalsIgnoreCase(rol.trim())) {
            sql.append("AND UPPER(u.rol) = ? ");
            parametros.add(rol.trim().toUpperCase());
        }

        if (estado != null && !estado.trim().isEmpty() && !"TODOS".equalsIgnoreCase(estado.trim())) {
            sql.append("AND UPPER(u.estado) = ? ");
            parametros.add(estado.trim().toUpperCase());
        }

        sql.append("ORDER BY u.id_usuario DESC");

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
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

    public boolean registrarCodigoCambioCorreo(int idUsuario, String correoActual, String nuevoCorreo, String codigoPlano) {
        if (idUsuario <= 0 || correoActual == null || nuevoCorreo == null || codigoPlano == null) {
            return false;
        }

        String sqlInvalidar = "UPDATE codigos_verificacion_correo SET estado = 'EXPIRADO' WHERE id_usuario = ? AND estado = 'PENDIENTE'";
        // Vigencia estricta de 60 segundos (1 minuto) en base de datos
        String sqlInsertar = "INSERT INTO codigos_verificacion_correo (id_usuario, correo_actual, nuevo_correo, codigo_hash, intentos_restantes, estado, fecha_expiracion) "
                           + "VALUES (?, ?, ?, ?, 5, 'PENDIENTE', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 MINUTE))";

        String hash = SeguridadUtil.encriptarPassword(codigoPlano);

        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);

            try (PreparedStatement psInv = conn.prepareStatement(sqlInvalidar)) {
                psInv.setInt(1, idUsuario);
                psInv.executeUpdate();
            }

            try (PreparedStatement psIns = conn.prepareStatement(sqlInsertar)) {
                psIns.setInt(1, idUsuario);
                psIns.setString(2, correoActual.trim().toLowerCase());
                psIns.setString(3, nuevoCorreo.trim().toLowerCase());
                psIns.setString(4, hash);
                psIns.executeUpdate();
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

    public void invalidarCodigosPendientes(int idUsuario) {
        if (idUsuario <= 0) return;
        String sql = "UPDATE codigos_verificacion_correo SET estado = 'EXPIRADO' WHERE id_usuario = ? AND estado = 'PENDIENTE'";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    public boolean verificarYAplicarCambioCorreo(int idUsuario, String nuevoCorreo, String codigoPlano) {
        if (idUsuario <= 0 || nuevoCorreo == null || codigoPlano == null) {
            return false;
        }

        String sqlBuscar = "SELECT id_codigo, codigo_hash, intentos_restantes "
                         + "FROM codigos_verificacion_correo "
                         + "WHERE id_usuario = ? AND nuevo_correo = ? AND estado = 'PENDIENTE' AND fecha_expiracion > CURRENT_TIMESTAMP "
                         + "ORDER BY id_codigo DESC LIMIT 1";

        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);

            int idCodigo = -1;
            String hash = null;
            int intentos = 0;

            try (PreparedStatement ps = conn.prepareStatement(sqlBuscar)) {
                ps.setInt(1, idUsuario);
                ps.setString(2, nuevoCorreo.trim().toLowerCase());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        idCodigo = rs.getInt("id_codigo");
                        hash = rs.getString("codigo_hash");
                        intentos = rs.getInt("intentos_restantes");
                    }
                }
            }

            if (idCodigo <= 0 || hash == null) {
                conn.rollback();
                return false;
            }

            if (intentos <= 0) {
                try (PreparedStatement psB = conn.prepareStatement("UPDATE codigos_verificacion_correo SET estado = 'BLOQUEADO' WHERE id_codigo = ?")) {
                    psB.setInt(1, idCodigo);
                    psB.executeUpdate();
                }
                conn.commit();
                return false;
            }

            boolean valido = SeguridadUtil.verificarPassword(codigoPlano.trim(), hash);
            if (!valido) {
                int restantes = intentos - 1;
                String sqlUpdate = (restantes <= 0)
                        ? "UPDATE codigos_verificacion_correo SET intentos_restantes = 0, estado = 'BLOQUEADO' WHERE id_codigo = ?"
                        : "UPDATE codigos_verificacion_correo SET intentos_restantes = ? WHERE id_codigo = ?";
                try (PreparedStatement psU = conn.prepareStatement(sqlUpdate)) {
                    if (restantes <= 0) {
                        psU.setInt(1, idCodigo);
                    } else {
                        psU.setInt(1, restantes);
                        psU.setInt(2, idCodigo);
                    }
                    psU.executeUpdate();
                }
                conn.commit();
                return false;
            }

            // CÃƒÂ³digo vÃƒÂ¡lido: Marcar como USADO y actualizar el correo del usuario
            try (PreparedStatement psUsado = conn.prepareStatement("UPDATE codigos_verificacion_correo SET estado = 'USADO' WHERE id_codigo = ?")) {
                psUsado.setInt(1, idCodigo);
                psUsado.executeUpdate();
            }

            try (PreparedStatement psUser = conn.prepareStatement("UPDATE usuarios SET correo = ? WHERE id_usuario = ?")) {
                psUser.setString(1, nuevoCorreo.trim().toLowerCase());
                psUser.setInt(2, idUsuario);
                psUser.executeUpdate();
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
