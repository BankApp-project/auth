package bankapp.auth.application.verification_initiate;

import bankapp.auth.application.verification_initiate.port.in.InitiateVerificationCommand;
import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.application.shared.port.out.*;
import bankapp.auth.domain.OtpService;
import bankapp.auth.domain.model.annotations.NotNull;

public class InitiateVerificationUseCase {

    private final OtpService otpService;
    private final OtpRepository otpRepository;
    private final NotificationPort notificator;

    public InitiateVerificationUseCase(
            @NotNull OtpRepository otpRepository,
            @NotNull NotificationPort notificator,
            @NotNull OtpService otpService) {
        this.otpRepository = otpRepository;
        this.notificator = notificator;
        this.otpService = otpService;
    }

    public void handle(InitiateVerificationCommand command) {

            VerificationData data = otpService.createVerificationOtp(command.email());

            otpRepository.save(data.otpToPersist());

            notificator.sendOtpToUserEmail(command.email(), data.rawOtpCode());

    }
}
