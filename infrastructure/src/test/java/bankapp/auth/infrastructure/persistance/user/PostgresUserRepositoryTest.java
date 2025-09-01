package bankapp.auth.infrastructure.persistance.user;

import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.infrastructure.WithPostgresContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@SpringJUnitConfig
@Import({PostgresUserRepository.class, UserMapper.class})
class PostgresUserRepositoryTest implements WithPostgresContainer {

    @Autowired
    private PostgresUserRepository postgresUserRepository;

    @Test
    void shouldSaveAndFindUserByEmail() {
        // Given
        EmailAddress email = new EmailAddress("test@example.com");
        User user = User.createNew(email);
        user.activate();

        // When
        postgresUserRepository.save(user);

        // Then
        Optional<User> foundUser = postgresUserRepository.findByEmail(email);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(user.getId());
        assertThat(foundUser.get().getEmail()).isEqualTo(email);
        assertThat(foundUser.get().isEnabled()).isTrue();
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        // Given
        EmailAddress email = new EmailAddress("nonexistent@example.com");

        // When
        Optional<User> foundUser = postgresUserRepository.findByEmail(email);

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void shouldSaveUserWithDisabledStatus() {
        // Given
        EmailAddress email = new EmailAddress("disabled@example.com");
        User user = User.createNew(email);
        // User is created as disabled by default

        // When
        postgresUserRepository.save(user);

        // Then
        Optional<User> foundUser = postgresUserRepository.findByEmail(email);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().isEnabled()).isFalse();
    }
}