package pe.edu.utp.techzone.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.utp.techzone.dto.ProductoDTO;
import pe.edu.utp.techzone.entity.Producto;
import pe.edu.utp.techzone.exception.BusinessException;
import pe.edu.utp.techzone.exception.NotFoundException;
import pe.edu.utp.techzone.repository.ProductoRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto mouse() {
        return Producto.builder()
                .idProducto(2).nombre("Mouse Logitech").stock(10)
                .precio(new BigDecimal("45.00")).categoria("Periféricos").build();
    }

    @Test
    @DisplayName("listar() convierte las entidades a DTO")
    void listar() {
        when(productoRepository.findAllByOrderByNombreAsc()).thenReturn(List.of(mouse()));

        List<ProductoDTO> resultado = productoService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Mouse Logitech");
        assertThat(resultado.get(0).getPrecio()).isEqualByComparingTo("45.00");
    }

    @Test
    @DisplayName("buscar() lanza NotFoundException si el producto no existe")
    void buscarInexistente() {
        when(productoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.buscar(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Producto no encontrado");
    }

    @Test
    @DisplayName("actualizar() modifica los campos y guarda")
    void actualizar() {
        Producto existente = mouse();
        when(productoRepository.findById(2)).thenReturn(Optional.of(existente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoDTO cambios = ProductoDTO.builder()
                .nombre("Mouse Gamer").stock(5).precio(new BigDecimal("60.00")).categoria("Gaming").build();

        ProductoDTO resultado = productoService.actualizar(2, cambios);

        assertThat(resultado.getNombre()).isEqualTo("Mouse Gamer");
        assertThat(resultado.getStock()).isEqualTo(5);
        assertThat(resultado.getPrecio()).isEqualByComparingTo("60.00");
    }

    @Test
    @DisplayName("eliminar() de un producto inexistente no borra nada")
    void eliminarInexistente() {
        when(productoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.eliminar(99)).isInstanceOf(NotFoundException.class);
        verify(productoRepository, never()).delete(any(Producto.class));
    }

    @Test
    @DisplayName("descontarStock() resta la cantidad y guarda")
    void descontarStock() {
        Producto producto = mouse();

        productoService.descontarStock(producto, 3);

        assertThat(producto.getStock()).isEqualTo(7);
        verify(productoRepository).save(producto);
    }

    @Test
    @DisplayName("descontarStock() permite dejar el stock exactamente en cero")
    void descontarTodoElStock() {
        Producto producto = mouse();

        productoService.descontarStock(producto, 10);

        assertThat(producto.getStock()).isZero();
    }

    @Test
    @DisplayName("descontarStock() falla si no hay stock suficiente y no modifica el stock")
    void stockInsuficiente() {
        Producto producto = mouse();

        assertThatThrownBy(() -> productoService.descontarStock(producto, 11))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Stock insuficiente")
                .hasMessageContaining("Mouse Logitech");

        assertThat(producto.getStock()).isEqualTo(10);
        verify(productoRepository, never()).save(any(Producto.class));
    }
}
