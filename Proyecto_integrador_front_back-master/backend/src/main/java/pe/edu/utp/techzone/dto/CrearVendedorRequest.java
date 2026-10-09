package pe.edu.utp.techzone.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearVendedorRequest {
    @NotBlank
    @Size(max = 25)
    private String idPersona;

    @NotBlank
    @Size(max = 50)
    private String nombre;

    @NotBlank
    @Size(max = 50)
    private String apellido;

    private LocalDate fechaNacimiento;

    @Email
    @Size(max = 100)
    private String correo;

    @Size(max = 9)
    private String telefono;

    @NotBlank
    @Size(min = 3, max = 50)
    private String usuario;

    @NotBlank
    @Size(min = 6)
    private String contrasena;
}
