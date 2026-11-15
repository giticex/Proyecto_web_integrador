package pe.edu.utp.techzone.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/** Usa el BCryptPasswordEncoder real (no hace falta mock ni base de datos). */
class PasswordServiceTest {

    private final PasswordService passwordService = new PasswordService(new BCryptPasswordEncoder());

    @Test
    @DisplayName("preparar() genera un hash BCrypt, no el texto plano")
    void prepararGeneraBcrypt() {
        String hash = passwordService.preparar("123456");

        assertThat(hash).isNotEqualTo("123456");
        assertThat(hash).startsWith("$2");
    }

    @Test
    @DisplayName("Una contraseña hasheada coincide con la original y no con otra")
    void coincideConHash() {
        String hash = passwordService.preparar("secreto1");

        assertThat(passwordService.coincide("secreto1", hash)).isTrue();
        assertThat(passwordService.coincide("otra", hash)).isFalse();
    }

    @Test
    @DisplayName("Soporta contraseñas antiguas en texto plano (datos de prueba del script)")
    void coincideConTextoPlano() {
        assertThat(passwordService.coincide("123456", "123456")).isTrue();
        assertThat(passwordService.coincide("654321", "123456")).isFalse();
    }

    @Test
    @DisplayName("Entradas nulas o vacías nunca coinciden")
    void entradasInvalidas() {
        assertThat(passwordService.coincide(null, "123456")).isFalse();
        assertThat(passwordService.coincide("123456", null)).isFalse();
        assertThat(passwordService.coincide("", "")).isFalse();
    }

    @Test
    @DisplayName("necesitaRehash: true para texto plano, false para BCrypt vigente")
    void necesitaRehash() {
        assertThat(passwordService.necesitaRehash("123456")).isTrue();
        assertThat(passwordService.necesitaRehash(passwordService.preparar("123456"))).isFalse();
    }
}
