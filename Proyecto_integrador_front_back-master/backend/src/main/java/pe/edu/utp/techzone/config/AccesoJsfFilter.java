package pe.edu.utp.techzone.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/** Restringe acceso DIRECTO a pantallas JSF; no solo oculta enlaces. */
@Component
public class AccesoJsfFilter extends OncePerRequestFilter {
    private static final Set<String> CLIENTE = Set.of("catalogo.xhtml", "carrito.xhtml");
    private static final Set<String> VENDEDOR = Set.of(
            "catalogo.xhtml", "puntoVenta.xhtml", "misVentas.xhtml", "miPanel.xhtml");
    private static final Set<String> ADMIN = Set.of("catalogo.xhtml", "carrito.xhtml",
            "clientes.xhtml", "dashboard.xhtml", "puntoVenta.xhtml", "misVentas.xhtml", "miPanel.xhtml");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        if (!ruta.startsWith("/jsf/") || !ruta.endsWith(".xhtml")) {
            chain.doFilter(request, response);
            return;
        }
        String pagina = ruta.substring("/jsf/".length());
        if ("login.xhtml".equals(pagina)) {
            chain.doFilter(request, response);
            return;
        }
        HttpSession sesion = request.getSession(false);
        Object rol = sesion == null ? null : sesion.getAttribute("jsfRol");
        if (rol == null) {
            response.sendRedirect(request.getContextPath() + "/jsf/login.xhtml");
            return;
        }
        boolean permitido = switch (rol.toString()) {
            case "ADMIN" -> ADMIN.contains(pagina);
            case "VENDEDOR" -> VENDEDOR.contains(pagina);
            case "CLIENTE" -> CLIENTE.contains(pagina);
            default -> false;
        };
        if (!permitido) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tienes permiso para esta vista JSF");
            return;
        }
        chain.doFilter(request, response);
    }
}
