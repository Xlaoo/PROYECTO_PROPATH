package com.propath.servicio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RecuperacionContrasenaServicio {

    private static final long EXPIRACION_CODIGO_MS = 10 * 60 * 1000L;
    private static final long EXPIRACION_TOKEN_MS = 10 * 60 * 1000L;
    private static final long COOLDOWN_CORREO_MS = 60 * 1000L;
    private static final int MAX_INTENTOS_CODIGO = 5;
    private static final int MAX_SOLICITUDES_IP = 5;
    private static final long VENTANA_IP_MS = 10 * 60 * 1000L;

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Map<String, DatosCodigo> codigosPorCorreo = new ConcurrentHashMap<>();
    private static final Map<String, DatosTokenReset> tokensReset = new ConcurrentHashMap<>();
    private static final Map<String, Long> ultimoEnvioPorCorreo = new ConcurrentHashMap<>();
    private static final Map<String, RegistroIp> solicitudesPorIp = new ConcurrentHashMap<>();

    private RecuperacionContrasenaServicio() {
    }

    private static class DatosCodigo {
        final int idUsuario;
        final String correo;
        final String hashCodigo;
        final long expiracion;
        int intentosRestantes;

        DatosCodigo(int idUsuario, String correo, String hashCodigo, long expiracion, int intentosRestantes) {
            this.idUsuario = idUsuario;
            this.correo = correo;
            this.hashCodigo = hashCodigo;
            this.expiracion = expiracion;
            this.intentosRestantes = intentosRestantes;
        }
    }

    public static class DatosTokenReset {
        public final int idUsuario;
        public final String correo;
        public final long expiracion;

        DatosTokenReset(int idUsuario, String correo, long expiracion) {
            this.idUsuario = idUsuario;
            this.correo = correo;
            this.expiracion = expiracion;
        }
    }

    private static class RegistroIp {
        long inicioVentana;
        int conteo;

        RegistroIp(long inicioVentana, int conteo) {
            this.inicioVentana = inicioVentana;
            this.conteo = conteo;
        }
    }

    public static synchronized boolean validarRateLimitIp(String ip) {
        limpiarExpirados();
        if (ip == null || ip.isBlank()) {
            return true;
        }
        long ahora = System.currentTimeMillis();
        RegistroIp reg = solicitudesPorIp.get(ip);
        if (reg == null || (ahora - reg.inicioVentana) > VENTANA_IP_MS) {
            solicitudesPorIp.put(ip, new RegistroIp(ahora, 1));
            return true;
        }
        if (reg.conteo >= MAX_SOLICITUDES_IP) {
            return false;
        }
        reg.conteo++;
        return true;
    }

    public static synchronized boolean puedeSolicitarCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return false;
        }
        String normalizado = correo.trim().toLowerCase();
        Long ultimo = ultimoEnvioPorCorreo.get(normalizado);
        if (ultimo != null && (System.currentTimeMillis() - ultimo) < COOLDOWN_CORREO_MS) {
            return false;
        }
        return true;
    }

    public static synchronized String generarYGuardarCodigo(int idUsuario, String correo) {
        limpiarExpirados();
        if (idUsuario <= 0 || correo == null || correo.isBlank()) {
            return null;
        }
        String normalizado = correo.trim().toLowerCase();
        int numero = 100000 + RANDOM.nextInt(900000);
        String codigo = String.valueOf(numero);
        String hash = calcularHashSha256(codigo);

        long ahora = System.currentTimeMillis();
        codigosPorCorreo.put(normalizado, new DatosCodigo(idUsuario, normalizado, hash, ahora + EXPIRACION_CODIGO_MS, MAX_INTENTOS_CODIGO));
        ultimoEnvioPorCorreo.put(normalizado, ahora);
        return codigo;
    }

    public static synchronized ResultadoValidacion validarCodigo(String correo, String codigoIngresado) {
        limpiarExpirados();
        if (correo == null || correo.isBlank() || codigoIngresado == null || codigoIngresado.isBlank()) {
            return new ResultadoValidacion(false, "El correo y el codigo son obligatorios.", null);
        }
        String normalizado = correo.trim().toLowerCase();
        DatosCodigo datos = codigosPorCorreo.get(normalizado);
        if (datos == null) {
            return new ResultadoValidacion(false, "No existe un codigo activo para este correo o ya expiro.", null);
        }

        long ahora = System.currentTimeMillis();
        if (ahora > datos.expiracion) {
            codigosPorCorreo.remove(normalizado);
            return new ResultadoValidacion(false, "El codigo ha expirado. Solicite uno nuevo.", null);
        }

        if (datos.intentosRestantes <= 0) {
            codigosPorCorreo.remove(normalizado);
            return new ResultadoValidacion(false, "Ha superado el numero maximo de intentos permitidos. Solicite un nuevo codigo.", null);
        }

        String hashIngresado = calcularHashSha256(codigoIngresado.trim());
        if (!datos.hashCodigo.equals(hashIngresado)) {
            datos.intentosRestantes--;
            if (datos.intentosRestantes <= 0) {
                codigosPorCorreo.remove(normalizado);
                return new ResultadoValidacion(false, "Codigo incorrecto. Ha alcanzado el limite de 5 intentos. Solicite un nuevo codigo.", null);
            }
            return new ResultadoValidacion(false, "Codigo de verificacion incorrecto. Le quedan " + datos.intentosRestantes + " intentos.", null);
        }

        codigosPorCorreo.remove(normalizado);

        String tokenReset = generarTokenHexSeguro(32);
        tokensReset.put(tokenReset, new DatosTokenReset(datos.idUsuario, normalizado, ahora + EXPIRACION_TOKEN_MS));
        return new ResultadoValidacion(true, "Codigo verificado correctamente.", tokenReset);
    }

    public static synchronized DatosTokenReset obtenerYValidarTokenReset(String token) {
        limpiarExpirados();
        if (token == null || token.isBlank()) {
            return null;
        }
        DatosTokenReset datos = tokensReset.get(token.trim());
        if (datos == null) {
            return null;
        }
        if (System.currentTimeMillis() > datos.expiracion) {
            tokensReset.remove(token.trim());
            return null;
        }
        return datos;
    }

    public static synchronized void consumirTokenReset(String token) {
        if (token != null) {
            DatosTokenReset datos = tokensReset.remove(token.trim());
            if (datos != null) {
                codigosPorCorreo.remove(datos.correo);
            }
        }
    }

    private static String calcularHashSha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error calculando hash SHA-256", e);
        }
    }

    private static String generarTokenHexSeguro(int bytesLength) {
        byte[] bytes = new byte[bytesLength];
        RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static void limpiarExpirados() {
        long ahora = System.currentTimeMillis();
        codigosPorCorreo.entrySet().removeIf(entry -> ahora > entry.getValue().expiracion || entry.getValue().intentosRestantes <= 0);
        tokensReset.entrySet().removeIf(entry -> ahora > entry.getValue().expiracion);
        ultimoEnvioPorCorreo.entrySet().removeIf(entry -> (ahora - entry.getValue()) > (COOLDOWN_CORREO_MS * 2));
        solicitudesPorIp.entrySet().removeIf(entry -> (ahora - entry.getValue().inicioVentana) > VENTANA_IP_MS);
    }

    public static class ResultadoValidacion {
        private final boolean exito;
        private final String mensaje;
        private final String tokenReset;

        public ResultadoValidacion(boolean exito, String mensaje, String tokenReset) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.tokenReset = tokenReset;
        }

        public boolean isExito() {
            return exito;
        }

        public String getMensaje() {
            return mensaje;
        }

        public String getTokenReset() {
            return tokenReset;
        }
    }
}
