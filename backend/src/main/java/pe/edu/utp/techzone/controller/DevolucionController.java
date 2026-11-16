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

import pe.edu.utp.techzone.dto.DevolucionDTO;
import pe.edu.utp.techzone.dto.RegistrarDevolucionRequest;
import pe.edu.utp.techzone.service.DevolucionService;
import pe.edu.utp.techzone.dto.CantidadDevueltaDTO;
import java.util.List;

import java.security.Principal;

@RestController
@RequestMapping("/api/devoluciones")
@RequiredArgsConstructor
public class DevolucionController {

    private final DevolucionService devolucionService;


    // =========================================================
    // 1. REGISTRAR DEVOLUCION
    // POST /api/devoluciones
    // =========================================================

    @PostMapping
    public ResponseEntity<DevolucionDTO> registrar(
            @Valid @RequestBody RegistrarDevolucionRequest request,
            Principal principal
    ) {

        String usuario = obtenerUsuarioAutenticado(principal);

        DevolucionDTO devolucion =
                devolucionService.registrar(request, usuario);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(devolucion);
    }


    // =========================================================
    // 2. LISTAR DEVOLUCIONES
    // GET /api/devoluciones?page=0&size=20
    // =========================================================

    @GetMapping
    public ResponseEntity<Page<DevolucionDTO>> listar(
            @PageableDefault(size = 20) Pageable pageable,
            Principal principal
    ) {

        String usuario = obtenerUsuarioAutenticado(principal);

        Pageable paginacionSegura = crearPaginacionSegura(pageable);

        return ResponseEntity.ok(
                devolucionService.listar(usuario, paginacionSegura)
        );
    }


    // =========================================================
    // 3. BUSCAR UNA DEVOLUCION
    // GET /api/devoluciones/{id}
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<DevolucionDTO> buscar(
            @PathVariable Long id,
            Principal principal
    ) {

        String usuario = obtenerUsuarioAutenticado(principal);

        return ResponseEntity.ok(
                devolucionService.buscar(id, usuario)
        );
    }


    // =========================================================
    // 4. DEVOLUCIONES DE UNA VENTA
    // GET /api/devoluciones/venta/{idVenta}
    // =========================================================

    @GetMapping("/venta/{idVenta}")
    public ResponseEntity<Page<DevolucionDTO>> listarPorVenta(
            @PathVariable String idVenta,
            @PageableDefault(size = 20) Pageable pageable,
            Principal principal
    ) {

        String usuario = obtenerUsuarioAutenticado(principal);

        Pageable paginacionSegura = crearPaginacionSegura(pageable);

        return ResponseEntity.ok(
                devolucionService.listarPorVenta(
                        idVenta,
                        usuario,
                        paginacionSegura
                )
        );
    }


// =========================================================
// 5. RESUMEN DE CANTIDADES DEVUELTAS
// GET /api/devoluciones/venta/{idVenta}/resumen
// =========================================================

@GetMapping("/venta/{idVenta}/resumen")
public ResponseEntity<List<CantidadDevueltaDTO>> resumenPorVenta(
        @PathVariable String idVenta,
        Principal principal
) {

    String usuario = obtenerUsuarioAutenticado(principal);

    return ResponseEntity.ok(
            devolucionService.resumenCantidadesDevueltas(
                    idVenta,
                    usuario
            )
    );
}


    // =========================================================
    // 5. VALIDAR SESION
    // =========================================================

    private String obtenerUsuarioAutenticado(Principal principal) {

        if (principal == null
                || principal.getName() == null
                || principal.getName().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Se requiere una sesión autenticada."
            );
        }

        return principal.getName();
    }


    // =========================================================
    // 6. PAGINACION SEGURA
    // =========================================================

    private Pageable crearPaginacionSegura(Pageable pageable) {

        int pagina = Math.max(0, pageable.getPageNumber());

        int tamano = Math.max(
                1,
                Math.min(pageable.getPageSize(), 100)
        );

        return PageRequest.of(pagina, tamano);
    }
}
