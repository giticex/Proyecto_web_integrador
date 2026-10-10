package pe.edu.utp.techzone.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private final PasswordEncoder passwordEncoder;

    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String preparar(String texto) {
        String normalizado = texto == null ? "" : texto.trim();
        return passwordEncoder.encode(normalizado);
    }

    public boolean coincide(String passwordIngresado, String passwordGuardado) {
        if (passwordIngresado == null || passwordGuardado == null) {
            return false;
        }

        String ingresado = passwordIngresado.trim();
        String guardado = passwordGuardado.trim();
        if (esBcrypt(guardado)) {
            return passwordEncoder.matches(ingresado, guardado);
        }

        return !ingresado.isEmpty() && ingresado.equals(guardado);
    }

    public boolean necesitaRehash(String passwordGuardado) {
        if (!esBcrypt(passwordGuardado)) {
            return true;
        }
        return passwordEncoder.upgradeEncoding(passwordGuardado);
    }

    private boolean esBcrypt(String password) {
        return password != null && password.matches("\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}");
    }
}
