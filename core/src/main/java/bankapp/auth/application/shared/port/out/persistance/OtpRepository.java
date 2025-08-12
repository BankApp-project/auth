package bankapp.auth.application.shared.port.out.persistance;

import bankapp.auth.domain.model.Otp;

public interface OtpRepository {
    void save(Otp otp, int ttl);
}