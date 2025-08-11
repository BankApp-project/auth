package bankapp.auth.application.port.out;

import bankapp.auth.domain.model.Otp;

public interface OtpSaverPort {
    void save(Otp otp);
}