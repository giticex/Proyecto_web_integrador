package pe.edu.utp.techzone.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import pe.edu.utp.techzone.dto.DetalleVentaDTO;
import pe.edu.utp.techzone.dto.ItemVentaRequest;
import pe.edu.utp.techzone.dto.RegistrarVentaRequest;
import pe.edu.utp.techzone.dto.VentaDTO;

import pe.edu.utp.techzone.entity.Caja;
import pe.edu.utp.techzone.entity.Cliente;
import pe.edu.utp.techzone.entity.DetalleVenta;
import pe.edu.utp.techzone.entity.MedioPago;
import pe.edu.utp.techzone.entity.Persona;
import pe.edu.utp.techzone.entity.Producto;
import pe.edu.utp.techzone.entity.RolUsuario;
import pe.edu.utp.techzone.entity.TipoDocumento;
import pe.edu.utp.techzone.entity.Usuario;
import pe.edu.utp.techzone.entity.Venta;

import pe.edu.utp.techzone.exception.BusinessException;
import pe.edu.utp.techzone.exception.NotFoundException;

import pe.edu.utp.techzone.repository.CajaRepository;
import pe.edu.utp.techzone.repository.DetalleVentaRepository;
import pe.edu.utp.techzone.repository.MedioPagoRepository;
import pe.edu.utp.techzone.repository.TipoDocumentoRepository;
import pe.edu.utp.techzone.repository.UsuarioRepository;
import pe.edu.utp.techzone.repository.VentaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VentaService {

    // Compatibilidad temporal con el registro anterior.
    // DEV1 coordinará la eliminación definitiva de esta constante.
    private static final String USUARIO_VENDEDOR = "admin";

    private static final Set<String> MEDIOS_PAGO_POS_HABILITADOS = Set.of(
        "MP001",
        "MP002",
        "MP003",
        "MP004",
        "MP005",
        "MP006",
        "MP008",
        "MP009",
        "MP010",
        "MP011",
        "MP012",
        "MP015"
   );
    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final MedioPagoRepository medioPagoRepository;

    private final ClienteService clienteService;
    private final ProductoService productoService;
    private final UsuarioRepository usuarioRepository;
    private final ParametroService parametroService;
    private final CajaRepository cajaRepository;


    // =========================================================
    // 1. REGISTRO DE COMPRA WEB
    // =========================================================

    @Transactional
    public VentaDTO registrar(RegistrarVentaRequest request) {

        return registrar(request, USUARIO_VENDEDOR);
    }


    @Transactional
    public VentaDTO registrar(
            RegistrarVentaRequest request,
            String usuarioVendedor
    ) {

        Usuario usuario = usuarioRepository.findByUsuario(usuarioVendedor)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Usuario vendedor no encontrado."
                        )
                );

        return registrarInterno(
                request,
                usuario.getPersona(),
                null
        );
    }


    // =========================================================
    // 2. REGISTRO DE VENTA DESDE EL POS
    // =========================================================

    @Transactional
    public VentaDTO registrarDesdePos(
            RegistrarVentaRequest request,
            String nombreUsuario
    ) {

        Usuario usuario = usuarioRepository.findByUsuario(nombreUsuario)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Usuario autenticado no encontrado."
                        )
                );
                validarMedioPagoPos(request.getMedioPago());

        // Validar que el usuario pueda operar el POS.
        if (usuario.getRol() != RolUsuario.ADMIN
                && usuario.getRol() != RolUsuario.VENDEDOR) {

            throw new BusinessException(
                    "No tienes permisos para realizar ventas desde el POS."
            );
        }

        // Buscar la caja abierta del usuario autenticado.
        // Buscar la caja abierta.
Caja cajaEncontrada = cajaRepository
        .findFirstByUsuario_IdUsuarioAndEstadoOrderByFechaAperturaDesc(
                usuario.getIdUsuario(),
                "ABIERTA"
        )
        .orElseThrow(() ->
                new BusinessException(
                        "Debes abrir una caja antes de realizar ventas."
                )
        );

// Bloquear la caja para evitar ventas simultáneas con el cierre.
Caja caja = cajaRepository
        .findByIdCajaAndUsuario_Usuario(
                cajaEncontrada.getIdCaja(),
                nombreUsuario
        )
        .orElseThrow(() ->
                new BusinessException(
                        "No se encontró la caja del usuario."
                )
        );

