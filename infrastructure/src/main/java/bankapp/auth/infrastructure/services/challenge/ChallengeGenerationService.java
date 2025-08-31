package bankapp.auth.infrastructure.services.challenge;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;

import java.time.Clock;
import java.util.UUID;

public class ChallengeGenerationService implements ChallengeGenerationPort {
    @Override
    public Challenge generate() {
        var sessionId = UUID.randomUUID();

        return new Challenge(
                sessionId,
                new byte[]{123},
                0L,
                Clock.systemUTC()
        );
    }
}
