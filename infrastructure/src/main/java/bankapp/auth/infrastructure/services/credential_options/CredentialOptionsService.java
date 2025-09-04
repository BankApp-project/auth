package bankapp.auth.infrastructure.services.credential_options;

import bankapp.auth.application.shared.enums.AuthMode;
import bankapp.auth.application.shared.enums.UserVerificationRequirement;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialDescriptor;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.application.shared.service.ByteArrayUtil;
import bankapp.auth.application.verification_complete.port.out.CredentialOptionsPort;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.annotations.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
//todo read carefully this whole class
public class CredentialOptionsService implements CredentialOptionsPort {

    private final static String PASSKEY_TYPE = "public-key";

    private final CredentialOptionsProperties properties;
    private final Clock clock;


    @Override
    public PublicKeyCredentialRequestOptions getPasskeyRequestOptions(Challenge challenge) {
        return getPasskeyRequestOptions(null, challenge);
    }

    public PublicKeyCredentialRequestOptions getPasskeyRequestOptions(@Nullable List<Passkey> userCredentials, Challenge challenge) {
        long timeoutMillis = getTimeoutMillis(challenge.expirationTime());
        return new PublicKeyCredentialRequestOptions(
                challenge.value(),
                timeoutMillis,
                properties.rpId(),
                getAllowedCredentials(userCredentials),
                UserVerificationRequirement.REQUIRED,
                null
        );
    }

    private List<PublicKeyCredentialDescriptor> getAllowedCredentials(@Nullable List<Passkey> userCredentials) {
        List<PublicKeyCredentialDescriptor> res = new ArrayList<>();
        if (userCredentials == null) {
            return res;
        }
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

    public PublicKeyCredentialCreationOptions getPasskeyCreationOptions(User user, Challenge challenge) {

        long timeoutMillis = getTimeoutMillis(challenge.expirationTime());

        String userDisplayName = user.getEmail().getValue();

        byte[] userHandle = getUserHandle(user.getId());

        return new PublicKeyCredentialCreationOptions(
                getRpEntity(),
                getUserEntity(userHandle, userDisplayName),
                challenge.value(),
                getPublicKeyCredentialParametersList(),
                timeoutMillis,
                new ArrayList<>(),
                getAuthenticatorSelectionCriteria(),
                getHints(),
                "none",
                new ArrayList<>(),
                null
        );
    }

    private long getTimeoutMillis(Instant expirationTime) {
        var timeout = Duration.between(Instant.now(clock), expirationTime);
        return timeout.toMillis();
    }

    private PublicKeyCredentialCreationOptions.PublicKeyCredentialRpEntity getRpEntity() {
        return new PublicKeyCredentialCreationOptions.PublicKeyCredentialRpEntity(properties.rpId(), properties.rpId());
    }

    private PublicKeyCredentialCreationOptions.PublicKeyCredentialUserEntity getUserEntity(byte[] userHandle, String name) {
        return new PublicKeyCredentialCreationOptions.PublicKeyCredentialUserEntity(userHandle, name, name);
    }

    private byte[] getUserHandle(UUID userId) {
        return ByteArrayUtil.uuidToBytes(userId);
    }

    /**
     * these parameters are default according to official documentation of webauthn:
     * <a href="https://www.w3.org/TR/webauthn-3/#dictdef-publickeycredentialparameters">...</a>
     **/
    private List<PublicKeyCredentialCreationOptions.PublicKeyCredentialParameters> getPublicKeyCredentialParametersList() {
        var pubKeyCredParamES256 = new PublicKeyCredentialCreationOptions.PublicKeyCredentialParameters("public-key", -7);
        var pubKeyCredParamRS256 = new PublicKeyCredentialCreationOptions.PublicKeyCredentialParameters("public-key", -257);

        return List.of(pubKeyCredParamES256, pubKeyCredParamRS256);
    }

    /**
     * this criteria is to make sure that user will be verificated at new credential registration
     * according to: <a href="https://www.w3.org/TR/webauthn-3/#dom-authenticatorselectioncriteria-residentkey">W3 Docs</a>
     **/
    private PublicKeyCredentialCreationOptions.AuthenticatorSelectionCriteria getAuthenticatorSelectionCriteria() {
        String authAttach = properties.authMode().equals(AuthMode.SMARTPHONE) ? "cross-platform" : "";

        return new PublicKeyCredentialCreationOptions.AuthenticatorSelectionCriteria(
                authAttach,
                true,
                UserVerificationRequirement.REQUIRED
        );
    }

    private List<String> getHints() {
        return properties.authMode().equals(AuthMode.SMARTPHONE) ? List.of("hybrid") : List.of("");
    }
}