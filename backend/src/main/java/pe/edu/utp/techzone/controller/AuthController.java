package pe.edu.utp.techzone.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.techzone.dto.AuthResponseDTO;
import pe.edu.utp.techzone.dto.LoginRequest;
import pe.edu.utp.techzone.dto.LogoutRequest;
import pe.edu.utp.techzone.dto.RefreshTokenRequest;
import pe.edu.utp.techzone.security.JwtService;
import pe.edu.utp.techzone.security.UsuarioAutenticado;
import pe.edu.utp.techzone.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponseDTO refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request.getRefreshToken());
    }

    @PostMapping("/logout")
    public Map<String, String> logout(@RequestBody(required = false) LogoutRequest request,
                                      HttpServletRequest http) {
        String accessToken = extraerBearer(http);
        if (accessToken != null) {
            jwtService.revocar(accessToken);
        }
        if (request != null && request.getRefreshToken() != null) {
            jwtService.revocar(request.getRefreshToken());
        }
        return Map.of("mensaje", "Sesion cerrada correctamente");
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public AuthResponseDTO me(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return authService.desdePrincipal(usuario);
    }

    private String extraerBearer(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        String token = header.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
