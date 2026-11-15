package pe.edu.utp.techzone.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.utp.techzone.dto.DashboardDTO;
import pe.edu.utp.techzone.repository.ClienteRepository;
import pe.edu.utp.techzone.repository.ProductoRepository;
import pe.edu.utp.techzone.repository.VentaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final VentaRepository ventaRepository;
    private final ParametroService parametroService;

    @Transactional(readOnly = true)
    public DashboardDTO obtenerResumen() {

        LocalDateTime ahora = LocalDateTime.now();

        LocalDateTime inicioSemana = LocalDate.now()
                .minusDays(7)
                .atStartOfDay();

        LocalDateTime inicioMes = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();

        List<String> productoMes =
                ventaRepository.obtenerProductoMasVendidoEnPeriodo(
                        inicioMes,
                        ahora,
                        PageRequest.of(0, 1)
                );

        List<String> productoSemana =
                ventaRepository.obtenerProductoMasVendidoEnPeriodo(
                        inicioSemana,
                        ahora,
                        PageRequest.of(0, 1)
                );

        return DashboardDTO.builder()

                .totalProductos(productoRepository.count())

                .totalClientes(clienteRepository.count())

                .productosAgotados(
                        productoRepository.countByStockLessThanEqual(0)
                )

                .stockCritico(
                        (int) productoRepository.countByStockLessThanEqual(5)
                )

                .ventasSemana(
                        ventaRepository.contarVentasEnPeriodo(
                                inicioSemana,
                                ahora
                        )
                )

                .clientesCompraron(
                        ventaRepository.contarClientesCompraron()
                )

                .ingresosSemana(
                        valor(
                                ventaRepository.obtenerIngresosEnPeriodo(
                                        inicioSemana,
                                        ahora
                                )
                        )
                )

                .ingresosMes(
                        valor(
                                ventaRepository.obtenerIngresosEnPeriodo(
                                        inicioMes,
                                        ahora
                                )
                        )
                )

                .igvPorcentaje(
                        parametroService.obtenerIgvPorcentaje()
                )

                .productoMasVendidoMes(
                        normalizarTexto(productoMes)
                )

                .productoMasVendidoSemana(
                        normalizarTexto(productoSemana)
                )

                .build();
    }

    private String normalizarTexto(List<String> productos) {

        return productos == null || productos.isEmpty()
                ? "Sin ventas"
                : productos.get(0);
    }

    private BigDecimal valor(BigDecimal monto) {

        return monto == null ? BigDecimal.ZERO : monto;
    }
}
