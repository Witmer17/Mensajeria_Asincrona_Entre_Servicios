package pe.edu.upeu.cliente.service;

import pe.edu.upeu.cliente.dto.ClienteRequest;
import pe.edu.upeu.cliente.dto.ClienteResponse;

import java.util.List;

public interface ClienteService {
    List<ClienteResponse> listar();
    ClienteResponse findById(Long id);
    ClienteResponse findByDni(String dni);
    ClienteResponse findByRuc(String ruc);
    ClienteResponse crear(ClienteRequest request);
    ClienteResponse actualizar(Long id, ClienteRequest request);
    void eliminar(Long id);
}
