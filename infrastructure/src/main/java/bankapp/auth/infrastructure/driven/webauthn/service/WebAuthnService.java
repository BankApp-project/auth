package bankapp.auth.infrastructure.driven.webauthn.service;

import bankapp.auth.application.shared.port.out.WebAuthnPort;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.PasskeyRegistrationData;
import bankapp.auth.domain.model.Passkey;
import com.webauthn4j.WebAuthnManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebAuthnService implements WebAuthnPort {

    private final WebAuthnManager webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();

    @Override
    public PasskeyRegistrationData confirmRegistrationChallenge(String registrationResponseJSON, Challenge challengeData) {
        throw new RegistrationConfirmAttemptException("Confirmation of registration attempt failed.");
    }

    @Override
    public Passkey confirmAuthenticationChallenge(String authenticationResponseJSON, Challenge challengeData, Passkey passkey) {
        return null;
    }
}
