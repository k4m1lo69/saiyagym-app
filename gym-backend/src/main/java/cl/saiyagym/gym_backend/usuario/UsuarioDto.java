package cl.saiyagym.gym_backend.usuario;

public record UsuarioDto(Long id, String nombre, String email, String rol) {

    public static UsuarioDto de(Usuario u) {
        return new UsuarioDto(u.getId(), u.getNombre(), u.getEmail(), u.getRol());
    }
}