package bankapp.auth.infrastructure.driven.tokens;

import bankapp.auth.application.shared.port.out.TokenIssuingPort;
import bankapp.auth.application.shared.port.out.dto.AuthTokens;

import java.util.UUID;

public class TokenIssuingStub implements TokenIssuingPort {


    /// Stub for now.
    ///
    /// Will implement when needed
    ///
    /// @Depracated do not use. its stub
    @Deprecated
    @Override
    public AuthTokens issueTokensForUser(UUID userId) {
        return new AuthTokens("accessToken", "refreshToken");
    }
}
