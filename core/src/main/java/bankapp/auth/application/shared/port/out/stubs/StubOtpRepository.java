package bankapp.auth.application.shared.port.out.stubs;

import bankapp.auth.application.shared.port.out.persistance.OtpRepository;
import bankapp.auth.domain.model.Otp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StubOtpRepository implements OtpRepository {
    private final Map<String, Otp> inMemoryOtpRepo = new ConcurrentHashMap<>();

    @Override
    public void save(Otp otp) {
        String key = otp.getKey();
        inMemoryOtpRepo.put(key, otp);
    }

    @Override
    public Otp load(String key) {
        return inMemoryOtpRepo.get(key);
    }
}
