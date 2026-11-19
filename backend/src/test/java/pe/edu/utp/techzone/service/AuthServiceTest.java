package pe.edu.utp.techzone.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import pe.edu.utp.techzone.dto.AuthResponseDTO;
import pe.edu.utp.techzone.dto.LoginRequest;
import pe.edu.utp.techzone.entity.Cliente;
import pe.edu.utp.techzone.entity.Persona;
import pe.edu.utp.techzone.entity.RolUsuario;
import pe.edu.utp.techzone.entity.Usuario;
import pe.edu.utp.techzone.exception.BusinessException;
import pe.edu.utp.techzone.repository.ClienteRepository;
import pe.edu.utp.techzone.repository.UsuarioRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private AuthService authService;

    private LoginRequest login(String id, String password) {
        LoginRequest request = new LoginRequest();
        request.setIdCliente(id);
        request.setPassword(password);
        return request;
    }

    private Usuario empleado(RolUsuario rol) {
        Persona persona = Persona.builder().idPersona("PERADM").nombre("Administrador").apellido("General").build();
        return Usuario.builder().usuario("admin").contrasena("hash").rol(rol).persona(persona).build();
    }

    @Test
    @DisplayName("Login de empleado correcto devuelve su rol")
    void loginEmpleado() {
        when(usuarioRepository.findByUsuario("admin")).thenReturn(Optional.of(empleado(RolUsuario.ADMIN)));
        when(passwordService.coincide("123456", "hash")).thenReturn(true);

        AuthResponseDTO respuesta = authService.login(login("admin", "123456"));

        assertThat(respuesta.getRol()).isEqualTo("ADMIN");
        assertThat(respuesta.getId()).isEqualTo("PERADM");
        assertThat(respuesta.getUsuario()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Empleado sin rol en BD se trata como VENDEDOR")
    void empleadoSinRolEsVendedor() {
        when(usuarioRepository.findByUsuario("admin")).thenReturn(Optional.of(empleado(null)));
        when(passwordService.coincide("123456", "hash")).thenReturn(true);

        assertThat(authService.login(login("admin", "123456")).getRol()).isEqualTo("VENDEDOR");
    }

    @Test
    @DisplayName("Contraseña incorrecta de empleado lanza BusinessException")
    void empleadoPasswordIncorrecto() {
        when(usuarioRepository.findByUsuario("admin")).thenReturn(Optional.of(empleado(RolUsuario.ADMIN)));
        when(passwordService.coincide("mala", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(login("admin", "mala")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Credenciales incorrectas");
    }

    @Test
    @DisplayName("Login de cliente correcto devuelve rol CLIENTE")
    void loginCliente() {
        Cliente cliente = Cliente.builder().idCliente("CLI001").nombre("Luis").apellido("García").contrasena("hash").build();
        when(usuarioRepository.findByUsuario("CLI001")).thenReturn(Optional.empty());
        when(clienteRepository.findById("CLI001")).thenReturn(Optional.of(cliente));
        when(passwordService.coincide("123456", "hash")).thenReturn(true);

        AuthResponseDTO respuesta = authService.login(login("CLI001", "123456"));

        assertThat(respuesta.getRol()).isEqualTo("CLIENTE");
        assertThat(respuesta.getId()).isEqualTo("CLI001");
    }

    @Test
    @DisplayName("Usuario inexistente lanza BusinessException con mensaje genérico")
    void usuarioInexistente() {
        when(usuarioRepository.findByUsuario("nadie")).thenReturn(Optional.empty());
        when(clienteRepository.findById("nadie")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(login("nadie", "123456")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Credenciales incorrectas");
    }
}
