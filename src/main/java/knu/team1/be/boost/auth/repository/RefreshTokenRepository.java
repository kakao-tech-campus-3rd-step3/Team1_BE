package knu.team1.be.boost.auth.repository;

import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByMemberId(UUID memberId);

    void deleteByMemberId(UUID memberId);
}
