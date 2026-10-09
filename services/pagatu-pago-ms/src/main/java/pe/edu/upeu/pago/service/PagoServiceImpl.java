package pe.edu.upeu.pago.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.pago.dto.PagoResponse;
import pe.edu.upeu.pago.entity.EstadoPago;
import pe.edu.upeu.pago.entity.Pago;
import pe.edu.upeu.pago.event.ComprobanteSolicitadoEvento;
import pe.edu.upeu.pago.event.OrdenCreadaEvento;
import pe.edu.upeu.pago.event.PagoValidadoEvento;
import pe.edu.upeu.pago.exception.ResourceNotFoundException;
import pe.edu.upeu.pago.mapper.PagoMapper;
import pe.edu.upeu.pago.messaging.PagoEventosProducer;
import pe.edu.upeu.pago.repository.PagoRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final PagoEventosProducer pagoEventosProducer;
    private final PagoMapper pagoMapper;

    @Override
    @Transactional
    public PagoResponse procesar(OrdenCreadaEvento evento) {
        log.info("Procesando pago para ordenId={}, total={}, metodoPago={}",
                evento.getOrdenId(), evento.getTotal(), evento.getMetodoPago());

        if (pagoRepository.existsByOrdenId(evento.getOrdenId())) {
            log.warn("El pago para la ordenId={} ya fue procesado previamente (idempotencia).", evento.getOrdenId());
            Pago pagoExistente = pagoRepository.findByOrdenId(evento.getOrdenId()).orElseThrow();
            return pagoMapper.toResponse(pagoExistente);
        }

        Pago pago = Pago.builder()
                .ordenId(evento.getOrdenId())
                .monto(evento.getTotal())
                .metodoPago(evento.getMetodoPago())
                .estado(EstadoPago.VALIDADO)
                .fechaPago(LocalDateTime.now())
                .build();

        Pago guardado = pagoRepository.save(pago);
        log.info("Pago guardado con id={} para ordenId={}", guardado.getId(), guardado.getOrdenId());

        PagoValidadoEvento pagoValidadoEvento = PagoValidadoEvento.builder()
                .tipoEvento("pago.validado")
                .ordenId(guardado.getOrdenId())
                .monto(guardado.getMonto())
                .estado(guardado.getEstado().name())
                .origen("pagatu-pago-ms")
                .timestamp(Instant.now())
                .build();

        pagoEventosProducer.publicarTrasCommit(pagoValidadoEvento);

        ComprobanteSolicitadoEvento comprobanteEvento = ComprobanteSolicitadoEvento.builder()
                .tipoEvento("comprobante.solicitado")
                .ordenId(guardado.getOrdenId())
                .idCliente(evento.getIdCliente())
                .monto(guardado.getMonto())
                .metodoPago(guardado.getMetodoPago())
                .origen("pagatu-pago-ms")
                .timestamp(Instant.now())
                .build();

        pagoEventosProducer.publicarComprobanteTrasCommit(comprobanteEvento);

        return pagoMapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listar() {
        return pagoMapper.toResponseList(pagoRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponse obtener(Long id) {
        return pagoRepository.findById(id)
                .map(pagoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id: " + id));
    }
}
