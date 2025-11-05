package knu.team1.be.boost.member.repository;

import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, UUID> {

    @Query(value = "SELECT * FROM members WHERE provider = :provider AND provider_id = :providerId LIMIT 1",
        nativeQuery = true)
    Optional<Member> findByOauthInfoProviderAndOauthInfoProviderIdIncludingDeleted(
        @Param("provider") String provider,
        @Param("providerId") Long providerId
    );
}
