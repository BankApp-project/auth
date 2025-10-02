package bankapp.auth.application.shared.port.out.service;

import bankapp.auth.application.shared.port.out.dto.Challenge;

//ttl and clock should be injected into implementation
public interface ChallengeGenerationPort {
    Challenge generate();
}