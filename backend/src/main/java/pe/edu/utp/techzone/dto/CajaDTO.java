
package pe.edu.utp.techzone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CajaDTO {

    private Long idCaja;
    private Integer idUsuario;
    private String cajero;

    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;

    private BigDecimal fondoInicial;

    private BigDecimal efectivoEsperado;
    private BigDecimal tarjetaEsperada;
    private BigDecimal transferenciaEsperada;
    private BigDecimal otrosEsperados;

    private BigDecimal efectivoContado;
    private BigDecimal tarjetaContada;
    private BigDecimal transferenciaContada;
    private BigDecimal otrosContados;

    private BigDecimal diferenciaEfectivo;
    private BigDecimal diferenciaTarjeta;
    private BigDecimal diferenciaTransferencia;
    private BigDecimal diferenciaOtros;

    private String observacionesApertura;
    private String observacionesCierre;

    private String estado;
}
