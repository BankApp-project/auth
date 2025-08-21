package bankapp.auth.application.authentication_initiate;

import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class InitiateAuthenticationUseCaseTest {

    @Mock
    ChallengeGenerationPort challengeGenerator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void should_generate_challenge() {
        var useCase = new InitiateAuthenticationUseCase(challengeGenerator);
        var command = new InitiateAuthenticationCommand();
        useCase.handle(command);

        verify(challengeGenerator).generate();
    }
}