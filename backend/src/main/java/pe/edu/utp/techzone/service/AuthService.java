package pe.edu.utp.techzone.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.techzone.dto.AuthResponseDTO;
import pe.edu.utp.techzone.dto.LoginRequest;
import pe.edu.utp.techzone.entity.Cliente;
import pe.edu.utp.techzone.entity.RolUsuario;
import pe.edu.utp.techzone.entity.Usuario;
import pe.edu.utp.techzone.exception.BusinessException;
import pe.edu.utp.techzone.repository.ClienteRepository;
import pe.edu.utp.techzone.repository.UsuarioRepository;
import pe.edu.utp.techzone.security.JwtService;
import pe.edu.utp.techzone.security.UsuarioAutenticado;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequest request) {
        String idIngresado = request.getIdCliente();

        Usuario empleado = usuarioRepository.findByUsuario(idIngresado).orElse(null);

        if (empleado != null) {
            if (!passwordService.coincide(request.getPassword(), empleado.getContrasena())) {
                throw new BusinessException("Credenciales incorrectas");
            }

            String rolUsuario = empleado.getRol() != null
                    ? empleado.getRol().name()
                    : RolUsuario.VENDEDOR.name();

            AuthResponseDTO datos = AuthResponseDTO.builder()
                    .id(empleado.getPersona().getIdPersona())
                    .nombre(empleado.getPersona().getNombre())
                    .apellido(empleado.getPersona().getApellido())
                    .correo(empleado.getPersona().getCorreo())
                    .telefono(empleado.getPersona().getTelefono())
                    .usuario(empleado.getUsuario())
                    .rol(rolUsuario)
                    .build();
            return emitirTokens(datos);
        }

        Cliente cliente = clienteRepository.findById(idIngresado)
                .orElseThrow(() -> new BusinessException("Credenciales incorrectas"));

        if (!passwordService.coincide(request.getPassword(), cliente.getContrasena())) {
            throw new BusinessException("Credenciales incorrectas");
        }

        AuthResponseDTO datos = AuthResponseDTO.builder()
                .id(cliente.getIdCliente())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .correo(cliente.getCorreo())
                .telefono(cliente.getTelefono())
                .usuario(cliente.getIdCliente())
                .rol(RolUsuario.CLIENTE.name())
                .build();
        return emitirTokens(datos);
    }

    /**
     * Renueva el access token a partir de un refresh token valido. El refresh
     * token usado se revoca (rotacion) para evitar reutilizacion.
     */
    @Transactional(readOnly = true)
    public AuthResponseDTO refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtService.parsearRefresh(refreshToken);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BusinessException("Refresh token invalido o expirado");
        }

        AuthResponseDTO datos = buscarDatos(claims.getSubject());
        jwtService.revocar(refreshToken);
        return emitirTokens(datos);
    }

    public AuthResponseDTO desdePrincipal(UsuarioAutenticado usuario) {
        return AuthResponseDTO.builder()
                .id(usuario.id())
                .nombre(usuario.nombre())
                .apellido(usuario.apellido())
                .correo(usuario.correo())
                .telefono(usuario.telefono())
                .usuario(usuario.usuario())
                .rol(usuario.rol())
                .build();
    }

    private AuthResponseDTO buscarDatos(String identificador) {
        return usuarioRepository.findByUsuario(identificador)
                .map(empleado -> AuthResponseDTO.builder()
                        .id(empleado.getPersona().getIdPersona())
                        .nombre(empleado.getPersona().getNombre())
                        .apellido(empleado.getPersona().getApellido())
                        .correo(empleado.getPersona().getCorreo())
                        .telefono(empleado.getPersona().getTelefono())
                        .usuario(empleado.getUsuario())
                        .rol(empleado.getRol() != null ? empleado.getRol().name() : RolUsuario.VENDEDOR.name())
                        .build())
                .orElseGet(() -> clienteRepository.findById(identificador)
                        .map(cliente -> AuthResponseDTO.builder()
                                .id(cliente.getIdCliente())
                                .nombre(cliente.getNombre())
                                .apellido(cliente.getApellido())
                                .correo(cliente.getCorreo())
                                .telefono(cliente.getTelefono())
                                .usuario(cliente.getIdCliente())
                                .rol(RolUsuario.CLIENTE.name())
                                .build())
                        .orElseThrow(() -> new BusinessException("El usuario del token ya no existe")));
    }

    private AuthResponseDTO emitirTokens(AuthResponseDTO datos) {
        UsuarioAutenticado principal = new UsuarioAutenticado(
                datos.getId(),
                datos.getUsuario(),
                datos.getNombre(),
                datos.getApellido(),
                datos.getCorreo(),
                datos.getTelefono(),
                datos.getRol());

        datos.setToken(jwtService.generarAccessToken(principal));
        datos.setRefreshToken(jwtService.generarRefreshToken(datos.getUsuario()));
        datos.setTokenType("Bearer");
        datos.setExpiresIn(jwtService.getExpiracionSegundos());
        return datos;
    }
}
