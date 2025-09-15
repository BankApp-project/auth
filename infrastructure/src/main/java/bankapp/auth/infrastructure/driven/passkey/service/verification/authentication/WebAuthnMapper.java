package bankapp.auth.infrastructure.driven.passkey.service.verification.authentication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webauthn4j.converter.AttestationObjectConverter;
import com.webauthn4j.converter.CollectedClientDataConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.AuthenticatorTransport;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.client.CollectedClientData;
import com.webauthn4j.data.extension.client.AuthenticationExtensionsClientOutputs;
import com.webauthn4j.data.extension.client.RegistrationExtensionClientOutput;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class WebAuthnMapper {

    private final ObjectConverter objectConverter;
    private final AttestationObjectConverter attestationObjectConverter;
    private final CollectedClientDataConverter collectedClientDataConverter;

    public WebAuthnMapper(ObjectConverter objectConverter) {
        this.objectConverter = objectConverter;
        this.attestationObjectConverter = new AttestationObjectConverter(objectConverter);
        this.collectedClientDataConverter = new CollectedClientDataConverter(objectConverter);
    }


    public AuthenticationExtensionsClientOutputs<RegistrationExtensionClientOutput> mapToClientExtensions(Map<String, Object> extensions) {
        var extensionsString = objectConverter.getJsonConverter().writeValueAsString(extensions);

        return objectConverter.getJsonConverter().readValue(
                extensionsString,
                new TypeReference<>() {
                });
    }

    public Set<AuthenticatorTransport> mapToWebAuthnTransports(List<bankapp.auth.application.shared.enums.AuthenticatorTransport> transports) {

        if (transports == null) {
            return new HashSet<>();
        }

        return transports.stream()
                .map(t -> AuthenticatorTransport.create(t.getValue()))
                .collect(Collectors.toSet());
    }

    public AttestationObject convertAttestationObject(byte[] attestationObject) {
        Objects.requireNonNull(attestationObject);

        return attestationObjectConverter.convert(attestationObject);
    }

    public CollectedClientData convertClientData(byte[] attestationClientDataJSON) {
        Objects.requireNonNull(attestationClientDataJSON);

        return collectedClientDataConverter.convert(attestationClientDataJSON);
    }
}
