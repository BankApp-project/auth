package bankapp.auth.infrastructure.driven.passkey.persistance.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface JpaPasskeyRepository extends JpaRepository<JpaPasskey, UUID> {
    List<JpaPasskey> findAllByUserHandle(UUID userHandle);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE JpaPasskey e SET e.signCount = :signCount WHERE e.id = :id")
    void updateSignCount(@Param("id") UUID id, @Param("signCount") long signCount);
}
