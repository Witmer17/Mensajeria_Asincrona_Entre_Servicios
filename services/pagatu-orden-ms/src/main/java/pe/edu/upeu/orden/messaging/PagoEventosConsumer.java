package pe.edu.upeu.orden.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.edu.upeu.orden.event.PagoValidadoEvento;
import pe.edu.upeu.orden.service.OrdenService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagoEventosConsumer {

    private final OrdenService ordenService;

    @KafkaListener(
            topics = "${app.kafka.topic.pagos:pago-eventos}",
            groupId = "${spring.kafka.consumer.group-id:pagatu-orden-group}"
    )
    public void consumirPagoValidado(PagoValidadoEvento evento) {
        if (evento == null) {
            log.warn("Evento de pago recibido nulo, ignorando.");
            return;
        }

        Long ordenId = evento.getOrdenId();
        if ("pago.validado".equalsIgnoreCase(evento.getTipoEvento())) {
            log.info("Recibido evento pago.validado para ordenId={}", ordenId);
            ordenService.marcarPagada(ordenId);
            log.info("Orden ID={} actualizada a PAGADA exitosamente", ordenId);
        } else {
            log.warn("Tipo de evento no manejado en pago-eventos: tipoEvento={}, ordenId={}",
                    evento.getTipoEvento(), ordenId);
        }
    }
}

