package bankapp.auth.domain.service;

import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.domain.model.Otp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StubOtpRepository implements OtpRepository {
    private final Map<String, Otp> inMemoryOtpRepo = new ConcurrentHashMap<>();

    @Override
    public void save(Otp otp, int ttlSeconds) {
        String key = otp.getKey(); // Assuming Otp has a getKey() method
        inMemoryOtpRepo.put(key, otp);
    }

    @Override
    public Otp load(String key) {
        return inMemoryOtpRepo.get(key);
    }
}
