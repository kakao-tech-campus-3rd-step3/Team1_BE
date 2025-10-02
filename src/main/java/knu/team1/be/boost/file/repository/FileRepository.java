package knu.team1.be.boost.file.repository;

import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.file.entity.File;
import knu.team1.be.boost.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, UUID> {

    List<File> findAllByTask(Task task);
}
