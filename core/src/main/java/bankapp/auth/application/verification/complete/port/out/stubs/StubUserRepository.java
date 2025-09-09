package bankapp.auth.application.verification.complete.port.out.stubs;

import bankapp.auth.application.shared.port.out.persistance.UserRepository;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class StubUserRepository implements UserRepository {

    private final HashMap<EmailAddress, User> emailToUserRepo = new HashMap<>();
    private final HashMap<UUID, User> idToUserRepo = new HashMap<>();

    @Override
    public Optional<User> findByEmail(EmailAddress email) {
        return Optional.ofNullable(emailToUserRepo.get(email));
    }

    @Override
    public void save(User user) {
        this.emailToUserRepo.put(user.getEmail(), user);
        this.idToUserRepo.put(user.getId(), user);
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return Optional.ofNullable(idToUserRepo.get(userId));
    }
}
