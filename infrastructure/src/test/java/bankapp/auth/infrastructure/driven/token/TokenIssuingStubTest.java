package bankapp.auth.infrastructure.driven.token;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TokenIssuingStubTest {

    @Test
    void issueTokensForUser_should_return_not_null_response() {
        var tokenIssuingAdapter = new TokenIssuingStub();
        var testUserId = UUID.randomUUID();

        var res = tokenIssuingAdapter.issueTokensForUser(testUserId);

        assertNotNull(res, "Response should not be null");
        assertNotNull(res.accessToken(), "Access token should not be null");
        assertNotNull(res.refreshToken(), "Refresh token should not be null");
    }
}