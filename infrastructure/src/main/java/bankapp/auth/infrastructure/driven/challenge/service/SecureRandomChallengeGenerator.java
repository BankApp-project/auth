package bankapp.auth.infrastructure.driven.challenge.service;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.verification.complete.port.out.ChallengeGenerationPort;
import bankapp.auth.infrastructure.driven.challenge.config.ChallengeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Clock;

@Component
@RequiredArgsConstructor
public class SecureRandomChallengeGenerator implements ChallengeGenerationPort {

    private final ChallengeProperties properties;
    private final SecureRandom secureRandom;
    private final Clock clock;

    @Override
    public Challenge generate() {

        byte[] value = getRandomChallengeValue();

        return new Challenge(
                value,
                properties.ttl(),
                clock
        );
    }

    private byte[] getRandomChallengeValue() {
        byte[] value = new byte[properties.length()];
        secureRandom.nextBytes(value);
        return value;
    }

}
