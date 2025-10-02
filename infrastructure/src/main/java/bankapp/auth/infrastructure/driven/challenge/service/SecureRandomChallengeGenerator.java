package bankapp.auth.infrastructure.driven.challenge.service;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.service.ChallengeGenerationPort;
import bankapp.auth.infrastructure.driven.challenge.config.ChallengeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Clock;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecureRandomChallengeGenerator implements ChallengeGenerationPort {

    private final ChallengeProperties properties;
    private final SecureRandom secureRandom;
    private final Clock clock;

    @Override
    public Challenge generate() {
        log.info("Generating challenge.");
        log.debug("Generating challenge with length: {} bytes and TTL: {}", properties.length(), properties.ttl());

        byte[] value = getRandomChallengeValue();

        log.info("Successfully generated challenge.");
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
