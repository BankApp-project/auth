package bankapp.auth.services;

import bankapp.auth.application.shared.port.out.HashingPort;
import bankapp.auth.annotations.WorkInProgress;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Deprecated
@WorkInProgress
@Service
@RequiredArgsConstructor
public class BCryptHashingService implements HashingPort {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String hashSecurely(@NonNull String value) {
        return passwordEncoder.encode(value);
    }

    @Override
    public boolean verify(String hashedValue, String value) {
        return false;
    }
}
