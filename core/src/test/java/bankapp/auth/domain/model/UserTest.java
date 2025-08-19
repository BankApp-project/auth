package bankapp.auth.domain.model;


import bankapp.auth.application.verification_complete.port.out.UserRepository;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.application.verification_complete.port.out.stubs.StubUserRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void should_generate_new_uuid_for_each_user() {
        // Given: No existing users

        // When: Creating two different users
        var user = new User(new EmailAddress("test@bankapp.online"));
        var user2 = new User(new EmailAddress("test2@bankapp.online"));

        // Then: Each user should have a unique ID
        assertNotEquals(user.getId(), user2.getId());
    }

    @Test
    void id_should_remain_same_after_persistance() {
        // Given: User is saved in the DB
        UserRepository userRepository = new StubUserRepository();
        var user = new User(new EmailAddress("test@bankapp.online"));
        userRepository.save(user);

        // When: User is retrieved via email address
        var sameUser = userRepository.findByEmail(user.getEmail());

        // Then: The retrieved user should be present and identical to the original
        assertTrue(sameUser.isPresent());
        assertEquals(user, sameUser.get());
    }
}
