package pe.edu.upeu.orden.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pe.edu.upeu.orden.event.OrdenCreadaEvento;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrdenEventosProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.ordenes:orden-eventos}")
    private String topicOrdenes;

    public void publicarTrasCommit(OrdenCreadaEvento evento) {
        Runnable action = () -> {
            log.info("Publicando evento orden.creada en topic '{}' para ordenId={}", topicOrdenes, evento.getOrdenId());
            kafkaTemplate.send(topicOrdenes, String.valueOf(evento.getOrdenId()), evento)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Error al publicar evento orden.creada para ordenId={}: {}", evento.getOrdenId(), ex.getMessage());
                        } else {
                            log.info("Evento orden.creada publicado con éxito. Partición={} Offset={}",
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
