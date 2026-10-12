package pe.edu.utp.techzone.bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import pe.edu.utp.techzone.dto.ProductoDTO;
import pe.edu.utp.techzone.service.ProductoService;

import java.util.List;
import java.util.Locale;

@Component("catalogoBean")
@SessionScope
@Getter
@Setter
public class CatalogoBean {
    private final ProductoService productoService;
    private final CarritoBean carritoBean;
    private String busqueda = "";

    public CatalogoBean(ProductoService productoService, CarritoBean carritoBean) {
        this.productoService = productoService;
        this.carritoBean = carritoBean;
    }

    public List<ProductoDTO> getProductos() {
        return productoService.listar();
    }

    public List<ProductoDTO> getProductosFiltrados() {
        String termino = busqueda == null ? "" : busqueda.strip().toLowerCase(Locale.ROOT);
        return getProductos().stream()
                .filter(p -> termino.isBlank()
                        || p.getNombre().toLowerCase(Locale.ROOT).contains(termino)
                        || (p.getCategoria() != null
                            && p.getCategoria().toLowerCase(Locale.ROOT).contains(termino)))
                .toList();
    }

    public void agregarAlCarrito(Integer idProducto) {
        try {
            carritoBean.agregar(idProducto, 1);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Producto agregado al carrito"));
        } catch (RuntimeException ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), null));
        }
    }
}
