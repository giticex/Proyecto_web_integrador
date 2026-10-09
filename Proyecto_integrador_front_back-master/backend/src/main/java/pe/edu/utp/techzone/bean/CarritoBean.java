package pe.edu.utp.techzone.bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import pe.edu.utp.techzone.dto.ItemVentaRequest;
import pe.edu.utp.techzone.dto.ProductoDTO;
import pe.edu.utp.techzone.dto.RegistrarVentaRequest;
import pe.edu.utp.techzone.dto.VentaDTO;
import pe.edu.utp.techzone.entity.MedioPago;
import pe.edu.utp.techzone.entity.TipoDocumento;
import pe.edu.utp.techzone.repository.MedioPagoRepository;
import pe.edu.utp.techzone.repository.TipoDocumentoRepository;
import pe.edu.utp.techzone.service.ProductoService;
import pe.edu.utp.techzone.service.VentaService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component("carritoBean")
@SessionScope
@Getter
@Setter
public class CarritoBean {
    private final ProductoService productoService;
    private final VentaService ventaService;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final MedioPagoRepository medioPagoRepository;
    private final LoginBean loginBean;
    private final Map<Integer, LineaCarrito> lineas = new LinkedHashMap<>();
    private String clienteVentaId;
    private String tipoDocumento = "TD001";
    private String medioPago = "MP001";
    private VentaDTO ultimaVenta;

    public CarritoBean(ProductoService productoService, VentaService ventaService,
                      TipoDocumentoRepository tipoDocumentoRepository,
                      MedioPagoRepository medioPagoRepository, LoginBean loginBean) {
        this.productoService = productoService;
        this.ventaService = ventaService;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.medioPagoRepository = medioPagoRepository;
        this.loginBean = loginBean;
    }

    public List<LineaCarrito> getItems() {
        return new ArrayList<>(lineas.values());
    }

    public boolean isVacio() {
        return lineas.isEmpty();
    }

    public int getCantidadItems() {
        return lineas.values().stream().mapToInt(LineaCarrito::getCantidad).sum();
    }

    public BigDecimal getTotal() {
        return lineas.values().stream().map(LineaCarrito::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<TipoDocumento> getTiposDocumento() {
        return tipoDocumentoRepository.findAll();
    }

    public List<MedioPago> getMediosPago() {
        return medioPagoRepository.findAll();
    }

    public void agregar(Integer idProducto, int cantidad) {
        if (idProducto == null || cantidad < 1) {
            throw new IllegalArgumentException("Selecciona un producto y una cantidad válida");
        }
        ProductoDTO producto = productoService.buscar(idProducto);
        int cantidadAnterior = lineas.containsKey(idProducto) ? lineas.get(idProducto).getCantidad() : 0;
        if (producto.getStock() == null || cantidadAnterior + cantidad > producto.getStock()) {
            throw new IllegalArgumentException("No hay stock suficiente de " + producto.getNombre());
        }
        if (cantidadAnterior == 0) {
            lineas.put(idProducto, new LineaCarrito(producto, cantidad));
        } else {
            lineas.get(idProducto).setCantidad(cantidadAnterior + cantidad);
        }
        ultimaVenta = null;
    }

    public void quitar(Integer idProducto) {
        lineas.remove(idProducto);
    }

    public void validarCantidades() {
        try {
            if (lineas.values().stream().anyMatch(i -> i.getCantidad() < 1)) {
                throw new IllegalArgumentException("Las cantidades deben ser mayores a cero");
            }
            for (LineaCarrito linea : lineas.values()) {
                ProductoDTO actual = productoService.buscar(linea.getProducto().getIdProducto());
                if (linea.getCantidad() > actual.getStock()) {
                    throw new IllegalArgumentException("Stock insuficiente de " + actual.getNombre());
                }
            }
            informar("Cantidades actualizadas");
        } catch (RuntimeException ex) {
            fallar(ex.getMessage());
        }
    }

    public void confirmar() {
        try {
            if (lineas.isEmpty()) throw new IllegalArgumentException("El carrito está vacío");
            if (!loginBean.isAutenticado()) throw new IllegalArgumentException("Inicia sesión primero");
            String idCliente = loginBean.isCliente()
                    ? loginBean.getUsuarioActual().getId()
                    : (clienteVentaId == null ? "" : clienteVentaId.trim());
            if (idCliente.isBlank()) throw new IllegalArgumentException("Indica el ID del cliente");
            if (tipoDocumento == null || medioPago == null) {
                throw new IllegalArgumentException("Selecciona comprobante y medio de pago");
            }
            List<ItemVentaRequest> items = new ArrayList<>();
            for (LineaCarrito linea : lineas.values()) {
                if (linea.getCantidad() < 1) throw new IllegalArgumentException("Cantidad inválida");
                ItemVentaRequest item = new ItemVentaRequest();
                item.setIdProducto(linea.getProducto().getIdProducto());
                item.setCantidad(linea.getCantidad());
                items.add(item);
            }
            RegistrarVentaRequest request = new RegistrarVentaRequest();
            request.setIdCliente(idCliente);
            request.setTipoDocumento(tipoDocumento);
            request.setMedioPago(medioPago);
            // Dejar nulo: el servicio genera el número de boleta/factura.
            request.setNumeroDocumento(null);
            request.setItems(items);
            if (loginBean.isPersonal()) {
                ultimaVenta = ventaService.registrar(request, loginBean.getUsuarioActual().getUsuario());
            } else {
                ultimaVenta = ventaService.registrar(request);
            }
            lineas.clear();
            informar("Venta registrada: " + ultimaVenta.getNumeroDocumento());
        } catch (IllegalArgumentException ex) {
            fallar("No se registró la venta: " + ex.getMessage());
        } catch (RuntimeException ex) {
            fallar("No se registró la venta. Revisa el ID de cliente, stock y datos de pago.");
        }
    }

    private void informar(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, mensaje, null));
    }

    private void fallar(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
    }

    @Getter
    @Setter
    public static class LineaCarrito {
        private ProductoDTO producto;
        private int cantidad;

        public LineaCarrito(ProductoDTO producto, int cantidad) {
            this.producto = producto;
            this.cantidad = cantidad;
        }

        public BigDecimal getSubtotal() {
            return producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
        }
    }
}
