package com.propath.util;

import com.propath.conexion.ConexionBD;
import com.propath.conexion.SeguridadUtil;
import java.io.Console;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class CrearAdmin {

    public static void main(String[] args) {
        String correo = "admin@propath.com";
        String nombre = "Administrador PROPATH";
        String rol = "ADMIN";
        String estado = "ACTIVO";

        try {
            char[] claveChars;
            Console console = System.console();
            if (console != null) {
                claveChars = console.readPassword("Ingrese la contrasena para el Administrador (%s): ", correo);
            } else {
                @SuppressWarnings("resource")
                Scanner scanner = new Scanner(System.in);
                if (scanner.hasNextLine()) {
                    claveChars = scanner.nextLine().toCharArray();
                } else {
                    claveChars = new char[0];
                }
            }

            if (claveChars == null || claveChars.length == 0) {
                System.err.println("Error: La contrasena no puede estar vacia.");
                return;
            }

            String clavePlana = new String(claveChars);
            String hashBcrypt = SeguridadUtil.encriptarPassword(clavePlana);

            try (Connection conn = ConexionBD.getConexion()) {
                String checkSql = "SELECT id_usuario FROM usuarios WHERE correo = ?";
                int idExistente = -1;
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, correo);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            idExistente = rs.getInt("id_usuario");
                        }
                    }
                }

                if (idExistente > 0) {
                    String updateSql = "UPDATE usuarios SET contrasena = ?, estado = 'ACTIVO', rol = 'ADMIN' WHERE id_usuario = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, hashBcrypt);
                        updateStmt.setInt(2, idExistente);
                        updateStmt.executeUpdate();
                    }
                    System.out.println("Administrador actualizado exitosamente en MySQL.");
                } else {
                    String insertSql = "INSERT INTO usuarios (nombre, correo, contrasena, rol, estado) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, nombre);
                        insertStmt.setString(2, correo);
                        insertStmt.setString(3, hashBcrypt);
                        insertStmt.setString(4, rol);
                        insertStmt.setString(5, estado);
                        insertStmt.executeUpdate();
                    }
                    System.out.println("Administrador creado exitosamente en MySQL.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error durante la configuracion del administrador: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
