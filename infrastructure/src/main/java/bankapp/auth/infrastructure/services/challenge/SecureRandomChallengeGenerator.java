package bankapp.auth.infrastructure.services.challenge;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecureRandomChallengeGenerator implements ChallengeGenerationPort {

    private final ChallengeProperties properties;
    private final SecureRandom secureRandom;
    private final Clock clock;

    @Override
    public Challenge generate() {
        var sessionId = getSessionId();

        byte[] value = getRandomChallengeValue();

        Instant expTime = getExpirationTime();

        return new Challenge(
                sessionId,
                value,
                expTime
        );
    }

    private UUID getSessionId() {
        return UUID.randomUUID();
    }

    private byte[] getRandomChallengeValue() {
        byte[] value = new byte[properties.length()];
        secureRandom.nextBytes(value);
        return value;
    }

    private Instant getExpirationTime() {
        return Instant.now(clock).plus(properties.ttl());
    }
}
