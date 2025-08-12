package bankapp.auth.application.initiate_verification.port.out;

import bankapp.auth.domain.model.Otp;

public interface OtpSavingPort {
    void save(Otp otp, int ttl);
}