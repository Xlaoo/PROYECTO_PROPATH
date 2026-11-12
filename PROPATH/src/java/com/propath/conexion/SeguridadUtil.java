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

    /**
     * Escapa caracteres especiales de HTML de forma segura contra inyecciones XSS.
     * Maneja valores nulos retornando cadena vacía y reemplaza los caracteres peligrosos.
     *
     * @param texto Cadena de entrada a sanitizar.
     * @return Cadena segura para su inserción en atributos o contenido HTML.
     */
    public static String escaparHtml(String texto) {
        if (texto == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(texto.length() + 16);
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }
}
