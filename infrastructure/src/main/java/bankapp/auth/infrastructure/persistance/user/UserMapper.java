package bankapp.auth.infrastructure.persistance.user;

import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.vo.EmailAddress;
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
        User user = new User(new EmailAddress(jpaUser.getEmail()));
        
        // Use reflection to set the ID since it's final and set during construction
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, jpaUser.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user ID", e);
        }
        
        // Set enabled status
        if (jpaUser.isEnabled()) {
            user.activate();
        }
        
        return user;
    }
}