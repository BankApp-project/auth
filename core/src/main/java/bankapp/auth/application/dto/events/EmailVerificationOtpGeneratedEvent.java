package bankapp.auth.application.dto.events;

import bankapp.auth.domain.model.Otp;

public class EmailVerificationOtpGeneratedEvent extends EventTemplate {
    private final String otp;
    private final String email;

    public EmailVerificationOtpGeneratedEvent(Otp otp) {
        super();
        this.otp = otp.getValue();
        this.email = otp.getKey();
    }
}
