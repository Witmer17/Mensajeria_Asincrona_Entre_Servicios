package pe.edu.upeu.pago.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.edu.upeu.pago.event.OrdenCreadaEvento;
import pe.edu.upeu.pago.service.PagoService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrdenEventosConsumer {

    private final PagoService pagoService;

    @KafkaListener(
            topics = "${app.kafka.topic.ordenes:orden-eventos}",
            groupId = "${spring.kafka.consumer.group-id:pagatu-pago-group}"
    )
    public void consumirOrdenCreada(OrdenCreadaEvento evento) {
        log.info("Recibido evento desde Kafka: tipoEvento={}, ordenId={}",
                evento != null ? evento.getTipoEvento() : "null",
                evento != null ? evento.getOrdenId() : "null");

        if (evento == null) {
            log.warn("Evento recibido nulo, ignorando mensaje.");
            return;
        }

        if ("orden.creada".equalsIgnoreCase(evento.getTipoEvento())) {
            log.info("Procesando evento orden.creada para ordenId={}", evento.getOrdenId());
            pagoService.procesar(evento);
        } else {
            log.warn("Tipo de evento no manejado: {}", evento.getTipoEvento());
        }
    }
}
