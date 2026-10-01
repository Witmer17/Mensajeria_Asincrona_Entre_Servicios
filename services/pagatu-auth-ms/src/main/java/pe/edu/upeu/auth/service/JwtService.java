package pe.edu.upeu.auth.service;

import com.nimbusds.jose.jwk.RSAKey;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import pe.edu.upeu.auth.entity.Rol;
import pe.edu.upeu.auth.entity.Usuario;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final RSAKey rsaKey;

    @Value("${jwt.issuer:http://localhost:8085}")
    private String issuer;

    @Getter
    @Value("${jwt.expiracion-segundos:3600}")
    private long expiracionSegundos;

    public JwtService(JwtEncoder jwtEncoder, RSAKey rsaKey) {
        this.jwtEncoder = jwtEncoder;
        this.rsaKey = rsaKey;
    }

    public String generarToken(Usuario usuario) {
        Instant now = Instant.now();
        List<String> roles = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .toList();

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiracionSegundos))
                .claim("preferred_username", usuario.getEmail())
                .claim("email", usuario.getEmail())
                .claim("realm_access", Map.of("roles", roles));

        if (usuario.getIdCliente() != null) {
            claimsBuilder.claim("idCliente", usuario.getIdCliente());
        }

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(rsaKey.getKeyID())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claimsBuilder.build())).getTokenValue();
    }
}
