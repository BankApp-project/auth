package bankapp.auth.application.registration.complete.port.in;

import java.util.UUID;

public record CompleteRegistrationCommand(
        UUID challengeId,
        String RegistrationResponseJSON
) {
    public CompleteRegistrationCommand {
        if (RegistrationResponseJSON == null || RegistrationResponseJSON.isBlank()) {
            throw new IllegalArgumentException("Registration Response JSON cannot be null or blank");
        }
        if (challengeId == null) {
            throw new IllegalArgumentException("Challenge ID cannot be null");
        }
    }
}