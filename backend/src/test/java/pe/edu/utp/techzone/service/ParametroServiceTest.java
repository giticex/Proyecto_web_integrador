package pe.edu.utp.techzone.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.utp.techzone.entity.Parametro;
import pe.edu.utp.techzone.repository.ParametroRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParametroServiceTest {

    @Mock
    private ParametroRepository parametroRepository;

    @InjectMocks
    private ParametroService parametroService;

    private void igvConfigurado(String valor) {
        when(parametroRepository.findById("IGV_PORCENTAJE")).thenReturn(Optional.of(
                Parametro.builder().idParametro("IGV_PORCENTAJE").valor(valor).descripcion("IGV").build()));
    }

    @Test
    @DisplayName("Desagrega el IGV 18% de un total con IGV incluido")
    void desagregaIgvDeTotalExacto() {
        igvConfigurado("18.00");

        BigDecimal total = new BigDecimal("118.00");

        assertThat(parametroService.calcularOpGravada(total)).isEqualByComparingTo("100.00");
        assertThat(parametroService.calcularIgv(total)).isEqualByComparingTo("18.00");
    }

    @Test
    @DisplayName("Redondea a 2 decimales (HALF_UP) y opGravada + igv = total")
    void redondeaYMantieneElTotal() {
        igvConfigurado("18.00");

        BigDecimal total = new BigDecimal("2590.00");
        BigDecimal opGravada = parametroService.calcularOpGravada(total);
        BigDecimal igv = parametroService.calcularIgv(total);

        assertThat(opGravada).isEqualByComparingTo("2194.92");
        assertThat(igv).isEqualByComparingTo("395.08");
        assertThat(opGravada.add(igv)).isEqualByComparingTo(total);
    }

    @Test
    @DisplayName("Si el parámetro no existe usa 18% por defecto")
    void usaValorPorDefectoSiNoExisteElParametro() {
        when(parametroRepository.findById("IGV_PORCENTAJE")).thenReturn(Optional.empty());

        assertThat(parametroService.obtenerIgvPorcentaje()).isEqualByComparingTo("18.00");
    }

    @Test
    @DisplayName("Si el valor del parámetro no es numérico usa 18% por defecto")
    void usaValorPorDefectoSiElValorEsInvalido() {
        igvConfigurado("abc");

        assertThat(parametroService.obtenerIgvPorcentaje()).isEqualByComparingTo("18.00");
    }

    @Test
    @DisplayName("Respeta un IGV distinto configurado en BD")
    void respetaIgvConfigurado() {
        igvConfigurado("10.00");

        assertThat(parametroService.calcularOpGravada(new BigDecimal("110.00"))).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Total nulo devuelve cero")
    void totalNuloDevuelveCero() {
        assertThat(parametroService.calcularOpGravada(null)).isEqualByComparingTo("0");
        assertThat(parametroService.calcularIgv(null)).isEqualByComparingTo("0");
    }
}
