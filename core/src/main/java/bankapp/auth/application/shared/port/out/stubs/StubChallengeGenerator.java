package bankapp.auth.application.shared.port.out.stubs;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.service.ChallengeGenerationPort;
import bankapp.auth.application.shared.service.ByteArrayUtil;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

public class StubChallengeGenerator implements ChallengeGenerationPort {

    private final long ttl;
    private final Clock clock;

    public StubChallengeGenerator(long ttl, Clock clock) {
        this.ttl = ttl;
        this.clock = clock;
    }

    @Override
    public Challenge generate() {
        var challenge = UUID.randomUUID();
        byte[] challengeBytes = ByteArrayUtil.uuidToBytes(challenge);
        return new Challenge(
                challengeBytes,
                Duration.ofSeconds(ttl),
                clock
        );
    }
}