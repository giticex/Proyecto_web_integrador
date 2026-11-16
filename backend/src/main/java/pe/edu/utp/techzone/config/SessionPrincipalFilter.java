
package pe.edu.utp.techzone.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Principal;

@Component
public class SessionPrincipalFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI()
                .substring(request.getContextPath().length());

        return !(
                path.equals("/api/cajas")
                || path.startsWith("/api/cajas/")
                || path.equals("/api/devoluciones")
                || path.startsWith("/api/devoluciones/")
                || path.equals("/api/ventas/pos")
        );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Si otro mecanismo de autenticación ya proporcionó Principal,
        // se conserva sin modificar.
        if (request.getUserPrincipal() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);

        Object usuario = session == null
                ? null
                : session.getAttribute("usuario");

        // Si no existe sesión, el controlador responderá 401.
        if (usuario == null || usuario.toString().isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        Principal principal = () -> usuario.toString();

        HttpServletRequestWrapper requestWrapper =
                new HttpServletRequestWrapper(request) {

            @Override
            public Principal getUserPrincipal() {
                return principal;
            }

            @Override
            public String getRemoteUser() {
                return principal.getName();
            }
        };

        filterChain.doFilter(requestWrapper, response);
    }
}
