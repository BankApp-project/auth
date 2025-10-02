package bankapp.auth.application.shared.port.out.repository;

import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findByEmail(EmailAddress email);

    void save(User user);

    Optional<User> findById(UUID userId);
}
