package bankapp.auth.infrastructure.driven.passkey.service.credential.options;

import bankapp.auth.application.shared.enums.AuthMode;
import bankapp.auth.application.shared.enums.UserVerificationRequirement;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialDescriptor;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.service.ByteArrayUtil;
import bankapp.auth.domain.model.User;
import bankapp.auth.infrastructure.driven.passkey.config.PasskeyRpProperties;
import bankapp.auth.infrastructure.driven.passkey.service.PublicKeyCredentialParametersProvider;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Assembles the PublicKeyCredentialCreationOptions object for new passkey registration.
 * This is a plain Java class, not a Spring component.
 */
@RequiredArgsConstructor
class PasskeyCreationOptionsAssembler {

    private final PasskeyRpProperties properties;
    private final Clock clock;

    public PublicKeyCredentialCreationOptions assemble(User user, Session session) {
        long timeoutMillis = getTimeoutMillis(session.challenge().expirationTime());

        String userDisplayName = user.getEmail().getValue();
        byte[] userHandle = getUserHandle(user.getId());

        return new PublicKeyCredentialCreationOptions(
                getRpEntity(),
                getUserEntity(userHandle, userDisplayName),
                session.challenge().challenge(),
                getPublicKeyCredentialParametersList(),
                timeoutMillis,
                getExcludeCredentials(),
                getAuthenticatorSelectionCriteria(),
                getHints(),
                getAttestation(),
                getAttestationFormats(),
                getExtensions()
        );
    }

    private long getTimeoutMillis(Instant expirationTime) {
        return Duration.between(Instant.now(clock), expirationTime).toMillis();
    }

    private byte[] getUserHandle(UUID userId) {
        return ByteArrayUtil.uuidToBytes(userId);
    }

    private PublicKeyCredentialCreationOptions.PublicKeyCredentialRpEntity getRpEntity() {
        return new PublicKeyCredentialCreationOptions.PublicKeyCredentialRpEntity(properties.rpId(), properties.rpId());
    }

    private PublicKeyCredentialCreationOptions.PublicKeyCredentialUserEntity getUserEntity(byte[] userHandle, String name) {
        return new PublicKeyCredentialCreationOptions.PublicKeyCredentialUserEntity(userHandle, name, name);
    }

    /**
     * These parameters are default according to official documentation of webauthn:
     * <a href="https://www.w3.org/TR/webauthn-3/#dictdef-publickeycredentialparameters">...</a>
     */
    private List<PublicKeyCredentialCreationOptions.PublicKeyCredentialParameters> getPublicKeyCredentialParametersList() {
        return PublicKeyCredentialParametersProvider.getDomainPubKeyCredParams();
    }

    private ArrayList<PublicKeyCredentialDescriptor> getExcludeCredentials() {
        //todo this should be populated when existing user will add new device. OOS for now.
        return new ArrayList<>();
    }

    /**
     * This criteria is to make sure that user will be verified at new credential registration
     * according to: <a href="https://www.w3.org/TR/webauthn-3/#dom-authenticatorselectioncriteria-residentkey">W3 Docs</a>
     */
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

    private String getAttestation() {
        return "none";
    }

    private ArrayList<String> getAttestationFormats() {
        return new ArrayList<>();
    }

    private Map<String, Object> getExtensions() {
        return null;
    }
}
