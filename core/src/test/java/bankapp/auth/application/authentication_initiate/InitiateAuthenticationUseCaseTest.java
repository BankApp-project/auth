package bankapp.auth.application.authentication_initiate;

import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;

import static org.mockito.Mockito.verify;

public class InitiateAuthenticationUseCaseTest {

    private final Clock clock = Clock.systemUTC();
    private final long challengeTtl = 60; // In seconds

    @Mock
    ChallengeGenerationPort challengeGenerator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void should_generate_challenge() {
        var useCase = new InitiateAuthenticationUseCase(challengeGenerator, clock, challengeTtl);
        var command = new InitiateAuthenticationCommand();
        useCase.handle(command);

        verify(challengeGenerator).generate(clock, challengeTtl);
    }
}