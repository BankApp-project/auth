package bankapp.auth.domain.service;

import bankapp.auth.application.verify_otp.port.out.UserRepository;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;

import java.util.Optional;

public class StubUserRepository implements UserRepository {
    @Override
    public Optional<User> findByEmail(EmailAddress email) {
        return null;
    }
}
