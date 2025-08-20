package bankapp.auth.domain.model;

import bankapp.auth.domain.model.vo.EmailAddress;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@EqualsAndHashCode
@Getter
public class User {

    private final UUID id = UUID.randomUUID();
    private final EmailAddress email;

    private boolean enabled;

    public User(EmailAddress email) {
        this.email = email;
        this.enabled = false;
    }

    public void activate() {
        this.enabled = true;
    }
}
