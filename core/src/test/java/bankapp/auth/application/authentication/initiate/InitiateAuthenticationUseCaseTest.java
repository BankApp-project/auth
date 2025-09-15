package bankapp.auth.application.authentication.initiate;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.persistance.SessionRepository;
import bankapp.auth.application.verification.complete.port.SessionIdGenerationPort;
import bankapp.auth.application.verification.complete.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verification.complete.port.out.PasskeyOptionsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InitiateAuthenticationUseCaseTest {

    private final static Clock DEFAULT_CLOCK = Clock.systemUTC();
    private final static Duration DEFAULT_CHALLENGE_TTL = Duration.ofSeconds(66); // In seconds

    private final static Challenge DEFAULT_CHALLENGE = new Challenge(
            new byte[]{123, 123},
            DEFAULT_CHALLENGE_TTL,
            DEFAULT_CLOCK
    );

    private InitiateAuthenticationUseCase useCase;

    @Mock
    ChallengeGenerationPort challengeGenerator;

    @Mock
    SessionRepository sessionRepository;

    @Mock
    PasskeyOptionsPort credentialOptionsService;

    @Mock
    SessionIdGenerationPort sessionIdGenerator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);


        when(challengeGenerator.generate()).thenReturn(DEFAULT_CHALLENGE);
        when(sessionIdGenerator.generate()).thenReturn(UUID.randomUUID());

        useCase = new InitiateAuthenticationUseCase(challengeGenerator, sessionRepository, credentialOptionsService, sessionIdGenerator);
    }

    @Test
    void should_generate_challenge() {
        useCase.handle();

        verify(challengeGenerator).generate();
    }

    @Test
    void should_persist_generated_challenge() {
        useCase.handle();

        verify(sessionRepository).save(argThat(session ->
                DEFAULT_CHALLENGE.equals(session.challenge()) && session.userId().isEmpty()));
    }

    @Test
    void should_generate_LoginResponse_for_given_challenge() {
        useCase.handle();

        verify(credentialOptionsService).getPasskeyRequestOptions(argThat(session ->
                DEFAULT_CHALLENGE.equals(session.challenge()) && session.userId().isEmpty()));
    }

    @Test
    void should_return_response_with_newly_generated_challenge() {
        var res = useCase.handle();

        assertNotNull(res.sessionId());
    }

    @Test
    void should_generate_sessionId() {
        useCase.handle();

        verify(sessionIdGenerator).generate();
    }
}