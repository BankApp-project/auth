package bankapp.auth.infrastructure.driven.session;

import bankapp.auth.application.verification.complete.port.out.SessionIdGenerationPort;
import com.github.f4b6a3.uuid.alt.GUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SessionIdGenerator implements SessionIdGenerationPort {

    private final Clock clock;
    private final SecureRandom secureRandom;

    @Override
    public UUID generate() {
        return GUID.v7(Instant.now(clock), secureRandom).toUUID();
    }
}
