package bankapp.auth.infrastructure.driven.session;

import bankapp.auth.application.shared.port.out.service.SessionIdGenerationPort;
import com.github.f4b6a3.uuid.alt.GUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionIdGenerator implements SessionIdGenerationPort {

    private final Clock clock;
    private final SecureRandom secureRandom;

    @Override
    public UUID generate() {
        log.info("Generating session ID.");
        log.debug("Generating UUIDv7 session ID using current timestamp.");

        UUID sessionId = GUID.v7(Instant.now(clock), secureRandom).toUUID();
        log.info("Successfully generated session ID.");
        return sessionId;
    }
}
