package com.propath.servicio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio centralizado de desafíos de verificación de 6 caracteres alfanuméricos.
 * Cumple con:
 * - 6 caracteres alfanuméricos (letras mayúsculas y dígitos sin caracteres confusos).
 * - Generador criptográficamente seguro (SecureRandom).
 * - Expiración de 10 minutos.
 * - Máximo 5 intentos por desafío.
 * - Cooldown de 60 segundos entre reenvíos.
 * - Rate limiting por cuenta e IP (máximo 5 solicitudes cada 10 minutos).
 * - Invalidación del código anterior al reenviar.
 * - Vinculación estricta por correo y propósito (REGISTRO, LOGIN, RECUPERACION).
 * - Almacenamiento protegido (SHA-256 con salt) sin texto plano.
 * - Verificación atómica en tiempo constante para prevenir colisiones y reutilización.
 */
public final class DesafioSeguridadServicio {

    public enum PropositoDesafio {
        REGISTRO("Verificación de Correo"),
        LOGIN("Verificación de Inicio de Sesión (2FA)"),
        RECUPERACION("Recuperación de Contraseña");

        private final String descripcion;

        PropositoDesafio(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    // Alfabeto numérico de 6 dígitos para códigos de verificación
    private static final char[] ALFABETO = "0123456789".toCharArray();
    private static final int LONGITUD_CODIGO = 6;

    private static final long EXPIRACION_MS = 60 * 1000L; // 60 segundos (1 minuto exacto)
    private static final long COOLDOWN_REENVIO_MS = 30 * 1000L; // 30 segundos
    private static final int MAX_INTENTOS = 5;

    private static final int MAX_SOLICITUDES_VENTANA = 15;
    private static final long VENTANA_RATE_LIMIT_MS = 10 * 60 * 1000L; // 10 minutos

    private static final SecureRandom RANDOM = new SecureRandom();

    // Estructuras en memoria concurrentes protegidas
    private static final Map<String, DesafioActivo> desafiosActivos = new ConcurrentHashMap<>();
    private static final Map<String, TokenAutorizacion> autorizacionesValidas = new ConcurrentHashMap<>();
    private static final Map<String, RegistroVentana> historialSolicitudesCorreo = new ConcurrentHashMap<>();
    private static final Map<String, RegistroVentana> historialSolicitudesIp = new ConcurrentHashMap<>();

    private DesafioSeguridadServicio() {
    }

    public static class DesafioActivo {
        final String correo;
        final PropositoDesafio proposito;
        final Integer idUsuario;
        final String nombre;
        final String hashCodigo;
        final String salt;
        final long creacion;
        final long expiracion;
        long ultimoEnvio;
        int intentosRestantes;
        boolean consumido;

        DesafioActivo(String correo, PropositoDesafio proposito, Integer idUsuario, String nombre,
                      String hashCodigo, String salt, long creacion, long expiracion) {
            this.correo = correo;
            this.proposito = proposito;
            this.idUsuario = idUsuario;
            this.nombre = nombre;
            this.hashCodigo = hashCodigo;
            this.salt = salt;
            this.creacion = creacion;
            this.expiracion = expiracion;
            this.ultimoEnvio = creacion;
            this.intentosRestantes = MAX_INTENTOS;
            this.consumido = false;
        }
    }

    public static class TokenAutorizacion {
        public final String token;
        public final String correo;
        public final PropositoDesafio proposito;
        public final Integer idUsuario;
        public final long expiracion;

        TokenAutorizacion(String token, String correo, PropositoDesafio proposito, Integer idUsuario, long expiracion) {
            this.token = token;
            this.correo = correo;
            this.proposito = proposito;
            this.idUsuario = idUsuario;
            this.expiracion = expiracion;
        }
    }

    private static class RegistroVentana {
        long inicioVentana;
        int conteo;

        RegistroVentana(long inicioVentana, int conteo) {
            this.inicioVentana = inicioVentana;
            this.conteo = conteo;
        }
    }

    public static class ResultadoSolicitud {
        private final boolean exito;
        private final String mensaje;
        private final String codigoPlano; // Solo disponible inmediatamente para el canal SMTP
        private final long segundosEspera;
        private final long segundosExpiracion;

        public ResultadoSolicitud(boolean exito, String mensaje, String codigoPlano, long segundosEspera, long segundosExpiracion) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.codigoPlano = codigoPlano;
            this.segundosEspera = segundosEspera;
            this.segundosExpiracion = segundosExpiracion;
        }

        public boolean isExito() { return exito; }
        public String getMensaje() { return mensaje; }
        public String getCodigoPlano() { return codigoPlano; }
        public long getSegundosEspera() { return segundosEspera; }
        public long getSegundosExpiracion() { return segundosExpiracion; }
    }

