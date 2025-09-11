package knu.team1.be.boost.projectMember.repository;

import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.projectMember.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    Optional<ProjectMember> findByProjectIdAndMemberId(
        @Param("projectId") UUID projectId,
        @Param("memberId") UUID memberId
    );

}
