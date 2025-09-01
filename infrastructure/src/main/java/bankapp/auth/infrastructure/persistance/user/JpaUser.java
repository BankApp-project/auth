package bankapp.auth.infrastructure.persistance.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email_activated", columnList = "email, enabled")
})
public class JpaUser {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    public JpaUser(UUID id, String email, boolean enabled) {
        this.id = id;
        this.email = email;
        this.enabled = enabled;
    }
}