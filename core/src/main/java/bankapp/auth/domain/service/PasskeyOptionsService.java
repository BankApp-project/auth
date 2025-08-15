package bankapp.auth.domain.service;

import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.domain.model.dto.PublicKeyCredentialRequestOptions;

public interface PasskeyOptionsService {
    PublicKeyCredentialCreationOptions getPasskeyCreationOptions(User user);
    PublicKeyCredentialRequestOptions getPasskeyRequestOptions();
}
