package com.propath.servicio;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Servicio SMTP optimizado para ProPath.
 * Cumple con:
 * - Credenciales locales en mail.properties y sobreescritura segura con variables de entorno.
 * - Distinción estricta entre correo remitente de ProPath y destinatarios de usuarios.
 * - Conexiones seguras con STARTTLS (puerto 587) o SSL directo (puerto 465).
 * - Timeouts de conexión (10s) y lectura (15s).
 * - Protección total de contraseñas: nunca se imprimen ni registran.
 * - Gestión rigurosa de errores sin falsos positivos de entrega.
 * - Plantillas HTML profesionales con la identidad visual ProPath y alternativa en texto plano.
 */
public final class ServicioCorreo {

    private static String host = "";
    private static int port = 587;
    private static String user = "";
    private static String password = "";
    private static String from = "notificaciones@propath.com";
    private static boolean auth = true;
    private static boolean starttls = true;
    private static String appUrl = "http://localhost:8080/PROPATH";

    private static final int TIMEOUT_CONEXION_MS = 10000;
    private static final int TIMEOUT_LECTURA_MS = 15000;

    static {
        cargarConfiguracion();
    }

    private ServicioCorreo() {
    }

    public static synchronized void cargarConfiguracion() {
        Properties props = new Properties();
        try (InputStream is = ServicioCorreo.class.getClassLoader().getResourceAsStream("com/propath/servicio/mail.properties")) {
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
                if (props.getProperty("app.url") != null && !props.getProperty("app.url").isBlank()) {
                    appUrl = props.getProperty("app.url").trim();
                }
            }
        } catch (Exception ignored) {
        }

