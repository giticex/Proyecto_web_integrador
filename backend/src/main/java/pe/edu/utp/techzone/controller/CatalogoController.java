package pe.edu.utp.techzone.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.techzone.entity.MedioPago;
import pe.edu.utp.techzone.entity.TipoDocumento;
import pe.edu.utp.techzone.repository.MedioPagoRepository;
import pe.edu.utp.techzone.repository.TipoDocumentoRepository;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos")
@RequiredArgsConstructor
public class CatalogoController {
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final MedioPagoRepository medioPagoRepository;

    @GetMapping("/tipos-documento")
    @PreAuthorize("isAuthenticated()")
    public List<TipoDocumento> tiposDocumento() {
        return tipoDocumentoRepository.findAll();
    }

    @GetMapping("/medios-pago")
    @PreAuthorize("isAuthenticated()")
    public List<MedioPago> mediosPago() {
        return medioPagoRepository.findAll();
    }
}
