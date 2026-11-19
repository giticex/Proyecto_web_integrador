package pe.edu.utp.techzone.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.techzone.dto.RegistrarVentaRequest;
import pe.edu.utp.techzone.dto.VentaDTO;
import pe.edu.utp.techzone.security.UsuarioAutenticado;
import pe.edu.utp.techzone.service.VentaService;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {
    private final VentaService ventaService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public VentaDTO registrar(@Valid @RequestBody RegistrarVentaRequest request) {
        return ventaService.registrar(request);
    }

    @GetMapping("/{idVenta}")
    @PreAuthorize("isAuthenticated()")
    public VentaDTO buscar(@PathVariable String idVenta) {
        return ventaService.buscar(idVenta);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    public List<VentaDTO> listarTodas() {
        return ventaService.listarTodas();
    }

    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("isAuthenticated()")
    public List<VentaDTO> historialCliente(@PathVariable String idCliente,
                                           @AuthenticationPrincipal UsuarioAutenticado usuario) {
        if ("CLIENTE".equals(usuario.rol()) && !idCliente.equals(usuario.usuario())) {
            throw new AccessDeniedException("Un cliente solo puede consultar su propio historial");
        }
        return ventaService.historialCliente(idCliente);
    }
}
