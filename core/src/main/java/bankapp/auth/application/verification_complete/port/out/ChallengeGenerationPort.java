package bankapp.auth.application.verification_complete.port.out;

import bankapp.auth.application.shared.port.out.dto.Challenge;

import java.time.Clock;

public interface ChallengeGenerationPort {
    Challenge generate(Clock clock, long ttl);
}