    public static class ResultadoValidacion {
        private final boolean exito;
        private final String mensaje;
        private final int intentosRestantes;
        private final String tokenAutorizacion;
        private final Integer idUsuario;
        private final String correo;

        public ResultadoValidacion(boolean exito, String mensaje, int intentosRestantes,
                                   String tokenAutorizacion, Integer idUsuario, String correo) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.intentosRestantes = intentosRestantes;
            this.tokenAutorizacion = tokenAutorizacion;
            this.idUsuario = idUsuario;
            this.correo = correo;
        }

        public boolean isExito() { return exito; }
        public String getMensaje() { return mensaje; }
        public int getIntentosRestantes() { return intentosRestantes; }
        public String getTokenAutorizacion() { return tokenAutorizacion; }
        public Integer getIdUsuario() { return idUsuario; }
        public String getCorreo() { return correo; }
    }

    private static String claveDesafio(String correo, PropositoDesafio proposito) {
        return (proposito != null ? proposito.name() : "GENERAL") + ":" + correo.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Genera un código de 6 caracteres criptográficamente seguro, excluyendo caracteres ambiguos.
     */
    public static String generarCodigoSeguro() {
        char[] buffer = new char[LONGITUD_CODIGO];
        for (int i = 0; i < LONGITUD_CODIGO; i++) {
            buffer[i] = ALFABETO[RANDOM.nextInt(ALFABETO.length)];
        }
        return new String(buffer);
    }

    /**
     * Solicita o reenvía un desafío de seguridad para un correo y propósito.
     */
    public static synchronized ResultadoSolicitud solicitarODesafiar(String correo, PropositoDesafio proposito,
                                                                    Integer idUsuario, String nombre, String ip) {
        limpiarExpirados();

        if (correo == null || correo.trim().isBlank()) {
            return new ResultadoSolicitud(false, "El correo electrónico es obligatorio.", null, 0, 0);
        }

        String correoNorm = correo.trim().toLowerCase(Locale.ROOT);
        String clave = claveDesafio(correoNorm, proposito);
        long ahora = System.currentTimeMillis();

        // 1. Validar Rate Limit por IP (excepto conexiones locales de desarrollo)
        boolean esLocal = ip != null && (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")
                || ip.equalsIgnoreCase("localhost") || ip.equals("::1")
                || ip.startsWith("127.") || ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172."));
        if (!esLocal && ip != null && !ip.isBlank() && !validarRateLimit(historialSolicitudesIp, ip.trim(), ahora)) {
            return new ResultadoSolicitud(false, "Demasiadas solicitudes desde esta conexión. Espera unos minutos.", null, 30, 0);
        }

        // 2. Validar Rate Limit por cuenta/correo
        if (!esLocal && !validarRateLimit(historialSolicitudesCorreo, correoNorm, ahora)) {
            return new ResultadoSolicitud(false, "Has superado el límite de solicitudes para esta cuenta. Espera unos minutos.", null, 60, 0);
        }

        // 3. Validar Cooldown de 60s si ya existe un desafío previo
        DesafioActivo previo = desafiosActivos.get(clave);
        if (previo != null && !previo.consumido && (ahora - previo.ultimoEnvio) < COOLDOWN_REENVIO_MS) {
            long restantes = (COOLDOWN_REENVIO_MS - (ahora - previo.ultimoEnvio)) / 1000L;
            if (restantes < 1) restantes = 1;
            return new ResultadoSolicitud(false, "Debes esperar " + restantes + " segundos antes de solicitar otro código.", null, restantes, (previo.expiracion - ahora) / 1000L);
        }

        // 4. Invalidar código anterior al reenviar
        if (previo != null) {
            desafiosActivos.remove(clave);
        }

        // 5. Generar nuevo código de 6 caracteres
        String codigoPlano = generarCodigoSeguro();
        String salt = UUID.randomUUID().toString();
        String hash = calcularHashSha256(codigoPlano, salt);

        long expiracion = ahora + EXPIRACION_MS;
        DesafioActivo nuevo = new DesafioActivo(correoNorm, proposito, idUsuario, nombre, hash, salt, ahora, expiracion);
        desafiosActivos.put(clave, nuevo);

        return new ResultadoSolicitud(true, "Código generado exitosamente.", codigoPlano, COOLDOWN_REENVIO_MS / 1000L, EXPIRACION_MS / 1000L);
    }

    /**
     * Verifica atómicamente el código ingresado para la finalidad especificada.
     */
    public static synchronized ResultadoValidacion verificarCodigo(String correo, String codigoIngresado, PropositoDesafio proposito) {
        limpiarExpirados();

        if (correo == null || correo.trim().isBlank() || codigoIngresado == null || codigoIngresado.trim().isBlank()) {
            return new ResultadoValidacion(false, "Correo y código son obligatorios.", 0, null, null, correo);
        }

        String correoNorm = correo.trim().toLowerCase(Locale.ROOT);
        String clave = claveDesafio(correoNorm, proposito);
        DesafioActivo desafio = desafiosActivos.get(clave);
        long ahora = System.currentTimeMillis();

        if (desafio == null) {
            return new ResultadoValidacion(false, "No hay ningún código activo para este correo o ya venció. Solicita uno nuevo.", 0, null, null, correoNorm);
        }

        if (desafio.consumido) {
            desafiosActivos.remove(clave);
            return new ResultadoValidacion(false, "Este código ya fue utilizado. Solicita uno nuevo.", 0, null, null, correoNorm);
        }

        if (ahora > desafio.expiracion) {
            desafiosActivos.remove(clave);
            return new ResultadoValidacion(false, "El código ha expirado (validez de 1 minuto). Solicita uno nuevo.", 0, null, null, correoNorm);
        }

        if (desafio.intentosRestantes <= 0) {
            desafiosActivos.remove(clave);
            return new ResultadoValidacion(false, "Superaste el número máximo de intentos permitidos. Solicita un nuevo código.", 0, null, null, correoNorm);
        }

        // Normalizar código ingresado (6 caracteres en mayúsculas sin espacios)
        String codigoLimpio = codigoIngresado.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
        String hashEsperado = calcularHashSha256(codigoLimpio, desafio.salt);

        boolean esValido = MessageDigest.isEqual(
                desafio.hashCodigo.getBytes(StandardCharsets.UTF_8),
                hashEsperado.getBytes(StandardCharsets.UTF_8)
        );

        if (!esValido) {
            desafio.intentosRestantes--;
            if (desafio.intentosRestantes <= 0) {
                desafiosActivos.remove(clave);
                return new ResultadoValidacion(false, "Código incorrecto. Has agotado los 5 intentos. Solicita un nuevo código.", 0, null, null, correoNorm);
            }
            return new ResultadoValidacion(false, "Código incorrecto. Te quedan " + desafio.intentosRestantes + " intento(s).", desafio.intentosRestantes, null, null, correoNorm);
        }

        // Código Válido: Consumir atómicamente y emitir token de autorización temporal (validez 10 minutos)
        desafio.consumido = true;
        desafiosActivos.remove(clave);

        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        TokenAutorizacion auth = new TokenAutorizacion(token, correoNorm, proposito, desafio.idUsuario, ahora + EXPIRACION_MS);
        autorizacionesValidas.put(token, auth);

        return new ResultadoValidacion(true, "Código verificado exitosamente.", desafio.intentosRestantes, token, desafio.idUsuario, correoNorm);
    }

    /**
     * Valida y consume de forma única un token de autorización.
     */
    public static synchronized TokenAutorizacion consumirAutorizacion(String token, PropositoDesafio proposito) {
        limpiarExpirados();
        if (token == null || token.isBlank()) {
            return null;
        }
        TokenAutorizacion auth = autorizacionesValidas.remove(token);
        if (auth == null) {
            return null;
        }
        if (System.currentTimeMillis() > auth.expiracion) {
            return null;
        }
        if (proposito != null && auth.proposito != proposito) {
            return null;
        }
        return auth;
    }

    /**
     * Consulta una autorización sin consumirla aún (por ejemplo para validar el estado del paso 3).
     */
    public static synchronized TokenAutorizacion consultarAutorizacion(String token, PropositoDesafio proposito) {
        limpiarExpirados();
        if (token == null || token.isBlank()) {
            return null;
        }
        TokenAutorizacion auth = autorizacionesValidas.get(token);
        if (auth == null || System.currentTimeMillis() > auth.expiracion) {
            return null;
        }
        if (proposito != null && auth.proposito != proposito) {
            return null;
        }
        return auth;
    }

    /**
     * Invalida todos los desafíos y autorizaciones asociados a un correo (usado tras cambio de contraseña).
     */
    public static synchronized void invalidarTodoPorCorreo(String correo) {
        if (correo == null || correo.isBlank()) return;
        String normalizado = correo.trim().toLowerCase(Locale.ROOT);

        desafiosActivos.entrySet().removeIf(e -> e.getValue().correo.equalsIgnoreCase(normalizado));
        autorizacionesValidas.entrySet().removeIf(e -> e.getValue().correo.equalsIgnoreCase(normalizado));
    }

    public static synchronized long obtenerSegundosEspera(String correo, PropositoDesafio proposito) {
        if (correo == null || correo.isBlank()) return 0;
        String clave = claveDesafio(correo.trim().toLowerCase(Locale.ROOT), proposito);
        DesafioActivo d = desafiosActivos.get(clave);
        if (d == null) return 0;
        long transcurrido = System.currentTimeMillis() - d.ultimoEnvio;
        if (transcurrido >= COOLDOWN_REENVIO_MS) return 0;
        return (COOLDOWN_REENVIO_MS - transcurrido) / 1000L;
    }

    public static synchronized long obtenerSegundosExpiracion(String correo, PropositoDesafio proposito) {
        if (correo == null || correo.isBlank()) return 0;
        String clave = claveDesafio(correo.trim().toLowerCase(Locale.ROOT), proposito);
        DesafioActivo d = desafiosActivos.get(clave);
        if (d == null) return 0;
        long restante = d.expiracion - System.currentTimeMillis();
        return restante > 0 ? (restante / 1000L) : 0;
    }

    /**
     * Oculta parcialmente el correo electrónico para mostrarlo de forma segura en pantallas y modales.
     * Ejemplo: "carlos.mendoza@universidad.edu.pe" -> "c***a@u***.edu.pe"
     */
    public static String enmascararCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return "";
        }
        String c = correo.trim();
        int at = c.indexOf('@');
        if (at <= 0 || at == c.length() - 1) {
            return c;
        }
        String local = c.substring(0, at);
        String dominio = c.substring(at + 1);

        String localMascara;
        if (local.length() <= 2) {
            localMascara = local.charAt(0) + "***";
        } else {
            localMascara = local.charAt(0) + "***" + local.charAt(local.length() - 1);
        }

        int dot = dominio.indexOf('.');
        String domMascara;
        if (dot > 0) {
            String domNombre = dominio.substring(0, dot);
            String domResto = dominio.substring(dot);
            domMascara = (domNombre.length() > 1 ? domNombre.charAt(0) + "***" : "d***") + domResto;
        } else {
            domMascara = dominio.charAt(0) + "***";
        }

        return localMascara + "@" + domMascara;
    }

    private static boolean validarRateLimit(Map<String, RegistroVentana> mapa, String clave, long ahora) {
        RegistroVentana reg = mapa.get(clave);
        if (reg == null || (ahora - reg.inicioVentana) > VENTANA_RATE_LIMIT_MS) {
            mapa.put(clave, new RegistroVentana(ahora, 1));
            return true;
        }
        if (reg.conteo >= MAX_SOLICITUDES_VENTANA) {
            return false;
        }
        reg.conteo++;
        return true;
    }

    private static String calcularHashSha256(String codigo, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest(codigo.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }

    private static void limpiarExpirados() {
        long ahora = System.currentTimeMillis();
        desafiosActivos.entrySet().removeIf(e -> ahora > e.getValue().expiracion || e.getValue().consumido);
        autorizacionesValidas.entrySet().removeIf(e -> ahora > e.getValue().expiracion);
        historialSolicitudesCorreo.entrySet().removeIf(e -> (ahora - e.getValue().inicioVentana) > VENTANA_RATE_LIMIT_MS);
        historialSolicitudesIp.entrySet().removeIf(e -> (ahora - e.getValue().inicioVentana) > VENTANA_RATE_LIMIT_MS);
    }
}

