package bankapp.auth.infrastructure.driven.user.persistance;

import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void shouldMapUserToJpaUser() {
        // Given
        EmailAddress email = new EmailAddress("test@example.com");
        User user = User.createNew(email);
        user.activate();

        // When
        JpaUser jpaUser = userMapper.toJpaUser(user);

        // Then
        assertThat(jpaUser).isNotNull();
        assertThat(jpaUser.getId()).isEqualTo(user.getId());
        assertThat(jpaUser.getEmail()).isEqualTo(user.getEmail().getValue());
        assertThat(jpaUser.isEnabled()).isEqualTo(user.isEnabled());
    }

    @Test
    void shouldMapJpaUserToUser() {
        // Given
        UUID id = UUID.randomUUID();
        String email = "test@example.com";
        boolean enabled = true;
        JpaUser jpaUser = new JpaUser(id, email, enabled);

        // When
        User user = userMapper.toDomainUser(jpaUser);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail().getValue()).isEqualTo(email);
        assertThat(user.isEnabled()).isEqualTo(enabled);
    }
}