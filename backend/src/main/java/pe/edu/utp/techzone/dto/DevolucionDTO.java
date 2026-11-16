package pe.edu.utp.techzone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevolucionDTO {

    private Long idDevolucion;

    private String numeroNotaCredito;

    private String idVenta;
    private String numeroDocumento;

    private String cliente;
    private String usuario;

    private Long idCaja;

    private String medioPagoReembolso;

    private LocalDateTime fechaRegistro;

    private String motivo;

    private BigDecimal montoReembolsado;

    private String estado;

    private String observaciones;

    private List<DetalleDevolucionDTO> detalles;
}
