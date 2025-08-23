package bankapp.auth.application.authentication_initiate;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;
import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InitiateAuthenticationUseCaseTest {

    private final Clock clock = Clock.systemUTC();
    private final long challengeTtl = 60; // In seconds

    private Challenge defaultChallenge;

    InitiateAuthenticationUseCase useCase;
    InitiateAuthenticationCommand command;

    @Mock
    ChallengeGenerationPort challengeGenerator;

    @Mock
    ChallengeRepository challengeRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        defaultChallenge = new Challenge(
                UUID.randomUUID(),
                new byte[]{123},
                challengeTtl,
                clock
        );

        when(challengeGenerator.generate(clock,challengeTtl)).thenReturn(defaultChallenge);

        useCase = new InitiateAuthenticationUseCase(challengeGenerator, clock, challengeTtl, challengeRepository);
        command = new InitiateAuthenticationCommand();
    }

    @Test
    void should_generate_challenge() {
        var useCase = new InitiateAuthenticationUseCase(challengeGenerator, clock, challengeTtl, challengeRepository);
        var command = new InitiateAuthenticationCommand();
        useCase.handle(command);

        verify(challengeGenerator).generate(clock, challengeTtl);
    }

    @Test
    void should_persist_generated_challenge() {
        useCase.handle(command);

        verify(challengeRepository).save(defaultChallenge);
    }
}