package bankapp.auth.application.verification_initiate;

import bankapp.auth.application.shared.UseCase;
import bankapp.auth.application.verification_initiate.port.in.InitiateVerificationCommand;
import bankapp.auth.application.shared.port.out.*;
import bankapp.auth.domain.OtpService;
import bankapp.auth.domain.model.annotations.NotNull;

@UseCase
public class InitiateVerificationUseCase {

    private final OtpService otpService;
    private final NotificationPort notificator;

    public InitiateVerificationUseCase(
            @NotNull NotificationPort notificator,
            @NotNull OtpService otpService) {
        this.notificator = notificator;
        this.otpService = otpService;
    }

    public void handle(InitiateVerificationCommand command) {

            VerificationData data = otpService.createVerificationOtp(command.email());

            notificator.sendOtpToUserEmail(command.email(), data.rawOtpCode());

    }
}
