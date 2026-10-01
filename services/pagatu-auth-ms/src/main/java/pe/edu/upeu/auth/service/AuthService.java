package pe.edu.upeu.auth.service;

import pe.edu.upeu.auth.dto.LoginRequest;
import pe.edu.upeu.auth.dto.LoginResponse;
import pe.edu.upeu.auth.dto.RegistroRequest;
import pe.edu.upeu.auth.dto.UsuarioResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    UsuarioResponse registrar(RegistroRequest request);
}
