package cl.saiyagym.gym_backend.auth;

import cl.saiyagym.gym_backend.seguridad.JwtService;
import cl.saiyagym.gym_backend.usuario.Usuario;
import cl.saiyagym.gym_backend.usuario.UsuarioDto;
import cl.saiyagym.gym_backend.usuario.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record LoginRequest(
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(max = 100) String password) {
    }

    public record LoginResponse(String token, String tipo, Instant expiraEn, UsuarioDto usuario) {
    }

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        Usuario u = usuarios.findByEmailIgnoreCase(req.email().trim())
                .filter(Usuario::isActivo)
                .filter(x -> encoder.matches(req.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));
        JwtService.TokenEmitido t = jwt.emitir(u);
        return new LoginResponse(t.token(), "Bearer", t.expiraEn(), UsuarioDto.de(u));
    }

    @GetMapping("/me")
    public UsuarioDto me(@AuthenticationPrincipal Jwt token) {
        return usuarios.findById(Long.valueOf(token.getSubject()))
                .map(UsuarioDto::de)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}