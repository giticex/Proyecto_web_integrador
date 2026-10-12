package pe.edu.utp.techzone.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.techzone.dto.AuthResponseDTO;
import pe.edu.utp.techzone.dto.LoginRequest;
import pe.edu.utp.techzone.entity.Cliente;
import pe.edu.utp.techzone.entity.Usuario;
import pe.edu.utp.techzone.exception.BusinessException;
import pe.edu.utp.techzone.repository.ClienteRepository;
import pe.edu.utp.techzone.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordService passwordService;

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequest request) {
        String idIngresado = request.getIdCliente();

        Usuario empleado = usuarioRepository.findByUsuario(idIngresado).orElse(null);
        
        if (empleado != null) {
            if (!passwordService.coincide(request.getPassword(), empleado.getContrasena())) {
                throw new BusinessException("Credenciales incorrectas");
            }
            
            String rolUsuario = (empleado.getRol() != null && !empleado.getRol().trim().isEmpty()) 
                                ? empleado.getRol().toUpperCase() 
                                : "VENDEDOR";

            return AuthResponseDTO.builder()
                    .id(empleado.getPersona().getIdPersona())
                    .nombre(empleado.getPersona().getNombre())
                    .apellido(empleado.getPersona().getApellido())
                    .correo(empleado.getPersona().getCorreo())
                    .telefono(empleado.getPersona().getTelefono())
                    .usuario(empleado.getUsuario())
                    .rol(rolUsuario)
                    .build();
        }

        Cliente cliente = clienteRepository.findById(idIngresado)
                .orElseThrow(() -> new BusinessException("Credenciales incorrectas"));

        if (!passwordService.coincide(request.getPassword(), cliente.getContrasena())) {
            throw new BusinessException("Credenciales incorrectas");
        }

        return AuthResponseDTO.builder()
                .id(cliente.getIdCliente())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .correo(cliente.getCorreo())
                .telefono(cliente.getTelefono())
                .usuario(cliente.getIdCliente())
                .rol("CLIENTE")
                .build();
    }
}