        // Variables de entorno tienen prioridad sobre mail.properties
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
        String envAppUrl = System.getenv("APP_URL");
        if (envAppUrl != null && !envAppUrl.isBlank()) {
            appUrl = envAppUrl.trim();
        }
    }

    public static boolean isConfigurado() {
        return host != null && !host.isBlank()
                && port > 0
                && user != null && !user.isBlank()
                && password != null && !password.isBlank();
    }

    public static String getHost() { return host; }
    public static int getPort() { return port; }
    public static String getUser() { return user; }
    public static String getFrom() { return from; }
    public static String getAppUrl() { return appUrl; }

    public static void setAppUrl(String url) {
        if (url != null && !url.isBlank()) {
            appUrl = url.trim();
        }
    }

    // =========================================================================
    // 1. PLANTILLA: Verificación de correo en el registro
    // =========================================================================
    public static void enviarCodigoRegistro(String destinatario, String nombre, String codigo, String urlDestino) throws Exception {
        String saludo = (nombre != null && !nombre.isBlank()) ? "Hola " + escaparHtml(nombre.trim()) : "Estimado(a) postulante";
        String enlace = (urlDestino != null && !urlDestino.isBlank()) ? urlDestino : appUrl + "/login.jsp";
        String asunto = "Código de verificación de tu correo - ProPath";

        String textoPlano = "ProPath - Portal de Prácticas y Empleo\n\n"
                + saludo + ",\n\n"
                + "Gracias por iniciar tu registro en ProPath. Tu código de verificación de 6 caracteres es:\n\n"
                + "    " + codigo + "\n\n"
                + "Este código es de un solo uso y vencerá en 1 minuto (60 segundos).\n"
                + "Para continuar, ingresa este código en el modal de verificación de la página de registro:\n"
                + enlace + "\n\n"
                + "Nota: Este enlace abrirá la página donde debes ingresar tu código. Nunca compartas este código con nadie.\n"
                + "Si no solicitaste este registro, puedes ignorar este mensaje.\n\n"
                + "© 2026 ProPath. Todos los derechos reservados.";

        String cuerpoHtml = construirPlantillaHtml(
                "Verifica tu Correo Electrónico",
                saludo,
                "Gracias por registrarte en ProPath. Para completar la creación de tu cuenta y verificar que eres el propietario de este correo, ingresa el siguiente código en el modal de verificación (vigencia: 1 minuto):",
                codigo,
                "Verificar Correo",
                enlace,
                "Al hacer clic en el botón se abrirá la página de ProPath con el modal para ingresar tu código. Por tu seguridad, el enlace no autentica automáticamente.",
                "Si no intentaste crear una cuenta en ProPath, ignora este mensaje con total seguridad. Tu correo no será registrado.",
                "1 minuto (60 segundos)"
        );

        enviarCorreoMultipart(destinatario, asunto, textoPlano, cuerpoHtml);
    }

    // =========================================================================
    // 2. PLANTILLA: Verificación de inicio de sesión (2FA)
    // =========================================================================
    public static void enviarCodigoLogin(String destinatario, String nombre, String codigo, String urlDestino) throws Exception {
        String saludo = (nombre != null && !nombre.isBlank()) ? "Hola " + escaparHtml(nombre.trim()) : "Hola";
        String enlace = (urlDestino != null && !urlDestino.isBlank()) ? urlDestino : appUrl + "/login.jsp";
        String asunto = "Código de seguridad de acceso (2FA) - ProPath";

        String textoPlano = "ProPath - Portal de Prácticas y Empleo\n\n"
                + saludo + ",\n\n"
                + "Se ha detectado un intento de inicio de sesión en tu cuenta ProPath.\n"
                + "Tu código de verificación de 6 caracteres para confirmar el acceso es:\n\n"
                + "    " + codigo + "\n\n"
                + "Este código es de un solo uso y vencerá en 10 minutos (máximo 5 intentos).\n"
                + "Ingresa este código en el modal de acceso de ProPath:\n"
                + enlace + "\n\n"
                + "Si tú no intentaste iniciar sesión, te recomendamos cambiar tu contraseña inmediatamente.\n\n"
                + "© 2026 ProPath. Todos los derechos reservados.";

        String cuerpoHtml = construirPlantillaHtml(
                "Código de Seguridad de Acceso",
                saludo,
                "Se ha detectado un inicio de sesión en tu cuenta ProPath. Para autorizar el ingreso y proteger tu información, ingresa el siguiente código de doble factor (2FA):",
                codigo,
                "Verificar Acceso",
                enlace,
                "El botón te redirigirá a la pantalla de verificación de ProPath. El acceso solo se concederá tras validar el código en el servidor.",
                "Si tú no has intentado acceder a tu cuenta en este momento, alguien podría tener tu contraseña. Te sugerimos ingresar a ProPath y cambiarla de inmediato."
        );

        enviarCorreoMultipart(destinatario, asunto, textoPlano, cuerpoHtml);
    }

    // =========================================================================
    // 3. PLANTILLA: Recuperación de contraseña
    // =========================================================================
    public static void enviarCodigoRecuperacion(String destinatario, String codigo) throws Exception {
        enviarCodigoRecuperacion(destinatario, null, codigo, appUrl + "/recuperar-contrasena.jsp");
    }

    public static void enviarCodigoRecuperacion(String destinatario, String nombre, String codigo, String urlDestino) throws Exception {
        String saludo = (nombre != null && !nombre.isBlank()) ? "Hola " + escaparHtml(nombre.trim()) : "Estimado(a) usuario(a)";
        String enlace = (urlDestino != null && !urlDestino.isBlank()) ? urlDestino : appUrl + "/recuperar-contrasena.jsp";
        String asunto = "Código para restablecer tu contraseña - ProPath";

        String textoPlano = "ProPath - Portal de Prácticas y Empleo\n\n"
                + saludo + ",\n\n"
                + "Has solicitado restablecer la contraseña de tu cuenta ProPath.\n"
                + "Tu código de recuperación de 6 caracteres es:\n\n"
                + "    " + codigo + "\n\n"
                + "Este código es de un solo uso y vencerá en 10 minutos (máximo 5 intentos).\n"
                + "Para continuar, ingresa el código en:\n"
                + enlace + "\n\n"
                + "Si no solicitaste restablecer tu contraseña, haz caso omiso a este correo. Tu contraseña actual sigue segura.\n\n"
                + "© 2026 ProPath. Todos los derechos reservados.";

        String cuerpoHtml = construirPlantillaHtml(
                "Recuperación de Contraseña",
                saludo,
                "Has solicitado restablecer tu contraseña en ProPath. Utiliza el siguiente código de un solo uso para verificar tu identidad y definir tu nueva clave:",
                codigo,
                "Continuar Recuperación",
                enlace,
                "El enlace te llevará al formulario de recuperación de ProPath. Por seguridad, la nueva contraseña solo podrá definirse tras verificar el código.",
                "Si no solicitaste este cambio, no te preocupes: tu cuenta sigue protegida y tu contraseña actual no ha sido modificada."
        );

        enviarCorreoMultipart(destinatario, asunto, textoPlano, cuerpoHtml);
    }

    // =========================================================================
    // 3.1. PLANTILLA: Verificación de cambio de correo electrónico (1 minuto)
    // =========================================================================
    public static void enviarCodigoCambioCorreo(String destinatario, String nombre, String codigo) throws Exception {
        String saludo = (nombre != null && !nombre.isBlank()) ? "Hola " + escaparHtml(nombre.trim()) : "Estimado(a) usuario(a)";
        String asunto = "Código de verificación para cambio de correo - ProPath";

        String textoPlano = "ProPath - Portal de Prácticas y Empleo\n\n"
                + saludo + ",\n\n"
                + "Has solicitado actualizar el correo electrónico de tu cuenta ProPath.\n"
                + "Tu código de verificación de 6 dígitos es:\n\n"
                + "    " + codigo + "\n\n"
                + "Este código es de un solo uso y vencerá en 1 minuto (60 segundos).\n"
                + "Ingresa este código en el modal de verificación de ProPath para confirmar tu nuevo correo.\n\n"
                + "Si no solicitaste este cambio, puedes ignorar este mensaje; tu cuenta mantendrá su correo anterior.\n\n"
                + "© 2026 ProPath. Todos los derechos reservados.";

        String cuerpoHtml = construirPlantillaHtml(
                "Verificación de Correo Electrónico",
                saludo,
                "Has solicitado cambiar el correo electrónico asociado a tu cuenta ProPath. Para confirmar que tienes acceso a este correo, ingresa el siguiente código de seguridad en el modal de verificación de tu perfil:",
                codigo,
                null,
                null,
                "Por estrictas medidas de seguridad, este código tiene una vigencia de exactamente 1 minuto (60 segundos).",
                "Si no solicitaste modificar tu correo, puedes ignorar este mensaje. Tu correo anterior permanecerá sin cambios."
        );

        enviarCorreoMultipart(destinatario, asunto, textoPlano, cuerpoHtml);
    }

    // =========================================================================
    // 4. PLANTILLA: Confirmación de cambio de contraseña
    // =========================================================================
    public static void enviarConfirmacionCambioPassword(String destinatario, String nombre) throws Exception {
        String saludo = (nombre != null && !nombre.isBlank()) ? "Hola " + escaparHtml(nombre.trim()) : "Estimado(a) usuario(a)";
        String asunto = "Aviso de seguridad: Tu contraseña ha sido cambiada - ProPath";

        String textoPlano = "ProPath - Portal de Prácticas y Empleo\n\n"
                + saludo + ",\n\n"
                + "Te informamos que la contraseña de tu cuenta ProPath ha sido modificada exitosamente.\n"
                + "Por motivos de seguridad, nunca incluimos contraseñas en nuestros correos.\n\n"
                + "Si realizaste este cambio, no necesitas realizar ninguna acción adicional.\n"
                + "Si NO fuiste tú, por favor comunícate de inmediato con el administrador o utiliza la opción 'Olvidé mi contraseña' en:\n"
                + appUrl + "/recuperar-contrasena.jsp\n\n"
                + "© 2026 ProPath. Todos los derechos reservados.";

        String cuerpoHtml = "<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'></head>"
                + "<body style='margin:0;padding:20px;background-color:#F5F7FB;font-family:Arial,sans-serif;color:#17243B;'>"
                + "<table align='center' border='0' cellpadding='0' cellspacing='0' width='100%' style='max-width:600px;background:#ffffff;border-radius:12px;border:1px solid #DCE4EF;overflow:hidden;box-shadow:0 4px 14px rgba(16,36,72,0.06);'>"
                + "  <tr><td style='background:#102448;padding:24px 30px;text-align:center;border-bottom:3px solid #2563EB;'>"
                + "    <h1 style='margin:0;color:#FFFFFF;font-size:24px;font-weight:bold;letter-spacing:1px;'>PROPATH</h1>"
                + "    <p style='margin:4px 0 0;color:#0891B2;font-size:13px;font-weight:600;'>PORTAL DE PRÁCTICAS Y EMPLEABILIDAD</p>"
                + "  </td></tr>"
                + "  <tr><td style='padding:30px;'>"
                + "    <div style='background:#E0F2FE;border-left:4px solid #0891B2;padding:12px 16px;border-radius:4px;margin-bottom:20px;'>"
                + "      <strong style='color:#0891B2;font-size:15px;'>Aviso de Seguridad de la Cuenta</strong>"
                + "    </div>"
                + "    <p style='font-size:15px;line-height:1.6;margin:0 0 15px;'>" + saludo + ",</p>"
                + "    <p style='font-size:14px;line-height:1.6;margin:0 0 15px;color:#334155;'>Te confirmamos que la contraseña de acceso a tu cuenta ProPath ha sido actualizada correctamente.</p>"
                + "    <p style='font-size:13px;line-height:1.6;margin:0 0 20px;color:#64748B;'>Por estrictas políticas de seguridad, ProPath nunca incluirá tu contraseña en mensajes de correo electrónico.</p>"
                + "    <div style='background:#FFFBEB;border:1px solid #FDE68A;border-radius:6px;padding:14px;margin:20px 0;'>"
                + "      <p style='margin:0;font-size:13px;color:#92400E;line-height:1.5;'><strong>¿No realizaste este cambio?</strong> Si sospechas que alguien más cambió tu contraseña, ingresa de inmediato a <a href='" + appUrl + "/recuperar-contrasena.jsp' style='color:#2563EB;font-weight:bold;'>Recuperar Contraseña</a> o contacta a soporte.</p>"
                + "    </div>"
                + "    <p style='text-align:center;margin:25px 0 10px;'><a href='" + appUrl + "/login.jsp' style='display:inline-block;background:#2563EB;color:#FFFFFF;text-decoration:none;font-weight:bold;font-size:14px;padding:12px 24px;border-radius:6px;'>Iniciar Sesión en ProPath</a></p>"
                + "  </td></tr>"
                + "  <tr><td style='background:#F8FAFC;padding:18px 30px;text-align:center;border-top:1px solid #EEF2F6;font-size:12px;color:#94A3B8;'>"
                + "    <p style='margin:0 0 4px;'>Este es un mensaje automático de seguridad generado por el sistema.</p>"
                + "    <p style='margin:0;'>&copy; 2026 ProPath. Todos los derechos reservados.</p>"
                + "  </td></tr>"
                + "</table></body></html>";

        enviarCorreoMultipart(destinatario, asunto, textoPlano, cuerpoHtml);
    }

    // =========================================================================
    // 5. PLANTILLA: Bienvenida tras verificar y completar registro
    // =========================================================================
    public static void enviarConfirmacionRegistro(String destinatario, String nombre, String rol) throws Exception {
        enviarBienvenidaRegistro(destinatario, nombre, rol);
    }

    public static void enviarBienvenidaRegistro(String destinatario, String nombre, String rol) throws Exception {
        String saludo = (nombre != null && !nombre.isBlank()) ? "Hola " + escaparHtml(nombre.trim()) : "Bienvenido(a)";
        String rolSeguro = (rol != null && !rol.isBlank()) ? rol.trim().toUpperCase() : "ESTUDIANTE";
        String asunto = "¡Bienvenido a ProPath! Tu registro ha sido completado";

        String textoPlano = "ProPath - Portal de Prácticas y Empleo\n\n"
                + saludo + ",\n\n"
                + "¡Te damos la bienvenida a ProPath!\n"
                + "Tu cuenta de " + rolSeguro + " ha sido verificada y registrada exitosamente.\n\n"
                + "Ya puedes iniciar sesión con tu correo electrónico y tu contraseña en:\n"
                + appUrl + "/login.jsp\n\n"
                + "Te deseamos el mayor de los éxitos en tu desarrollo profesional.\n\n"
                + "© 2026 ProPath. Todos los derechos reservados.";

        String cuerpoHtml = "<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'></head>"
                + "<body style='margin:0;padding:20px;background-color:#F5F7FB;font-family:Arial,sans-serif;color:#17243B;'>"
                + "<table align='center' border='0' cellpadding='0' cellspacing='0' width='100%' style='max-width:600px;background:#ffffff;border-radius:12px;border:1px solid #DCE4EF;overflow:hidden;box-shadow:0 4px 14px rgba(16,36,72,0.06);'>"
                + "  <tr><td style='background:#102448;padding:24px 30px;text-align:center;border-bottom:3px solid #2563EB;'>"
                + "    <h1 style='margin:0;color:#FFFFFF;font-size:24px;font-weight:bold;letter-spacing:1px;'>PROPATH</h1>"
                + "    <p style='margin:4px 0 0;color:#0891B2;font-size:13px;font-weight:600;'>PORTAL DE PRÁCTICAS Y EMPLEABILIDAD</p>"
                + "  </td></tr>"
                + "  <tr><td style='padding:30px;'>"
                + "    <div style='background:#DCFCE7;border-left:4px solid #16A34A;padding:12px 16px;border-radius:4px;margin-bottom:20px;'>"
                + "      <strong style='color:#16A34A;font-size:15px;'>¡Cuenta Creada y Verificada con Éxito!</strong>"
                + "    </div>"
                + "    <p style='font-size:15px;line-height:1.6;margin:0 0 15px;'>" + saludo + ",</p>"
                + "    <p style='font-size:14px;line-height:1.6;margin:0 0 15px;color:#334155;'>Tu registro como <strong>" + escaparHtml(rolSeguro) + "</strong> en ProPath ha culminado satisfactoriamente. Tu correo electrónico ha quedado certificado como canal oficial de notificaciones.</p>"
                + "    <p style='font-size:14px;line-height:1.6;margin:0 0 25px;color:#334155;'>Ahora puedes acceder a la plataforma para postular a ofertas de prácticas preprofesionales, gestionar documentos y conectar con oportunidades laborales.</p>"
                + "    <p style='text-align:center;margin:30px 0 15px;'><a href='" + appUrl + "/login.jsp' style='display:inline-block;background:#2563EB;color:#FFFFFF;text-decoration:none;font-weight:bold;font-size:15px;padding:14px 28px;border-radius:6px;box-shadow:0 2px 6px rgba(37,99,235,0.3);'>Iniciar Sesión Ahora</a></p>"
                + "  </td></tr>"
                + "  <tr><td style='background:#F8FAFC;padding:18px 30px;text-align:center;border-top:1px solid #EEF2F6;font-size:12px;color:#94A3B8;'>"
                + "    <p style='margin:0;'>&copy; 2026 ProPath. Todos los derechos reservados.</p>"
                + "  </td></tr>"
                + "</table></body></html>";

        enviarCorreoMultipart(destinatario, asunto, textoPlano, cuerpoHtml);
    }

    // =========================================================================
    // CONSTRUCTOR BASE DE PLANTILLA CON IDENTIDAD PROPATH
    // =========================================================================
    private static String construirPlantillaHtml(String tituloHeader, String saludo, String introduccion,
                                                String codigo, String textoBoton, String enlaceBoton,
                                                String aclaracionEnlace, String avisoSeguridad) {
        return construirPlantillaHtml(tituloHeader, saludo, introduccion, codigo, textoBoton, enlaceBoton, aclaracionEnlace, avisoSeguridad, "10 minutos");
    }

    private static String construirPlantillaHtml(String tituloHeader, String saludo, String introduccion,
                                                String codigo, String textoBoton, String enlaceBoton,
                                                String aclaracionEnlace, String avisoSeguridad, String vigenciaTexto) {
        String vigencia = (vigenciaTexto != null && !vigenciaTexto.isBlank()) ? vigenciaTexto : "10 minutos";
        return "<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'></head>"
                + "<body style='margin:0;padding:20px;background-color:#F5F7FB;font-family:Arial,Helvetica,sans-serif;color:#17243B;'>"
                + "<table align='center' border='0' cellpadding='0' cellspacing='0' width='100%' style='max-width:600px;background:#ffffff;border-radius:12px;border:1px solid #DCE4EF;overflow:hidden;box-shadow:0 4px 14px rgba(16,36,72,0.06);'>"
                + "  <!-- Encabezado ProPath -->"
                + "  <tr><td style='background:#102448;padding:24px 30px;text-align:center;border-bottom:3px solid #2563EB;'>"
                + "    <h1 style='margin:0;color:#FFFFFF;font-size:24px;font-weight:bold;letter-spacing:1px;'>PROPATH</h1>"
                + "    <p style='margin:4px 0 0;color:#0891B2;font-size:13px;font-weight:600;'>PORTAL DE PRÁCTICAS Y EMPLEABILIDAD</p>"
                + "  </td></tr>"
                + "  <!-- Contenido -->"
                + "  <tr><td style='padding:30px;'>"
                + "    <h2 style='margin:0 0 15px;color:#102448;font-size:18px;font-weight:bold;border-bottom:1px solid #EEF2F6;padding-bottom:10px;'>" + escaparHtml(tituloHeader) + "</h2>"
                + "    <p style='font-size:15px;line-height:1.6;margin:0 0 12px;color:#17243B;'>" + saludo + ",</p>"
                + "    <p style='font-size:14px;line-height:1.6;margin:0 0 20px;color:#475569;'>" + escaparHtml(introduccion) + "</p>"
                + "    <!-- Código de 6 caracteres destacado -->"
                + "    <div style='text-align:center;margin:25px 0;'>"
                + "      <div style='display:inline-block;background:#F8FAFC;border:2px dashed #2563EB;border-radius:10px;padding:16px 32px;box-shadow:0 2px 8px rgba(37,99,235,0.08);'>"
                + "        <span style='display:block;font-size:12px;color:#64748B;font-weight:600;margin-bottom:6px;text-transform:uppercase;letter-spacing:1px;'>Código de Seguridad (6 caracteres)</span>"
                + "        <span style='font-family:\"Courier New\",Courier,monospace;font-size:32px;font-weight:bold;color:#2563EB;letter-spacing:8px;'>"
                +            escaparHtml(codigo)
                + "        </span>"
                + "      </div>"
                + "    </div>"
                + "    <p style='text-align:center;color:#64748B;font-size:13px;margin:0 0 25px;'>⏰ Este código es de un solo uso y vencerá en <strong>" + escaparHtml(vigencia) + "</strong> (máximo 5 intentos).</p>"
                + "    <!-- Botón hacia la aplicación -->"
                + "    <div style='text-align:center;margin:25px 0 15px;'>"
                + "      <a href='" + escaparHtml(enlaceBoton) + "' style='display:inline-block;background:#2563EB;color:#FFFFFF;text-decoration:none;font-weight:bold;font-size:14px;padding:12px 28px;border-radius:6px;box-shadow:0 2px 6px rgba(37,99,235,0.3);'>" + escaparHtml(textoBoton) + "</a>"
                + "    </div>"
                + "    <p style='font-size:12px;color:#94A3B8;text-align:center;margin:0 0 25px;line-height:1.4;'>" + escaparHtml(aclaracionEnlace) + "</p>"
                + "    <!-- Aviso de seguridad -->"
                + "    <div style='background:#F8FAFC;border-radius:6px;padding:12px 16px;border-left:3px solid #CBD5E1;'>"
                + "      <p style='margin:0;font-size:12px;color:#64748B;line-height:1.5;'>" + escaparHtml(avisoSeguridad) + "</p>"
                + "    </div>"
                + "  </td></tr>"
                + "  <!-- Pie de página -->"
                + "  <tr><td style='background:#F8FAFC;padding:18px 30px;text-align:center;border-top:1px solid #EEF2F6;font-size:12px;color:#94A3B8;'>"
                + "    <p style='margin:0 0 4px;'>Mensaje automático enviado por ProPath Seguridad. No respondas a este correo.</p>"
                + "    <p style='margin:0;'>&copy; 2026 ProPath. Todos los derechos reservados.</p>"
                + "  </td></tr>"
                + "</table></body></html>";
    }

    private static String escaparHtml(String texto) {
        if (texto == null) return "";
        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static void enviarCorreo(String destinatario, String asunto, String cuerpoHtml) throws Exception {
        enviarCorreoMultipart(destinatario, asunto, null, cuerpoHtml);
    }

    /**
     * Envía un correo electrónico seguro mediante protocolo SMTP con soporte Multipart (Texto plano + HTML).
     */
    public static void enviarCorreoMultipart(String destinatario, String asunto, String textoPlano, String cuerpoHtml) throws Exception {
        if (!isConfigurado()) {
            throw new IllegalStateException("Servicio SMTP no configurado. Revisa mail.properties o define SMTP_HOST, SMTP_USER, SMTP_PASSWORD y SMTP_FROM.");
        }

        if (destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException("El destinatario no puede estar vacío.");
        }
        if (asunto == null || asunto.isBlank()) {
            throw new IllegalArgumentException("El asunto no puede estar vacío.");
        }
        if (cuerpoHtml == null || cuerpoHtml.isBlank()) {
            throw new IllegalArgumentException("El cuerpo HTML no puede estar vacío.");
        }

        Socket socket = null;
        BufferedReader reader = null;
        PrintWriter writer = null;

        try {
            if (port == 465) {
                SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
                socket = ssf.createSocket();
                socket.connect(new InetSocketAddress(host, port), TIMEOUT_CONEXION_MS);
            } else {
                socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), TIMEOUT_CONEXION_MS);
            }
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(TIMEOUT_LECTURA_MS);

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
                socket.setTcpNoDelay(true);
            socket.setSoTimeout(TIMEOUT_LECTURA_MS);
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

            writer.println("RCPT TO:<" + destinatario.trim() + ">");
            esperarRespuesta(reader, 250);

            writer.println("DATA");
            esperarRespuesta(reader, 354);

            String boundary = "----=_Part_" + System.currentTimeMillis() + "_" + Math.abs(socket.hashCode());

            writer.println("From: ProPath <" + from + ">");
            writer.println("To: <" + destinatario.trim() + ">");
            writer.println("Subject: =?UTF-8?B?" + Base64.getEncoder().encodeToString(asunto.getBytes(StandardCharsets.UTF_8)) + "?=");
            writer.println("MIME-Version: 1.0");
            writer.println("Content-Type: multipart/alternative; boundary=\"" + boundary + "\"");
            writer.println();

            // Parte 1: Texto plano si está presente
            if (textoPlano != null && !textoPlano.isBlank()) {
                writer.println("--" + boundary);
                writer.println("Content-Type: text/plain; charset=UTF-8");
                writer.println("Content-Transfer-Encoding: 8bit");
                writer.println();
                writer.println(textoPlano);
                writer.println();
            }

            // Parte 2: HTML
            writer.println("--" + boundary);
            writer.println("Content-Type: text/html; charset=UTF-8");
            writer.println("Content-Transfer-Encoding: 8bit");
            writer.println();
            writer.println(cuerpoHtml);
            writer.println();

            writer.println("--" + boundary + "--");
            writer.println(".");
            esperarRespuesta(reader, 250);

            writer.println("QUIT");
            try {
                esperarRespuesta(reader, 221);
            } catch (Exception ignored) {
            }
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (Exception ignored) {}
            }
            if (reader != null) {
                try { reader.close(); } catch (Exception ignored) {}
            }
            if (socket != null) {
                try { socket.close(); } catch (Exception ignored) {}
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
            throw new IllegalStateException("Error SMTP. Esperado: " + codigoEsperado + ", Recibido: " + codigoRespuesta + " (" + detalle.toString().trim() + ")");
        }
    }
}
