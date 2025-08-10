package bankapp.auth.application.port.out;

import bankapp.auth.domain.model.Otp;

public interface OtpGeneratorPort {
    Otp generate(String email, int len);
}
