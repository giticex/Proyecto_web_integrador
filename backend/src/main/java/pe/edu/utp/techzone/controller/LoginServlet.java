package pe.edu.utp.techzone.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Muestra de forma segura la vista del login que está en WEB-INF
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String usuario = request.getParameter("username");
        String password = request.getParameter("password");
        
        if (usuario != null && !usuario.isEmpty() && password != null && !password.isEmpty()) {
            HttpSession session = request.getSession();
            session.setAttribute("usuario", usuario);
            session.setAttribute("rol", "vendedor"); 
            
            // Redirige correctamente al dashboard
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } else {
            // Si hay error, recarga el login de forma segura mediante el servlet
            response.sendRedirect(request.getContextPath() + "/login?error=true");
        }
    }
}