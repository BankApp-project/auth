package bankapp.auth.domain.service;

import bankapp.auth.application.verify_otp.port.out.ChallengeGenerationPort;
import bankapp.auth.application.verify_otp.port.out.dto.LoginResponse;
import bankapp.auth.application.verify_otp.port.out.dto.RegistrationResponse;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.domain.model.dto.PublicKeyCredentialRequestOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PasskeyOptionsServiceImpl implements PasskeyOptionsService{

    //"smartphone" for smartphone-first userflow
    //anything else / "default" for default userflow
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

    public LoginResponse getLoginResponse() {
        byte[] challenge = challengeGenerator.generate();
        return new LoginResponse(new PublicKeyCredentialRequestOptions(challenge, null, null, null, null, null));
    }

    public RegistrationResponse getRegistrationResponse(User user) {
        String name = user.getEmail().getValue();
        UUID userId = user.getId();

        byte[] challenge = challengeGenerator.generate();
        byte[] userHandle = ByteArrayUtil.uuidToBytes(userId);

        var userEntity = new PublicKeyCredentialCreationOptions.PublicKeyCredentialUserEntity(userHandle, name, name);
        var rp = new PublicKeyCredentialCreationOptions.PublicKeyCredentialRpEntity(rpId, rpId);

        var pubKeyCredParamList = getPublicKeyCredentialParametersList();

        /*
         *    this criteria is to make sure that user will be verificated at new credential registration
         *    according to: https://www.w3.org/TR/webauthn-3/#dom-authenticatorselectioncriteria-residentkey
         */
        String authAttach = "";
        List<String> hints = new ArrayList<>();
        if (authMode.equals("smartphone")) {
            authAttach = "cross-platform";
            hints.add("hybrid");
        }
        var authSelectionCrit = new PublicKeyCredentialCreationOptions.AuthenticatorSelectionCriteria(
                authAttach,
                true,
                "required"
        );

        return new RegistrationResponse(
                new PublicKeyCredentialCreationOptions(
                        rp,
                        userEntity,
                        challenge,
                        pubKeyCredParamList,
                        timeout,
                        null,
                        authSelectionCrit,
                        hints,
                        null,
                        null,
                        null
                ));
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
