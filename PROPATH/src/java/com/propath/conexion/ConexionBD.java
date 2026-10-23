package com.propath.conexion;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexionBD {

    private static String driver = "com.mysql.cj.jdbc.Driver";
    private static String url = "jdbc:mysql://localhost:3306/propath_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String user = "root";
    private static String password = "";

    static {
        cargarConfiguracion();
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("org.mariadb.jdbc.Driver");
                driver = "org.mariadb.jdbc.Driver";
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException("Driver JDBC no encontrado", ex);
            }
        }
    }

    private ConexionBD() {
    }

    private static void cargarConfiguracion() {
        Properties props = new Properties();
        try (InputStream is = ConexionBD.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
                if (props.getProperty("db.driver") != null) {
                    driver = props.getProperty("db.driver").trim();
                }
                if (props.getProperty("db.url") != null) {
                    url = props.getProperty("db.url").trim();
                }
                if (props.getProperty("db.user") != null) {
                    user = props.getProperty("db.user").trim();
                }
                if (props.getProperty("db.password") != null) {
                    password = props.getProperty("db.password").trim();
                }
            }
        } catch (Exception ignored) {
        }

        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            url = envUrl;
        }
        String envUser = System.getenv("DB_USER");
        if (envUser != null && !envUser.isBlank()) {
            user = envUser;
        }
        String envPass = System.getenv("DB_PASSWORD");
        if (envPass != null) {
            password = envPass;
        }
    }

public static Connection getConexion() throws SQLException {
    Connection conn = DriverManager.getConnection(url, user, password);
    System.out.println("PROPATH JDBC - Base de datos: " + conn.getCatalog());
    System.out.println("PROPATH JDBC - Servidor: " + conn.getMetaData().getURL());
    return conn;
}

    public static void cerrar(Connection conexion) {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public static void cerrar(AutoCloseable... recursos) {
        if (recursos != null) {
            for (AutoCloseable recurso : recursos) {
                if (recurso != null) {
                    try {
                        recurso.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    public static boolean probarConexion() {
        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement("SELECT 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }
}
