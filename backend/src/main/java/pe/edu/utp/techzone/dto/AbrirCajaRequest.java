package pe.edu.utp.techzone.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AbrirCajaRequest {

    @NotNull(message = "El fondo inicial es obligatorio")
    @DecimalMin(value = "0.00", message = "El fondo no puede ser negativo")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal fondoInicial;

    @Size(max = 500)
    private String observaciones;
}
