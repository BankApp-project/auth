package bankapp.auth.domain.model;

import bankapp.auth.domain.model.vo.EmailAddress;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@EqualsAndHashCode
@Getter
public class User {

    private final UUID id;
    private final EmailAddress email;

    private boolean enabled;

    private User(EmailAddress email) {
        this.id = UUID.randomUUID();
        this.email = email;
        this.enabled = false;
    }

    public User(UUID id, EmailAddress email, boolean enabled) {
        this.id = id;
        this.email = email;
        this.enabled = enabled;
    }
    public static User createNew(EmailAddress email) {
        return new User(email);
    }

    public void activate() {
        this.enabled = true;
    }
}
