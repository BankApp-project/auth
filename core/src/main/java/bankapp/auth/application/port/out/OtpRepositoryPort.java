package bankapp.auth.application.port.out;

import bankapp.auth.domain.model.Otp;

public interface OtpRepositoryPort {
    void save(Otp otp);
}