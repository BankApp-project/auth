package bankapp.auth.application.dto.events;

import bankapp.auth.domain.model.Otp;
import lombok.Getter;

//TODO: delete otp from this event. it shouldnt be published.
@Getter
public class EmailVerificationOtpGeneratedEvent extends EventTemplate {
    private final String otpValue;
    private final String email;

    public EmailVerificationOtpGeneratedEvent(Otp otp) {
        super();
        this.otpValue = otp.getValue();
        this.email = otp.getKey();
    }
}
