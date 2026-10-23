package com.propath.conexion;

import org.mindrot.jbcrypt.BCrypt;

public final class SeguridadUtil {

    private SeguridadUtil() {
    }

    public static String encriptarPassword(String passwordPlana) {
        if (passwordPlana == null || passwordPlana.isEmpty()) {
            return null;
        }
        return BCrypt.hashpw(passwordPlana, BCrypt.gensalt(10));
    }

    public static String encriptar(String passwordPlana) {
        return encriptarPassword(passwordPlana);
    }

    public static boolean verificarPassword(String passwordPlana, String passwordHasheada) {
        if (passwordPlana == null || passwordHasheada == null || passwordHasheada.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(passwordPlana, passwordHasheada);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
