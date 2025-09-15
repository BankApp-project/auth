package bankapp.auth.infrastructure.driven.passkey.service.verification.authentication;

import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.service.ByteArrayUtil;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.infrastructure.driven.passkey.config.PasskeyConfiguration;
import bankapp.auth.infrastructure.utils.TestPasskeyProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.AuthenticatorTransport;
import com.webauthn4j.data.extension.client.AuthenticationExtensionsClientOutputs;
import com.webauthn4j.data.extension.client.RegistrationExtensionClientOutput;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthenticationParametersProviderTest {

    @Autowired
    private PasskeyConfiguration passkeyConfig;

    @Autowired
    private AuthenticationParametersProvider authenticationParametersProvider;

    @Autowired
    private ObjectConverter objectConverter;

    @Test
    void getAuthenticationParameters_should_return_valid_data_with_valid_parameters() {
        var sessionData = generateSessionData();
        var passkey = generatePasskey();
        var res = authenticationParametersProvider.getAuthenticationParameters(sessionData, passkey);

        assertNotNull(res, "AuthenticationParameters cannot be null");
        assertNotNull(res.getServerProperty().getChallenge(), "Challenge cannot be null");
        assertNotNull(res.getServerProperty().getChallenge().getValue(), "Challenge cannot be null");
        assertNotNull(res.getAuthenticator(), "Authenticator cannot be null");
        assertNotNull(res.getAuthenticator().getAttestedCredentialData(), "AttestedCredentialData cannot be null");
        assertNotNull(res.getServerProperty().getOrigins(), "Origins cannot be null");
        assertNotNull(res.getAuthenticator().getAttestedCredentialData().getCOSEKey().getPublicKey(), "Public Key should not be null");

        assertEquals(
                getNormalisedTransports(res),
                getNormalisedTransports(passkey),
                "Transports should match");
        assertArrayEquals(
                ByteArrayUtil.uuidToBytes(passkey.getId()),
                Arrays.copyOfRange(res.getAuthenticator().getAttestedCredentialData().getCredentialId(), 0, 16),
                "Credential ID should match");
        assertArrayEquals(
                passkey.getPublicKey(),
                res.getAuthenticator().getAttestedCredentialData().getCOSEKey().getPublicKey().getEncoded(),
                "Public keys should match");
        assertEquals(
                mapToClientExtensions(passkey.getExtensions()),
                res.getAuthenticator().getClientExtensions(),
                "Client extensions should match");
        assertEquals(
                passkey.getSignCount(),
                res.getAuthenticator().getCounter(),
                "Signature counter should match");
        assertEquals(
                passkeyConfig.userPresenceRequired(),
                res.isUserPresenceRequired(),
                "User presence required should match"
        );
        assertEquals(
                passkeyConfig.userVerificationRequired(),
                res.isUserVerificationRequired(),
                "User verification required should match"
        );
        assertEquals(
                passkeyConfig.rpId(),
                res.getServerProperty().getRpId(),
                "RP ID should match"
        );
        assertArrayEquals(
                sessionData.challenge().challenge(),
                res.getServerProperty().getChallenge().getValue(),
                "Challenge should match"
        );
        assertEquals(
                getCredentialsBytes(sessionData),
                res.getAllowCredentials(),
                "Allow Credentials should match"
        );
        assertTrue(
                res.getServerProperty().getOrigins().stream()
                        .anyMatch(o -> o.equals(passkeyConfig.origin())),
                "Origin should be present in the list");

    }

    private List<byte[]> getCredentialsBytes(Session sessionData) {
        if (sessionData.credentialsIds().isEmpty()) {
            return null;
        }

        return sessionData.credentialsIds().get().stream()
                .map(ByteArrayUtil::uuidToBytes)
                .toList();
    }


    public AuthenticationExtensionsClientOutputs<RegistrationExtensionClientOutput> mapToClientExtensions(Map<String, Object> extensions) {
        var extensionsString = objectConverter.getJsonConverter().writeValueAsString(extensions);

        return objectConverter.getJsonConverter().readValue(
                extensionsString,
                new TypeReference<>() {
                });
    }

    private List<String> getNormalisedTransports(Passkey passkey) {
        return passkey.getTransports().stream()
                .map(bankapp.auth.application.shared.enums.AuthenticatorTransport::getValue)
                .toList();
    }

    private List<String> getNormalisedTransports(AuthenticationParameters res) {

        assertNotNull(res.getAuthenticator().getTransports(), "Transports cannot be null");

        return res.getAuthenticator().getTransports().stream()
                .map(AuthenticatorTransport::getValue)
                .toList();
    }

    private Passkey generatePasskey() {
        return TestPasskeyProvider.createSamplePasskey();
    }

    private Session generateSessionData() {
        return new Session(
                UUID.randomUUID(),
                generateChallenge()
        );
    }

    private Challenge generateChallenge() {
        return new Challenge(
                new byte[]{123, 123},
                Instant.now().plusSeconds(60)
        );
    }
    //check allowed credentials
    //check server property
    //check flags
    //check credentialRecord?
}