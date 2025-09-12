package knu.team1.be.boost.project.repository;

import java.util.UUID;
import knu.team1.be.boost.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

}
