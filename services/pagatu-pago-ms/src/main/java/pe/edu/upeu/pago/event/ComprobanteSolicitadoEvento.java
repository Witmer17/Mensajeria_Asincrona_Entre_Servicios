package pe.edu.upeu.pago.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteSolicitadoEvento {
    private String tipoEvento;
    private Long ordenId;
    private Long idCliente;
    private BigDecimal monto;
    private String metodoPago;
    private String origen;
    private Instant timestamp;
}
