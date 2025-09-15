package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.domain.model.Passkey;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RegistrationConfirmationHandler {

    private final WebAuthnManager webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();

    private final RegistrationParametersProvider registrationParametersProvider;
    private final RegistrationDataMapper registrationDataMapper;

    public Passkey handle(String registrationResponseJSON, Session sessionData) {
        var registrationParameters = getRegistrationParameters(sessionData);

        var registrationData = verifyRegistrationResponse(registrationResponseJSON, registrationParameters);

        return mapToPasskey(sessionData, registrationData);
    }

    private RegistrationParameters getRegistrationParameters(Session sessionData) {
        return registrationParametersProvider.getRegistrationParameters(sessionData);
    }

    private RegistrationData verifyRegistrationResponse(String registrationResponseJSON, RegistrationParameters registrationParameters) {
        return webAuthnManager.verifyRegistrationResponseJSON(registrationResponseJSON, registrationParameters);
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
