package bankapp.auth.domain.service;

import bankapp.auth.domain.model.CredentialRecord;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.domain.service.stubs.StubChallengeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class CredentialOptionsServiceLoginFlowTest {


    private static final String DEFAULT_AUTH_MODE = "smartphone";
    private static final String DEFAULT_RPID = "bankapp.online";
    private static final long DEFAULT_TIMEOUT = 30000; //30s in ms
    private static final EmailAddress DEFAULT_EMAIL_ADDRESS = new EmailAddress("test@bankapp.online");
    private static final User DEFAULT_USER = new User(DEFAULT_EMAIL_ADDRESS);
    private static final List<CredentialRecord> DEFAULT_USER_CREDENTIALS = List.of(new CredentialRecord(
                ByteArrayUtil.uuidToBytes(UUID.randomUUID()),
                ByteArrayUtil.uuidToBytes(DEFAULT_USER.getId()),
                null,
                null,
                0L,
                false,
                false,
                false,
                null,
                null,
                null,
            null
        ));

    CredentialOptionsServiceImpl passkeyOptionsService;

    @BeforeEach
    void setup() {
        passkeyOptionsService = new CredentialOptionsServiceImpl(
                DEFAULT_AUTH_MODE,
                DEFAULT_RPID,
                DEFAULT_TIMEOUT,
                new StubChallengeGenerator()
        );
    }

    @Test
    void should_return_response_with_at_least_16bytes_long_challenge() {
        // Given

        // When
        var res = passkeyOptionsService.getPasskeyRequestOptions(DEFAULT_USER, DEFAULT_USER_CREDENTIALS);

        // Then
        byte[] challenge = res.challenge();
        assertThat(challenge).hasSizeGreaterThanOrEqualTo(16);
    }

    @Test
    void should_return_unique_response() {

        // When
        var res1 = passkeyOptionsService.getPasskeyRequestOptions(DEFAULT_USER, DEFAULT_USER_CREDENTIALS);
        var res2 = passkeyOptionsService.getPasskeyRequestOptions(DEFAULT_USER, DEFAULT_USER_CREDENTIALS);

        // Then
        byte[] challenge1 = res1.challenge();
        byte[] challenge2 = res2.challenge();

        assertFalse(java.util.Arrays.equals(challenge1, challenge2), "Challenges should be unique");
    }

    @Test
    void should_return_response_with_default_timeout() {
        var timeout = passkeyOptionsService.getPasskeyRequestOptions(DEFAULT_USER, DEFAULT_USER_CREDENTIALS).timeout();

        assertNotNull(timeout);
        assertEquals(DEFAULT_TIMEOUT, timeout);
    }

    @Test
    void should_return_response_with_default_rpid() {
        var rpId = passkeyOptionsService.getPasskeyRequestOptions(DEFAULT_USER, DEFAULT_USER_CREDENTIALS).rpId();

        assertNotNull(rpId);
        assertEquals(DEFAULT_RPID,rpId);
    }

    @Test
    void should_return_response_with_allowedCredentials_list_corresponding_to_given_user() {

        var allowedCredentials = passkeyOptionsService.getPasskeyRequestOptions(DEFAULT_USER, DEFAULT_USER_CREDENTIALS).allowCredentials();

        assertNotNull(allowedCredentials);
        assertEquals(DEFAULT_USER_CREDENTIALS.getFirst().id() ,allowedCredentials.getFirst().id());
        assertEquals(DEFAULT_USER_CREDENTIALS.getFirst().type(), allowedCredentials.getFirst().type());
        assertEquals(DEFAULT_USER_CREDENTIALS.getFirst().transports(),allowedCredentials.getFirst().transports());
    }

    @Test
    void should_return_response_with_userVerification_set_to_required() {
        var uv = passkeyOptionsService.getPasskeyRequestOptions(DEFAULT_USER, DEFAULT_USER_CREDENTIALS).userVerification();

        assertNotNull(uv);
        assertEquals("required", uv);
    }
}
