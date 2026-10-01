package pe.edu.upeu.cliente.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.edu.upeu.cliente.entity.TipoPersona;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteRequest {

    @NotNull(message = "El tipo de persona es obligatorio (NATURAL o JURIDICA)")
    private TipoPersona tipoPersona;

    private String dni;
    private String ruc;
    private String nombreCompleto;
    private String razonSocial;
    private String direccion;
    private String email;
    private String whatsapp;
}
