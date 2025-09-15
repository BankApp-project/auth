package bankapp.auth.infrastructure.driven.passkey.service.verification.registration;

import bankapp.auth.application.shared.enums.AuthenticatorTransport;
import bankapp.auth.domain.model.Passkey;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.authenticator.AuthenticatorData;
import com.webauthn4j.data.extension.authenticator.AuthenticationExtensionsAuthenticatorOutputs;
import com.webauthn4j.data.extension.authenticator.RegistrationExtensionAuthenticatorOutput;
import com.webauthn4j.util.UUIDUtil;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RegistrationDataMapper {


    public @NotNull Passkey toDomainEntity(@NotNull RegistrationData registrationData, @NotNull UUID userId) {
        Objects.requireNonNull(registrationData);
        Objects.requireNonNull(userId);

        var authData = getAuthData(registrationData);
        var attCredData = getAttestedCredentialData(registrationData);
        var credentialId = UUIDUtil.fromBytes(attCredData.getCredentialId());

        return new Passkey(
                credentialId,
                userId,
                "public-key",
                getPublicKey(attCredData),
                authData.getSignCount(),
                authData.isFlagUV(),
                authData.isFlagBE(),
                authData.isFlagBS(),
                getTransports(registrationData),
                getExtensions(authData),
                registrationData.getAttestationObjectBytes(),
                registrationData.getCollectedClientDataBytes()
        );
    }

    private AuthenticatorData<RegistrationExtensionAuthenticatorOutput> getAuthData(@NotNull RegistrationData registrationData) {
        Objects.requireNonNull(registrationData.getAttestationObject(), "Attestation object cannot be null");
        return registrationData.getAttestationObject().getAuthenticatorData();
    }

    private AttestedCredentialData getAttestedCredentialData(@NotNull RegistrationData registrationData) {
        var authData = getAuthData(registrationData);
        return Objects.requireNonNull(authData.getAttestedCredentialData(), "Attested credential data cannot be null");
    }

    private byte[] getPublicKey(AttestedCredentialData attCredData) {
        var publicKey = Objects.requireNonNull(attCredData.getCOSEKey().getPublicKey(), "Public key cannot be null");

        return publicKey.getEncoded();
    }

    private List<AuthenticatorTransport> getTransports(@NotNull RegistrationData registrationData) {
        var transports = Objects.requireNonNull(registrationData.getTransports(), "Transports cannot be null");

        if (transports.isEmpty()) {
            return Collections.emptyList();
        }

        return transports.stream()
                .map(t -> AuthenticatorTransport.fromValue(t.getValue()))
                .toList();
    }

    private Map<String, Object> getExtensions(AuthenticatorData<RegistrationExtensionAuthenticatorOutput> authData) {
        var extensions = authData.getExtensions();
        Objects.requireNonNull(extensions, "Extensions cannot be null");

        return extractExtensionsIntoHashMap(extensions);
    }

    private HashMap<String, Object> extractExtensionsIntoHashMap(AuthenticationExtensionsAuthenticatorOutputs<RegistrationExtensionAuthenticatorOutput> extensions) {
        HashMap<String, Object> res = new HashMap<>();

        extensions.getKeys().forEach(
                key -> res.put(key, extensions.getValue(key))
        );
        return res;
    }
}
