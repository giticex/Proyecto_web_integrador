package pe.edu.utp.techzone.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Restringe la API REST de vendedores: solo sesiones con rol ADMIN pueden
 * listar o crear vendedores. Sigue el mismo patrón de autorización por
 * sesión que usa el resto de la aplicación.
 */
@Component
public class VendedorApiAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String contextPath = req.getContextPath();
        if (!req.getRequestURI().startsWith(contextPath + "/api/vendedores")) {
            chain.doFilter(request, response);
            return;
        }
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        Object rol = session == null ? null : session.getAttribute("rol");
        if (rol == null || !"ADMIN".equalsIgnoreCase(rol.toString())) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"mensaje\":\"Acceso denegado: se requiere rol ADMIN\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}