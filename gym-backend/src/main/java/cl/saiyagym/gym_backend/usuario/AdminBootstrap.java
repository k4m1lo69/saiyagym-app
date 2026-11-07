package cl.saiyagym.gym_backend.usuario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final Environment env;

    public AdminBootstrap(UsuarioRepository usuarios, PasswordEncoder encoder, Environment env) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) {
        crear("admin1");
        crear("admin2");
    }

    private void crear(String clave) {
        String prefijo = "gym.bootstrap." + clave + ".";
        String email = env.getProperty(prefijo + "email", "").trim().toLowerCase();
        String nombre = env.getProperty(prefijo + "nombre", "").trim();
        String password = env.getProperty(prefijo + "password", "");
        if (email.isEmpty()) {
            return;
        }
        if (usuarios.existsByEmailIgnoreCase(email)) {
            log.info("Admin {} ya existe, no se modifica", email);
            return;
        }
        if (nombre.isEmpty() || password.length() < 10) {
            throw new IllegalStateException(
                    "Datos invalidos para " + clave + ": nombre requerido y clave de al menos 10 caracteres");
        }
        usuarios.save(new Usuario(nombre, email, encoder.encode(password), "ADMIN"));
        log.info("Admin creado: {}", email);
    }
}