package cl.saiyagym.gym_backend.seguridad;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gym.jwt")
public record JwtProperties(String secret, int expiracionHoras) {
}