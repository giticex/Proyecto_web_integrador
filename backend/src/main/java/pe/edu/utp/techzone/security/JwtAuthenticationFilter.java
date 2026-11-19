package pe.edu.utp.techzone.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.edu.utp.techzone.entity.Cliente;
import pe.edu.utp.techzone.entity.Usuario;
import pe.edu.utp.techzone.repository.ClienteRepository;
import pe.edu.utp.techzone.repository.UsuarioRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Lee el header Authorization: Bearer &lt;token&gt;, valida el JWT y coloca el
 * usuario autenticado en el SecurityContext para que @PreAuthorize funcione.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UsuarioRepository usuarioRepository,
                                   ClienteRepository clienteRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extraerToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtService.parsearAccess(token);
                Optional<UsuarioAutenticado> principal = cargarPrincipal(claims);
                principal.ifPresent(usuario -> autenticar(usuario, request));
            } catch (JwtException | IllegalArgumentException ignored) {
                // Token invalido/expirado: se continua sin autenticacion; el
                // entry point devolvera 401 en las rutas protegidas.
            }
        }

        filterChain.doFilter(request, response);
    }

    private void autenticar(UsuarioAutenticado usuario, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol())));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Optional<UsuarioAutenticado> cargarPrincipal(Claims claims) {
        String identificador = claims.getSubject();
        if (identificador == null || identificador.isBlank()) {
            return Optional.empty();
        }

        // Primero se busca un empleado (ADMIN / VENDEDOR) y luego un cliente.
        Optional<Usuario> empleado = usuarioRepository.findByUsuario(identificador);
        if (empleado.isPresent()) {
            Usuario u = empleado.get();
            String rol = u.getRol() == null ? "VENDEDOR" : u.getRol().name();
            return Optional.of(new UsuarioAutenticado(
                    u.getPersona() != null ? u.getPersona().getIdPersona() : null,
                    u.getUsuario(),
                    u.getPersona() != null ? u.getPersona().getNombre() : null,
                    u.getPersona() != null ? u.getPersona().getApellido() : null,
                    u.getPersona() != null ? u.getPersona().getCorreo() : null,
                    u.getPersona() != null ? u.getPersona().getTelefono() : null,
                    rol));
        }

        return clienteRepository.findById(identificador)
                .map(this::principalCliente);
    }

    private UsuarioAutenticado principalCliente(Cliente cliente) {
        return new UsuarioAutenticado(
                cliente.getIdCliente(),
                cliente.getIdCliente(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getCorreo(),
                cliente.getTelefono(),
                "CLIENTE");
    }

    private String extraerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(PREFIJO_BEARER)) {
            return null;
        }
        String token = header.substring(PREFIJO_BEARER.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
