package bankapp.auth.infrastructure.services.credential_options;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialCreationOptions;
import bankapp.auth.application.shared.port.out.dto.PublicKeyCredentialRequestOptions;
import bankapp.auth.application.verification.complete.port.out.CredentialOptionsPort;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.annotations.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

/**
 * A service that acts as a facade to generate credential options for WebAuthn ceremonies.
 * It delegates the complex construction logic to specialized assembler classes.
 */
@Service
@RequiredArgsConstructor
public class CredentialOptionsService implements CredentialOptionsPort {

    private final CredentialOptionsProperties properties;
    private final Clock clock;

    /**
     * {@inheritDoc}
     */
    @Override
    public PublicKeyCredentialCreationOptions getPasskeyCreationOptions(User user, Challenge challenge) {
        PasskeyCreationOptionsAssembler assembler = new PasskeyCreationOptionsAssembler(properties, clock);
        return assembler.assemble(user, challenge);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PublicKeyCredentialRequestOptions getPasskeyRequestOptions(Challenge challenge) {
        return getPasskeyRequestOptions(null, challenge);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PublicKeyCredentialRequestOptions getPasskeyRequestOptions(@Nullable List<Passkey> userCredentials, Challenge challenge) {
        PasskeyRequestOptionsAssembler assembler = new PasskeyRequestOptionsAssembler(properties, clock);
        return assembler.assemble(userCredentials, challenge);
    }
}