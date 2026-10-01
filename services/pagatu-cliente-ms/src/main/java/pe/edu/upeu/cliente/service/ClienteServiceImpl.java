package pe.edu.upeu.cliente.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.cliente.dto.ClienteRequest;
import pe.edu.upeu.cliente.dto.ClienteResponse;
import pe.edu.upeu.cliente.entity.Cliente;
import pe.edu.upeu.cliente.exception.ResourceNotFoundException;
import pe.edu.upeu.cliente.repository.ClienteRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse findById(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
        return toResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse findByDni(String dni) {
        Cliente cliente = clienteRepository.findByDni(dni)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con DNI: " + dni));
        return toResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse findByRuc(String ruc) {
        Cliente cliente = clienteRepository.findByRuc(ruc)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con RUC: " + ruc));
        return toResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        if (request.getDni() != null && !request.getDni().isBlank() && clienteRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("Ya existe un cliente con el DNI: " + request.getDni());
        }
        if (request.getRuc() != null && !request.getRuc().isBlank() && clienteRepository.existsByRuc(request.getRuc())) {
            throw new IllegalArgumentException("Ya existe un cliente con el RUC: " + request.getRuc());
        }

        Cliente cliente = Cliente.builder()
                .tipoPersona(request.getTipoPersona())
                .dni(request.getDni())
                .ruc(request.getRuc())
                .nombreCompleto(request.getNombreCompleto())
                .razonSocial(request.getRazonSocial())
                .direccion(request.getDireccion())
                .email(request.getEmail())
                .whatsapp(request.getWhatsapp())
                .build();

        return toResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        cliente.setTipoPersona(request.getTipoPersona());
        cliente.setDni(request.getDni());
        cliente.setRuc(request.getRuc());
        cliente.setNombreCompleto(request.getNombreCompleto());
        cliente.setRazonSocial(request.getRazonSocial());
        cliente.setDireccion(request.getDireccion());
        cliente.setEmail(request.getEmail());
        cliente.setWhatsapp(request.getWhatsapp());

        return toResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente no encontrado con id: " + id);
        }
        clienteRepository.deleteById(id);
    }

    private ClienteResponse toResponse(Cliente c) {
        return ClienteResponse.builder()
                .id(c.getId())
                .tipoPersona(c.getTipoPersona())
                .dni(c.getDni())
                .ruc(c.getRuc())
                .nombreCompleto(c.getNombreCompleto())
                .razonSocial(c.getRazonSocial())
                .direccion(c.getDireccion())
                .email(c.getEmail())
                .whatsapp(c.getWhatsapp())
                .build();
    }
}
