package bankapp.auth.infrastructure.driven.passkey.service.options;

import bankapp.auth.application.shared.enums.UserVerificationRequirement;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialDescriptor;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.service.ByteArrayUtil;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.annotations.Nullable;
import bankapp.auth.infrastructure.driven.passkey.config.PasskeyRpProperties;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Assembles the PublicKeyCredentialRequestOptions object for passkey authentication.
 * This is a plain Java class, not a Spring component.
 */
@RequiredArgsConstructor
class PasskeyRequestOptionsAssembler {

    private final static String PASSKEY_TYPE = "public-key";

    private final PasskeyRpProperties properties;
    private final Clock clock;

    public PublicKeyCredentialRequestOptions assemble(@Nullable List<Passkey> userCredentials, Session session) {
        long timeoutMillis = getTimeoutMillis(session.challenge().expirationTime());

        return new PublicKeyCredentialRequestOptions(
                session.challenge().challenge(),
                timeoutMillis,
                properties.rpId(),
                getAllowedCredentials(userCredentials),
                UserVerificationRequirement.REQUIRED,
                getExtensions()
        );
    }

    private long getTimeoutMillis(Instant expirationTime) {
        return Duration.between(Instant.now(clock), expirationTime).toMillis();
    }

    private List<PublicKeyCredentialDescriptor> getAllowedCredentials(@Nullable List<Passkey> userCredentials) {
        if (userCredentials == null) {
            return new ArrayList<>();
        }
        List<PublicKeyCredentialDescriptor> res = new ArrayList<>();
        for (var credential : userCredentials) {
            var idBytes = ByteArrayUtil.uuidToBytes(credential.getId());
            var credentialDescriptor = new PublicKeyCredentialDescriptor(
                    PASSKEY_TYPE,
                    idBytes,
                    credential.getTransports()
            );
            res.add(credentialDescriptor);
        }
        return res;
    }

    private Map<String, Object> getExtensions() {
        return null;
    }
}
