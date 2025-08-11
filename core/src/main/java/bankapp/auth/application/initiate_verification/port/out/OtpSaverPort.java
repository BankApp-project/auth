package bankapp.auth.application.initiate_verification.port.out;

import bankapp.auth.domain.model.Otp;

public interface OtpSaverPort {
    void save(Otp otp);
}