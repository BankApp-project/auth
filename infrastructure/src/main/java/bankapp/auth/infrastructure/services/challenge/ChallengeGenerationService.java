package bankapp.auth.infrastructure.services.challenge;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChallengeGenerationService implements ChallengeGenerationPort {

    private final static int CHALLENGE_LENGTH = 32;
    private final SecureRandom secureRandom;

    @Override
    public Challenge generate() {
        var sessionId = UUID.randomUUID();
        byte[] value = new byte[CHALLENGE_LENGTH];
        secureRandom.nextBytes(value);

        return new Challenge(
                sessionId,
                value,
                0L,
                Clock.systemUTC()
        );
    }
}
