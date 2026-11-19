package pe.edu.utp.techzone.security;

/**
 * Datos minimos del usuario autenticado que viajan como principal de Spring
 * Security despues de validar el JWT.
 */
public record UsuarioAutenticado(
        String id,
        String usuario,
        String nombre,
        String apellido,
        String correo,
        String telefono,
        String rol
) {
}
