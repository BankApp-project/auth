package bankapp.auth.infrastructure.driven.passkey.service.verification.registration;

import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.infrastructure.driven.passkey.exception.RegistrationConfirmAttemptException;
import com.webauthn4j.WebAuthnRegistrationManager;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasskeyRegistrationHandler {

    private final WebAuthnRegistrationManager registrationManager;

    private final RegistrationParametersProvider registrationParametersProvider;
    private final RegistrationDataMapper registrationDataMapper;

    public Passkey handle(String registrationResponseJSON, Session sessionData) {
        log.debug("Processing passkey registration response.");

        var registrationParameters = getRegistrationParameters(sessionData);

        var registrationData = verifyRegistrationResponse(registrationResponseJSON, registrationParameters);

        Passkey passkey = mapToPasskey(sessionData, registrationData);
        log.debug("Successfully processed passkey registration response.");
        return passkey;
    }

    private RegistrationParameters getRegistrationParameters(Session sessionData) {
        return registrationParametersProvider.getRegistrationParameters(sessionData);
    }

    private RegistrationData verifyRegistrationResponse(String registrationResponseJSON, RegistrationParameters registrationParameters) {
        return registrationManager.verify(registrationResponseJSON, registrationParameters);
    }

    private Passkey mapToPasskey(Session sessionData, RegistrationData registrationData) {
        return registrationDataMapper.toDomainEntity(registrationData, getUserId(sessionData));
    }

    private UUID getUserId(Session sessionData) {
        return sessionData.userId().orElseThrow(
                () -> new RegistrationConfirmAttemptException("User ID is missing in session data")
        );
    }
}
