package pe.edu.upeu.cliente.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.cliente.entity.Cliente;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByDni(String dni);
    Optional<Cliente> findByRuc(String ruc);
    Optional<Cliente> findByEmail(String email);
    boolean existsByDni(String dni);
    boolean existsByRuc(String ruc);
}
