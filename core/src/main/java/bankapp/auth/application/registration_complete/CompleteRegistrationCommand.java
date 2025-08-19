package bankapp.auth.application.registration_complete;

import java.util.UUID;

public record CompleteRegistrationCommand(
        UUID sessionId,
        String publicKeyCredentialJson
) {
}
