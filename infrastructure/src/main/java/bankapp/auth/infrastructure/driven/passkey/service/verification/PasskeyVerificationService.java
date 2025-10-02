package bankapp.auth.infrastructure.driven.passkey.service.verification;

import bankapp.auth.application.shared.exception.MaliciousCounterException;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.port.out.service.PasskeyVerificationPort;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.infrastructure.driven.passkey.exception.AuthenticationConfirmAttemptException;
import bankapp.auth.infrastructure.driven.passkey.exception.RegistrationConfirmAttemptException;
import bankapp.auth.infrastructure.driven.passkey.service.verification.authentication.PasskeyAuthenticationHandler;
import bankapp.auth.infrastructure.driven.passkey.service.verification.registration.PasskeyRegistrationHandler;
import com.webauthn4j.util.exception.WebAuthnException;
import com.webauthn4j.verifier.exception.MaliciousCounterValueException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasskeyVerificationService implements PasskeyVerificationPort {

    private final PasskeyRegistrationHandler passkeyRegistrationHandler;
    private final PasskeyAuthenticationHandler passkeyAuthenticationHandler;

    @Override
    public Passkey handleRegistration(String registrationResponseJSON, Session sessionData) {
        log.info("Handling passkey registration verification.");

        try {
            Passkey passkey = passkeyRegistrationHandler.handle(registrationResponseJSON, sessionData);
            log.info("Successfully verified passkey registration.");
            return passkey;
        } catch (WebAuthnException e) {
            log.error("Failed to verify passkey registration", e);
            throw new RegistrationConfirmAttemptException("Confirmation of registration attempt failed.", e);
        }
    }

    @Override
    public Passkey handleAuthentication(String authenticationResponseJSON, Session sessionData, Passkey passkey) throws MaliciousCounterException {
        log.info("Handling passkey authentication verification.");

        try {
            Passkey updatedPasskey = passkeyAuthenticationHandler.handle(
                    authenticationResponseJSON,
                    sessionData,
                    passkey
            );
            log.info("Successfully verified passkey authentication.");
            return updatedPasskey;
        } catch (MaliciousCounterValueException e) {
            log.error("Malicious counter value detected for credential ID: {}", passkey.getId(), e);
            throw new MaliciousCounterException("Malicious counter value detected in authentication response.");
        } catch (WebAuthnException e) {
            log.error("Failed to verify passkey authentication for credential ID: {}", passkey.getId(), e);
            throw new AuthenticationConfirmAttemptException("Authentication attempt failed", e);
        }
    }
}
