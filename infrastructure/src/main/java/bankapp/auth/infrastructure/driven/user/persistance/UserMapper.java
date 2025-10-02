package bankapp.auth.infrastructure.driven.user.persistance;

import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
import bankapp.auth.infrastructure.driven.user.persistance.jpa.JpaUser;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public JpaUser toJpaUser(User user) {
        return new JpaUser(
            user.getId(),
            user.getEmail().getValue(),
            user.isEnabled()
        );
    }

    public User toDomainUser(JpaUser jpaUser) {
        var email = new EmailAddress(jpaUser.getEmail());
        return User.reconstitute(jpaUser.getId(), email, jpaUser.isEnabled());
    }
}