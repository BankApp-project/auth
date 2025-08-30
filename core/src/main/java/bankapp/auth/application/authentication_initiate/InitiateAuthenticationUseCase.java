package bankapp.auth.application.authentication_initiate;

import bankapp.auth.application.shared.UseCase;
import bankapp.auth.application.shared.port.out.persistance.ChallengeRepository;
import bankapp.auth.application.verification_complete.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verification_complete.port.out.CredentialOptionsPort;
import bankapp.auth.application.verification_complete.port.out.dto.LoginResponse;

@UseCase
public class InitiateAuthenticationUseCase {

    private final ChallengeGenerationPort challengeGenerator;
    private final ChallengeRepository challengeRepository;
    private final CredentialOptionsPort credentialOptionsService;

    public InitiateAuthenticationUseCase(
            ChallengeGenerationPort challengeGenerator,
            ChallengeRepository challengeRepository,
            CredentialOptionsPort credentialOptionsService) {
        this.challengeGenerator = challengeGenerator;
        this.challengeRepository = challengeRepository;
        this.credentialOptionsService = credentialOptionsService;
    }

    public LoginResponse handle(InitiateAuthenticationCommand command) {
        var challenge = challengeGenerator.generate();

        challengeRepository.save(challenge);

        var passkeyRequestOptions = credentialOptionsService.getPasskeyRequestOptions(challenge);
        return new LoginResponse(passkeyRequestOptions, challenge.sessionId());
    }
}
