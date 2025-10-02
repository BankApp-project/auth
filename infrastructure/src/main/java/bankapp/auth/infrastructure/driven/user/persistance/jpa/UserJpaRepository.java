package bankapp.auth.infrastructure.driven.user.persistance.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<JpaUser, UUID> {
    
    Optional<JpaUser> findByEmail(String email);
}