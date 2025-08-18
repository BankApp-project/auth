package bankapp.auth.application.shared.port.out.stubs;

import bankapp.auth.application.shared.port.out.HashingPort;

public class StubHasher implements HashingPort {
    @Override
    public String hashSecurely(String value) {
        return value + "-hashed";
    }

    @Override
    public boolean verify(String hashedValue, String value) {
        return hashedValue.equals(hashSecurely(value));
    }
}
