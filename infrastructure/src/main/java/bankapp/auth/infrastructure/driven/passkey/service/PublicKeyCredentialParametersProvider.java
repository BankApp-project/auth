package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;
import com.webauthn4j.data.PublicKeyCredentialParameters;
import com.webauthn4j.data.PublicKeyCredentialType;
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier;

import java.util.List;

public class PublicKeyCredentialParametersProvider {

    public static List<PublicKeyCredentialCreationOptions.PublicKeyCredentialParameters> getDomainPubKeyCredParams() {
        var pubKeyCredParamES256 = new PublicKeyCredentialCreationOptions.PublicKeyCredentialParameters("public-key", -7);
        var pubKeyCredParamRS256 = new PublicKeyCredentialCreationOptions.PublicKeyCredentialParameters("public-key", -257);

        return List.of(pubKeyCredParamES256, pubKeyCredParamRS256);
    }

    public static List<PublicKeyCredentialParameters> getInfraPubKeyCredParams() {
        PublicKeyCredentialType type = PublicKeyCredentialType.PUBLIC_KEY;
        PublicKeyCredentialParameters ES256 = new PublicKeyCredentialParameters(type, COSEAlgorithmIdentifier.ES256);
        PublicKeyCredentialParameters RS256 = new PublicKeyCredentialParameters(type, COSEAlgorithmIdentifier.RS256);

        return List.of(ES256, RS256);
    }
}
