package pe.edu.utp.techzone.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-with-at-least-32-characters-2026";

    private TokenBlacklistService blacklist;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        blacklist = new TokenBlacklistService();
        jwtService = new JwtService(SECRET, 60_000, 300_000, blacklist);
    }

    @Test
    void generaYValidaAccessTokenConClaimsDelUsuario() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(
                "PERADM", "admin", "Administrador", "General",
                "admin@techzone.pe", "999999999", "ADMIN");

        String token = jwtService.generarAccessToken(usuario);
        var claims = jwtService.parsearAccess(token);

        assertNotNull(token);
        assertEquals("admin", claims.getSubject());
        assertEquals("ADMIN", claims.get("rol", String.class));
        assertEquals("PERADM", claims.get("id", String.class));
    }

    @Test
    void unAccessTokenNoPuedeUsarseComoRefresh() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(
                "PERADM", "admin", "Administrador", "General",
                "admin@techzone.pe", "999999999", "ADMIN");

        String token = jwtService.generarAccessToken(usuario);

        assertThrows(Exception.class, () -> jwtService.parsearRefresh(token));
    }

    @Test
    void revocarInvalidaElToken() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(
                "PERADM", "admin", "Administrador", "General",
                "admin@techzone.pe", "999999999", "ADMIN");

        String token = jwtService.generarAccessToken(usuario);
        jwtService.revocar(token);

        assertThrows(Exception.class, () -> jwtService.parsearAccess(token));
    }

    @Test
    void laListaLimpiaTokensExpirados() {
        blacklist.revocar("jti-expirado", java.time.Instant.now().minusSeconds(10));

        assertFalse(blacklist.estaRevocado("jti-expirado"));
    }

    @Test
    void registraTokenRevocadoVigente() {
        blacklist.revocar("jti-vigente", java.time.Instant.now().plusSeconds(60));

        assertTrue(blacklist.estaRevocado("jti-vigente"));
    }
}
