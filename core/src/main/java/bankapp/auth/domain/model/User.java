package bankapp.auth.domain.model;

import bankapp.auth.domain.model.vo.EmailAddress;
import lombok.Data;

import java.util.UUID;

@Data
public class User {

    private UUID id = UUID.randomUUID();
    private EmailAddress email;

    public User(EmailAddress email) {
        this.email = email;
    }
}
