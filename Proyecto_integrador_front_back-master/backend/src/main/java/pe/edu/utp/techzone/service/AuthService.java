package pe.edu.utp.techzone.service;

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

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordService passwordService;

    public Optional<Usuario> buscarUsuarioConRol(String username) {
        return usuarioRepository.findByUsuario(username)
                .map(cuenta -> Usuario.builder()
                        .usuario(cuenta.getUsuario())
                        .rol(cuenta.getRol())
                        .build());
    }

    @Transactional
    public AuthResponseDTO login(LoginRequest request) {
        String usuario = request.getIdCliente();

        Usuario cuenta = usuarioRepository.findByUsuario(usuario).orElse(null);
        if (cuenta != null && passwordService.coincide(request.getPassword(), cuenta.getContrasena())) {
            if (passwordService.necesitaRehash(cuenta.getContrasena())) {
                cuenta.setContrasena(passwordService.preparar(request.getPassword()));
                usuarioRepository.save(cuenta);
            }

            return AuthResponseDTO.builder()
                    .id(cuenta.getPersona().getIdPersona())
                    .nombre(cuenta.getPersona().getNombre())
                    .apellido(cuenta.getPersona().getApellido())
                    .correo(cuenta.getPersona().getCorreo())
                    .telefono(cuenta.getPersona().getTelefono())
                    .usuario(cuenta.getUsuario())
                    .rol(cuenta.getRol().name())
                    .build();
        }

        Cliente cliente = clienteRepository.findById(usuario)
                .orElseThrow(() -> new BusinessException("Credenciales incorrectas"));

        if (!passwordService.coincide(request.getPassword(), cliente.getContrasena())) {
            throw new BusinessException("Credenciales incorrectas");
        }

        if (passwordService.necesitaRehash(cliente.getContrasena())) {
            cliente.setContrasena(passwordService.preparar(request.getPassword()));
            clienteRepository.save(cliente);
        }

        return AuthResponseDTO.builder()
                .id(cliente.getIdCliente())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .correo(cliente.getCorreo())
                .telefono(cliente.getTelefono())
                .usuario(cliente.getIdCliente())
                .rol(RolUsuario.CLIENTE.name())
                .build();
    }
}
