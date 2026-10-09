package pe.edu.utp.techzone.bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import pe.edu.utp.techzone.dto.DashboardDTO;
import pe.edu.utp.techzone.dto.ProductoDTO;
import pe.edu.utp.techzone.dto.VentaDTO;
import pe.edu.utp.techzone.service.DashboardService;
import pe.edu.utp.techzone.service.ProductoService;
import pe.edu.utp.techzone.service.VentaService;

import java.math.BigDecimal;
import java.util.List;

/** Se mantiene por sesión JSF, con navegación diferenciada por rol. */
@Component("vendedorBean")
@SessionScope
@Getter
@Setter
public class VendedorBean {
    private final LoginBean loginBean;
    private final CarritoBean carritoBean;
    private final ProductoService productoService;
    private final VentaService ventaService;
    private final DashboardService dashboardService;
    private Integer idProducto;
    private int cantidad = 1;

    public VendedorBean(LoginBean loginBean, CarritoBean carritoBean,
                        ProductoService productoService, VentaService ventaService,
                        DashboardService dashboardService) {
        this.loginBean = loginBean;
        this.carritoBean = carritoBean;
        this.productoService = productoService;
        this.ventaService = ventaService;
        this.dashboardService = dashboardService;
    }

    public String navegarPorRol() {
        if (loginBean.isAdministrador()) return "irAdmin";
        if (loginBean.isVendedor()) return "irVendedor";
        return "irCliente";
    }

    public List<ProductoDTO> getProductosDisponibles() {
        return productoService.listar().stream().filter(p -> p.getStock() > 0).toList();
    }

    public void agregarAlPedido() {
        try {
            carritoBean.agregar(idProducto, cantidad);
            cantidad = 1;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Producto agregado a la venta"));
        } catch (RuntimeException ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), null));
        }
    }

    public List<VentaDTO> getMisVentas() {
        if (!loginBean.isPersonal()) return List.of();
        return ventaService.historialVendedor(loginBean.getUsuarioActual().getUsuario());
    }

    public int getNumeroDeVentas() {
        return getMisVentas().size();
    }

    public BigDecimal getTotalVendido() {
        return getMisVentas().stream().map(VentaDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public DashboardDTO getResumen() {
        return dashboardService.obtenerResumen();
    }
}
