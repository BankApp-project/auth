package bankapp.auth.application.registration_complete;

import java.util.UUID;

public interface TokenIssuingPort {
    AuthTokens issueTokensForUser(UUID userId);
}
