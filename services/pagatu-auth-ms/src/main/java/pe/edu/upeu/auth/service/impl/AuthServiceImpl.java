package pe.edu.upeu.auth.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.auth.dto.LoginRequest;
import pe.edu.upeu.auth.dto.LoginResponse;
import pe.edu.upeu.auth.dto.RegistroRequest;
import pe.edu.upeu.auth.dto.UsuarioResponse;
import pe.edu.upeu.auth.entity.Rol;
import pe.edu.upeu.auth.entity.Usuario;
import pe.edu.upeu.auth.exception.EmailYaRegistradoException;
import pe.edu.upeu.auth.exception.ResourceNotFoundException;
import pe.edu.upeu.auth.repository.RolRepository;
import pe.edu.upeu.auth.repository.UsuarioRepository;
import pe.edu.upeu.auth.service.AuthService;
import pe.edu.upeu.auth.service.JwtService;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UsuarioRepository usuarioRepository,
                           RolRepository rolRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + request.getEmail()));

        String token = jwtService.generarToken(usuario);

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiracionSegundos())
                .build();
    }

    @Override
    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new EmailYaRegistradoException("El email ya está registrado: " + request.getEmail());
        }

        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new ResourceNotFoundException("Rol CLIENTE no configurado en la base de datos"));

        Set<Rol> roles = new HashSet<>();
        roles.add(rolCliente);

        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .habilitado(true)
                .idCliente(request.getIdCliente())
                .roles(roles)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);

        return UsuarioResponse.builder()
                .id(guardado.getId())
                .email(guardado.getEmail())
                .habilitado(guardado.isHabilitado())
                .idCliente(guardado.getIdCliente())
                .roles(guardado.getRoles().stream().map(Rol::getNombre).toList())
                .build();
    }
}
