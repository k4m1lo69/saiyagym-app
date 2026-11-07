package cl.saiyagym.gym_backend.usuario;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> listar() {
        return usuarios.findAll(Sort.by("id")).stream().map(UsuarioDto::de).toList();
    }

    @Transactional
    public UsuarioDto crear(String nombre, String email, String password) {
        String emailNormalizado = email.trim().toLowerCase();
        if (usuarios.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya esta registrado");
        }
        try {
            Usuario u = usuarios.saveAndFlush(
                    new Usuario(nombre.trim(), emailNormalizado, encoder.encode(password), "ADMIN"));
            return UsuarioDto.de(u);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya esta registrado");
        }
    }

    @Transactional
    public UsuarioDto desactivar(Long id, Long actorId) {
        if (id.equals(actorId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes desactivar tu propio usuario");
        }
        Usuario u = usuarios.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        u.setActivo(false);
        return UsuarioDto.de(u);
    }

    @Transactional
    public void cambiarPassword(Long id, String actual, String nueva) {
        Usuario u = usuarios.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (!encoder.matches(actual, u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La clave actual no es correcta");
        }
        if (encoder.matches(nueva, u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva clave debe ser distinta");
        }
        u.setPasswordHash(encoder.encode(nueva));
    }
}