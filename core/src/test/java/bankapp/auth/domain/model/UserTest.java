package bankapp.auth.domain.model;


import bankapp.auth.domain.model.vo.EmailAddress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class UserTest {

    @Test
    void should_generate_new_uuid_for_each_user() {
        var user = new User(new EmailAddress("test@bankapp.online"));
        var user2 = new User(new EmailAddress("test2@bankapp.online"));

        assertNotEquals(user.getId(), user2.getId());
    }

}