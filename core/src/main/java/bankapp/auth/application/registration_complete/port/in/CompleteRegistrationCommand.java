package bankapp.auth.application.registration_complete.port.in;

import java.util.UUID;

public record CompleteRegistrationCommand(
        UUID sessionId,
        String RegistrationResponseJSON
) {
    public CompleteRegistrationCommand {
        if (RegistrationResponseJSON == null || RegistrationResponseJSON.isBlank()) {
            throw new IllegalArgumentException("Registration Response JSON cannot be null or blank");
        }
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
    }
}