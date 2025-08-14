package bankapp.auth.application.verify_otp.port.out;

import bankapp.auth.domain.model.dto.PublicKeyCredentialCreationOptions;

public interface PublicKeyCredentialCreationOptionsProvider {

    PublicKeyCredentialCreationOptions provide();
}
