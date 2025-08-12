package bankapp.auth.application.initiate_verification.port.out;

import bankapp.auth.domain.model.Otp;

public interface OtpGeneratorPort {
    Otp generate(String email, int len);
}
