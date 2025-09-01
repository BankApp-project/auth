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

    private User(UUID id, EmailAddress email, boolean enabled) {
        this.id = id;
        this.email = email;
        this.enabled = enabled;
    }

    public static User createNew(EmailAddress email) {
        return new User(email);
    }

    public static User reconstitute(UUID id, EmailAddress email, boolean enabled) {
        return new User(id, email, enabled);
    }

    public void activate() {
        this.enabled = true;
    }
}
