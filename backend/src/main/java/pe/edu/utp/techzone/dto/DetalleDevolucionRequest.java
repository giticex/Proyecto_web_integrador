package pe.edu.utp.techzone.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetalleDevolucionRequest {

    @NotNull(message = "El detalle de venta es obligatorio")
    @Positive
    private Integer idDetalleVenta;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    @Size(max = 255)
    private String motivo;
}
