package pe.edu.utp.techzone.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;
    @Mock
    private DetalleVentaRepository detalleVentaRepository;
    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;
    @Mock
    private MedioPagoRepository medioPagoRepository;
    @Mock
    private ClienteService clienteService;
    @Mock
    private ProductoService productoService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ParametroService parametroService;

    @InjectMocks
    private VentaService ventaService;

    private Venta ventaGuardada;
    private final List<DetalleVenta> detallesGuardados = new ArrayList<>();
    private Producto mouse;

    @BeforeEach
    void preparar() {
        Cliente cliente = Cliente.builder().idCliente("CLI001").nombre("Luis").apellido("García").build();
        Persona admin = Persona.builder().idPersona("PERADM").nombre("Administrador").apellido("General").build();
        Usuario usuarioAdmin = Usuario.builder().usuario("admin").persona(admin).build();
        mouse = Producto.builder().idProducto(2).nombre("Mouse Logitech").stock(50)
                .precio(new BigDecimal("45.00")).categoria("Periféricos").build();

        when(clienteService.obtenerEntidad("CLI001")).thenReturn(cliente);
        when(usuarioRepository.findByUsuario("admin")).thenReturn(Optional.of(usuarioAdmin));
        when(tipoDocumentoRepository.findById("TD001")).thenReturn(Optional.of(
                TipoDocumento.builder().idDocumento("TD001").nombre("Boleta").build()));
        when(tipoDocumentoRepository.findById("TD002")).thenReturn(Optional.of(
                TipoDocumento.builder().idDocumento("TD002").nombre("Factura").build()));
        when(medioPagoRepository.findById("MP001")).thenReturn(Optional.of(
                MedioPago.builder().idMedioPago("MP001").descripcion("Efectivo").build()));
        when(productoService.obtenerEntidad(2)).thenReturn(mouse);

        // Ya existen 5 comprobantes en la serie: el siguiente debe ser el 6
        when(ventaRepository.maxCorrelativoPorSerie(anyString())).thenReturn(5L);
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            ventaGuardada = inv.getArgument(0);
            return ventaGuardada;
        });
        when(ventaRepository.findById(anyString())).thenAnswer(inv -> Optional.ofNullable(ventaGuardada));
        when(detalleVentaRepository.save(any(DetalleVenta.class))).thenAnswer(inv -> {
            DetalleVenta detalle = inv.getArgument(0);
            detallesGuardados.add(detalle);
            return detalle;
        });
        when(detalleVentaRepository.findByVentaIdVenta(anyString())).thenAnswer(inv -> detallesGuardados);

        when(parametroService.obtenerIgvPorcentaje()).thenReturn(new BigDecimal("18.00"));
        when(parametroService.calcularOpGravada(any())).thenReturn(BigDecimal.ZERO);
        when(parametroService.calcularIgv(any())).thenReturn(BigDecimal.ZERO);
    }

    private RegistrarVentaRequest solicitud(String tipoDocumento, int cantidad) {
        ItemVentaRequest item = new ItemVentaRequest();
        item.setIdProducto(2);
        item.setCantidad(cantidad);

        RegistrarVentaRequest request = new RegistrarVentaRequest();
        request.setIdCliente("CLI001");
        request.setTipoDocumento(tipoDocumento);
        request.setMedioPago("MP001");
        request.setItems(List.of(item));
        return request;
    }

    @Test
    @DisplayName("Boleta: serie B001 con el siguiente correlativo de 8 dígitos")
    void boletaUsaSerieB001() {
        VentaDTO venta = ventaService.registrar(solicitud("TD001", 1));

        assertThat(venta.getNumeroDocumento()).isEqualTo("B001-00000006");
        assertThat(venta.getTipoDocumento()).isEqualTo("Boleta");
    }

    @Test
    @DisplayName("Factura: serie F001 con el siguiente correlativo de 8 dígitos")
    void facturaUsaSerieF001() {
        VentaDTO venta = ventaService.registrar(solicitud("TD002", 1));

        assertThat(venta.getNumeroDocumento()).isEqualTo("F001-00000006");
        assertThat(venta.getTipoDocumento()).isEqualTo("Factura");
    }

    @Test
    @DisplayName("Calcula subtotal y total (precio x cantidad) y descuenta stock")
    void calculaTotalYDescuentaStock() {
        VentaDTO venta = ventaService.registrar(solicitud("TD001", 3));

        assertThat(venta.getDetalles()).hasSize(1);
        assertThat(venta.getDetalles().get(0).getPrecioUnitario()).isEqualByComparingTo("45.00");
        assertThat(venta.getDetalles().get(0).getSubtotal()).isEqualByComparingTo("135.00");
        assertThat(venta.getTotal()).isEqualByComparingTo("135.00");
        verify(productoService).descontarStock(mouse, 3);
    }

    @Test
    @DisplayName("Guarda al comprador y el documento del cliente en la venta")
    void guardaComprador() {
        RegistrarVentaRequest request = solicitud("TD001", 1);
        request.setNumeroDocumento("12345678");

        VentaDTO venta = ventaService.registrar(request);

        assertThat(venta.getIdCliente()).isEqualTo("CLI001");
        assertThat(venta.getCliente()).isEqualTo("Luis García");
        assertThat(venta.getNumeroDocumentoCliente()).isEqualTo("12345678");
        assertThat(venta.getMedioPago()).isEqualTo("Efectivo");
    }

    @Test
    @DisplayName("Stock insuficiente: la excepción se propaga (la transacción hace rollback)")
    void stockInsuficiente() {
        doThrow(new BusinessException("Stock insuficiente para Mouse Logitech"))
                .when(productoService).descontarStock(any(Producto.class), eq(999));

        assertThatThrownBy(() -> ventaService.registrar(solicitud("TD001", 999)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    @DisplayName("Cliente inexistente lanza NotFoundException")
    void clienteInexistente() {
        when(clienteService.obtenerEntidad("CLI999")).thenThrow(new NotFoundException("Cliente no encontrado"));
        RegistrarVentaRequest request = solicitud("TD001", 1);
        request.setIdCliente("CLI999");

        assertThatThrownBy(() -> ventaService.registrar(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Cliente no encontrado");
    }

    @Test
    @DisplayName("Tipo de documento inválido lanza NotFoundException")
    void tipoDocumentoInvalido() {
        assertThatThrownBy(() -> ventaService.registrar(solicitud("TD999", 1)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Tipo de documento no encontrado");
    }

    @Test
    @DisplayName("Medio de pago inválido lanza NotFoundException")
    void medioPagoInvalido() {
        RegistrarVentaRequest request = solicitud("TD001", 1);
        request.setMedioPago("MP999");

        assertThatThrownBy(() -> ventaService.registrar(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Medio de pago no encontrado");
    }

    @Test
    @DisplayName("buscar() de una venta inexistente lanza NotFoundException")
    void buscarInexistente() {
        assertThatThrownBy(() -> ventaService.buscar("no-existe"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Venta no encontrada");
    }
}
