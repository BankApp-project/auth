package bankapp.auth.application.verify_otp.port.out;

import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByEmail(EmailAddress email);

    void save(User user);
}
