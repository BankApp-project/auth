package bankapp.auth.application.shared.port.out.persistance;

import bankapp.auth.domain.model.Otp;

import java.util.Optional;

public interface OtpRepository {

    void save(Otp otp);

    Optional<Otp> load(String key);

    void delete(String key);
}