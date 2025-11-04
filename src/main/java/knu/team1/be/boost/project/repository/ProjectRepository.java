package knu.team1.be.boost.project.repository;

import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Query("""
            select distinct p from Project p
            left join fetch p.projectMemberships pm
            left join fetch pm.member
            where p.id = :projectId
        """)
    Optional<Project> findByIdWithMemberships(@Param("projectId") UUID projectId);
}
