package com.propath.conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PruebaConexionBD {

    public static void main(String[] args) {
        System.out.println("=== PRUEBA DE CONEXION JDBC PROPATH ===");
        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            if (conn != null && !conn.isClosed()) {
                System.out.println("[OK] Conexion abierta exitosamente: " + conn.getMetaData().getDatabaseProductName() + " " + conn.getMetaData().getDatabaseProductVersion());
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT 1 AS test");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("[OK] Consulta SELECT 1 ejecutada correctamente. Valor: " + rs.getInt("test"));
                }
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS total FROM usuarios");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("[OK] Consulta de solo lectura a tabla 'usuarios'. Total registros: " + rs.getInt("total"));
                }
            }

            String hashPrueba = SeguridadUtil.encriptarPassword("PruebaSegura123");
            boolean esValido = SeguridadUtil.verificarPassword("PruebaSegura123", hashPrueba);
            if (esValido) {
                System.out.println("[OK] SeguridadUtil (BCrypt) funciona correctamente con hashes compatibles.");
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Fallo en conexion JDBC: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConexionBD.cerrar(conn);
            try {
                if (conn != null && conn.isClosed()) {
                    System.out.println("[OK] Conexion cerrada correctamente.");
                }
            } catch (SQLException e) {
                System.err.println("[ERROR] Error al verificar cierre de conexion: " + e.getMessage());
            }
        }
        System.out.println("=== FIN DE PRUEBA JDBC ===");
    }
}
