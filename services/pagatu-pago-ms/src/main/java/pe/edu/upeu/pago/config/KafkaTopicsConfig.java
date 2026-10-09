package pe.edu.upeu.pago.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    @Value("${app.kafka.topic.ordenes:orden-eventos}")
    private String topicOrdenes;

    @Value("${app.kafka.topic.pagos:pago-eventos}")
    private String topicPagos;

    @Value("${app.kafka.topic.comprobantes:comprobante-eventos}")
    private String topicComprobantes;

    @Bean
    public NewTopic ordenEventosTopic() {
        return TopicBuilder.name(topicOrdenes)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic pagoEventosTopic() {
        return TopicBuilder.name(topicPagos)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic comprobanteEventosTopic() {
        return TopicBuilder.name(topicComprobantes)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
