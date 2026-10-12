package pe.edu.utp.techzone.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/vendedor/panel")
public class VendedorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Validar control de acceso por rol en sesión (HttpSession.setAttribute("rol"))
        HttpSession session = request.getSession(false);
        String rol = (session != null) ? (String) session.getAttribute("rol") : null;

        if ("vendedor".equals(rol)) {
            // Si es vendedor, redirige o carga la vista de su panel
            request.getRequestDispatcher("/WEB-INF/views/mi_panel.jsp").forward(request, response);
        } else {
            // Si no tiene el rol, se le bloquea y redirige al login o error
            response.sendRedirect(request.getContextPath() + "/WEB-INF/views/error/404.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}