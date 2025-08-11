package bankapp.auth.application.dto.events;

import bankapp.auth.domain.model.Otp;

//TODO: delete otp from this event. it shouldnt be published.
public class EmailVerificationOtpGeneratedEvent extends EventTemplate {
    private final String otp;
    private final String email;

    public EmailVerificationOtpGeneratedEvent(Otp otp) {
        super();
        this.otp = otp.getValue();
        this.email = otp.getKey();
    }
}
