package pe.edu.upeu.auth.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponse {

    private Long id;
    private String email;
    private boolean habilitado;
    private Long idCliente;
    private List<String> roles;
}
