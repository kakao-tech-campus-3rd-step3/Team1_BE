package knu.team1.be.boost.projectMembership.repository;

import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.projectMembership.entity.ProjectJoinCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProjectJoinCodeRepository extends JpaRepository<ProjectJoinCode, UUID> {

    @Query("SELECT p FROM ProjectJoinCode p WHERE p.project.id = :projectId AND p.status = 'ACTIVE'")
    Optional<ProjectJoinCode> findActiveByProjectId(UUID projectId);

    @Modifying
    @Query("UPDATE ProjectJoinCode p SET p.status = 'REVOKED' WHERE p.project.id = :projectId AND p.status = 'ACTIVE'")
    void revokeActiveCodesByProjectId(UUID projectId);

    boolean existsByJoinCode(String joinCode);

    Optional<ProjectJoinCode> findByJoinCode(String joinCode);
}
