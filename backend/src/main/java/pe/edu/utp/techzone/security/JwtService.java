package pe.edu.utp.techzone.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Servicio central de emision, lectura y revocacion de tokens JWT.
 *
 * <p>Se emiten dos tipos de token:</p>
 * <ul>
 *   <li><strong>access</strong>: vida corta (1 hora por defecto) y se envia en
 *       el header Authorization de cada peticion.</li>
 *   <li><strong>refresh</strong>: vida larga (7 dias por defecto) y solo se usa
 *       en POST /api/auth/refresh para renovar el access token.</li>
 * </ul>
 */
@Service
public class JwtService {

    public static final String TIPO_ACCESS = "access";
    public static final String TIPO_REFRESH = "refresh";

    private final SecretKey clave;
    private final long expiracionMs;
    private final long expiracionRefreshMs;
    private final TokenBlacklistService blacklistService;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:3600000}") long expiracionMs,
            @Value("${app.jwt.refresh-expiration-ms:604800000}") long expiracionRefreshMs,
            TokenBlacklistService blacklistService) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("app.jwt.secret debe tener al menos 32 caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
        this.expiracionRefreshMs = expiracionRefreshMs;
        this.blacklistService = blacklistService;
    }

    public String generarAccessToken(UsuarioAutenticado usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(usuario.usuario())
                .claim("tipo", TIPO_ACCESS)
                .claim("id", usuario.id())
                .claim("rol", usuario.rol())
                .claim("nombre", usuario.nombre())
                .claim("apellido", usuario.apellido())
                .claim("correo", usuario.correo())
                .claim("telefono", usuario.telefono())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusMillis(expiracionMs)))
                .signWith(clave)
                .compact();
    }

    public String generarRefreshToken(String usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(usuario)
                .claim("tipo", TIPO_REFRESH)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusMillis(expiracionRefreshMs)))
                .signWith(clave)
                .compact();
    }

    /** Valida la firma y devuelve los claims, o lanza JwtException si es invalido. */
    public Claims parsear(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims parsearAccess(String token) {
        Claims claims = parsear(token);
        if (!TIPO_ACCESS.equals(claims.get("tipo", String.class))) {
            throw new JwtException("El token no es de tipo access");
        }
        if (blacklistService.estaRevocado(claims.getId())) {
            throw new JwtException("Token revocado");
        }
        return claims;
    }

    public Claims parsearRefresh(String token) {
        Claims claims = parsear(token);
        if (!TIPO_REFRESH.equals(claims.get("tipo", String.class))) {
            throw new JwtException("El token no es de tipo refresh");
        }
        if (blacklistService.estaRevocado(claims.getId())) {
            throw new JwtException("Token revocado");
        }
        return claims;
    }

    /** Invalida un token (logout o rotacion del refresh token). */
    public void revocar(String token) {
        try {
            Claims claims = parsear(token);
            blacklistService.revocar(claims.getId(), claims.getExpiration().toInstant());
        } catch (JwtException | IllegalArgumentException ignored) {
            // Un token ya invalido no necesita entrar a la lista negra.
        }
    }

    public long getExpiracionSegundos() {
        return expiracionMs / 1000;
    }

    public long getExpiracionMs() {
        return expiracionMs;
    }

    public long getExpiracionRefreshMs() {
        return expiracionRefreshMs;
    }
}
