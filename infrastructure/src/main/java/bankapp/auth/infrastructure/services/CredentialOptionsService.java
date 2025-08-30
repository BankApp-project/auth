package bankapp.auth.infrastructure.services;

import bankapp.auth.application.shared.enums.AuthMode;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.User;
import bankapp.auth.application.shared.enums.UserVerificationRequirement;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialDescriptor;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.application.shared.service.ByteArrayUtil;
import bankapp.auth.application.verification_complete.port.out.CredentialOptionsPort;
import bankapp.auth.domain.model.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CredentialOptionsService implements CredentialOptionsPort {

    private final AuthMode authMode;
    private final String rpId;
    private final long timeout;
    private final static String PASSKEY_TYPE = "public-key";

    public CredentialOptionsService(
            AuthMode authMode,
            String rpId,
            long timeout
    ) {
        this.authMode = authMode;
        this.rpId = rpId;
        this.timeout = timeout;
    }

    @Override
    public PublicKeyCredentialRequestOptions getPasskeyRequestOptions(Challenge challenge) {
        return getPasskeyRequestOptions(null, challenge);
    }

    // TODO should calculate ttl based on challenge.expirationTime()
    public PublicKeyCredentialRequestOptions getPasskeyRequestOptions(@Nullable List<Passkey> userCredentials, Challenge challenge) {
        return new PublicKeyCredentialRequestOptions(
                challenge.challenge(),
                timeout,
                rpId,
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
            var credentialDescriptor = new PublicKeyCredentialDescriptor(
                    PASSKEY_TYPE,
                    credential.getId(),
                    credential.getTransports()
            );
            res.add(credentialDescriptor);
        }
        return res;
    }

    // TODO should calculate ttl based on challenge.expirationTime()
    public PublicKeyCredentialCreationOptions getPasskeyCreationOptions(User user, Challenge challenge) {
        String userDisplayName = user.getEmail().getValue();

        byte[] userHandle = getUserHandle(user.getId());

        return new PublicKeyCredentialCreationOptions(
                        getRpEntity(),
                        getUserEntity(userHandle, userDisplayName),
                        challenge.challenge(),
                        getPublicKeyCredentialParametersList(),
                        timeout,
                        new ArrayList<>(),
                        getAuthenticatorSelectionCriteria(),
                        getHints(),
                        "none",
                        new ArrayList<>(),
                        null
                        );
    }



    private PublicKeyCredentialCreationOptions.PublicKeyCredentialRpEntity getRpEntity() {
        return new PublicKeyCredentialCreationOptions.PublicKeyCredentialRpEntity(rpId, rpId);
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
        var pubKeyCredParamRS256 = new PublicKeyCredentialCreationOptions.PublicKeyCredentialParameters("public-key",-257);

        return List.of(pubKeyCredParamES256, pubKeyCredParamRS256);
    }

    /**
     *    this criteria is to make sure that user will be verificated at new credential registration
     *    according to: <a href="https://www.w3.org/TR/webauthn-3/#dom-authenticatorselectioncriteria-residentkey">W3 Docs</a>
     **/
    private PublicKeyCredentialCreationOptions.AuthenticatorSelectionCriteria getAuthenticatorSelectionCriteria() {
        String authAttach = authMode.equals(AuthMode.SMARTPHONE) ? "cross-platform" : "";

        return new PublicKeyCredentialCreationOptions.AuthenticatorSelectionCriteria(
                authAttach,
                true,
                UserVerificationRequirement.REQUIRED
        );
    }

    private List<String> getHints() {
        return authMode.equals(AuthMode.SMARTPHONE) ? List.of("hybrid") : List.of("");
    }
}