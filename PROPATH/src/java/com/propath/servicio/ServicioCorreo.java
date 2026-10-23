package com.propath.servicio;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public final class ServicioCorreo {

    private static String host = "";
    private static int port = 587;
    private static String user = "";
    private static String password = "";
    private static String from = "notificaciones@propath.com";
    private static boolean auth = true;
    private static boolean starttls = true;

    static {
        cargarConfiguracion();
    }

    private ServicioCorreo() {
    }

    public static synchronized void cargarConfiguracion() {
        Properties props = new Properties();
        try (InputStream is = ServicioCorreo.class.getClassLoader().getResourceAsStream("mail.properties")) {
            if (is != null) {
                props.load(is);
                if (props.getProperty("mail.smtp.host") != null) {
                    host = props.getProperty("mail.smtp.host").trim();
                }
                if (props.getProperty("mail.smtp.port") != null) {
                    try {
                        port = Integer.parseInt(props.getProperty("mail.smtp.port").trim());
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (props.getProperty("mail.smtp.user") != null) {
                    user = props.getProperty("mail.smtp.user").trim();
                }
                if (props.getProperty("mail.smtp.password") != null) {
                    password = props.getProperty("mail.smtp.password").trim();
                }
                if (props.getProperty("mail.smtp.from") != null) {
                    from = props.getProperty("mail.smtp.from").trim();
                }
                if (props.getProperty("mail.smtp.auth") != null) {
                    auth = Boolean.parseBoolean(props.getProperty("mail.smtp.auth").trim());
                }
                if (props.getProperty("mail.smtp.starttls.enable") != null) {
                    starttls = Boolean.parseBoolean(props.getProperty("mail.smtp.starttls.enable").trim());
                }
            }
        } catch (Exception ignored) {
        }

        String envHost = System.getenv("SMTP_HOST");
        if (envHost != null && !envHost.isBlank()) {
            host = envHost.trim();
        }
        String envPort = System.getenv("SMTP_PORT");
        if (envPort != null && !envPort.isBlank()) {
            try {
                port = Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        String envUser = System.getenv("SMTP_USER");
        if (envUser != null && !envUser.isBlank()) {
            user = envUser.trim();
        }
        String envPass = System.getenv("SMTP_PASSWORD");
        if (envPass != null) {
            password = envPass.trim();
        }
        String envFrom = System.getenv("SMTP_FROM");
        if (envFrom != null && !envFrom.isBlank()) {
            from = envFrom.trim();
        }
    }

    public static boolean isConfigurado() {
        return host != null && !host.isBlank() && port > 0 && user != null && !user.isBlank() && password != null && !password.isBlank();
    }

    public static String getHost() {
        return host;
    }

    public static int getPort() {
        return port;
    }

    public static String getUser() {
        return user;
    }

    public static String getFrom() {
        return from;
    }

    public static void enviarCodigoRecuperacion(String destinatario, String codigo) throws Exception {
        if (!isConfigurado()) {
            throw new IllegalStateException("Servicio de correo SMTP no configurado. Configure las variables de entorno SMTP_HOST, SMTP_USER y SMTP_PASSWORD o el archivo mail.properties.");
        }

        if (destinatario == null || destinatario.isBlank() || codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Destinatario y codigo no pueden estar vacios.");
        }

        String asunto = "Codigo de recuperacion de contrasena - ProPath";
        String cuerpoHtml = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;border:1px solid #e0e0e0;border-radius:8px;'>"
                + "<div style='text-align:center;padding-bottom:15px;border-bottom:1px solid #eee;'>"
                + "<h2 style='color:#0d6efd;margin:0;'>ProPath</h2>"
                + "<p style='color:#666;font-size:14px;margin:5px 0 0;'>Portal de Insercion Laboral</p>"
                + "</div>"
                + "<div style='padding:20px 0;'>"
                + "<p style='color:#333;font-size:16px;'>Has solicitado restablecer tu contrasena de acceso a ProPath.</p>"
                + "<p style='color:#333;font-size:14px;'>Usa el siguiente codigo de verificacion de un solo uso:</p>"
                + "<div style='text-align:center;margin:25px 0;'>"
                + "<span style='display:inline-block;background:#f8f9fa;border:2px dashed #0d6efd;color:#0d6efd;font-size:28px;font-weight:bold;letter-spacing:6px;padding:12px 24px;border-radius:6px;'>"
                + codigo
                + "</span>"
                + "</div>"
                + "<p style='color:#666;font-size:13px;'>Este codigo expirara en <strong>10 minutos</strong> y permite hasta 5 intentos.</p>"
                + "<p style='color:#999;font-size:12px;'>Si no solicitaste este cambio, puedes ignorar este mensaje de forma segura. Tu contrasena actual no ha sido modificada.</p>"
                + "</div>"
                + "<div style='text-align:center;padding-top:15px;border-top:1px solid #eee;color:#aaa;font-size:11px;'>"
                + "&copy; 2026 ProPath. Todos los derechos reservados."
                + "</div>"
                + "</div>";

        enviarSmtp(destinatario.trim(), asunto, cuerpoHtml);
    }

    private static void enviarSmtp(String destinatario, String asunto, String cuerpoHtml) throws Exception {
        Socket socket = null;
        BufferedReader reader = null;
        PrintWriter writer = null;

        try {
            if (port == 465) {
                SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
                socket = ssf.createSocket(host, port);
            } else {
                socket = new Socket(host, port);
            }
            socket.setSoTimeout(15000);

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            esperarRespuesta(reader, 220);

            writer.println("EHLO localhost");
            esperarRespuesta(reader, 250);

            if (starttls && port != 465) {
                writer.println("STARTTLS");
                esperarRespuesta(reader, 220);

                SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket sslSocket = (SSLSocket) ssf.createSocket(socket, host, port, true);
                sslSocket.setUseClientMode(true);
                sslSocket.startHandshake();

                socket = sslSocket;
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

                writer.println("EHLO localhost");
                esperarRespuesta(reader, 250);
            }

            if (auth) {
                writer.println("AUTH LOGIN");
                esperarRespuesta(reader, 334);

                String userB64 = Base64.getEncoder().encodeToString(user.getBytes(StandardCharsets.UTF_8));
                writer.println(userB64);
                esperarRespuesta(reader, 334);

                String passB64 = Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
                writer.println(passB64);
                esperarRespuesta(reader, 235);
            }

            writer.println("MAIL FROM:<" + from + ">");
            esperarRespuesta(reader, 250);

            writer.println("RCPT TO:<" + destinatario + ">");
            esperarRespuesta(reader, 250);

            writer.println("DATA");
            esperarRespuesta(reader, 354);

            writer.println("From: ProPath <" + from + ">");
            writer.println("To: <" + destinatario + ">");
            writer.println("Subject: =?UTF-8?B?" + Base64.getEncoder().encodeToString(asunto.getBytes(StandardCharsets.UTF_8)) + "?=");
            writer.println("MIME-Version: 1.0");
            writer.println("Content-Type: text/html; charset=UTF-8");
            writer.println("Content-Transfer-Encoding: 8bit");
            writer.println();
            writer.println(cuerpoHtml);
            writer.println(".");
            esperarRespuesta(reader, 250);

            writer.println("QUIT");
            try {
                esperarRespuesta(reader, 221);
            } catch (Exception ignored) {
            }
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ignored) {
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignored) {
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static void esperarRespuesta(BufferedReader reader, int codigoEsperado) throws Exception {
        String linea;
        int codigoRespuesta = 0;
        StringBuilder detalle = new StringBuilder();

        while ((linea = reader.readLine()) != null) {
            detalle.append(linea).append("\n");
            if (linea.length() >= 3) {
                try {
                    codigoRespuesta = Integer.parseInt(linea.substring(0, 3));
                } catch (NumberFormatException ignored) {
                }
                if (linea.length() == 3 || linea.charAt(3) != '-') {
                    break;
                }
            }
        }

        if (codigoRespuesta != codigoEsperado) {
            throw new IllegalStateException("Error en comunicacion SMTP. Esperado: " + codigoEsperado + ", Recibido: " + codigoRespuesta + " (" + detalle.toString().trim() + ")");
        }
    }
}
