package pe.edu.utp.techzone.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pe.edu.utp.techzone.dto.ProductoDTO;
import pe.edu.utp.techzone.exception.GlobalExceptionHandler;
import pe.edu.utp.techzone.exception.NotFoundException;
import pe.edu.utp.techzone.service.ProductoService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba la capa web real (controlador + validación + GlobalExceptionHandler + JSON)
 * sin levantar Spring completo ni la base de datos.
 */
@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    @Mock
    private ProductoService productoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductoController(productoService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ProductoDTO mouse() {
        return ProductoDTO.builder().idProducto(2).nombre("Mouse Logitech").stock(50)
                .precio(new BigDecimal("45.00")).categoria("Periféricos").build();
    }

    @Test
    @DisplayName("GET /api/productos devuelve la lista en JSON")
    void listar() throws Exception {
        when(productoService.listar()).thenReturn(List.of(mouse()));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idProducto").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Mouse Logitech"))
                .andExpect(jsonPath("$[0].stock").value(50));
    }

    @Test
    @DisplayName("GET /api/productos/{id} inexistente responde 404 con mensaje")
    void buscarInexistente() throws Exception {
        when(productoService.buscar(99)).thenThrow(new NotFoundException("Producto no encontrado"));

        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.mensaje").value("Producto no encontrado"));
    }

    @Test
    @DisplayName("POST /api/productos válido responde 200 con el producto creado")
    void crearValido() throws Exception {
        when(productoService.guardar(any(ProductoDTO.class))).thenReturn(mouse());

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idProducto":2,"nombre":"Mouse Logitech","stock":50,"precio":45.00,"categoria":"Periféricos"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Mouse Logitech"));
    }

    @Test
    @DisplayName("POST /api/productos con nombre vacío y stock negativo responde 400")
    void crearInvalido() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"","stock":-1,"precio":10.00}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("DELETE /api/productos/{id} delega en el servicio")
    void eliminar() throws Exception {
        mockMvc.perform(delete("/api/productos/2")).andExpect(status().isOk());

        verify(productoService).eliminar(2);
    }
}
