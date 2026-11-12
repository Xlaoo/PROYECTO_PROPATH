package com.propath.dao;

import com.propath.conexion.ConexionBD;
import com.propath.conexion.SeguridadUtil;
import com.propath.dto.DashboardEmpresasDTO;
import com.propath.modelo.Empresa;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

    public boolean actualizarConEstadoCuenta(Empresa emp, String estadoEmpresa, String estadoCuenta) {
        if (emp == null || emp.getId() <= 0) {
            return false;
        }

        String sqlEmp = "UPDATE empresas SET razon_social = ?, ruc = ?, telefono = ?, direccion = ?, descripcion = ?, estado = ? WHERE id_empresa = ?";
        String sqlUsr = "UPDATE usuarios u INNER JOIN empresas e ON u.id_usuario = e.id_usuario SET u.estado = ? WHERE e.id_empresa = ?";

        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);

            try (PreparedStatement psEmp = conn.prepareStatement(sqlEmp)) {
                psEmp.setString(1, emp.getRazonSocial() != null ? emp.getRazonSocial().trim() : "");
                psEmp.setString(2, emp.getRuc() != null ? emp.getRuc().trim() : "");
                psEmp.setString(3, emp.getTelefono() != null ? emp.getTelefono().trim() : "");
                psEmp.setString(4, emp.getDireccion() != null ? emp.getDireccion().trim() : "");
                psEmp.setString(5, emp.getDescripcion() != null ? emp.getDescripcion().trim() : "");
                psEmp.setString(6, estadoEmpresa != null && !estadoEmpresa.trim().isEmpty() ? estadoEmpresa.trim().toUpperCase() : "APROBADA");
                psEmp.setInt(7, emp.getId());
                psEmp.executeUpdate();
            }

            try (PreparedStatement psUsr = conn.prepareStatement(sqlUsr)) {
                psUsr.setString(1, estadoCuenta != null && !estadoCuenta.trim().isEmpty() ? estadoCuenta.trim().toUpperCase() : "ACTIVO");
                psUsr.setInt(2, emp.getId());
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

    /**
     * Obtiene el DTO completo del Dashboard interactivo de Empresas para los segmentadores
     * combinables: estado del perfil, estado de la cuenta, presencia de ofertas y presencia de ofertas activas.
     */
    public DashboardEmpresasDTO obtenerDashboardEmpresas(
            String estadoPerfil, String estadoCuenta,
            String conOfertas, String conOfertasActivas) {
        return obtenerDashboardEmpresas(estadoPerfil, estadoCuenta, conOfertas, conOfertasActivas, "", "", "");
    }

    /**
     * Obtiene el DTO completo del Dashboard de Empresas con sus segmentadores,
     * indicadores KPI, mapas de distribución y lista de organizaciones filtradas.
     * Utiliza subconsultas para calcular el total de ofertas y ofertas activas sin
     * duplicar registros de empresas.
     */
    public DashboardEmpresasDTO obtenerDashboardEmpresas(
            String estadoPerfil, String estadoCuenta,
            String conOfertas, String conOfertasActivas,
            String fechaDesde, String fechaHasta, String busqueda) {

        DashboardEmpresasDTO dto = new DashboardEmpresasDTO();
        dto.setEstadoPerfil(estadoPerfil != null ? estadoPerfil : "TODOS");
        dto.setEstadoCuenta(estadoCuenta != null ? estadoCuenta : "TODOS");
        dto.setConOfertas(conOfertas != null ? conOfertas : "TODOS");
        dto.setConOfertasActivas(conOfertasActivas != null ? conOfertasActivas : "TODOS");
        dto.setFechaDesde(fechaDesde != null ? fechaDesde : "");
        dto.setFechaHasta(fechaHasta != null ? fechaHasta : "");
        dto.setBusqueda(busqueda != null ? busqueda : "");

        boolean filtrosActivos = (estadoPerfil != null && !estadoPerfil.trim().isEmpty() && !"TODOS".equalsIgnoreCase(estadoPerfil.trim()))
                || (estadoCuenta != null && !estadoCuenta.trim().isEmpty() && !"TODOS".equalsIgnoreCase(estadoCuenta.trim()))
                || (conOfertas != null && !conOfertas.trim().isEmpty() && !"TODOS".equalsIgnoreCase(conOfertas.trim()))
                || (conOfertasActivas != null && !conOfertasActivas.trim().isEmpty() && !"TODOS".equalsIgnoreCase(conOfertasActivas.trim()))
                || (fechaDesde != null && !fechaDesde.trim().isEmpty())
                || (fechaHasta != null && !fechaHasta.trim().isEmpty())
                || (busqueda != null && !busqueda.trim().isEmpty());
        dto.setFiltrosActivos(filtrosActivos);

        try (Connection conn = ConexionBD.getConexion()) {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT e.id_empresa, e.id_usuario, e.razon_social, e.ruc, e.telefono, e.direccion, e.descripcion, e.estado AS estado_perfil, ")
               .append("u.correo, u.nombre, u.apellidos, u.estado AS estado_cuenta, u.fecha_registro, ")
               .append("(SELECT COUNT(*) FROM ofertas_practicas op WHERE op.id_empresa = e.id_empresa) AS total_ofertas, ")
               .append("(SELECT COUNT(*) FROM ofertas_practicas op WHERE op.id_empresa = e.id_empresa AND UPPER(op.estado) = 'ACTIVA') AS ofertas_activas ")
               .append("FROM empresas e ")
               .append("INNER JOIN usuarios u ON e.id_usuario = u.id_usuario ")
               .append("WHERE 1=1 ");

            List<Object> params = new ArrayList<>();

            if (estadoPerfil != null && !estadoPerfil.trim().isEmpty() && !"TODOS".equalsIgnoreCase(estadoPerfil.trim())) {
                sql.append("AND UPPER(e.estado) = ? ");
                params.add(estadoPerfil.trim().toUpperCase());
            }

            if (estadoCuenta != null && !estadoCuenta.trim().isEmpty() && !"TODOS".equalsIgnoreCase(estadoCuenta.trim())) {
                sql.append("AND UPPER(u.estado) = ? ");
                params.add(estadoCuenta.trim().toUpperCase());
            }

            if ("CON_OFERTAS".equalsIgnoreCase(conOfertas)) {
                sql.append("AND EXISTS (SELECT 1 FROM ofertas_practicas op WHERE op.id_empresa = e.id_empresa) ");
            } else if ("SIN_OFERTAS".equalsIgnoreCase(conOfertas)) {
                sql.append("AND NOT EXISTS (SELECT 1 FROM ofertas_practicas op WHERE op.id_empresa = e.id_empresa) ");
            }

            if ("CON_ACTIVAS".equalsIgnoreCase(conOfertasActivas)) {
                sql.append("AND EXISTS (SELECT 1 FROM ofertas_practicas op WHERE op.id_empresa = e.id_empresa AND UPPER(op.estado) = 'ACTIVA') ");
            } else if ("SIN_ACTIVAS".equalsIgnoreCase(conOfertasActivas)) {
                sql.append("AND NOT EXISTS (SELECT 1 FROM ofertas_practicas op WHERE op.id_empresa = e.id_empresa AND UPPER(op.estado) = 'ACTIVA') ");
            }

            if (fechaDesde != null && !fechaDesde.trim().isEmpty()) {
                sql.append("AND u.fecha_registro >= ? ");
                params.add(fechaDesde.trim() + " 00:00:00");
            }

            if (fechaHasta != null && !fechaHasta.trim().isEmpty()) {
                sql.append("AND u.fecha_registro <= ? ");
                params.add(fechaHasta.trim() + " 23:59:59");
            }

            if (busqueda != null && !busqueda.trim().isEmpty()) {
                String pattern = "%" + busqueda.trim().toLowerCase() + "%";
                sql.append("AND (LOWER(e.razon_social) LIKE ? OR LOWER(e.ruc) LIKE ? OR LOWER(u.correo) LIKE ? OR LOWER(u.nombre) LIKE ? OR LOWER(u.apellidos) LIKE ?) ");
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
            }

            sql.append("ORDER BY e.id_empresa DESC");

            List<Empresa> lista = new ArrayList<>();
            Map<String, Integer> mapEstadoPerfil = new LinkedHashMap<>();
            mapEstadoPerfil.put("Aprobada", 0);
            mapEstadoPerfil.put("Pendiente", 0);
            mapEstadoPerfil.put("Rechazada", 0);
            mapEstadoPerfil.put("Suspendida", 0);

            Map<String, Integer> mapEstadoCuenta = new LinkedHashMap<>();
            mapEstadoCuenta.put("Activo", 0);
            mapEstadoCuenta.put("Inactivo", 0);
            mapEstadoCuenta.put("Bloqueado", 0);

            Map<String, Integer> mapOfertas = new LinkedHashMap<>();
            mapOfertas.put("Con Ofertas", 0);
            mapOfertas.put("Sin Ofertas", 0);

            Map<String, Integer> mapRankingOfertas = new HashMap<>();
            Map<String, Integer> mapMeses = new TreeMap<>();

            int pendientes = 0;
            int aprobadas = 0;
            int rechazadas = 0;
            int suspendidas = 0;
            int cuentasActivas = 0;
            int cuentasInactivas = 0;
            int cuentasBloqueadas = 0;
            int conOfertasCount = 0;
            int sinOfertasCount = 0;
            int conOfertasActivasCount = 0;

            SimpleDateFormat sdfRegistro = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdfMesClave = new SimpleDateFormat("yyyy-MM");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Empresa emp = new Empresa();
                        emp.setId(rs.getInt("id_empresa"));
                        emp.setIdUsuario(rs.getInt("id_usuario"));
                        emp.setRazonSocial(rs.getString("razon_social"));
                        emp.setRuc(rs.getString("ruc"));
                        emp.setTelefono(rs.getString("telefono"));
                        emp.setDireccion(rs.getString("direccion"));
                        emp.setDescripcion(rs.getString("descripcion"));
                        emp.setEstado(rs.getString("estado_perfil"));
                        emp.setCorreo(rs.getString("correo"));
                        emp.setRepresentanteNombre(rs.getString("nombre"));
                        emp.setRepresentanteApellidos(rs.getString("apellidos"));
                        emp.setEstadoCuenta(rs.getString("estado_cuenta"));

                        java.sql.Timestamp ts = rs.getTimestamp("fecha_registro");
                        if (ts != null) {
                            emp.setFechaRegistro(sdfRegistro.format(ts));
                            String mesClave = sdfMesClave.format(ts);
                            mapMeses.put(mesClave, mapMeses.getOrDefault(mesClave, 0) + 1);
                        }

                        int totOfertas = rs.getInt("total_ofertas");
                        int ofActivas = rs.getInt("ofertas_activas");
                        emp.setTotalOfertas(totOfertas);
                        emp.setOfertasActivas(ofActivas);

                        // Estado perfil
                        String ep = emp.getEstado() != null ? emp.getEstado().toUpperCase() : "PENDIENTE";
                        if ("APROBADA".equals(ep)) {
                            aprobadas++;
                            mapEstadoPerfil.put("Aprobada", mapEstadoPerfil.get("Aprobada") + 1);
                        } else if ("PENDIENTE".equals(ep)) {
                            pendientes++;
                            mapEstadoPerfil.put("Pendiente", mapEstadoPerfil.get("Pendiente") + 1);
                        } else if ("RECHAZADA".equals(ep)) {
                            rechazadas++;
                            mapEstadoPerfil.put("Rechazada", mapEstadoPerfil.get("Rechazada") + 1);
                        } else if ("SUSPENDIDA".equals(ep)) {
                            suspendidas++;
                            mapEstadoPerfil.put("Suspendida", mapEstadoPerfil.get("Suspendida") + 1);
                        }

                        // Estado cuenta
                        String ec = emp.getEstadoCuenta() != null ? emp.getEstadoCuenta().toUpperCase() : "ACTIVO";
                        if ("ACTIVO".equals(ec)) {
                            cuentasActivas++;
                            mapEstadoCuenta.put("Activo", mapEstadoCuenta.get("Activo") + 1);
                        } else if ("INACTIVO".equals(ec)) {
                            cuentasInactivas++;
                            mapEstadoCuenta.put("Inactivo", mapEstadoCuenta.get("Inactivo") + 1);
                        } else if ("BLOQUEADO".equals(ec)) {
                            cuentasBloqueadas++;
                            mapEstadoCuenta.put("Bloqueado", mapEstadoCuenta.get("Bloqueado") + 1);
                        }

                        // Ofertas
                        if (totOfertas > 0) {
                            conOfertasCount++;
                            mapOfertas.put("Con Ofertas", mapOfertas.get("Con Ofertas") + 1);
                            String nom = emp.getRazonSocial() != null ? emp.getRazonSocial().trim() : "Empresa #" + emp.getId();
                            mapRankingOfertas.put(nom, totOfertas);
                        } else {
                            sinOfertasCount++;
                            mapOfertas.put("Sin Ofertas", mapOfertas.get("Sin Ofertas") + 1);
                        }

                        if (ofActivas > 0) {
                            conOfertasActivasCount++;
                        }

                        lista.add(emp);
                    }
                }
            }

            dto.setTotalEmpresas(lista.size());
            dto.setPendientes(pendientes);
            dto.setAprobadas(aprobadas);
            dto.setRechazadas(rechazadas);
            dto.setSuspendidas(suspendidas);
            dto.setCuentasActivas(cuentasActivas);
            dto.setCuentasInactivas(cuentasInactivas);
            dto.setCuentasBloqueadas(cuentasBloqueadas);
            dto.setEmpresasConOfertas(conOfertasCount);
            dto.setEmpresasSinOfertas(sinOfertasCount);
            dto.setEmpresasConOfertasActivas(conOfertasActivasCount);
            dto.setRegistrosPeriodo(lista.size());

            dto.setDistribucionEstadoPerfil(mapEstadoPerfil);
            dto.setDistribucionEstadoCuenta(mapEstadoCuenta);
            dto.setDistribucionOfertas(mapOfertas);

            // Ranking de empresas por ofertas creadas (de mayor a menor)
            Map<String, Integer> sortedRanking = mapRankingOfertas.entrySet().stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .limit(5)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new));
            dto.setRankingEmpresasOfertas(sortedRanking);

            // Formatear meses en español
            Map<String, Integer> tendenciaFormateada = new LinkedHashMap<>();
            String[] nombresMeses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Set", "Oct", "Nov", "Dic"};
            for (Map.Entry<String, Integer> entry : mapMeses.entrySet()) {
                String ym = entry.getKey();
                try {
                    String[] parts = ym.split("-");
                    int mesIdx = Integer.parseInt(parts[1]) - 1;
                    String etiqueta = nombresMeses[mesIdx] + " " + parts[0];
                    tendenciaFormateada.put(etiqueta, entry.getValue());
                } catch (Exception ex) {
                    tendenciaFormateada.put(ym, entry.getValue());
                }
            }
            dto.setTendenciaMeses(tendenciaFormateada);

            dto.setEmpresasFiltradas(lista);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dto;
    }

    public int contarOfertas(int idEmpresa) {
        String sql = "SELECT COUNT(*) FROM ofertas_practicas WHERE id_empresa = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
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
