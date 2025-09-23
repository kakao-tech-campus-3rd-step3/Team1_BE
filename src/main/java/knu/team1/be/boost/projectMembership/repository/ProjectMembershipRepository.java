package knu.team1.be.boost.projectMembership.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.projectMembership.entity.ProjectMembership;
import knu.team1.be.boost.projectMembership.entity.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectMembershipRepository extends JpaRepository<ProjectMembership, UUID> {

    Optional<ProjectMembership> findByProjectIdAndMemberId(
        @Param("projectId") UUID projectId,
        @Param("memberId") UUID memberId
    );

    @Query(value = "SELECT * FROM project_member WHERE project_id = :projectId AND member_id = :memberId",
        nativeQuery = true)
    Optional<ProjectMembership> findByProjectIdAndMemberIdIncludingDeleted(
        @Param("projectId") UUID projectId,
        @Param("memberId") UUID memberId
    );

    boolean existsByProjectIdAndMemberId(UUID projectId, UUID memberId);

    int countByProjectIdAndMemberIdIn(UUID projectId, Collection<UUID> memberIds);

    boolean existsByProjectIdAndMemberIdAndRole(UUID projectId, UUID memberId, ProjectRole role);
}
