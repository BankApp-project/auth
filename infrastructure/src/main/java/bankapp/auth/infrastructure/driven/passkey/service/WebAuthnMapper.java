package bankapp.auth.infrastructure.driven.passkey.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.AuthenticatorTransport;
import com.webauthn4j.data.extension.client.AuthenticationExtensionsClientOutputs;
import com.webauthn4j.data.extension.client.RegistrationExtensionClientOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class WebAuthnMapper {

    private final ObjectConverter objectConverter;

    public AuthenticationExtensionsClientOutputs<RegistrationExtensionClientOutput> mapToClientExtensions(Map<String, Object> extensions) {
        var extensionsString = objectConverter.getJsonConverter().writeValueAsString(extensions);

        return objectConverter.getJsonConverter().readValue(
                extensionsString,
                new TypeReference<AuthenticationExtensionsClientOutputs<RegistrationExtensionClientOutput>>() {
                });
    }

    public Set<AuthenticatorTransport> mapToWebAuthnTransports(List<bankapp.auth.application.shared.enums.AuthenticatorTransport> transports) {
        throw new UnsupportedOperationException(); //not implemented yet!
    }
}
