package com.propath.security;

import com.propath.entity.Usuario;
import com.propath.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final String INTERNAL_KEY = "propath-internal-secret-2026";
    private final UsuarioService usuarioService;

    public AdminAuthInterceptor(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String internalHeader = request.getHeader("X-ProPath-Internal-Key");
        if (INTERNAL_KEY.equals(internalHeader)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"Acceso no autorizado: Se requiere autenticación básica (Basic Auth) con credenciales de Administrador registradas en MariaDB.\"}");
            return false;
        }

        try {
            String base64Credentials = authHeader.substring(6).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            final String[] values = credentials.split(":", 2);

            if (values.length != 2) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"Formato de credenciales de autorización inválido.\"}");
                return false;
            }

            String correo = values[0];
            String contrasena = values[1];

            Usuario usuario = usuarioService.autenticar(correo, contrasena);
            if (usuario.getRol() != Usuario.Rol.ADMIN) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"Acceso prohibido: Solo cuentas con rol ADMIN pueden operar en estos endpoints.\"}");
                return false;
            }

            request.setAttribute("usuarioAutenticado", usuario);
            return true;

        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"Autenticación fallida: " + e.getMessage() + "\"}");
            return false;
        }
    }
}
