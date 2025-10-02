package bankapp.auth.application.shared.port.out.service;

import java.util.UUID;

public interface SessionIdGenerationPort {

    //it should generate UUID v7, to ensure the best performance with DBs operations.
    UUID generate();
}
