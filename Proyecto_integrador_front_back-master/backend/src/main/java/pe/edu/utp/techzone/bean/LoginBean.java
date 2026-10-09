package pe.edu.utp.techzone.bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import pe.edu.utp.techzone.dto.AuthResponseDTO;
import pe.edu.utp.techzone.dto.LoginRequest;
import pe.edu.utp.techzone.service.AuthService;

/** Sesion exclusivamente JSF: no modifica el flujo de autenticacion de React. */
@Component("loginBean")
@SessionScope
@Getter
@Setter
public class LoginBean {
    private final AuthService authService;
    private String usuario;
    private String password;
    private AuthResponseDTO usuarioActual;
    private String rol;

    public LoginBean(AuthService authService) {
        this.authService = authService;
    }

    public String ingresar() {
        try {
            LoginRequest request = new LoginRequest();
            request.setIdCliente(usuario == null ? "" : usuario.trim());
            request.setPassword(password);
            AuthResponseDTO respuesta = authService.login(request);
            // Modelo actual: usuario 'admin' es ADMIN; los demas registros en
            // tabla 'usuario' son VENDEDORES. No existe columna de roles.
            if ("ADMIN".equals(respuesta.getRol())) {
                rol = "admin".equalsIgnoreCase(respuesta.getUsuario()) ? "ADMIN" : "VENDEDOR";
            } else {
                rol = "CLIENTE";
            }
            usuarioActual = respuesta;
            password = null;
            HttpServletRequest http = (HttpServletRequest) FacesContext.getCurrentInstance()
                    .getExternalContext().getRequest();
            http.getSession(true);
            http.changeSessionId();
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("jsfRol", rol);
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
                    .put("jsfUsuario", respuesta.getUsuario());
            return switch (rol) {
                case "ADMIN" -> "irAdmin";
                case "VENDEDOR" -> "irVendedor";
                default -> "irCliente";
            };
        } catch (RuntimeException error) {
            usuarioActual = null;
            rol = null;
            password = null;
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("jsfRol");
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("jsfUsuario");
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Usuario o contraseña incorrectos", null));
            return null;
        }
    }

    public String cerrarSesion() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/jsf/login.xhtml?faces-redirect=true";
    }

    public boolean isAutenticado() {
        return usuarioActual != null;
    }

    public boolean isAdministrador() {
        return "ADMIN".equals(rol);
    }

    public boolean isVendedor() {
        return "VENDEDOR".equals(rol);
    }

    public boolean isCliente() {
        return "CLIENTE".equals(rol);
    }

    public boolean isPersonal() {
        return isAdministrador() || isVendedor();
    }

    public String getNombreCompleto() {
        if (usuarioActual == null) return "Invitado";
        return usuarioActual.getNombre() + " " + usuarioActual.getApellido();
    }
}
