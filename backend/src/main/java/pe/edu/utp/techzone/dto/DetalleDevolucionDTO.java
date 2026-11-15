package pe.edu.utp.techzone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleDevolucionDTO {

    private Long idDetalleDevolucion;

    private Integer idDetalleVenta;
    private Integer idProducto;

    private String producto;

    private Integer cantidad;

    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    private String motivo;
}
