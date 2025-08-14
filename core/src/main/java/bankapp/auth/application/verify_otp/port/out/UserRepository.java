package bankapp.auth.application.verify_otp.port.out;

import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;

public interface UserRepository {

    User findByEmail(EmailAddress email);
}
