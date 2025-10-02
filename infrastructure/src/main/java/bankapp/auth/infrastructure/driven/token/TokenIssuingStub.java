package bankapp.auth.infrastructure.driven.token;

import bankapp.auth.application.shared.port.out.dto.AuthTokens;
import bankapp.auth.application.shared.port.out.service.TokenIssuingPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TokenIssuingStub implements TokenIssuingPort {


    /// Stub for now.
    ///
    /// Will implement when needed
    ///
    /// @deprecated do not use. its stub
    @Deprecated
    @Override
    public AuthTokens issueTokensForUser(UUID userId) {
        return new AuthTokens("accessToken", "refreshToken");
    }
}
