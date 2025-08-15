package bankapp.auth.domain.model;

import bankapp.auth.domain.model.vo.EmailAddress;
import lombok.Data;

import java.util.UUID;

@Data
public class User {

    private final UUID id = UUID.randomUUID();
    private final EmailAddress email;

    private boolean enabled;

    public User(EmailAddress email) {
        this.email = email;
        this.enabled = false;
    }
}
