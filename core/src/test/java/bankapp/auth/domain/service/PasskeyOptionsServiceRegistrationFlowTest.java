package bankapp.auth.domain.service;

import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.service.stubs.StubChallengeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PasskeyOptionsServiceRegistrationFlowTest {

    private static final String DEFAULT_AUTH_MODE = "smartphone";
    private static final String DEFAULT_RPID = "bankapp.online";
    private static final long DEFAULT_TIMEOUT = 30000; //30s in ms

    private static final EmailAddress DEFAULT_EMAIL = new EmailAddress("test@bankapp.online");
    public static final User TEST_USER = new User(DEFAULT_EMAIL);

    PasskeyOptionsServiceImpl passkeyOptionsService;


    @BeforeEach
    void setup() {
        passkeyOptionsService = new PasskeyOptionsServiceImpl(
                DEFAULT_AUTH_MODE,
                DEFAULT_RPID,
                DEFAULT_TIMEOUT,
                new StubChallengeGenerator()
        );
    }

    @Test
    void should_return_response_with_userId_as_userHandle() {

        // When
        var res = passkeyOptionsService.getPasskeyCreationOptions(TEST_USER);

        assertArrayEquals(ByteArrayUtil.uuidToBytes(TEST_USER.getId()), res.user().id());
    }

    @Test
    void should_return_response_with_at_least_16bytes_long_challenge() {

        var res = passkeyOptionsService.getPasskeyCreationOptions(TEST_USER);
        byte[] challenge = res.challenge();
        assertThat(challenge).hasSizeGreaterThanOrEqualTo(16);
    }

    @Test
    void should_return_response_with_unique_Challenge() {
        // Given
        User testUser1 = new User(DEFAULT_EMAIL);
        User testUser2 = new User(new EmailAddress("test2@bankapp.online"));

        // When
        var res1 = passkeyOptionsService.getPasskeyCreationOptions(testUser1);
        var res2 = passkeyOptionsService.getPasskeyCreationOptions(testUser2);

        // Then
        byte[] challenge1 = res1.challenge();
        byte[] challenge2 = res2.challenge();
        assertFalse(java.util.Arrays.equals(challenge1, challenge2), "Challenges should be unique");
    }


    @Test
    void should_return_response_with_email_as_userEntity_name_and_displayName() {
        // Given
        User testUser = new User(DEFAULT_EMAIL);

        // When
        var res = passkeyOptionsService.getPasskeyCreationOptions(testUser);

        // Then
        String name = res.user().name();
        String displayName = res.user().displayName();

        assertEquals(DEFAULT_EMAIL.getValue(), name);
        assertEquals(DEFAULT_EMAIL.getValue(), displayName);
    }

    @Test
    void should_return_response_with_valid_rpId() {
        // Given
        User testUser = new User(DEFAULT_EMAIL);

        // When
        var res = passkeyOptionsService.getPasskeyCreationOptions(testUser);

        // Then
        String rpId = res.rp().id();

        assertEquals(DEFAULT_RPID, rpId);
    }

    @Test
    void should_return_response_with_valid_PublicKeyCredentialParameters() {
        // Given
        User testUser = new User(DEFAULT_EMAIL);

        // When
        var res = passkeyOptionsService.getPasskeyCreationOptions(testUser);

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
        User testUser = new User(DEFAULT_EMAIL);

        // When
        var res = passkeyOptionsService.getPasskeyCreationOptions(testUser);

        // Then
        var timeout = res.timeout();

        assertNotNull(timeout);
        assertEquals(DEFAULT_TIMEOUT, timeout);
    }

    @Test
    void should_return_response_with_valid_and_secure_AuthenticatorSelectionCriteria() {
        // Given
        User testUser = new User(DEFAULT_EMAIL);

        // When
        var res = passkeyOptionsService.getPasskeyCreationOptions(testUser);

        // Then
        var authSelCriteria = res.authenticatorSelection();

        assertNotNull(authSelCriteria);
        assertTrue(authSelCriteria.requireResidentKey());
        assertEquals("required", authSelCriteria.userVerification());
    }

    @Test
    void should_return_response_with_correct_settings_based_on_authViaSmartphone_flag() {
        // Given
        User testUser = new User(DEFAULT_EMAIL);

        // When
        var res = passkeyOptionsService.getPasskeyCreationOptions(testUser);

        // Then
        var authAttach = res.authenticatorSelection().authenticatorAttachment();
        var hints = res.hints();

        assertEquals("hybrid", hints.getFirst());
        assertEquals("cross-platform", authAttach);
    }
}
