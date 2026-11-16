package pe.edu.utp.techzone.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RegistrarDevolucionRequest {

    @NotBlank(message = "El identificador de venta es obligatorio")
    private String idVenta;

    @NotBlank(message = "El medio de pago del reembolso es obligatorio")
    @Size(max = 10)
    private String idMedioPagoReembolso;

    @NotBlank(message = "El motivo de devolución es obligatorio")
    @Size(max = 255)
    private String motivo;

    @Size(max = 500)
    private String observaciones;

    @NotEmpty(message = "Debe seleccionar al menos un producto")
    @Valid
    private List<DetalleDevolucionRequest> items;
}
