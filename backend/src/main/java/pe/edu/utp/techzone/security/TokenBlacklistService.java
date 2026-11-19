package pe.edu.utp.techzone.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lista negra en memoria de tokens JWT revocados (logout).
 *
 * <p>Para un despliegue con multiples instancias se recomienda sustituir este
 * mapa por Redis o una tabla en base de datos manteniendo la misma interfaz.</p>
 */
@Service
public class TokenBlacklistService {

    private final Map<String, Instant> revocados = new ConcurrentHashMap<>();

    public void revocar(String jti, Instant expiracion) {
        if (jti == null || jti.isBlank()) {
            return;
        }
        limpiarExpirados();
        revocados.put(jti, expiracion == null ? Instant.now() : expiracion);
    }

    public boolean estaRevocado(String jti) {
        if (jti == null) {
            return false;
        }
        Instant expiracion = revocados.get(jti);
        if (expiracion == null) {
            return false;
        }
        if (expiracion.isBefore(Instant.now())) {
            revocados.remove(jti);
            return false;
        }
        return true;
    }

    private void limpiarExpirados() {
        Instant ahora = Instant.now();
        revocados.entrySet().removeIf(entry -> entry.getValue().isBefore(ahora));
    }
}
