package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.domain.model.Passkey;
import com.webauthn4j.converter.AttestationObjectConverter;
import com.webauthn4j.converter.CollectedClientDataConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.credential.CredentialRecord;
import com.webauthn4j.credential.CredentialRecordImpl;
import com.webauthn4j.data.AuthenticatorTransport;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.client.CollectedClientData;
import com.webauthn4j.data.extension.client.AuthenticationExtensionsClientOutputs;
import com.webauthn4j.data.extension.client.RegistrationExtensionClientOutput;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class PasskeyToCredentialRecordMapper {

    //make is as bean
    private final ObjectConverter objectConverter = new ObjectConverter();
    private final AttestationObjectConverter attestationObjectConverter = new AttestationObjectConverter(objectConverter);
    private final CollectedClientDataConverter collectedClientDataConverter = new CollectedClientDataConverter(objectConverter);


    public CredentialRecord from(Passkey source) {

        AttestationObject attestationObject = attestationObjectConverter.convert(source.getAttestationObject());
        CollectedClientData collectedClientData = collectedClientDataConverter.convert(source.getAttestationClientDataJSON());

        return new CredentialRecordImpl(attestationObject, collectedClientData, parseClientExtensions(source.getExtensions()), parseTransports(source.getTransports()));
    }

    private AuthenticationExtensionsClientOutputs<RegistrationExtensionClientOutput> parseClientExtensions(Map<String, Object> extensions) {
        throw new UnsupportedOperationException(); //not implemented yet!
    }

    private Set<AuthenticatorTransport> parseTransports(List<bankapp.auth.application.shared.enums.AuthenticatorTransport> transports) {
        throw new UnsupportedOperationException(); //not implemented yet!
    }
}
