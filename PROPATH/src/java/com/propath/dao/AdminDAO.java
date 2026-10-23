package com.propath.dao;

import com.propath.conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AdminDAO {

    public Map<String, Object> obtenerEstadisticasAdmin() {
        Map<String, Object> map = new HashMap<>();
        try (Connection conn = ConexionBD.getConexion()) {
            map.put("totalEstudiantes", contar(conn, "SELECT COUNT(*) FROM estudiantes"));
            map.put("estudiantesEstaSemana", contar(conn, "SELECT COUNT(*) FROM estudiantes e INNER JOIN usuarios u ON e.id_usuario = u.id_usuario WHERE u.fecha_registro >= DATE_SUB(NOW(), INTERVAL 7 DAY)"));
            map.put("totalEmpresas", contar(conn, "SELECT COUNT(*) FROM empresas"));
            map.put("empresasAprobadas", contar(conn, "SELECT COUNT(*) FROM empresas WHERE estado = 'APROBADA'"));
            map.put("empresasPendientes", contar(conn, "SELECT COUNT(*) FROM empresas WHERE estado = 'PENDIENTE'"));
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
}
