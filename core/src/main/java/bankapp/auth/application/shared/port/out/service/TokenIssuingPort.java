package bankapp.auth.application.shared.port.out.service;

import bankapp.auth.application.shared.port.out.dto.AuthTokens;

import java.util.UUID;

public interface TokenIssuingPort {
    AuthTokens issueTokensForUser(UUID userId);
}
