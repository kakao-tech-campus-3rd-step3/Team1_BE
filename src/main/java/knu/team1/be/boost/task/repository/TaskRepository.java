package knu.team1.be.boost.task.repository;

import java.util.UUID;
import knu.team1.be.boost.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, UUID> {

}
