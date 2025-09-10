package bankapp.auth.infrastructure.driven.passkey.service;

import bankapp.auth.application.shared.enums.AuthMode;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.service.ByteArrayUtil;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.infrastructure.driven.passkey.config.PasskeyProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CredentialOptionsServiceRegistrationFlowTest {

    private static final AuthMode DEFAULT_AUTH_MODE = AuthMode.SMARTPHONE;
    private static final String DEFAULT_RPID = "bankapp.online";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final Clock DEFAULT_CLOCK = Clock.fixed(Instant.now(), ZoneId.of("Z"));

    private static final EmailAddress DEFAULT_EMAIL = new EmailAddress("test@bankapp.online");
    public static final User TEST_USER = User.createNew(DEFAULT_EMAIL);
    private static final Challenge DEFAULT_CHALLENGE = new Challenge(
            UUID.randomUUID(),
            new byte[]{123, 123, 123, 123, 123, 123, 123, 123, 123, 123, 123, 123, 123, 123, 123, 123},
            DEFAULT_TIMEOUT,
            DEFAULT_CLOCK
    );

    CredentialOptionsService passkeyOptionsService;

    @BeforeEach
    void setup() {
        var passkeyOptionsProperties = new PasskeyProperties(
                DEFAULT_RPID,
                DEFAULT_AUTH_MODE
        );

        passkeyOptionsService = new CredentialOptionsService(
                passkeyOptionsProperties,
                DEFAULT_CLOCK
        );
    }

    @Test
    void should_return_response_with_userId_as_userHandle() {

        // When
        var res = passkeyOptionsService.getPasskeyCreationOptions(TEST_USER, DEFAULT_CHALLENGE);

        assertArrayEquals(ByteArrayUtil.uuidToBytes(TEST_USER.getId()), res.user().id());
    }

    @Test
    void should_return_response_with_at_least_16bytes_long_challenge() {

        var res = passkeyOptionsService.getPasskeyCreationOptions(TEST_USER, DEFAULT_CHALLENGE);
        byte[] challenge = res.challenge();
        assertThat(challenge).hasSizeGreaterThanOrEqualTo(16);
    }

    @Test
    void should_return_response_with_email_as_userEntity_name_and_displayName() {
        // Given
        User testUser = User.createNew(DEFAULT_EMAIL);

        // When
        var res = passkeyOptionsService.getPasskeyCreationOptions(testUser, DEFAULT_CHALLENGE);

        // Then
        String name = res.user().name();
        String displayName = res.user().displayName();

        assertEquals(DEFAULT_EMAIL.getValue(), name);
        assertEquals(DEFAULT_EMAIL.getValue(), displayName);
    }

    @Test
    void should_return_response_with_valid_rpId() {
        // Given
        User testUser = User.createNew(DEFAULT_EMAIL);

        // When
        var res = passkeyOptionsService.getPasskeyCreationOptions(testUser, DEFAULT_CHALLENGE);

        // Then
        String rpId = res.rp().id();

        assertEquals(DEFAULT_RPID, rpId);
    }

    @Test
    void should_return_response_with_valid_PublicKeyCredentialParameters() {
        // Given
        User testUser = User.createNew(DEFAULT_EMAIL);

        // When
        var res = passkeyOptionsService.getPasskeyCreationOptions(testUser, DEFAULT_CHALLENGE);

        // Then
        var pubKeyCredParams = res.pubKeyCredParams();

        //check for "public-key" type
        assertTrue(pubKeyCredParams.stream()
                .allMatch(param -> param.type().equals("public-key")));

        //check for ES256 alg
        assertTrue(pubKeyCredParams.stream()
                .anyMatch(param -> param.alg() == -7));

        //check for RS256 alg
        assertTrue(pubKeyCredParams.stream()
                .anyMatch(param -> param.alg() == -257));
    }

    @Test
    void should_return_response_with_valid_timeout() {
        // Given
        User testUser = User.createNew(DEFAULT_EMAIL);

        // When
        var res = passkeyOptionsService.getPasskeyCreationOptions(testUser, DEFAULT_CHALLENGE);

        // Then
        var timeout = res.timeout();

        assertNotNull(timeout);
        assertEquals(DEFAULT_TIMEOUT.toMillis(), timeout);
    }

    @Test
    void should_return_response_with_valid_and_secure_AuthenticatorSelectionCriteria() {
        // Given
        User testUser = User.createNew(DEFAULT_EMAIL);

        // When
        var res = passkeyOptionsService.getPasskeyCreationOptions(testUser, DEFAULT_CHALLENGE);

        // Then
        var authSelCriteria = res.authenticatorSelection();

        assertNotNull(authSelCriteria);
        assertTrue(authSelCriteria.requireResidentKey());
        assertEquals("required", authSelCriteria.userVerification().getValue());
    }

    @Test
    void should_return_response_with_correct_settings_when_DEFAULT_AUTH_MODE_flag_is_smartphone() {
        // Given
        User testUser = User.createNew(DEFAULT_EMAIL);

        // When

        var res = passkeyOptionsService.getPasskeyCreationOptions(testUser, DEFAULT_CHALLENGE);

        // Then
        var authAttach = res.authenticatorSelection().authenticatorAttachment();
        var hints = res.hints();

        assertEquals("hybrid", hints.getFirst());
        assertEquals("cross-platform", authAttach);
    }

    @Test
    void should_return_response_with_default_settings_when_DEFAULT_AUTH_MODE_flag_is_default() {
        //Given
        User testUser = User.createNew(DEFAULT_EMAIL);
        var passkeyOptionsProperties = new PasskeyProperties(
                DEFAULT_RPID,
                AuthMode.STANDARD
        );
        var passkeyOptionsService = new CredentialOptionsService(
                passkeyOptionsProperties,
                DEFAULT_CLOCK
        );

        // When
        var res = passkeyOptionsService.getPasskeyCreationOptions(testUser, DEFAULT_CHALLENGE);

        // Then
        var authAttach = res.authenticatorSelection().authenticatorAttachment();
        var hints = res.hints();

        assertEquals("", hints.getFirst());
        assertEquals("", authAttach);
    }

    /**
     * Default values according to official DOCS: <a href="https://www.w3.org/TR/webauthn-3/#dom-publickeycredentialcreationoptions">...</a>
     */
    @Test
    void should_return_default_values_for_every_not_set_parameter() {
        User testUser = User.createNew(DEFAULT_EMAIL);

        var res = passkeyOptionsService.getPasskeyCreationOptions(testUser, DEFAULT_CHALLENGE);

        // Then
        var exclCred = res.excludeCredentials();
        var attestation = res.attestation();
        var attestationFormats = res.attestationFormats();
        var extensions = res.extensions();

        assertThat(exclCred).as("Excluded credentials should be empty by default").isEmpty();
        assertThat(attestation).as("Attestation should be 'none' by default").isEqualTo("none");
        assertThat(attestationFormats).as("Attestation formats should be empty by default").isEmpty();
        assertThat(extensions).as("Extensions should be null or empty by default").isNullOrEmpty();
    }
}