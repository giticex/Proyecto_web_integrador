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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String usuario = request.getParameter("username");
        String password = request.getParameter("password");
        
        
        if (usuario != null && !usuario.isEmpty()) {
            HttpSession session = request.getSession();
            session.setAttribute("usuario", usuario);
            
            session.setAttribute("rol", "vendedor"); 
            
            
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } else {
            
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }
}