package bankapp.auth.application.verification_complete.port.out.stubs;

import bankapp.auth.application.verification_complete.port.out.UserRepository;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;

import java.util.HashMap;
import java.util.Optional;

public class StubUserRepository implements UserRepository {

    private final HashMap<EmailAddress, User> emailToUserRepo = new HashMap<>();

    @Override
    public Optional<User> findByEmail(EmailAddress email) {
        return Optional.ofNullable(emailToUserRepo.get(email));
    }

    @Override
    public void save(User user) {
        this.emailToUserRepo.put(user.getEmail(), user);
    }
}
