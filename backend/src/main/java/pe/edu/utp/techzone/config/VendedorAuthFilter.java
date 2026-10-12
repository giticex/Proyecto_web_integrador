package pe.edu.utp.techzone.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class VendedorAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        String path = req.getRequestURI();

        if (path.startsWith(req.getContextPath() + "/vendedor") || path.startsWith(req.getContextPath() + "/dashboard")) {
            HttpSession session = req.getSession(false);
            String rol = (session != null) ? (String) session.getAttribute("rol") : null;

            boolean isVendedor = "VENDEDOR".equalsIgnoreCase(rol) || "ADMIN".equalsIgnoreCase(rol);

            if (!isVendedor) {
                res.sendRedirect(req.getContextPath() + "/login");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
}