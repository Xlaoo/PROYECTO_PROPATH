package com.propath.filtro;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(filterName = "EmpresaAuthFilter", urlPatterns = {"/empresa/*"})
public class EmpresaAuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        httpResponse.setHeader("Pragma", "no-cache");
        httpResponse.setDateHeader("Expires", 0);

        HttpSession session = httpRequest.getSession(false);
        boolean autenticado = false;

        if (session != null) {
            Object usuario = session.getAttribute("usuarioLogueado");
            Object rol = session.getAttribute("rol");
            if (usuario != null && ("EMPRESA".equalsIgnoreCase((String) rol) || "ADMIN".equalsIgnoreCase((String) rol))) {
                autenticado = true;
            }
        }

        if (autenticado) {
            chain.doFilter(request, response);
        } else {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp?error=no_autorizado");
        }
    }

    @Override
    public void destroy() {
    }
}
