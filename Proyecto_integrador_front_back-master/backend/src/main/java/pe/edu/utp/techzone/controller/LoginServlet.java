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
        // Aquí validarías con tu base de datos o lógica de negocio
        String rol = "vendedor"; 
        
        // Guardar datos en la sesión como pide la rúbrica
        HttpSession session = request.getSession();
        session.setAttribute("usuario", usuario);
        session.setAttribute("rol", rol);
        
        // Redirigir a la vista correspondiente
        response.sendRedirect("dashboard.jsp");
    }
}