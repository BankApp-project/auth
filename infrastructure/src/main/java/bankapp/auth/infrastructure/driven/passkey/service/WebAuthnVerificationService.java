package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.application.shared.exception.MaliciousCounterException;
import bankapp.auth.application.shared.port.out.WebAuthnVerificationPort;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.domain.model.Passkey;
import com.webauthn4j.util.exception.WebAuthnException;
import com.webauthn4j.verifier.exception.MaliciousCounterValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebAuthnVerificationService implements WebAuthnVerificationPort {

    private final RegistrationConfirmationHandler registrationConfirmationHandler;
    private final AuthenticationConfirmationHandler authenticationConfirmationHandler;

    @Override
    public Passkey handleRegistrationConfirmation(String registrationResponseJSON, Session sessionData) {
        try {
            return registrationConfirmationHandler.handle(registrationResponseJSON, sessionData);
        } catch (WebAuthnException e) {
            throw new RegistrationConfirmAttemptException("Confirmation of registration attempt failed.");
        }
    }

    @Override
    public Passkey handleAuthenticationConfirmation(String authenticationResponseJSON, Session sessionData, Passkey passkey) throws MaliciousCounterException {
        try {
            return authenticationConfirmationHandler.handle(
                    authenticationResponseJSON,
                    sessionData,
                    passkey
            );
        } catch (MaliciousCounterValueException e) {
            throw new MaliciousCounterException("Malicious counter value detected in authentication response.");
        } catch (WebAuthnException e) {
            throw new AuthenticationConfirmAttemptException("Authentication attempt failed", e);
        }
    }
}
