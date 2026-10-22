package com.propath.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.Console;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class CrearAdmin {

    private static final String DB_URL = "jdbc:mariadb://localhost:3306/propath_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   PROPATH - REGISTRO SEGURO DE ADMINISTRADOR     ");
        System.out.println("==================================================");

        String nombre = "Administrador PROPATH";
        String correo = "admin@propath.com";
        String rol = "ADMIN";
        String estado = "ACTIVO";

        try {
            
            Class.forName("org.mariadb.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                
                String checkSql = "SELECT id_usuario, correo FROM usuarios WHERE rol = 'ADMIN'";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                     ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("⚠️ Ya existe una cuenta con rol ADMIN en MariaDB (ID: " 
                                + rs.getInt("id_usuario") + ", Correo: " + rs.getString("correo") + ").");
                        System.out.println("No se realizarán duplicados.");
                        return;
                    }
                }

                
                char[] claveChars;
                Console console = System.console();
                if (console != null) {
                    claveChars = console.readPassword("Ingrese la contraseña para el Administrador (%s): ", correo);
                } else {
                    System.out.print("Ingrese la contraseña para el Administrador (" + correo + "): ");
                    Scanner scanner = new Scanner(System.in);
                    claveChars = scanner.nextLine().toCharArray();
                }

                if (claveChars == null || claveChars.length == 0) {
                    System.err.println("❌ Error: La contraseña no puede estar vacía.");
                    return;
                }

                String clavePlana = new String(claveChars);

                
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                String hashBcrypt = encoder.encode(clavePlana);

                
                String insertSql = "INSERT INTO usuarios (nombre, correo, contrasena, rol, estado) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, nombre);
                    insertStmt.setString(2, correo);
                    insertStmt.setString(3, hashBcrypt);
                    insertStmt.setString(4, rol);
                    insertStmt.setString(5, estado);

                    int filas = insertStmt.executeUpdate();
                    if (filas > 0) {
                        System.out.println("\n✅ Administrador creado exitosamente en MariaDB.");
                        System.out.println("   Nombre: " + nombre);
                        System.out.println("   Correo: " + correo);
                        System.out.println("   Rol:    " + rol);
                        System.out.println("   Estado: " + estado);
                        System.out.println("   Hash BCrypt generado: " + hashBcrypt.substring(0, 10) + "****************");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error durante la creación del administrador: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
