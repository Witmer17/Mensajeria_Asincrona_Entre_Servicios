package pe.edu.upeu.pago.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pe.edu.upeu.pago.event.ComprobanteSolicitadoEvento;
import pe.edu.upeu.pago.event.PagoValidadoEvento;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagoEventosProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.pagos:pago-eventos}")
    private String topicPagos;

    @Value("${app.kafka.topic.comprobantes:comprobante-eventos}")
    private String topicComprobantes;

    public void publicarTrasCommit(PagoValidadoEvento evento) {
        Runnable action = () -> {
            log.info("Publicando evento pago.validado en topic '{}' para ordenId={}", topicPagos, evento.getOrdenId());
            kafkaTemplate.send(topicPagos, String.valueOf(evento.getOrdenId()), evento)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Error al publicar evento pago.validado para ordenId={}: {}", evento.getOrdenId(), ex.getMessage());
                        } else {
                            log.info("Evento pago.validado publicado exitosamente en partición={} offset={}",
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        };

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    public void publicarComprobanteTrasCommit(ComprobanteSolicitadoEvento evento) {
        Runnable action = () -> {
            log.info("Publicando evento comprobante.solicitado en topic '{}' para ordenId={}, clienteId={}",
                    topicComprobantes, evento.getOrdenId(), evento.getIdCliente());
            kafkaTemplate.send(topicComprobantes, String.valueOf(evento.getOrdenId()), evento)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Error al publicar comprobante.solicitado para ordenId={}: {}", evento.getOrdenId(), ex.getMessage());
                        } else {
                            log.info("Evento comprobante.solicitado publicado exitosamente en partición={} offset={}",
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        };

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
