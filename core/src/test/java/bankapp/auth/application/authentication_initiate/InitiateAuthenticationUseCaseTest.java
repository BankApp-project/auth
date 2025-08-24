package bankapp.auth.application.authentication_initiate;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;
import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verification_complete.port.out.CredentialOptionsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InitiateAuthenticationUseCaseTest {

    private final static Clock DEFAULT_CLOCK = Clock.systemUTC();
    private final static long DEFAULT_CHALLENGE_TTL = 60; // In seconds

    private final static Challenge DEFAULT_CHALLENGE = new Challenge(
            UUID.randomUUID(),
            new byte[]{123},
            DEFAULT_CHALLENGE_TTL,
            DEFAULT_CLOCK
    );

    private InitiateAuthenticationUseCase useCase;
    private InitiateAuthenticationCommand command;

    @Mock
    ChallengeGenerationPort challengeGenerator;

    @Mock
    ChallengeRepository challengeRepository;

    @Mock
    CredentialOptionsPort credentialOptionsService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);


        when(challengeGenerator.generate()).thenReturn(DEFAULT_CHALLENGE);

        useCase = new InitiateAuthenticationUseCase(challengeGenerator, challengeRepository, credentialOptionsService);
        command = new InitiateAuthenticationCommand();
    }

    @Test
    void should_generate_challenge() {
        useCase.handle(command);

        verify(challengeGenerator).generate();
    }

    @Test
    void should_persist_generated_challenge() {
        useCase.handle(command);

        verify(challengeRepository).save(DEFAULT_CHALLENGE);
    }

    @Test
    void should_generate_LoginResponse_for_given_challenge() {
        useCase.handle(command);

        verify(credentialOptionsService).getPasskeyRequestOptions(eq(DEFAULT_CHALLENGE));
    }

    @Test
    void should_return_response_with_newly_generated_challenge() {
        var res = useCase.handle(command);

        assertEquals(DEFAULT_CHALLENGE.sessionId(), res.challengeId());
    }
}