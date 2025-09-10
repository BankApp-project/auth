package bankapp.auth.infrastructure.driven.passkey.persistance;

import bankapp.auth.application.shared.enums.AuthenticatorTransport;
import bankapp.auth.application.shared.port.out.dto.PasskeyRegistrationData;
import bankapp.auth.infrastructure.WithPostgresContainer;
import com.github.f4b6a3.uuid.alt.GUID;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test-postgres")
public class PostgresPasskeyRepositoryTest implements WithPostgresContainer {

    @Autowired
    private PostgresPasskeyRepository repo;

    @Test
    public void should_return_list_of_passkeys_of_saved_elements() {

        var registrationData = createSampleRegistrationData();
        var credentialId = registrationData.id();
        repo.save(registrationData);

        var loadedPasskeyOpt = repo.load(credentialId);

        assertTrue(loadedPasskeyOpt.isPresent());

        var loadedPasskey = loadedPasskeyOpt.get();
        assertNotNull(loadedPasskey);
        assertEquals(registrationData.id(), loadedPasskey.getId());
        assertThat(loadedPasskey)
                .usingRecursiveComparison()
                .isEqualTo(registrationData);

    }

    private PasskeyRegistrationData createSampleRegistrationData() {
        var userId = UUID.randomUUID();
        return createSampleRegistrationData(userId);
    }


    @Test
    void load_should_return_empty_optional_if_values_not_present() {
        var res = repo.load(UUID.randomUUID());

        assertThat(res).isEmpty();
    }

    @Test
    void loadForUserId_should_load_passkey_list_for_given_user() {
        var userId = UUID.randomUUID();
        var registrationData1 = createSampleRegistrationData(userId);
        var registrationData2 = createSampleRegistrationData(userId);

        repo.save(registrationData1);
        repo.save(registrationData2);

        var credentialList = repo.loadForUserId(userId);

        assertThat(credentialList).isNotEmpty();

        for (var credential : credentialList) {
            assertThat(credential)
                    .usingRecursiveComparison()
                    .isIn(registrationData1, registrationData2);
        }
    }

    @Test
    void loadForUserId_should_return_empty_list_when_no_credential_is_present_for_given_userId() {
        var userIdWithoutAnyPasskey = UUID.randomUUID();

        //to check for not empty db.
        repo.save(createSampleRegistrationData());
        repo.save(createSampleRegistrationData());

        var credentialList = repo.loadForUserId(userIdWithoutAnyPasskey);

        assertThat(credentialList).isEmpty();
    }

    @Transactional
    @Test
    void updateSignCount_should_update_SignCount_signCount_of_passkey_record() {

        var userId = UUID.randomUUID();
        var registrationData = createSampleRegistrationData(userId);
        var passkeyId = registrationData.id();
        repo.save(registrationData);

        var loadedPasskeyOpt = repo.load(passkeyId);
        assertThat(loadedPasskeyOpt).isPresent();

        var loadedPasskey = loadedPasskeyOpt.get();
        var oldSignCount = loadedPasskey.getSignCount();

        loadedPasskey.setSignCount(oldSignCount + 1);

        repo.updateSignCount(loadedPasskey);

        var loadedUpdatedPasskeyOpt = repo.load(passkeyId);
        assertThat(loadedUpdatedPasskeyOpt).isPresent();

        var loadedUpdatedPasskey = loadedUpdatedPasskeyOpt.get();

        assertThat(loadedUpdatedPasskey.getSignCount())
                .isGreaterThan(oldSignCount);
    }

    private PasskeyRegistrationData createSampleRegistrationData(UUID userHandle) {

        var credentialId = UUID.randomUUID();

        var publicKey = GUID.v4().toBytes();

        return new PasskeyRegistrationData(
                credentialId,
                userHandle,
                "public-key",
                publicKey,
                100L,
                true,
                true,
                true,
                Arrays.asList(AuthenticatorTransport.INTERNAL, AuthenticatorTransport.USB),
                Collections.singletonMap("extKey", "extValue"),
                "attestation-object-bytes".getBytes(),
                "client-data-json-bytes".getBytes()
        );
    }
}