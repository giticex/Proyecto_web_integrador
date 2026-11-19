package pe.edu.utp.techzone.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordServiceTest {

    private final PasswordService passwordService =
            new PasswordService(new BCryptPasswordEncoder(4));

    @Test
    void validaPasswordBcrypt() {
        String hash = passwordService.preparar("123456");

        assertTrue(passwordService.coincide("123456", hash));
        assertFalse(passwordService.coincide("incorrecta", hash));
    }

    @Test
    void mantieneCompatibilidadConPasswordsPlanos() {
        assertTrue(passwordService.coincide("123456", "123456"));
        assertFalse(passwordService.coincide("000000", "123456"));
    }
}
