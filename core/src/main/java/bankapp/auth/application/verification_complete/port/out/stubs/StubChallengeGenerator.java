package bankapp.auth.application.verification_complete.port.out.stubs;

import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;

import java.util.UUID;

import static bankapp.auth.application.shared.service.ByteArrayUtil.uuidToBytes;

public class StubChallengeGenerator implements ChallengeGenerationPort {
    @Override
    public byte[] generate() {
        return uuidToBytes(UUID.randomUUID());
    }
}