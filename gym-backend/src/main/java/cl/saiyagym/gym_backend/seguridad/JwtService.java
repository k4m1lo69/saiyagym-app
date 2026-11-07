package cl.saiyagym.gym_backend.seguridad;

import cl.saiyagym.gym_backend.usuario.Usuario;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    public record TokenEmitido(String token, Instant expiraEn) {
    }

    private final JwtEncoder encoder;
    private final JwtProperties props;

    public JwtService(JwtEncoder encoder, JwtProperties props) {
        this.encoder = encoder;
        this.props = props;
    }

    public TokenEmitido emitir(Usuario u) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(props.expiracionHoras(), ChronoUnit.HOURS);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("saiyagym-app")
                .subject(String.valueOf(u.getId()))
                .issuedAt(ahora)
                .expiresAt(expira)
                .claim("rol", u.getRol())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenEmitido(token, expira);
    }
}