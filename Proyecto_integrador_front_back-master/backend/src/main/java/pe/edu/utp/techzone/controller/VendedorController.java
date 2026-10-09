package pe.edu.utp.techzone.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.techzone.dto.CrearVendedorRequest;
import pe.edu.utp.techzone.dto.VendedorDTO;
import pe.edu.utp.techzone.service.VendedorService;

import java.util.List;

@RestController
@RequestMapping("/api/vendedores")
@RequiredArgsConstructor
public class VendedorController {
    private final VendedorService vendedorService;

    @GetMapping
    public List<VendedorDTO> listar() {
        return vendedorService.listar();
    }

    @PostMapping
    public VendedorDTO crear(@Valid @RequestBody CrearVendedorRequest request) {
        return vendedorService.crear(request);
    }
}
