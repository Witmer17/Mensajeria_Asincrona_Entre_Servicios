package pe.edu.upeu.cliente.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.edu.upeu.cliente.entity.Cliente;
import pe.edu.upeu.cliente.event.ComprobanteSolicitadoEvento;
import pe.edu.upeu.cliente.repository.ClienteRepository;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ComprobanteEventosConsumer {

    private final ClienteRepository clienteRepository;

    @KafkaListener(topics = "${app.kafka.topic.comprobantes:comprobante-eventos}")
    public void consumirComprobanteSolicitado(ComprobanteSolicitadoEvento evento) {
        log.info("Recibido evento desde Kafka en pagatu-cliente-ms: tipoEvento={}, ordenId={}, clienteId={}",
                evento != null ? evento.getTipoEvento() : "null",
                evento != null ? evento.getOrdenId() : "null",
                evento != null ? evento.getIdCliente() : "null");

        if (evento == null) {
            log.warn("Evento de comprobante recibido nulo, ignorando.");
            return;
        }

        if ("comprobante.solicitado".equalsIgnoreCase(evento.getTipoEvento())) {
            Optional<Cliente> clienteOpt = evento.getIdCliente() != null 
                    ? clienteRepository.findById(evento.getIdCliente()) 
                    : Optional.empty();

            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                String destinatario = cliente.getTipoPersona() != null && "JURIDICA".equalsIgnoreCase(cliente.getTipoPersona().name())
                        ? cliente.getRazonSocial()
                        : cliente.getNombreCompleto();

                log.info("Comprobante electrónico preparado para ordenId={}: Cliente='{}' ({}), Email='{}', Monto=S/ {}",
                        evento.getOrdenId(), destinatario, cliente.getTipoPersona(), cliente.getEmail(), evento.getMonto());
            } else {
                log.info("Comprobante preparado para ordenId={} sin cliente registrado (clienteId={}), Monto=S/ {}",
                        evento.getOrdenId(), evento.getIdCliente(), evento.getMonto());
            }
        } else {
            log.warn("Tipo de evento no manejado en comprobante-eventos: {}", evento.getTipoEvento());
        }
    }
}
