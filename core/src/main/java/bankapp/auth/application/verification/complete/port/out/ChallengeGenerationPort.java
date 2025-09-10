package bankapp.auth.application.verification.complete.port.out;

import bankapp.auth.application.shared.port.out.dto.Challenge;

import java.util.UUID;

//ttl and clock should be injected into implementation
public interface ChallengeGenerationPort {
    Challenge generate(UUID userId);
}