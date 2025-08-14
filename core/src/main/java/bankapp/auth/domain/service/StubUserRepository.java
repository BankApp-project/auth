package bankapp.auth.domain.service;

import bankapp.auth.application.verify_otp.port.out.UserRepository;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;

public class StubUserRepository implements UserRepository {
    @Override
    public User findByEmail(EmailAddress email) {
        return null;
    }
}
