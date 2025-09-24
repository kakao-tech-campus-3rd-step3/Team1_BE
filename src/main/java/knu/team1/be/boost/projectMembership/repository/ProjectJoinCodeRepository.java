package knu.team1.be.boost.projectMembership.repository;

import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.projectMembership.entity.ProjectJoinCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectJoinCodeRepository extends JpaRepository<ProjectJoinCode, UUID> {
    
    Optional<ProjectJoinCode> findByProjectId(UUID projectId);

    boolean existsByJoinCode(String joinCode);
}
