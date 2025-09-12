package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.domain.model.Passkey;
import com.webauthn4j.credential.CredentialRecord;
import com.webauthn4j.credential.CredentialRecordImpl;
import com.webauthn4j.data.AuthenticatorTransport;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.client.CollectedClientData;
import com.webauthn4j.data.extension.client.AuthenticationExtensionsClientOutputs;
import com.webauthn4j.data.extension.client.RegistrationExtensionClientOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PasskeyToCredentialRecordMapper {

    private final WebAuthnMapper webAuthnMapper;

    public CredentialRecord from(Passkey source) {

        AttestationObject attestationObject = webAuthnMapper.convertAttestationObject(source.getAttestationObject());
        CollectedClientData collectedClientData = webAuthnMapper.convertClientData(source.getAttestationClientDataJSON());

        return new CredentialRecordImpl(
                attestationObject,
                collectedClientData,
                parseClientExtensions(source.getExtensions()),
                parseTransports(source.getTransports())
        );
    }

    private AuthenticationExtensionsClientOutputs<RegistrationExtensionClientOutput> parseClientExtensions(Map<String, Object> extensions) {
        return webAuthnMapper.mapToClientExtensions(extensions);
    }

    private Set<AuthenticatorTransport> parseTransports(List<bankapp.auth.application.shared.enums.AuthenticatorTransport> transports) {
        return webAuthnMapper.mapToWebAuthnTransports(transports);
    }
}
