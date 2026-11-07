package cl.saiyagym.gym_backend.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 20)
    private String rol;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private OffsetDateTime creadoEn;

    protected Usuario() {
    }

    public Usuario(String nombre, String email, String passwordHash, String rol) {
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRol() { return rol; }
    public boolean isActivo() { return activo; }
    public OffsetDateTime getCreadoEn() { return creadoEn; }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setActivo(boolean activo) { this.activo = activo; }
}