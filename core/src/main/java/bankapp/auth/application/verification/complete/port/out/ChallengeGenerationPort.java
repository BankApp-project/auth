package bankapp.auth.application.verification.complete.port.out;

import bankapp.auth.application.shared.port.out.dto.Session;

import java.util.UUID;

//ttl and clock should be injected into implementation
public interface ChallengeGenerationPort {
    Session generate(UUID userId);
}