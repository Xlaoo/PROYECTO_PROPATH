package com.propath.dao;

import com.propath.conexion.ConexionBD;
import com.propath.conexion.SeguridadUtil;
import com.propath.modelo.Estudiante;
import com.propath.dto.DashboardEstudiantesDTO;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAO {

    public List<Estudiante> listarTodos() {
        List<Estudiante> lista = new ArrayList<>();
        String sql = "SELECT e.id_estudiante, e.id_usuario, e.dni, e.nombres, e.apellidos, u.correo, "
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
        String sql = "SELECT e.id_estudiante, e.id_usuario, e.dni, e.nombres, e.apellidos, u.correo, "
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
        String sql = "SELECT e.id_estudiante, e.id_usuario, e.dni, e.nombres, e.apellidos, u.correo, "
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
        String sql = "SELECT e.id_estudiante, e.id_usuario, e.dni, e.nombres, e.apellidos, u.correo, "
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

    public Estudiante obtenerPorDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT e.id_estudiante, e.id_usuario, e.dni, e.nombres, e.apellidos, u.correo, "
                   + "e.universidad, e.carrera, e.ciclo, e.telefono, e.cv, u.estado "
                   + "FROM estudiantes e "
                   + "INNER JOIN usuarios u ON e.id_usuario = u.id_usuario "
                   + "WHERE e.dni = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dni.trim());
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

    private void setDniOrNull(PreparedStatement ps, int index, String dni) throws SQLException {
        if (dni == null || dni.trim().isEmpty()) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, dni.trim());
        }
    }

    public int crear(Estudiante estudiante) {
        if (estudiante == null || estudiante.getIdUsuario() <= 0) {
            return -1;
        }

        String sql = "INSERT INTO estudiantes (id_usuario, dni, nombres, apellidos, universidad, carrera, ciclo, telefono, cv) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, estudiante.getIdUsuario());
            setDniOrNull(ps, 2, estudiante.getDni());
            ps.setString(3, estudiante.getNombre() != null ? estudiante.getNombre().trim() : "");
            ps.setString(4, estudiante.getApellido() != null ? estudiante.getApellido().trim() : "");
            ps.setString(5, estudiante.getUniversidad() != null ? estudiante.getUniversidad().trim() : "");
            ps.setString(6, estudiante.getCarrera() != null ? estudiante.getCarrera().trim() : "");
            ps.setInt(7, estudiante.getCiclo());
            ps.setString(8, estudiante.getTelefono() != null ? estudiante.getTelefono().trim() : "");
            ps.setString(9, estudiante.getCv() != null ? estudiante.getCv().trim() : "");

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
        String sqlEstudiante = "INSERT INTO estudiantes (id_usuario, dni, nombres, apellidos, universidad, carrera, ciclo, telefono, cv) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
                setDniOrNull(psEst, 2, estudiante.getDni());
                psEst.setString(3, estudiante.getNombre() != null ? estudiante.getNombre().trim() : "");
                psEst.setString(4, estudiante.getApellido() != null ? estudiante.getApellido().trim() : "");
                psEst.setString(5, estudiante.getUniversidad() != null ? estudiante.getUniversidad().trim() : "");
                psEst.setString(6, estudiante.getCarrera() != null ? estudiante.getCarrera().trim() : "");
                psEst.setInt(7, estudiante.getCiclo());
                psEst.setString(8, estudiante.getTelefono() != null ? estudiante.getTelefono().trim() : "");
                psEst.setString(9, estudiante.getCv() != null ? estudiante.getCv().trim() : "");

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

        String sqlEst = "UPDATE estudiantes SET dni = ?, nombres = ?, apellidos = ?, universidad = ?, carrera = ?, ciclo = ?, telefono = ?, cv = ? WHERE id_estudiante = ?";
        String sqlUser = "UPDATE usuarios SET nombre = ?, apellidos = ? WHERE id_usuario = ?";

        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlEst)) {
                setDniOrNull(ps, 1, estudiante.getDni());
                ps.setString(2, estudiante.getNombre() != null ? estudiante.getNombre().trim() : "");
                ps.setString(3, estudiante.getApellido() != null ? estudiante.getApellido().trim() : "");
                ps.setString(4, estudiante.getUniversidad() != null ? estudiante.getUniversidad().trim() : "");
                ps.setString(5, estudiante.getCarrera() != null ? estudiante.getCarrera().trim() : "");
                ps.setInt(6, estudiante.getCiclo());
                ps.setString(7, estudiante.getTelefono() != null ? estudiante.getTelefono().trim() : "");
                ps.setString(8, estudiante.getCv() != null ? estudiante.getCv().trim() : "");
                ps.setInt(9, estudiante.getId());

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

    public boolean actualizarConEstado(Estudiante estudiante, String nuevoEstado) {
        if (estudiante == null || estudiante.getId() <= 0) {
            return false;
        }

        String sqlEst = "UPDATE estudiantes SET dni = ?, nombres = ?, apellidos = ?, universidad = ?, carrera = ?, ciclo = ?, telefono = ? WHERE id_estudiante = ?";
        String sqlUser = "UPDATE usuarios SET nombre = ?, apellidos = ?, estado = ? WHERE id_usuario = ?";

        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlEst)) {
                setDniOrNull(ps, 1, estudiante.getDni());
                ps.setString(2, estudiante.getNombre() != null ? estudiante.getNombre().trim() : "");
                ps.setString(3, estudiante.getApellido() != null ? estudiante.getApellido().trim() : "");
                ps.setString(4, estudiante.getUniversidad() != null ? estudiante.getUniversidad().trim() : "");
                ps.setString(5, estudiante.getCarrera() != null ? estudiante.getCarrera().trim() : "");
                ps.setInt(6, estudiante.getCiclo());
                ps.setString(7, estudiante.getTelefono() != null ? estudiante.getTelefono().trim() : "");
                ps.setInt(8, estudiante.getId());
                ps.executeUpdate();
            }

            if (estudiante.getIdUsuario() > 0) {
                try (PreparedStatement psUser = conn.prepareStatement(sqlUser)) {
                    psUser.setString(1, estudiante.getNombre() != null ? estudiante.getNombre().trim() : "");
                    psUser.setString(2, estudiante.getApellido() != null ? estudiante.getApellido().trim() : "");
                    psUser.setString(3, nuevoEstado != null && !nuevoEstado.trim().isEmpty() ? nuevoEstado.trim().toUpperCase() : "ACTIVO");
                    psUser.setInt(4, estudiante.getIdUsuario());
                    psUser.executeUpdate();
                }
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

    public int contarPostulaciones(int idEstudiante) {
        String sql = "SELECT COUNT(*) FROM postulaciones WHERE id_estudiante = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
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

            try (PreparedStatement psDoc = conn.prepareStatement("DELETE FROM documentos_estudiante WHERE id_estudiante = ?")) {
                psDoc.setInt(1, idEstudiante);
                psDoc.executeUpdate();
            }

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

    /**
     * Obtiene el DTO completo del Dashboard interactivo de Estudiantes para los segmentadores
     * combinables: universidad, carrera, ciclo, estado de cuenta y presencia de CV.
     */
    public DashboardEstudiantesDTO obtenerDashboardEstudiantes(
            String universidad, String carrera, int ciclo,
            String estadoCuenta, String cv) {
        return obtenerDashboardEstudiantes(universidad, carrera, ciclo, estadoCuenta, cv, "", "", "");
    }

    /**
     * Obtiene el DTO completo del Dashboard de Estudiantes con sus segmentadores,
     * indicadores KPI, mapas de distribuciÃƒÂ³n y lista de postulantes filtrados.
     */
    public DashboardEstudiantesDTO obtenerDashboardEstudiantes(
            String universidad, String carrera, int ciclo,
            String estadoCuenta, String cv,
            String fechaDesde, String fechaHasta, String busqueda) {

        DashboardEstudiantesDTO dto = new DashboardEstudiantesDTO();
        dto.setUniversidad(universidad != null ? universidad : "TODOS");
        dto.setCarrera(carrera != null ? carrera : "TODOS");
        dto.setCiclo(ciclo);
        dto.setEstadoCuenta(estadoCuenta != null ? estadoCuenta : "TODOS");
        dto.setCv(cv != null ? cv : "TODOS");
        dto.setFechaDesde(fechaDesde != null ? fechaDesde : "");
        dto.setFechaHasta(fechaHasta != null ? fechaHasta : "");
        dto.setBusqueda(busqueda != null ? busqueda : "");

        boolean filtrosActivos = (universidad != null && !universidad.trim().isEmpty() && !"TODOS".equalsIgnoreCase(universidad.trim()))
                || (carrera != null && !carrera.trim().isEmpty() && !"TODOS".equalsIgnoreCase(carrera.trim()))
                || (ciclo > 0)
                || (estadoCuenta != null && !estadoCuenta.trim().isEmpty() && !"TODOS".equalsIgnoreCase(estadoCuenta.trim()))
                || (cv != null && !cv.trim().isEmpty() && !"TODOS".equalsIgnoreCase(cv.trim()))
                || (fechaDesde != null && !fechaDesde.trim().isEmpty())
                || (fechaHasta != null && !fechaHasta.trim().isEmpty())
                || (busqueda != null && !busqueda.trim().isEmpty());
        dto.setFiltrosActivos(filtrosActivos);

        try (Connection conn = ConexionBD.getConexion()) {
            // 1. Opciones dinÃƒÂ¡micas para selects de Universidad y Carrera
            List<String> opcionesUniv = new ArrayList<>();
            String sqlUniv = "SELECT DISTINCT TRIM(universidad) AS u FROM estudiantes WHERE universidad IS NOT NULL AND TRIM(universidad) != '' ORDER BY 1";
            try (PreparedStatement psU = conn.prepareStatement(sqlUniv);
                 ResultSet rsU = psU.executeQuery()) {
                while (rsU.next()) {
                    opcionesUniv.add(rsU.getString("u"));
                }
            }
            dto.setOpcionesUniversidades(opcionesUniv);

            List<String> opcionesCarr = new ArrayList<>();
            String sqlCarr = "SELECT DISTINCT TRIM(carrera) AS c FROM estudiantes WHERE carrera IS NOT NULL AND TRIM(carrera) != '' ORDER BY 1";
            try (PreparedStatement psC = conn.prepareStatement(sqlCarr);
                 ResultSet rsC = psC.executeQuery()) {
                while (rsC.next()) {
                    opcionesCarr.add(rsC.getString("c"));
                }
            }
            dto.setOpcionesCarreras(opcionesCarr);

            // 2. Consulta filtrada
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT e.id_estudiante, e.id_usuario, e.nombres, e.apellidos, u.correo, ")
               .append("e.universidad, e.carrera, e.ciclo, e.telefono, e.cv, u.estado AS estado_cuenta, ")
               .append("u.fecha_registro, ")
               .append("(CASE WHEN (e.cv IS NOT NULL AND TRIM(e.cv) != '') OR EXISTS (SELECT 1 FROM documentos_estudiante d WHERE d.id_estudiante = e.id_estudiante) THEN 1 ELSE 0 END) AS tiene_cv ")
               .append("FROM estudiantes e ")
               .append("INNER JOIN usuarios u ON e.id_usuario = u.id_usuario ")
               .append("WHERE 1=1 ");

            List<Object> params = new ArrayList<>();

            if (universidad != null && !universidad.trim().isEmpty() && !"TODOS".equalsIgnoreCase(universidad.trim())) {
                sql.append("AND LOWER(TRIM(e.universidad)) = LOWER(?) ");
                params.add(universidad.trim());
            }

            if (carrera != null && !carrera.trim().isEmpty() && !"TODOS".equalsIgnoreCase(carrera.trim())) {
                sql.append("AND LOWER(TRIM(e.carrera)) = LOWER(?) ");
                params.add(carrera.trim());
            }

            if (ciclo > 0) {
                sql.append("AND e.ciclo = ? ");
                params.add(ciclo);
            }

            if (estadoCuenta != null && !estadoCuenta.trim().isEmpty() && !"TODOS".equalsIgnoreCase(estadoCuenta.trim())) {
                sql.append("AND UPPER(u.estado) = ? ");
                params.add(estadoCuenta.trim().toUpperCase());
            }

            if ("CON_CV".equalsIgnoreCase(cv)) {
                sql.append("AND ((e.cv IS NOT NULL AND TRIM(e.cv) != '') OR EXISTS (SELECT 1 FROM documentos_estudiante d WHERE d.id_estudiante = e.id_estudiante)) ");
            } else if ("SIN_CV".equalsIgnoreCase(cv)) {
                sql.append("AND ((e.cv IS NULL OR TRIM(e.cv) = '') AND NOT EXISTS (SELECT 1 FROM documentos_estudiante d WHERE d.id_estudiante = e.id_estudiante)) ");
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
                sql.append("AND (LOWER(e.nombres) LIKE ? OR LOWER(e.apellidos) LIKE ? OR LOWER(u.correo) LIKE ? OR LOWER(e.universidad) LIKE ? OR LOWER(e.carrera) LIKE ?) ");
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
            }

            sql.append("ORDER BY e.id_estudiante DESC");

            List<Estudiante> lista = new ArrayList<>();
            Set<String> univSet = new HashSet<>();
            Set<String> carrSet = new HashSet<>();
            Map<String, Integer> mapUniv = new HashMap<>();
            Map<String, Integer> mapCarr = new HashMap<>();
            Map<Integer, Integer> mapCiclo = new LinkedHashMap<>();
            for (int i = 1; i <= 12; i++) {
                mapCiclo.put(i, 0);
            }
            Map<String, Integer> mapEstado = new LinkedHashMap<>();
            mapEstado.put("Activo", 0);
            mapEstado.put("Inactivo", 0);
            mapEstado.put("Bloqueado", 0);

            Map<String, Integer> mapCv = new LinkedHashMap<>();
            mapCv.put("Con CV", 0);
            mapCv.put("Sin CV", 0);

            Map<String, Integer> mapMeses = new TreeMap<>();

            int activos = 0;
            int inactivos = 0;
            int bloqueados = 0;
            int conCvCount = 0;
            int sinCvCount = 0;

            SimpleDateFormat sdfRegistro = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdfMesClave = new SimpleDateFormat("yyyy-MM");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Estudiante est = new Estudiante();
                        est.setId(rs.getInt("id_estudiante"));
                        est.setIdUsuario(rs.getInt("id_usuario"));
                        est.setNombre(rs.getString("nombres"));
                        est.setApellido(rs.getString("apellidos"));
                        est.setCorreo(rs.getString("correo"));
                        est.setUniversidad(rs.getString("universidad"));
                        est.setCarrera(rs.getString("carrera"));
                        est.setCiclo(rs.getInt("ciclo"));
                        est.setTelefono(rs.getString("telefono"));
                        est.setCv(rs.getString("cv"));
                        est.setEstado(rs.getString("estado_cuenta"));

                        java.sql.Timestamp ts = rs.getTimestamp("fecha_registro");
                        if (ts != null) {
                            est.setFechaRegistro(sdfRegistro.format(ts));
                            String mesClave = sdfMesClave.format(ts);
                            mapMeses.put(mesClave, mapMeses.getOrDefault(mesClave, 0) + 1);
                        }

                        boolean tieneCv = rs.getInt("tiene_cv") == 1;
                        if (tieneCv && (est.getCv() == null || est.getCv().trim().isEmpty())) {
                            est.setCv("doc");
                        }

                        // MÃƒÂ©tricas
                        String st = est.getEstado() != null ? est.getEstado().toUpperCase() : "ACTIVO";
                        if ("ACTIVO".equals(st)) {
                            activos++;
                            mapEstado.put("Activo", mapEstado.get("Activo") + 1);
                        } else if ("INACTIVO".equals(st)) {
                            inactivos++;
                            mapEstado.put("Inactivo", mapEstado.get("Inactivo") + 1);
                        } else if ("BLOQUEADO".equals(st)) {
                            bloqueados++;
                            mapEstado.put("Bloqueado", mapEstado.get("Bloqueado") + 1);
                        }

                        if (tieneCv) {
                            conCvCount++;
                            mapCv.put("Con CV", mapCv.get("Con CV") + 1);
                        } else {
                            sinCvCount++;
                            mapCv.put("Sin CV", mapCv.get("Sin CV") + 1);
                        }

                        if (est.getUniversidad() != null && !est.getUniversidad().trim().isEmpty()) {
                            String uNorm = est.getUniversidad().trim();
                            univSet.add(uNorm);
                            mapUniv.put(uNorm, mapUniv.getOrDefault(uNorm, 0) + 1);
                        }

                        if (est.getCarrera() != null && !est.getCarrera().trim().isEmpty()) {
                            String cNorm = est.getCarrera().trim();
                            carrSet.add(cNorm);
                            mapCarr.put(cNorm, mapCarr.getOrDefault(cNorm, 0) + 1);
                        }

                        int c = est.getCiclo();
                        if (c >= 1 && c <= 12) {
                            mapCiclo.put(c, mapCiclo.get(c) + 1);
                        }

                        lista.add(est);
                    }
                }
            }

            dto.setTotalEstudiantes(lista.size());
            dto.setActivos(activos);
            dto.setInactivos(inactivos);
            dto.setBloqueados(bloqueados);
            dto.setConCv(conCvCount);
            dto.setSinCv(sinCvCount);
            dto.setTotalUniversidades(univSet.size());
            dto.setTotalCarreras(carrSet.size());
            dto.setRegistrosPeriodo(lista.size());

            // Top universidades ordenadas de mayor a menor
            Map<String, Integer> sortedUniv = mapUniv.entrySet().stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new));
            dto.setDistribucionUniversidades(sortedUniv);

            // Top carreras ordenadas de mayor a menor
            Map<String, Integer> sortedCarr = mapCarr.entrySet().stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new));
            dto.setDistribucionCarreras(sortedCarr);

            dto.setDistribucionCiclos(mapCiclo);
            dto.setDistribucionEstados(mapEstado);
            dto.setDistribucionCv(mapCv);

            // Formatear meses en espaÃƒÂ±ol
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

            dto.setEstudiantesFiltrados(lista);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dto;
    }

    private Estudiante mapearEstudiante(ResultSet rs) throws SQLException {
        Estudiante e = new Estudiante();
        e.setId(rs.getInt("id_estudiante"));
        e.setIdUsuario(rs.getInt("id_usuario"));
        try {
            e.setDni(rs.getString("dni"));
        } catch (SQLException ignored) {
        }
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