// Comprobar nuevamente su estado después de obtener el bloqueo.
if (!"ABIERTA".equals(caja.getEstado())) {
    throw new BusinessException(
            "La caja ya está cerrada. No se pueden registrar más ventas."
    );
}

        // Registrar la venta asociada a esa caja.
        return registrarInterno(
                request,
                usuario.getPersona(),
                caja
        );
    }


    // =========================================================
    // 3. LÓGICA COMPARTIDA DE REGISTRO
    // =========================================================

    private VentaDTO registrarInterno(
            RegistrarVentaRequest request,
            Persona vendedor,
            Caja caja
    ) {

        Cliente cliente = clienteService.obtenerEntidad(
                request.getIdCliente()
        );

        TipoDocumento tipoDocumento = tipoDocumentoRepository
                .findById(request.getTipoDocumento())
                .orElseThrow(() ->
                        new NotFoundException(
                                "Tipo de documento no encontrado."
                        )
                );

        MedioPago medioPago = medioPagoRepository
                .findById(request.getMedioPago())
                .orElseThrow(() ->
                        new NotFoundException(
                                "Medio de pago no encontrado."
                        )
                );

        String idVenta = UUID.randomUUID().toString();

        String serie = "TD002".equals(request.getTipoDocumento())
                ? "F001"
                : "B001";

        String numeroDocumento = generarNumeroComprobante(serie);

        // Crear venta y asociar la caja cuando corresponda.
        Venta venta = Venta.builder()
                .idVenta(idVenta)
                .cliente(cliente)
                .persona(vendedor)
                .caja(caja)
                .tipoDocumento(tipoDocumento)
                .numeroDocumento(numeroDocumento)
                .numeroDocumentoCliente(request.getNumeroDocumento())
                .medioPago(medioPago)
                .fechaEmision(LocalDateTime.now())
                .build();

        ventaRepository.save(venta);

        // Registrar detalles y descontar inventario.
        for (ItemVentaRequest item : request.getItems()) {

            Producto producto = productoService.obtenerEntidad(
                    item.getIdProducto()
            );

            productoService.descontarStock(
                    producto,
                    item.getCantidad()
            );

            BigDecimal precio = producto.getPrecio();

            BigDecimal subtotal = precio.multiply(
                    BigDecimal.valueOf(item.getCantidad())
            );

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


    // =========================================================
    // 4. GENERAR NÚMERO DE COMPROBANTE
    // =========================================================

    private String generarNumeroComprobante(String serie) {

        BigDecimal ultimo = ventaRepository.maxCorrelativoPorSerie(serie);

        long siguiente = (ultimo == null ? 0L : ultimo.longValue()) + 1L;

        return serie + "-" + String.format("%08d", siguiente);
    }


    // =========================================================
    // 5. BUSCAR VENTA
    // =========================================================

    @Transactional(readOnly = true)
    public VentaDTO buscar(String idVenta) {

        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() ->
                        new NotFoundException("Venta no encontrada.")
                );

        return toDTO(venta);
    }


    // =========================================================
    // 6. HISTORIAL DE CLIENTES
    // =========================================================

    @Transactional(readOnly = true)
    public List<VentaDTO> historialCliente(String idCliente) {

        return ventaRepository
                .findByClienteIdClienteOrderByFechaEmisionDesc(idCliente)
                .stream()
                .map(this::toDTO)
                .toList();
    }


    // =========================================================
    // 7. HISTORIAL DE VENDEDORES
    // =========================================================

    @Transactional(readOnly = true)
    public List<VentaDTO> historialVendedor(String usuario) {

        Persona vendedor = usuarioRepository.findByUsuario(usuario)
                .map(Usuario::getPersona)
                .orElseThrow(() ->
                        new NotFoundException("Vendedor no encontrado.")
                );

        return ventaRepository
                .findByPersonaIdPersonaOrderByFechaEmisionDesc(
                        vendedor.getIdPersona()
                )
                .stream()
                .map(this::toDTO)
                .toList();
    }


    // =========================================================
    // 8. LISTAR TODAS LAS VENTAS
    // =========================================================

    @Transactional(readOnly = true)
    public List<VentaDTO> listarTodas() {

        return ventaRepository.findAllByOrderByFechaEmisionDesc()
                .stream()
                .map(this::toDTO)
                .toList();
    }


    // =========================================================
    // 9. PAGINACIÓN DE VENTAS
    // =========================================================

    @Transactional(readOnly = true)
    public Page<VentaDTO> listarTodasPaginadas(Pageable pageable) {

        return ventaRepository
                .findAllByOrderByFechaEmisionDesc(pageable)
                .map(this::toDTO);
    }


    @Transactional(readOnly = true)
    public Page<VentaDTO> historialClientePaginado(
            String idCliente,
            Pageable pageable
    ) {

        return ventaRepository
                .findByCliente_IdClienteOrderByFechaEmisionDesc(
                        idCliente,
                        pageable
                )
                .map(this::toDTO);
    }


    // =========================================================
    // 10. CONVERTIR VENTA A DTO
    // =========================================================

    public VentaDTO toDTO(Venta venta) {

        List<DetalleVentaDTO> detalles =
                detalleVentaRepository
                        .findByVentaIdVenta(venta.getIdVenta())
                        .stream()
                        .map(detalle -> DetalleVentaDTO.builder()
                                .idDetalle(detalle.getIdDetalle())
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
                .cliente(
                        venta.getCliente().getNombre()
                                + " "
                                + venta.getCliente().getApellido()
                )
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
    private void validarMedioPagoPos(String idMedioPago) {

    if (
            idMedioPago == null
            || !MEDIOS_PAGO_POS_HABILITADOS.contains(idMedioPago)
    ) {
        throw new BusinessException(
                "El medio de pago seleccionado no está habilitado "
                + "para ventas en el Punto de Venta."
        );
    }
}
}
