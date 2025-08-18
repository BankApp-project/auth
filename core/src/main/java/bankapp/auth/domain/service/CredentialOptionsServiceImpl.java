package bankapp.auth.domain.service;

import bankapp.auth.domain.model.enums.AuthMode;
import bankapp.auth.domain.model.CredentialRecord;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.enums.UserVerificationRequirement;
import bankapp.auth.domain.model.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.domain.model.dto.PublicKeyCredentialDescriptor;
import bankapp.auth.domain.model.dto.PublicKeyCredentialRequestOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CredentialOptionsServiceImpl implements CredentialOptionsService {

    private final AuthMode authMode;
    private final String rpId;
    private final long timeout;

    public CredentialOptionsServiceImpl(
            AuthMode authMode,
            String rpId,
            long timeout
    ) {
        this.authMode = authMode;
        this.rpId = rpId;
        this.timeout = timeout;
    }

    public PublicKeyCredentialRequestOptions getPasskeyRequestOptions(User user, List<CredentialRecord> userCredentials, byte[] challenge) {
        return new PublicKeyCredentialRequestOptions(
                challenge,
                timeout,
                rpId,
                getAllowedCredentials(userCredentials),
                UserVerificationRequirement.REQUIRED,
                null
        );
    }

    private List<PublicKeyCredentialDescriptor> getAllowedCredentials(List<CredentialRecord> userCredentials) {
        List<PublicKeyCredentialDescriptor> res = new ArrayList<>();
        for (var credential : userCredentials) {
            var credentialDescriptor = new PublicKeyCredentialDescriptor(
                    credential.type(),
                    credential.id(),
                    credential.transports()
            );
            res.add(credentialDescriptor);
        }
        return res;
    }

    public PublicKeyCredentialCreationOptions getPasskeyCreationOptions(User user, byte[] challenge) {
        String userDisplayName = user.getEmail().getValue();

        byte[] userHandle = getUserHandle(user.getId());

        return new PublicKeyCredentialCreationOptions(
                        getRpEntity(),
                        getUserEntity(userHandle, userDisplayName),
                        challenge,
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