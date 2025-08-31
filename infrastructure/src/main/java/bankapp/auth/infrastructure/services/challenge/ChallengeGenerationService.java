package bankapp.auth.infrastructure.services.challenge;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;

import java.time.Clock;

public class ChallengeGenerationService implements ChallengeGenerationPort {
    @Override
    public Challenge generate() {
        return new Challenge(
                null,
                null,
                0L,
                Clock.systemUTC()
        );
    }
}
