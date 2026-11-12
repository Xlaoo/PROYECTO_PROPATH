package com.propath.dao;

import com.propath.conexion.ConexionBD;
import com.propath.conexion.SeguridadUtil;
import com.propath.dto.DashboardAdministradoresDTO;
import com.propath.modelo.Administrador;
import com.propath.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminDAO {

    public Map<String, Object> obtenerEstadisticasAdmin() {
        Map<String, Object> map = new HashMap<>();
        try (Connection conn = ConexionBD.getConexion()) {
            map.put("totalEstudiantes", contar(conn, "SELECT COUNT(*) FROM estudiantes"));
            map.put("estudiantesEstaSemana", contar(conn, "SELECT COUNT(*) FROM estudiantes e INNER JOIN usuarios u ON e.id_usuario = u.id_usuario WHERE u.fecha_registro >= DATE_SUB(NOW(), INTERVAL 7 DAY)"));
            map.put("estudiantesActivos", contar(conn, "SELECT COUNT(*) FROM estudiantes e INNER JOIN usuarios u ON e.id_usuario = u.id_usuario WHERE u.estado = 'ACTIVO'"));
            map.put("estudiantesInactivos", contar(conn, "SELECT COUNT(*) FROM estudiantes e INNER JOIN usuarios u ON e.id_usuario = u.id_usuario WHERE u.estado = 'INACTIVO'"));
            map.put("totalEmpresas", contar(conn, "SELECT COUNT(*) FROM empresas"));
            map.put("empresasAprobadas", contar(conn, "SELECT COUNT(*) FROM empresas WHERE estado = 'APROBADA'"));
            map.put("empresasPendientes", contar(conn, "SELECT COUNT(*) FROM empresas WHERE estado = 'PENDIENTE'"));
            map.put("empresasInactivas", contar(conn, "SELECT COUNT(*) FROM empresas e INNER JOIN usuarios u ON e.id_usuario = u.id_usuario WHERE u.estado = 'INACTIVO'"));
            map.put("totalUsuarios", contar(conn, "SELECT COUNT(*) FROM usuarios"));
            map.put("totalOfertas", contar(conn, "SELECT COUNT(*) FROM ofertas_practicas"));
            map.put("ofertasActivas", contar(conn, "SELECT COUNT(*) FROM ofertas_practicas WHERE estado = 'ACTIVA'"));
            map.put("ofertasPendientes", contar(conn, "SELECT COUNT(*) FROM ofertas_practicas WHERE estado = 'PENDIENTE'"));
            map.put("totalPostulaciones", contar(conn, "SELECT COUNT(*) FROM postulaciones"));
            map.put("postulacionesEnRevision", contar(conn, "SELECT COUNT(*) FROM postulaciones WHERE estado = 'EN_REVISION'"));
            map.put("postulacionesSeleccionadas", contar(conn, "SELECT COUNT(*) FROM postulaciones WHERE estado = 'SELECCIONADO'"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public Map<String, Long> obtenerEstadisticasPublicas() {
        Map<String, Long> stats = new HashMap<>();
        try (Connection conn = ConexionBD.getConexion()) {
            stats.put("estudiantes", contar(conn, "SELECT COUNT(*) FROM estudiantes"));
            stats.put("empresas", contar(conn, "SELECT COUNT(*) FROM empresas WHERE estado = 'APROBADA'"));
            stats.put("ofertas", contar(conn, "SELECT COUNT(*) FROM ofertas_practicas WHERE estado = 'ACTIVA'"));
            stats.put("postulaciones", contar(conn, "SELECT COUNT(*) FROM postulaciones"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    private long contar(Connection conn, String sql) {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    // =========================================================================
    // CRUD Y GESTIÓN DEL MÓDULO ADMINISTRADORES
    // =========================================================================

    public List<Administrador> listarAdministradores(String busqueda) {
        List<Administrador> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT a.id_administrador, a.id_usuario, a.cargo, ")
           .append("u.nombre, u.apellidos, u.correo, u.rol, u.estado, u.fecha_registro ")
           .append("FROM administradores a ")
           .append("INNER JOIN usuarios u ON a.id_usuario = u.id_usuario ")
           .append("WHERE u.rol = 'ADMIN' ");

        List<Object> params = new ArrayList<>();
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            sql.append("AND (LOWER(u.nombre) LIKE ? OR LOWER(u.apellidos) LIKE ? OR LOWER(u.correo) LIKE ? OR LOWER(a.cargo) LIKE ?) ");
            String term = "%" + busqueda.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }

        sql.append("ORDER BY a.id_administrador DESC");

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAdministrador(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Administrador> listarTodos() {
        return listarAdministradores(null);
    }

    public Administrador obtenerAdministradorPorId(int idAdministrador) {
        if (idAdministrador <= 0) return null;
        String sql = "SELECT a.id_administrador, a.id_usuario, a.cargo, "
                   + "u.nombre, u.apellidos, u.correo, u.rol, u.estado, u.fecha_registro "
                   + "FROM administradores a "
                   + "INNER JOIN usuarios u ON a.id_usuario = u.id_usuario "
                   + "WHERE a.id_administrador = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idAdministrador);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearAdministrador(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Administrador obtenerAdministradorPorUsuarioId(int idUsuario) {
        if (idUsuario <= 0) return null;
        String sql = "SELECT a.id_administrador, a.id_usuario, a.cargo, "
                   + "u.nombre, u.apellidos, u.correo, u.rol, u.estado, u.fecha_registro "
                   + "FROM administradores a "
                   + "INNER JOIN usuarios u ON a.id_usuario = u.id_usuario "
                   + "WHERE a.id_usuario = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearAdministrador(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Registra un nuevo administrador y su cuenta de usuario en una sola transacción JDBC.
     * Asigna rol ADMIN y estado ACTIVO en el servidor con contraseña hasheada.
     */
    public int registrarAdministradorConUsuario(Administrador admin, String correo, String contrasenaPlana, String nombre, String apellidos) {
        if (admin == null || correo == null || contrasenaPlana == null) {
            return -1;
        }

        String hash = SeguridadUtil.encriptarPassword(contrasenaPlana);
        String sqlUsuario = "INSERT INTO usuarios (nombre, apellidos, correo, contrasena, rol, estado) VALUES (?, ?, ?, ?, 'ADMIN', 'ACTIVO')";
        String sqlAdmin = "INSERT INTO administradores (id_usuario, cargo) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);

            int idUsuarioGenerado = -1;
            try (PreparedStatement psUser = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, nombre != null ? nombre.trim() : "");
                psUser.setString(2, apellidos != null ? apellidos.trim() : "");
                psUser.setString(3, correo.trim().toLowerCase());
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

            int idAdminGenerado = -1;
            try (PreparedStatement psAdmin = conn.prepareStatement(sqlAdmin, Statement.RETURN_GENERATED_KEYS)) {
                psAdmin.setInt(1, idUsuarioGenerado);
                psAdmin.setString(2, admin.getCargo() != null && !admin.getCargo().trim().isEmpty() ? admin.getCargo().trim() : "Administrador General");

                psAdmin.executeUpdate();
                try (ResultSet rsAdmin = psAdmin.getGeneratedKeys()) {
                    if (rsAdmin.next()) {
                        idAdminGenerado = rsAdmin.getInt(1);
                    }
                }
            }

            if (idAdminGenerado > 0) {
                conn.commit();
                admin.setId(idAdminGenerado);
                admin.setIdUsuario(idUsuarioGenerado);
                return idAdminGenerado;
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
                } catch (SQLException ignored) {
                }
            }
        }
        return -1;
    }

    /**
     * Actualiza los datos permitidos del administrador (nombre, apellidos y cargo) en una transacción JDBC.
     */
    public boolean actualizarAdministrador(int idAdministrador, String nombre, String apellidos, String cargo) {
        if (idAdministrador <= 0) return false;

        String sqlSelect = "SELECT id_usuario FROM administradores WHERE id_administrador = ?";
        String sqlUpdateAdmin = "UPDATE administradores SET cargo = ? WHERE id_administrador = ?";
        String sqlUpdateUser = "UPDATE usuarios SET nombre = ?, apellidos = ? WHERE id_usuario = ?";

        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);

            int idUsuario = -1;
            try (PreparedStatement psSel = conn.prepareStatement(sqlSelect)) {
                psSel.setInt(1, idAdministrador);
                try (ResultSet rs = psSel.executeQuery()) {
                    if (rs.next()) {
                        idUsuario = rs.getInt("id_usuario");
                    }
                }
            }

            if (idUsuario <= 0) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement psA = conn.prepareStatement(sqlUpdateAdmin)) {
                psA.setString(1, cargo != null && !cargo.trim().isEmpty() ? cargo.trim() : "Administrador General");
                psA.setInt(2, idAdministrador);
                psA.executeUpdate();
            }

            try (PreparedStatement psU = conn.prepareStatement(sqlUpdateUser)) {
                psU.setString(1, nombre != null ? nombre.trim() : "");
                psU.setString(2, apellidos != null ? apellidos.trim() : "");
                psU.setInt(3, idUsuario);
                psU.executeUpdate();
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

    /**
     * Aplica baja lógica cambiando el estado del usuario asociado a INACTIVO, conservando el perfil en administradores.
     */
    public boolean darDeBajaAdministrador(int idAdministrador) {
        if (idAdministrador <= 0) return false;

        String sql = "UPDATE usuarios u "
                   + "INNER JOIN administradores a ON u.id_usuario = a.id_usuario "
                   + "SET u.estado = 'INACTIVO' "
                   + "WHERE a.id_administrador = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAdministrador);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reactiva una cuenta de administrador a estado ACTIVO.
     */
    public boolean reactivarAdministrador(int idAdministrador) {
        if (idAdministrador <= 0) return false;

        String sql = "UPDATE usuarios u "
                   + "INNER JOIN administradores a ON u.id_usuario = a.id_usuario "
                   + "SET u.estado = 'ACTIVO' "
                   + "WHERE a.id_administrador = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAdministrador);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene la lista de cargos únicos existentes para poblar el selector de filtros.
     */
    public List<String> obtenerOpcionesCargos() {
        List<String> cargos = new ArrayList<>();
        String sql = "SELECT DISTINCT cargo FROM administradores WHERE cargo IS NOT NULL AND TRIM(cargo) != '' ORDER BY cargo ASC";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cargos.add(rs.getString("cargo").trim());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cargos;
    }

    /**
     * Obtiene los indicadores y métricas del Dashboard de Administradores combinando
     * filtros por estado y cargo en tiempo real desde MySQL.
     */
    public DashboardAdministradoresDTO obtenerDashboardAdministradores(String filtroEstado, String filtroCargo) {
        DashboardAdministradoresDTO dto = new DashboardAdministradoresDTO();

        String normEstado = (filtroEstado != null && !filtroEstado.trim().isEmpty()) ? filtroEstado.trim().toUpperCase() : "TODOS";
        String normCargo = (filtroCargo != null && !filtroCargo.trim().isEmpty()) ? filtroCargo.trim() : "TODOS";

        dto.setEstado(normEstado);
        dto.setCargo(normCargo);
        dto.setFiltrosActivos(!"TODOS".equalsIgnoreCase(normEstado) || !"TODOS".equalsIgnoreCase(normCargo));
        dto.setOpcionesCargos(obtenerOpcionesCargos());

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT a.id_administrador, a.id_usuario, a.cargo, ")
           .append("u.nombre, u.apellidos, u.correo, u.rol, u.estado, u.fecha_registro ")
           .append("FROM administradores a ")
           .append("INNER JOIN usuarios u ON a.id_usuario = u.id_usuario ")
           .append("WHERE u.rol = 'ADMIN' ");

        List<Object> params = new ArrayList<>();
        if (!"TODOS".equalsIgnoreCase(normEstado)) {
            sql.append("AND UPPER(u.estado) = ? ");
            params.add(normEstado);
        }
        if (!"TODOS".equalsIgnoreCase(normCargo)) {
            sql.append("AND a.cargo = ? ");
            params.add(normCargo);
        }

        sql.append("ORDER BY a.id_administrador DESC");

        List<Administrador> lista = new ArrayList<>();
        int total = 0;
        int activos = 0;
        int inactivos = 0;
        int bloqueados = 0;

        Map<String, Integer> distEstados = new LinkedHashMap<>();
        distEstados.put("ACTIVO", 0);
        distEstados.put("INACTIVO", 0);
        distEstados.put("BLOQUEADO", 0);

        Map<String, Integer> distCargos = new LinkedHashMap<>();

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Administrador adm = mapearAdministrador(rs);
                    lista.add(adm);
                    total++;

                    String est = adm.getEstado() != null ? adm.getEstado().toUpperCase() : "ACTIVO";
                    if ("ACTIVO".equals(est)) {
                        activos++;
                    } else if ("INACTIVO".equals(est)) {
                        inactivos++;
                    } else if ("BLOQUEADO".equals(est)) {
                        bloqueados++;
                    }
                    distEstados.put(est, distEstados.getOrDefault(est, 0) + 1);

                    String crg = adm.getCargo() != null && !adm.getCargo().trim().isEmpty() ? adm.getCargo().trim() : "Administrador General";
                    distCargos.put(crg, distCargos.getOrDefault(crg, 0) + 1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        dto.setTotalAdministradores(total);
        dto.setActivos(activos);
        dto.setInactivos(inactivos);
        dto.setBloqueados(bloqueados);
        dto.setDistribucionEstados(distEstados);
        dto.setDistribucionCargos(distCargos);
        dto.setAdministradoresFiltrados(lista);

        return dto;
    }

    private Administrador mapearAdministrador(ResultSet rs) throws SQLException {
        Administrador a = new Administrador();
        a.setId(rs.getInt("id_administrador"));
        a.setIdUsuario(rs.getInt("id_usuario"));
        a.setCargo(rs.getString("cargo"));

        Usuario u = new Usuario();
        u.setId(rs.getInt("id_usuario"));
        u.setNombre(rs.getString("nombre"));
        u.setApellidos(rs.getString("apellidos"));
        u.setCorreo(rs.getString("correo"));
        u.setRol(rs.getString("rol"));
        u.setEstado(rs.getString("estado"));
        try {
            java.sql.Timestamp ts = rs.getTimestamp("fecha_registro");
            if (ts != null) {
                u.setFechaRegistro(ts.toString());
            }
        } catch (SQLException ignored) {
        }
        a.setUsuario(u);

        return a;
    }
}
