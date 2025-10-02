package bankapp.auth.infrastructure.usecases.it;

import bankapp.auth.application.registration.complete.CompleteRegistrationCommand;
import bankapp.auth.application.registration.complete.CompleteRegistrationUseCase;
import bankapp.auth.application.shared.port.out.ChallengeGenerationPort;
import bankapp.auth.application.shared.port.out.dto.Challenge;
import bankapp.auth.application.shared.port.out.dto.Session;
import bankapp.auth.application.shared.port.out.repository.PasskeyRepository;
import bankapp.auth.application.shared.port.out.repository.SessionRepository;
import bankapp.auth.application.shared.port.out.repository.UserRepository;
import bankapp.auth.application.shared.service.ByteArrayUtil;
import bankapp.auth.domain.model.Passkey;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.infrastructure.utils.TestPasskeyProvider;
import bankapp.auth.infrastructure.utils.WebAuthnTestHelper;
import bankapp.auth.infrastructure.utils.WithPostgresContainer;
import bankapp.auth.infrastructure.utils.WithRedisContainer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext
@SpringBootTest
@ActiveProfiles("test-postgres")
@Transactional
public class CompleteRegistrationIT implements WithPostgresContainer, WithRedisContainer {

    private final static String DEFAULT_EMAIL_ADDRESS = "test@bankapp.online";
    private final static EmailAddress DEFAULT_EMAIL = new EmailAddress(DEFAULT_EMAIL_ADDRESS);
    private final static UUID DEFAULT_SESSION_ID = UUID.randomUUID();

    private TestPasskeyProvider.PasskeyInfo passkeyInfo;
    private Passkey passkey;
    private Challenge challenge;
    private User user;

    @Autowired
    private CompleteRegistrationUseCase completeRegistrationUseCase;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasskeyRepository passkeyRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private ChallengeGenerationPort challengeGenerationPort;

    @BeforeEach
    void setup() {
        user = User.createNew(DEFAULT_EMAIL);
        userRepository.save(user);

        passkeyInfo = TestPasskeyProvider.createSamplePasskeyInfo(user.getId());
        passkey = passkeyInfo.passkey();
        passkeyRepository.save(passkey);

        challenge = challengeGenerationPort.generate();
    }

    @Test
    void completeRegistration_should_return_auth_tokens_and_activate_user_when_valid_request() throws Exception {
        //given
        var session = generateAndPersistSession();
        assertThat(sessionRepository.load(session.sessionId())).isPresent();
        var command = prepareCompleteRegistrationCommand(session);

        var userBeforeOp = fetchUser();
        assertFalse(userBeforeOp.isEnabled(), "User should be disabled before registration completion");

        //when
        var res = completeRegistrationUseCase.handle(command);

        //then
        assertNotNull(res.authTokens(), "authTokens should not be null");
        assertNotNull(res.authTokens().refreshToken(), "refreshToken should not be null");
        assertNotNull(res.authTokens().accessToken(), "accessToken should not be null");

        //session should be deleted after operation
        assertThat(sessionRepository.load(session.sessionId())).isEmpty();

        var userAfterOp = fetchUser();
        assertTrue(userAfterOp.isEnabled(), "User should be enabled after registration completion");
    }

    private User fetchUser() {
        var loadedUserOpt = userRepository.findByEmail(DEFAULT_EMAIL);
        assertThat(loadedUserOpt).isPresent();
        return loadedUserOpt.get();
    }

    private @NotNull Session generateAndPersistSession() {
        var session = new Session(DEFAULT_SESSION_ID, challenge, user.getId());
        sessionRepository.save(session);
        return session;
    }

    private @NotNull CompleteRegistrationCommand prepareCompleteRegistrationCommand(Session session) throws Exception {
        var authResponseJson = createAuthResponseJSON();
        return new CompleteRegistrationCommand(session.sessionId(), authResponseJson);
    }

    private String createAuthResponseJSON() throws Exception {
        return WebAuthnTestHelper.generateValidRegistrationResponseJSON(
                challenge.challenge(),
                getCredentialId(passkey),
                passkeyInfo.keyPair()
        );
    }

    private byte[] getCredentialId(Passkey passkey) {
        return ByteArrayUtil.uuidToBytes(passkey.getId());
    }
}
