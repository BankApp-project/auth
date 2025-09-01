package bankapp.auth.infrastructure.persistance.user;

import bankapp.auth.application.shared.port.out.persistance.UserRepository;
import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PostgresUserRepository implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findByEmail(EmailAddress email) {
        return userJpaRepository.findByEmail(email.getValue())
            .map(userMapper::toDomainUser);
    }

    @Override
    public void save(@NonNull User user) {
        JpaUser jpaUser = userMapper.toJpaUser(user);
        userJpaRepository.save(jpaUser);
    }

    @Override
    public Optional<User> findById(@NonNull UUID userId) {
        return userJpaRepository.findById(userId)
            .map(userMapper::toDomainUser);
    }
}