package bankapp.auth.domain.service;

import bankapp.auth.domain.model.User;
import bankapp.auth.domain.model.annotations.NotNull;
import bankapp.auth.domain.model.vo.EmailAddress;

public class UserService {
    public User createUser(@NotNull EmailAddress email) {
        return new User(email);
    }
}
