package cl.saiyagym.gym_backend.usuario;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    public record CrearUsuarioRequest(
            @NotBlank @Size(max = 100) String nombre,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(min = 10, max = 100) String password) {
    }

    public record CambiarPasswordRequest(
            @NotBlank @Size(max = 100) String actual,
            @NotBlank @Size(min = 10, max = 100) String nueva) {
    }

    private final UsuarioService servicio;

    public UsuarioController(UsuarioService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<UsuarioDto> listar() {
        return servicio.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto crear(@Valid @RequestBody CrearUsuarioRequest req) {
        return servicio.crear(req.nombre(), req.email(), req.password());
    }

    @PatchMapping("/{id}/desactivar")
    public UsuarioDto desactivar(@PathVariable Long id, @AuthenticationPrincipal Jwt token) {
        return servicio.desactivar(id, Long.valueOf(token.getSubject()));
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarPassword(@Valid @RequestBody CambiarPasswordRequest req,
                                @AuthenticationPrincipal Jwt token) {
        servicio.cambiarPassword(Long.valueOf(token.getSubject()), req.actual(), req.nueva());
    }
}