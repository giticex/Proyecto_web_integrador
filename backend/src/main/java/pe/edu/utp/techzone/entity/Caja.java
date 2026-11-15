package pe.edu.utp.techzone.entity;

import jakarta.persistence.*;
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
@Entity
@Table(name = "caja")
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_caja")
    private Long idCaja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "fondo_inicial", nullable = false, precision = 12, scale = 2)
    private BigDecimal fondoInicial;

    @Builder.Default
    @Column(name = "efectivo_esperado", nullable = false, precision = 12, scale = 2)
    private BigDecimal efectivoEsperado = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "tarjeta_esperada", nullable = false, precision = 12, scale = 2)
    private BigDecimal tarjetaEsperada = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "transferencia_esperada", nullable = false, precision = 12, scale = 2)
    private BigDecimal transferenciaEsperada = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "otros_esperados", nullable = false, precision = 12, scale = 2)
    private BigDecimal otrosEsperados = BigDecimal.ZERO;

    @Column(name = "efectivo_contado", precision = 12, scale = 2)
    private BigDecimal efectivoContado;

    @Column(name = "tarjeta_contada", precision = 12, scale = 2)
    private BigDecimal tarjetaContada;

    @Column(name = "transferencia_contada", precision = 12, scale = 2)
    private BigDecimal transferenciaContada;

    @Column(name = "otros_contados", precision = 12, scale = 2)
    private BigDecimal otrosContados;

    @Column(name = "diferencia_efectivo", precision = 12, scale = 2)
    private BigDecimal diferenciaEfectivo;

    @Column(name = "diferencia_tarjeta", precision = 12, scale = 2)
    private BigDecimal diferenciaTarjeta;

    @Column(name = "diferencia_transferencia", precision = 12, scale = 2)
    private BigDecimal diferenciaTransferencia;

    @Column(name = "diferencia_otros", precision = 12, scale = 2)
    private BigDecimal diferenciaOtros;

    @Column(name = "observaciones_apertura", length = 500)
    private String observacionesApertura;

    @Column(name = "observaciones_cierre", length = 500)
    private String observacionesCierre;

    @Builder.Default
    @Column(nullable = false, length = 15)
    private String estado = "ABIERTA";
}
