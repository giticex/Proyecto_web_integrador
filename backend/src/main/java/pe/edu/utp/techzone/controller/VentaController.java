package pe.edu.utp.techzone.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.utp.techzone.dto.RegistrarVentaRequest;
import pe.edu.utp.techzone.dto.VentaDTO;
import pe.edu.utp.techzone.service.VentaService;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {
    private final VentaService ventaService;
    @PostMapping
    public VentaDTO registrar(@Valid @RequestBody RegistrarVentaRequest request) {
        return ventaService.registrar(request);
    }

    @PostMapping("/pos")
    public ResponseEntity<VentaDTO> registrarDesdePos(
        @Valid @RequestBody RegistrarVentaRequest request,
        Principal principal
   ) {
    if (principal == null
            || principal.getName() == null
            || principal.getName().isBlank()) {

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Se requiere una sesión autenticada."
        );
    }

    VentaDTO venta = ventaService.registrarDesdePos(
            request,
            principal.getName()
    );

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(venta);
    }

    @GetMapping("/{idVenta}")
    public VentaDTO buscar(@PathVariable String idVenta) {
        return ventaService.buscar(idVenta);
    }

    @GetMapping
    public List<VentaDTO> listarTodas() {
        return ventaService.listarTodas();
    }

    @GetMapping("/cliente/{idCliente}")
    public List<VentaDTO> historialCliente(@PathVariable String idCliente) {
        return ventaService.historialCliente(idCliente);
    }


@GetMapping("/paginadas")
public ResponseEntity<Page<VentaDTO>> listarPaginadas(
        @PageableDefault(size = 20) Pageable pageable
) {
    Pageable paginacionSegura = PageRequest.of(
            Math.max(0, pageable.getPageNumber()),
            Math.max(1, Math.min(pageable.getPageSize(), 100))
    );

    return ResponseEntity.ok(
            ventaService.listarTodasPaginadas(paginacionSegura)
    );
}

@GetMapping("/cliente/{idCliente}/paginadas")
public ResponseEntity<Page<VentaDTO>> historialClientePaginado(
        @PathVariable String idCliente,
        @PageableDefault(size = 20) Pageable pageable
) {
    Pageable paginacionSegura = PageRequest.of(
            Math.max(0, pageable.getPageNumber()),
            Math.max(1, Math.min(pageable.getPageSize(), 100))
    );

    return ResponseEntity.ok(
            ventaService.historialClientePaginado(
                    idCliente,
                    paginacionSegura
            )
    );
}

}
