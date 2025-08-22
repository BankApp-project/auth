package bankapp.auth.application.verification_complete.port.out.stubs;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.service.ByteArrayUtil;
import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;

import java.time.Clock;
import java.util.UUID;

public class StubChallengeGenerator implements ChallengeGenerationPort {
    @Override
    public Challenge generate(Clock clock, long ttl) {
        var sessionId = UUID.randomUUID();
        var challenge = UUID.randomUUID();
        byte[] challengeBytes = ByteArrayUtil.uuidToBytes(challenge);
        return new Challenge(
                sessionId,
                challengeBytes,
                ttl,
                clock
        );
    }
}