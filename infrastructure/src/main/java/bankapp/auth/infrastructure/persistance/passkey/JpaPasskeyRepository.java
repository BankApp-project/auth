package bankapp.auth.infrastructure.persistance.passkey;

import bankapp.auth.infrastructure.persistance.passkey.dto.JpaPasskey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface JpaPasskeyRepository extends JpaRepository<JpaPasskey, UUID> {
}
