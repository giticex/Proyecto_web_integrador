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
import pe.edu.utp.techzone.dto.RegistrarVentaRequest;
import pe.edu.utp.techzone.dto.VentaDTO;
import pe.edu.utp.techzone.exception.BusinessException;
import pe.edu.utp.techzone.exception.GlobalExceptionHandler;
import pe.edu.utp.techzone.exception.NotFoundException;
import pe.edu.utp.techzone.service.VentaService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VentaControllerTest {

    @Mock
    private VentaService ventaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new VentaController(ventaService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private VentaDTO ventaDemo() {
        return VentaDTO.builder()
                .idVenta("abc-123").idCliente("CLI001").cliente("Luis García")
                .tipoDocumento("Boleta").numeroDocumento("B001-00000016")
                .medioPago("Efectivo").total(new BigDecimal("45.00")).build();
    }

    private static final String VENTA_VALIDA = """
            {"idCliente":"CLI001","tipoDocumento":"TD001","medioPago":"MP001",
             "items":[{"idProducto":2,"cantidad":1}]}
            """;

    @Test
    @DisplayName("POST /api/ventas válido responde 200 con el comprobante")
    void registrarValido() throws Exception {
        when(ventaService.registrar(any(RegistrarVentaRequest.class))).thenReturn(ventaDemo());

        mockMvc.perform(post("/api/ventas").contentType(MediaType.APPLICATION_JSON).content(VENTA_VALIDA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idVenta").value("abc-123"))
                .andExpect(jsonPath("$.numeroDocumento").value("B001-00000016"))
                .andExpect(jsonPath("$.tipoDocumento").value("Boleta"));
    }

    @Test
    @DisplayName("POST /api/ventas sin items responde 400 y no llama al servicio")
    void registrarSinItems() throws Exception {
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idCliente":"CLI001","tipoDocumento":"TD001","medioPago":"MP001","items":[]}
                                """))
                .andExpect(status().isBadRequest());

        verify(ventaService, never()).registrar(any(RegistrarVentaRequest.class));
    }

    @Test
    @DisplayName("POST /api/ventas sin idCliente responde 400")
    void registrarSinCliente() throws Exception {
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoDocumento":"TD001","medioPago":"MP001","items":[{"idProducto":2,"cantidad":1}]}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/ventas con cantidad 0 responde 400")
    void registrarCantidadInvalida() throws Exception {
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idCliente":"CLI001","tipoDocumento":"TD001","medioPago":"MP001",
                                 "items":[{"idProducto":2,"cantidad":0}]}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Stock insuficiente se traduce a 400 con el mensaje de negocio")
    void registrarStockInsuficiente() throws Exception {
        when(ventaService.registrar(any(RegistrarVentaRequest.class)))
                .thenThrow(new BusinessException("Stock insuficiente para Mouse Logitech"));

        mockMvc.perform(post("/api/ventas").contentType(MediaType.APPLICATION_JSON).content(VENTA_VALIDA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Stock insuficiente para Mouse Logitech"));
    }

    @Test
    @DisplayName("GET /api/ventas/{id} inexistente responde 404")
    void buscarInexistente() throws Exception {
        when(ventaService.buscar("xyz")).thenThrow(new NotFoundException("Venta no encontrada"));

        mockMvc.perform(get("/api/ventas/xyz"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Venta no encontrada"));
    }

    @Test
    @DisplayName("GET /api/ventas/cliente/{id} devuelve el historial del cliente")
    void historialCliente() throws Exception {
        when(ventaService.historialCliente("CLI001")).thenReturn(List.of(ventaDemo()));

        mockMvc.perform(get("/api/ventas/cliente/CLI001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idCliente").value("CLI001"));
    }
}
