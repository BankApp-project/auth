package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.application.shared.exception.MaliciousCounterException;
import bankapp.auth.application.shared.port.out.PasskeyVerificationPort;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.infrastructure.driven.passkey.exception.AuthenticationConfirmAttemptException;
import bankapp.auth.infrastructure.driven.passkey.exception.RegistrationConfirmAttemptException;
import com.webauthn4j.util.exception.WebAuthnException;
import com.webauthn4j.verifier.exception.MaliciousCounterValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasskeyVerificationService implements PasskeyVerificationPort {

    private final PasskeyRegistrationHandler passkeyRegistrationHandler;
    private final PasskeyAuthenticationHandler passkeyAuthenticationHandler;

    @Override
    public Passkey handleRegistration(String registrationResponseJSON, Session sessionData) {
        try {
            return passkeyRegistrationHandler.handle(registrationResponseJSON, sessionData);
        } catch (WebAuthnException e) {
            throw new RegistrationConfirmAttemptException("Confirmation of registration attempt failed.");
        }
    }

    @Override
    public Passkey handleAuthentication(String authenticationResponseJSON, Session sessionData, Passkey passkey) throws MaliciousCounterException {
        try {
            return passkeyAuthenticationHandler.handle(
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
