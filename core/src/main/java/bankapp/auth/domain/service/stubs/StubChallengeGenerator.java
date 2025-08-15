package bankapp.auth.domain.service.stubs;

import bankapp.auth.application.verify_otp.port.out.ChallengeGenerationPort;

import java.util.UUID;

import static bankapp.auth.domain.service.ByteArrayUtil.uuidToBytes;

public class StubChallengeGenerator implements ChallengeGenerationPort {
    @Override
    public byte[] generate() {
        return uuidToBytes(UUID.randomUUID());
    }
}