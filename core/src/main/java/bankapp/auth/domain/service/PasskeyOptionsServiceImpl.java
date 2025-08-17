package bankapp.auth.domain.service;

import bankapp.auth.application.verify_otp.port.out.ChallengeGenerationPort;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.domain.model.dto.PublicKeyCredentialRequestOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PasskeyOptionsServiceImpl implements PasskeyOptionsService{

    //"smartphone" for smartphone-first userflow
    //"default" for default userflow
    //it can be enum later on
    private final String authMode;
    private final String rpId;
    private final long timeout;

    private final ChallengeGenerationPort challengeGenerator;

    public PasskeyOptionsServiceImpl(String authMode, String rpId, long timeout, ChallengeGenerationPort challengeGenerator) {
        this.authMode = authMode;
        this.rpId = rpId;
        this.timeout = timeout;
        this.challengeGenerator = challengeGenerator;
    }

    public PublicKeyCredentialRequestOptions getPasskeyRequestOptions() {
        byte[] challenge = getChallenge();
        return new PublicKeyCredentialRequestOptions(challenge, null, null, null, null, null);
    }

    public PublicKeyCredentialCreationOptions getPasskeyCreationOptions(User user) {
        String name = user.getEmail().getValue();

        UUID userId = user.getId();
        byte[] userHandle = getUserHandle(userId);

        return new PublicKeyCredentialCreationOptions(
                        getRpEntity(),
                        getUserEntity(userHandle, name),
                        getChallenge(),
                        getPublicKeyCredentialParametersList(),
                        timeout,
                        null,
                        getAuthenticatorSelectionCriteria(),
                        getHints(),
                        null,
                        null,
                        null
                );
    }

    private PublicKeyCredentialCreationOptions.PublicKeyCredentialRpEntity getRpEntity() {
        return new PublicKeyCredentialCreationOptions.PublicKeyCredentialRpEntity(rpId, rpId);
    }

    private byte[] getUserHandle(UUID userId) {
        return ByteArrayUtil.uuidToBytes(userId);
    }

    private List<String> getHints() {
        List<String> hints = new ArrayList<>();
        if (authMode.equals("smartphone")) {
            hints.add("hybrid");
        }
        return hints;
    }

    /**
     *    this criteria is to make sure that user will be verificated at new credential registration
     *    according to: <a href="https://www.w3.org/TR/webauthn-3/#dom-authenticatorselectioncriteria-residentkey">W3 Docs</a>
     **/
    private PublicKeyCredentialCreationOptions.AuthenticatorSelectionCriteria getAuthenticatorSelectionCriteria() {
        String authAttach = "";
        if (authMode.equals("smartphone")) {
            authAttach = "cross-platform";
        }

        return new PublicKeyCredentialCreationOptions.AuthenticatorSelectionCriteria(
                authAttach,
                true,
                "required"
        );
    }

    private PublicKeyCredentialCreationOptions.PublicKeyCredentialUserEntity getUserEntity(byte[] userHandle, String name) {
        return new PublicKeyCredentialCreationOptions.PublicKeyCredentialUserEntity(userHandle, name, name);
    }


    private byte[] getChallenge() {
        return challengeGenerator.generate();
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
}