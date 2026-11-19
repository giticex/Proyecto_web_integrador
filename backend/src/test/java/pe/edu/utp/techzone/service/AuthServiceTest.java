package pe.edu.utp.techzone.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.utp.techzone.dto.AuthResponseDTO;
import pe.edu.utp.techzone.dto.LoginRequest;
import pe.edu.utp.techzone.entity.Persona;
import pe.edu.utp.techzone.entity.RolUsuario;
import pe.edu.utp.techzone.entity.Usuario;
import pe.edu.utp.techzone.exception.BusinessException;
import pe.edu.utp.techzone.repository.ClienteRepository;
import pe.edu.utp.techzone.repository.UsuarioRepository;
import pe.edu.utp.techzone.security.JwtService;
import pe.edu.utp.techzone.security.TokenBlacklistService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private PasswordService passwordService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        JwtService jwtService = new JwtService(
                "test-secret-key-with-at-least-32-characters-2026",
                60_000, 300_000, new TokenBlacklistService());
        authService = new AuthService(usuarioRepository, clienteRepository, passwordService, jwtService);
    }

    @Test
    void loginDeEmpleadoDevuelveAccessYRefreshToken() {
        Persona persona = Persona.builder()
                .idPersona("PERADM").nombre("Administrador").apellido("General")
                .correo("admin@techzone.pe").telefono("999999999").usuario("admin")
                .build();
        Usuario admin = Usuario.builder()
                .idUsuario(1).usuario("admin").contrasena("hash")
                .rol(RolUsuario.ADMIN).persona(persona)
                .build();

        when(usuarioRepository.findByUsuario("admin")).thenReturn(Optional.of(admin));
        when(passwordService.coincide("123456", "hash")).thenReturn(true);

        LoginRequest request = new LoginRequest();
        request.setIdCliente("admin");
        request.setPassword("123456");

        AuthResponseDTO respuesta = authService.login(request);

        assertEquals("ADMIN", respuesta.getRol());
        assertEquals("Bearer", respuesta.getTokenType());
        assertNotNull(respuesta.getToken());
        assertNotNull(respuesta.getRefreshToken());
    }

    @Test
    void loginConPasswordIncorrectaFalla() {
        Persona persona = Persona.builder().idPersona("PERADM").nombre("Admin").apellido("Uno").build();
        Usuario admin = Usuario.builder()
                .usuario("admin").contrasena("hash").rol(RolUsuario.ADMIN).persona(persona).build();

        when(usuarioRepository.findByUsuario("admin")).thenReturn(Optional.of(admin));
        when(passwordService.coincide("mala", "hash")).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setIdCliente("admin");
        request.setPassword("mala");

        assertThrows(BusinessException.class, () -> authService.login(request));
    }
}
