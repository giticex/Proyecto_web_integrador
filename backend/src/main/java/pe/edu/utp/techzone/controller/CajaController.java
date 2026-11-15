package pe.edu.utp.techzone.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.utp.techzone.dto.AbrirCajaRequest;
import pe.edu.utp.techzone.dto.CajaDTO;
import pe.edu.utp.techzone.dto.CerrarCajaRequest;
import pe.edu.utp.techzone.service.CajaService;

import java.security.Principal;

@RestController
@RequestMapping("/api/cajas")
@RequiredArgsConstructor
public class CajaController {

    private final CajaService cajaService;

    // =========================================================
    // 1. ABRIR CAJA
    // POST /api/cajas/abrir
    // =========================================================

    @PostMapping("/abrir")
    public ResponseEntity<CajaDTO> abrir(
            @Valid @RequestBody AbrirCajaRequest request,
            Principal principal
    ) {

        String usuario = obtenerUsuarioAutenticado(principal);

        CajaDTO caja = cajaService.abrirCaja(request, usuario);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(caja);
    }


    // =========================================================
    // 2. CONSULTAR CAJA ACTUAL
    // GET /api/cajas/actual
    // =========================================================

    @GetMapping("/actual")
    public ResponseEntity<CajaDTO> obtenerActual(
            Principal principal
    ) {

        String usuario = obtenerUsuarioAutenticado(principal);

        return ResponseEntity.ok(
                cajaService.obtenerCajaActual(usuario)
        );
    }


    // =========================================================
    // 3. CERRAR CAJA
    // POST /api/cajas/{id}/cerrar
    // =========================================================

    @PostMapping("/{id}/cerrar")
    public ResponseEntity<CajaDTO> cerrar(
            @PathVariable Long id,
            @Valid @RequestBody CerrarCajaRequest request,
            Principal principal
    ) {

        String usuario = obtenerUsuarioAutenticado(principal);

        CajaDTO caja = cajaService.cerrarCaja(
                id,
                request,
                usuario
        );

        return ResponseEntity.ok(caja);
    }


    // =========================================================
    // 4. HISTORIAL DE CAJAS
    // GET /api/cajas/historial?page=0&size=20
    // =========================================================

    @GetMapping("/historial")
    public ResponseEntity<Page<CajaDTO>> historial(
            @PageableDefault(size = 20) Pageable pageable,
            Principal principal
    ) {

        String usuario = obtenerUsuarioAutenticado(principal);

        // Limitar el tamaño de página para evitar consultas excesivas.
        int pagina = Math.max(0, pageable.getPageNumber());
        int tamano = Math.max(1, Math.min(pageable.getPageSize(), 100));

        Pageable paginacionSegura = PageRequest.of(pagina, tamano);

        return ResponseEntity.ok(
                cajaService.listarHistorial(usuario, paginacionSegura)
        );
    }


    // =========================================================
    // 5. OBTENER USUARIO AUTENTICADO
    // =========================================================

    private String obtenerUsuarioAutenticado(Principal principal) {

        if (principal == null || principal.getName() == null
                || principal.getName().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Se requiere una sesión autenticada."
            );
        }

        return principal.getName();
    }
}
