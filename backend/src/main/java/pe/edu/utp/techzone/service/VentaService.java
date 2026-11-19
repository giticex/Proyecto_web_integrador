package pe.edu.utp.techzone.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.techzone.dto.DetalleVentaDTO;
import pe.edu.utp.techzone.dto.ItemVentaRequest;
import pe.edu.utp.techzone.dto.RegistrarVentaRequest;
import pe.edu.utp.techzone.dto.VentaDTO;
import pe.edu.utp.techzone.entity.Cliente;
import pe.edu.utp.techzone.entity.DetalleVenta;
import pe.edu.utp.techzone.entity.MedioPago;
import pe.edu.utp.techzone.entity.Persona;
import pe.edu.utp.techzone.entity.Producto;
import pe.edu.utp.techzone.entity.TipoDocumento;
import pe.edu.utp.techzone.entity.Usuario;
import pe.edu.utp.techzone.entity.Venta;
import pe.edu.utp.techzone.exception.BusinessException;
import pe.edu.utp.techzone.exception.NotFoundException;
import pe.edu.utp.techzone.repository.DetalleVentaRepository;
import pe.edu.utp.techzone.repository.MedioPagoRepository;
import pe.edu.utp.techzone.repository.TipoDocumentoRepository;
import pe.edu.utp.techzone.repository.UsuarioRepository;
import pe.edu.utp.techzone.repository.VentaRepository;
import pe.edu.utp.techzone.security.UsuarioAutenticado;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VentaService {
    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final MedioPagoRepository medioPagoRepository;
    private final ClienteService clienteService;
    private final ProductoService productoService;
    private final UsuarioRepository usuarioRepository;
    private final ParametroService parametroService;

    @Transactional
    public VentaDTO registrar(RegistrarVentaRequest request) {
        // El vendedor se obtiene del JWT del usuario autenticado, nunca de una
        // constante hardcodeada.
        return registrar(request, usuarioAutenticado());
    }

    @Transactional
    public VentaDTO registrar(RegistrarVentaRequest request, String usuarioVendedor) {
        Cliente cliente = clienteService.obtenerEntidad(request.getIdCliente());
        Persona vendedor = resolverPersonaVendedor(usuarioVendedor);
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(request.getTipoDocumento())
                .orElseThrow(() -> new NotFoundException("Tipo de documento no encontrado"));
        MedioPago medioPago = medioPagoRepository.findById(request.getMedioPago())
                .orElseThrow(() -> new NotFoundException("Medio de pago no encontrado"));

        String idVenta = UUID.randomUUID().toString();
        // Serie oficial SUNAT: F001 para Facturas (TD002), B001 para Boletas (TD001)
        String serie = "TD002".equals(request.getTipoDocumento()) ? "F001" : "B001";
        String numeroDocumento = generarNumeroComprobante(serie);

        Venta venta = Venta.builder()
                .idVenta(idVenta)
                .cliente(cliente)
                .persona(vendedor)
                .tipoDocumento(tipoDocumento)
                .numeroDocumento(numeroDocumento)
                .numeroDocumentoCliente(request.getNumeroDocumento())
                .medioPago(medioPago)
                .fechaEmision(LocalDateTime.now())
                .build();
        ventaRepository.save(venta);

        for (ItemVentaRequest item : request.getItems()) {
            Producto producto = productoService.obtenerEntidad(item.getIdProducto());
            productoService.descontarStock(producto, item.getCantidad());
            BigDecimal precio = producto.getPrecio();
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(item.getCantidad()));

            DetalleVenta detalle = DetalleVenta.builder()
                    .venta(venta)
                    .producto(producto)
                    .cantidad(item.getCantidad())
                    .precioUnitario(precio)
                    .subtotal(subtotal)
                    .build();
            detalleVentaRepository.save(detalle);
        }

        return buscar(idVenta);
    }

    /**
     * Usuario real autenticado en el SecurityContext (cargado desde el JWT).
     */
    private String usuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("No hay un usuario autenticado para registrar la venta");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UsuarioAutenticado usuario) {
            return usuario.usuario();
        }
        throw new BusinessException("No se pudo determinar el usuario autenticado");
    }

    /**
     * Empleados (ADMIN/VENDEDOR) se resuelven desde usuario; los clientes que
     * compran desde la tienda se resuelven desde su persona asociada.
     */
    private Persona resolverPersonaVendedor(String usuarioVendedor) {
        return usuarioRepository.findByUsuario(usuarioVendedor)
                .map(Usuario::getPersona)
                .orElseGet(() -> clienteService.obtenerPersonaDelCliente(usuarioVendedor));
    }

    /** Genera el número de comprobante oficial con formato SERIE-CORRELATIVO (correlativo de 8 dígitos). */
    private String generarNumeroComprobante(String serie) {
        long siguiente = ventaRepository.maxCorrelativoPorSerie(serie) + 1;
        return serie + "-" + String.format("%08d", siguiente);
    }

    @Transactional(readOnly = true)
    public VentaDTO buscar(String idVenta) {
        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada"));
        return toDTO(venta);
    }

    @Transactional(readOnly = true)
    public List<VentaDTO> historialCliente(String idCliente) {
        return ventaRepository.findByClienteIdClienteOrderByFechaEmisionDesc(idCliente)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VentaDTO> historialVendedor(String usuario) {
        Persona vendedor = usuarioRepository.findByUsuario(usuario)
                .map(Usuario::getPersona)
                .orElseThrow(() -> new NotFoundException("Vendedor no encontrado"));
        return ventaRepository.findByPersonaIdPersonaOrderByFechaEmisionDesc(vendedor.getIdPersona())
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<VentaDTO> listarTodas() {
        return ventaRepository.findAllByOrderByFechaEmisionDesc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public VentaDTO toDTO(Venta venta) {
        List<DetalleVentaDTO> detalles = detalleVentaRepository.findByVentaIdVenta(venta.getIdVenta())
                .stream()
                .map(detalle -> DetalleVentaDTO.builder()
                        .idProducto(detalle.getProducto().getIdProducto())
                        .producto(detalle.getProducto().getNombre())
                        .cantidad(detalle.getCantidad())
                        .precioUnitario(detalle.getPrecioUnitario())
                        .subtotal(detalle.getSubtotal())
                        .build())
                .toList();

        BigDecimal total = detalles.stream()
                .map(DetalleVentaDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return VentaDTO.builder()
                .idVenta(venta.getIdVenta())
                .idCliente(venta.getCliente().getIdCliente())
                .cliente(venta.getCliente().getNombre() + " " + venta.getCliente().getApellido())
                .tipoDocumento(venta.getTipoDocumento().getNombre())
                .numeroDocumento(venta.getNumeroDocumento())
                .numeroDocumentoCliente(venta.getNumeroDocumentoCliente())
                .medioPago(venta.getMedioPago().getDescripcion())
                .fechaEmision(venta.getFechaEmision())
                .total(total)
                .opGravada(parametroService.calcularOpGravada(total))
                .igv(parametroService.calcularIgv(total))
                .igvPorcentaje(parametroService.obtenerIgvPorcentaje())
                .detalles(detalles)
                .build();
    }
}
