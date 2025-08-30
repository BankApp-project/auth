package bankapp.auth.services.otp;

import bankapp.auth.application.shared.port.out.HashingPort;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BCryptHashingService implements HashingPort {

    private final PasswordEncoder passwordEncoder;


    /**
     * Generates a secure hash of the provided input value using the configured password encoder.
     *
     * @param value the input string to be securely hashed; must not be null
     * @return the securely hashed representation of the input value
     */
    @Override
    public String hashSecurely(@NonNull String value) {
        return passwordEncoder.encode(value);
    }

    @Override
    public boolean verify(String hashedValue, String value) {
        return passwordEncoder.matches(value, hashedValue);
    }
}
