package knu.team1.be.boost.member.repository;

import java.util.UUID;
import knu.team1.be.boost.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, UUID> {

}
