package pe.edu.utp.techzone.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.techzone.dto.CrearVendedorRequest;
import pe.edu.utp.techzone.dto.VendedorDTO;
import pe.edu.utp.techzone.entity.Persona;
import pe.edu.utp.techzone.entity.RolUsuario;
import pe.edu.utp.techzone.entity.Usuario;
import pe.edu.utp.techzone.exception.BusinessException;
import pe.edu.utp.techzone.repository.PersonaRepository;
import pe.edu.utp.techzone.repository.UsuarioRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendedorService {
    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final PasswordService passwordService;

    @Transactional(readOnly = true)
    public List<VendedorDTO> listar() {
        return usuarioRepository.findAllByRol(RolUsuario.VENDEDOR)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public VendedorDTO crear(CrearVendedorRequest request) {
        String username = request.getUsuario().trim();
        String idPersona = request.getIdPersona().trim();

        if (usuarioRepository.findByUsuario(username).isPresent()) {
            throw new BusinessException("El nombre de usuario ya está registrado");
        }
        if (personaRepository.existsById(idPersona)) {
            throw new BusinessException("La persona ya está registrada");
        }

        Persona persona = Persona.builder()
                .idPersona(idPersona)
                .nombre(request.getNombre().trim())
                .apellido(request.getApellido().trim())
                .fechaNacimiento(request.getFechaNacimiento())
                .correo(normalizar(request.getCorreo()))
                .telefono(normalizar(request.getTelefono()))
                .usuario(username)
                .build();
        personaRepository.save(persona);

        Usuario vendedor = Usuario.builder()
                .usuario(username)
                .contrasena(passwordService.preparar(request.getContrasena()))
                .rol(RolUsuario.VENDEDOR)
                .persona(persona)
                .build();

        return toDTO(usuarioRepository.save(vendedor));
    }

    private VendedorDTO toDTO(Usuario usuario) {
        Persona persona = usuario.getPersona();
        return VendedorDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .idPersona(persona.getIdPersona())
                .nombre(persona.getNombre())
                .apellido(persona.getApellido())
                .fechaNacimiento(persona.getFechaNacimiento())
                .correo(persona.getCorreo())
                .telefono(persona.getTelefono())
                .usuario(usuario.getUsuario())
                .rol(usuario.getRol().name())
                .build();
    }

    private String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
