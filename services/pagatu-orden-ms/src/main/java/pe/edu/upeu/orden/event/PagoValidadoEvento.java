package pe.edu.upeu.orden.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoValidadoEvento {
    private String tipoEvento;
    private Long ordenId;
    private BigDecimal monto;
    private String estado;
    private String origen;
    private Instant timestamp;
}
