package pe.edu.utp.techzone.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.utp.techzone.dto.*;
import pe.edu.utp.techzone.entity.*;

import pe.edu.utp.techzone.exception.BusinessException;
import pe.edu.utp.techzone.exception.NotFoundException;

import pe.edu.utp.techzone.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DevolucionService {

    private static final BigDecimal CERO = new BigDecimal("0.00");

    private final DevolucionRepository devolucionRepository;
    private final DetalleDevolucionRepository detalleDevolucionRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CajaRepository cajaRepository;


    // =========================================================
    // 1. REGISTRAR DEVOLUCION
    // =========================================================

    @Transactional
    public DevolucionDTO registrar(
            RegistrarDevolucionRequest request,
            String nombreUsuario
    ) {

        Usuario usuario = obtenerUsuario(nombreUsuario);
        validarPermiso(usuario);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException(
                    "Debe seleccionar al menos un producto para devolver."
            );
        }

        // Evitar que un mismo detalle se repita en la solicitud.
        Set<Integer> identificadores = new HashSet<>();

        for (DetalleDevolucionRequest item : request.getItems()) {

            if (item.getIdDetalleVenta() == null
                    || item.getCantidad() == null
                    || item.getCantidad() <= 0) {

                throw new BusinessException(
                        "Los detalles y cantidades de devolución no son válidos."
                );
            }

            if (!identificadores.add(item.getIdDetalleVenta())) {
                throw new BusinessException(
                        "No se permite repetir un mismo detalle de venta."
                );
            }
        }

        // Bloquear la venta para serializar sus devoluciones.
        Venta venta = ventaRepository.buscarPorIdConBloqueo(
                request.getIdVenta()
        ).orElseThrow(() ->
                new NotFoundException("Venta original no encontrada.")
        );

        // Ordenar los detalles para reducir el riesgo de deadlocks.
        List<DetalleDevolucionRequest> itemsOrdenados =
                request.getItems().stream()
                        .sorted(Comparator.comparing(
                                DetalleDevolucionRequest::getIdDetalleVenta
                        ))
                        .toList();

        Map<Integer, DetalleVenta> detallesVenta = new LinkedHashMap<>();

        // Bloquear y validar cada detalle de la venta.
        for (DetalleDevolucionRequest item : itemsOrdenados) {

            DetalleVenta detalle = detalleVentaRepository
                    .buscarPorIdConBloqueo(item.getIdDetalleVenta())
                    .orElseThrow(() ->
                            new NotFoundException(
                                    "No se encontró el detalle de venta "
                                            + item.getIdDetalleVenta()
                            )
                    );

            if (!detalle.getVenta().getIdVenta().equals(venta.getIdVenta())) {
                throw new BusinessException(
                        "El producto seleccionado no pertenece a la venta indicada."
                );
            }

            long cantidadDevuelta =
                    detalleDevolucionRepository
                            .totalDevueltoPorDetalleVenta(
                                    detalle.getIdDetalle()
                            );

            long cantidadDisponible =
                    detalle.getCantidad().longValue() - cantidadDevuelta;

            if (item.getCantidad() > cantidadDisponible) {
                throw new BusinessException(
                        "La cantidad solicitada supera las unidades disponibles "
                                + "para devolución del producto "
                                + detalle.getProducto().getNombre()
                                + ". Disponibles: " + cantidadDisponible
                );
            }

            detallesVenta.put(detalle.getIdDetalle(), detalle);
        }

        // Bloquear productos en orden de ID para reducir deadlocks.
        List<Integer> idsProductos = detallesVenta.values().stream()
                .map(detalle -> detalle.getProducto().getIdProducto())
                .distinct()
                .sorted()
                .toList();

        Map<Integer, Producto> productos = new HashMap<>();

        for (Integer idProducto : idsProductos) {

            Producto producto = productoRepository
                    .buscarPorIdConBloqueo(idProducto)
                    .orElseThrow(() ->
                            new NotFoundException(
                                    "Producto no encontrado: " + idProducto
                            )
                    );

            productos.put(idProducto, producto);
        }

        // Preparar la devolución y calcular el importe con precios históricos.
        List<DetalleDevolucion> nuevosDetalles = new ArrayList<>();

        BigDecimal montoTotal = CERO;

        for (DetalleDevolucionRequest item : itemsOrdenados) {

            DetalleVenta detalleVenta =
                    detallesVenta.get(item.getIdDetalleVenta());

            Producto producto = productos.get(
                    detalleVenta.getProducto().getIdProducto()
            );

            // Recuperar inventario.
            long nuevoStock =
                    producto.getStock().longValue() + item.getCantidad();

            if (nuevoStock > Integer.MAX_VALUE) {
                throw new BusinessException(
                        "La cantidad de inventario supera el límite permitido."
                );
            }

            producto.setStock((int) nuevoStock);
            productoRepository.save(producto);

            BigDecimal precioOriginal =
                    detalleVenta.getPrecioUnitario()
                            .setScale(2, RoundingMode.HALF_UP);

            BigDecimal subtotal = precioOriginal
                    .multiply(BigDecimal.valueOf(item.getCantidad()))
                    .setScale(2, RoundingMode.HALF_UP);

            montoTotal = montoTotal.add(subtotal);

            String motivoDetalle =
                    item.getMotivo() != null && !item.getMotivo().isBlank()
                            ? item.getMotivo().trim()
                            : request.getMotivo().trim();

            DetalleDevolucion nuevoDetalle = DetalleDevolucion.builder()
                    .detalleVenta(detalleVenta)
                    .cantidad(item.getCantidad())
                    .precioUnitario(precioOriginal)
                    .subtotal(subtotal)
                    .motivo(motivoDetalle)
                    .build();

            nuevosDetalles.add(nuevoDetalle);
        }

        montoTotal = montoTotal.setScale(2, RoundingMode.HALF_UP);

        // Asociar la devolución a la caja abierta, si existe.
        Caja caja = cajaRepository
                .findFirstByUsuario_IdUsuarioAndEstadoOrderByFechaAperturaDesc(
                        usuario.getIdUsuario(),
                        "ABIERTA"
                )
                .orElse(null);

        // Número interno de nota de crédito.
        // No representa una autorización electrónica SUNAT.
        String numeroNota = "NC-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 20)
                .toUpperCase(Locale.ROOT);

        Devolucion devolucion = Devolucion.builder()
                .venta(venta)
                .usuario(usuario)
                .caja(caja)
                .numeroNotaCredito(numeroNota)
                .fechaRegistro(java.time.LocalDateTime.now())
                .motivo(request.getMotivo().trim())
                .montoReembolsado(montoTotal)
                .estado("REGISTRADA")
                .observaciones(request.getObservaciones())
                .build();

        // Guardar encabezado.
        devolucionRepository.saveAndFlush(devolucion);

        // Asociar los detalles al encabezado.
        nuevosDetalles.forEach(detalle ->
                detalle.setDevolucion(devolucion)
        );

        // Guardar los detalles.
        detalleDevolucionRepository.saveAll(nuevosDetalles);

        return convertirDTO(devolucion, nuevosDetalles);
    }


    // =========================================================
    // 2. LISTAR DEVOLUCIONES CON PAGINACION
    // =========================================================

    @Transactional(readOnly = true)
    public Page<DevolucionDTO> listar(
            String nombreUsuario,
            Pageable pageable
    ) {

        Usuario usuario = obtenerUsuario(nombreUsuario);
        validarPermiso(usuario);

        Page<Devolucion> resultados;

        if (usuario.getRol() == RolUsuario.ADMIN) {

            resultados = devolucionRepository
                    .findAllByOrderByFechaRegistroDesc(pageable);

        } else {

            resultados = devolucionRepository
                    .findByUsuario_UsuarioOrderByFechaRegistroDesc(
                            nombreUsuario,
                            pageable
                    );
        }

        return resultados.map(this::convertirDTO);
    }


    // =========================================================
    // 3. BUSCAR UNA DEVOLUCION
    // =========================================================

    @Transactional(readOnly = true)
    public DevolucionDTO buscar(
            Long idDevolucion,
            String nombreUsuario
    ) {

        Usuario usuario = obtenerUsuario(nombreUsuario);
        validarPermiso(usuario);

        Devolucion devolucion = devolucionRepository
                .findById(idDevolucion)
                .orElseThrow(() ->
                        new NotFoundException("Devolución no encontrada.")
                );

        validarPropietario(devolucion, usuario);

        return convertirDTO(devolucion);
    }


    // =========================================================
    // 4. LISTAR DEVOLUCIONES DE UNA VENTA
    // =========================================================

    @Transactional(readOnly = true)
    public Page<DevolucionDTO> listarPorVenta(
            String idVenta,
            String nombreUsuario,
            Pageable pageable
    ) {

        Usuario usuario = obtenerUsuario(nombreUsuario);
        validarPermiso(usuario);

        Page<Devolucion> resultados;

        if (usuario.getRol() == RolUsuario.ADMIN) {

            resultados = devolucionRepository
                    .findByVenta_IdVentaOrderByFechaRegistroDesc(
                            idVenta,
                            pageable
                    );

        } else {

            resultados = devolucionRepository
                    .findByVenta_IdVentaAndUsuario_UsuarioOrderByFechaRegistroDesc(
                            idVenta,
                            nombreUsuario,
                            pageable
                    );
        }

        return resultados.map(this::convertirDTO);
    }


    // =========================================================
    // 5. VALIDACIONES
    // =========================================================

    private Usuario obtenerUsuario(String nombreUsuario) {

        return usuarioRepository.findByUsuario(nombreUsuario)
                .orElseThrow(() ->
                        new NotFoundException("Usuario autenticado no encontrado.")
                );
    }


    private void validarPermiso(Usuario usuario) {

        if (usuario.getRol() != RolUsuario.ADMIN
                && usuario.getRol() != RolUsuario.VENDEDOR) {

            throw new BusinessException(
                    "No tienes permisos para gestionar devoluciones."
            );
        }
    }


    private void validarPropietario(
            Devolucion devolucion,
            Usuario usuario
    ) {

        if (usuario.getRol() != RolUsuario.ADMIN
                && !devolucion.getUsuario().getIdUsuario()
                        .equals(usuario.getIdUsuario())) {

            throw new BusinessException(
                    "No tienes permisos para consultar esta devolución."
            );
        }
    }


    // =========================================================
    // 6. CONVERTIR ENTIDAD A DTO
    // =========================================================

    private DevolucionDTO convertirDTO(Devolucion devolucion) {

        List<DetalleDevolucion> detalles =
                detalleDevolucionRepository
                        .findByDevolucion_IdDevolucion(
                                devolucion.getIdDevolucion()
                        );

        return convertirDTO(devolucion, detalles);
    }


    private DevolucionDTO convertirDTO(
            Devolucion devolucion,
            List<DetalleDevolucion> detalles
    ) {

        Venta venta = devolucion.getVenta();

        List<DetalleDevolucionDTO> detallesDTO = detalles.stream()
                .map(detalle -> DetalleDevolucionDTO.builder()
                        .idDetalleDevolucion(
                                detalle.getIdDetalleDevolucion()
                        )
                        .idDetalleVenta(
                                detalle.getDetalleVenta().getIdDetalle()
                        )
                        .idProducto(
                                detalle.getDetalleVenta()
                                        .getProducto()
                                        .getIdProducto()
                        )
                        .producto(
                                detalle.getDetalleVenta()
                                        .getProducto()
                                        .getNombre()
                        )
                        .cantidad(detalle.getCantidad())
                        .precioUnitario(detalle.getPrecioUnitario())
                        .subtotal(detalle.getSubtotal())
                        .motivo(detalle.getMotivo())
                        .build()
                )
                .toList();

        return DevolucionDTO.builder()
                .idDevolucion(devolucion.getIdDevolucion())
                .numeroNotaCredito(devolucion.getNumeroNotaCredito())
                .idVenta(venta.getIdVenta())
                .numeroDocumento(venta.getNumeroDocumento())
                .cliente(
                        venta.getCliente().getNombre()
                                + " "
                                + venta.getCliente().getApellido()
                )
                .usuario(devolucion.getUsuario().getUsuario())
                .idCaja(
                        devolucion.getCaja() != null
                                ? devolucion.getCaja().getIdCaja()
                                : null
                )
                .fechaRegistro(devolucion.getFechaRegistro())
                .motivo(devolucion.getMotivo())
                .montoReembolsado(devolucion.getMontoReembolsado())
                .estado(devolucion.getEstado())
                .observaciones(devolucion.getObservaciones())
                .detalles(detallesDTO)
                .build();
    }
}
