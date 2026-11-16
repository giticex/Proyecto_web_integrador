package pe.edu.utp.techzone.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "devolucion")
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_devolucion")
    private Long idDevolucion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caja")
    private Caja caja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_medio_pago_reembolso", nullable = false)
    private MedioPago medioPagoReembolso;

    @Column(name = "numero_nota_credito", nullable = false, unique = true, length = 40)
    private String numeroNotaCredito;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false, length = 255)
    private String motivo;

    @Column(name = "monto_reembolsado", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoReembolsado;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String estado = "REGISTRADA";

    @Column(length = 500)
    private String observaciones;
}
