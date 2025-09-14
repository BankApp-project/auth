package bankapp.auth.application.authentication.initiate;

import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;
import bankapp.auth.application.verification.complete.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verification.complete.port.out.CredentialOptionsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InitiateAuthenticationUseCaseTest {

    private final static Clock DEFAULT_CLOCK = Clock.systemUTC();
    private final static Duration DEFAULT_CHALLENGE_TTL = Duration.ofSeconds(66); // In seconds

    private final static Session DEFAULT_SESSION = new Session(
            UUID.randomUUID(),
            new byte[]{123},
            DEFAULT_CHALLENGE_TTL,
            DEFAULT_CLOCK,
            null
    );

    private InitiateAuthenticationUseCase useCase;

    @Mock
    ChallengeGenerationPort challengeGenerator;

    @Mock
    ChallengeRepository challengeRepository;

    @Mock
    CredentialOptionsPort credentialOptionsService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);


        when(challengeGenerator.generate(null)).thenReturn(DEFAULT_SESSION);

        useCase = new InitiateAuthenticationUseCase(challengeGenerator, challengeRepository, credentialOptionsService);
    }

    @Test
    void should_generate_challenge() {
        useCase.handle();

        verify(challengeGenerator).generate(null);
    }

    @Test
    void should_persist_generated_challenge() {
        useCase.handle();

        verify(challengeRepository).save(DEFAULT_SESSION);
    }

    @Test
    void should_generate_LoginResponse_for_given_challenge() {
        useCase.handle();

        verify(credentialOptionsService).getPasskeyRequestOptions(eq(DEFAULT_SESSION));
    }

    @Test
    void should_return_response_with_newly_generated_challenge() {
        var res = useCase.handle();

        assertEquals(DEFAULT_SESSION.sessionId(), res.sessionId());
    }
}