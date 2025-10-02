package bankapp.auth.application.verification.initiate;

import bankapp.auth.application.shared.UseCase;
import bankapp.auth.application.shared.port.out.service.NotificationPort;
import bankapp.auth.application.verification.initiate.port.in.InitiateVerificationCommand;
import bankapp.auth.domain.OtpService;
import bankapp.auth.domain.model.annotations.NotNull;
import bankapp.auth.domain.model.annotations.TransactionalUseCase;

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

    @TransactionalUseCase
    public void handle(InitiateVerificationCommand command) {

            VerificationData data = otpService.createVerificationOtp(command.email());

            notificator.sendOtpToUserEmail(command.email(), data.rawOtpCode());

    }
}
