package knu.team1.be.boost.file.repository;

import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.file.entity.File;
import knu.team1.be.boost.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileRepository extends JpaRepository<File, UUID> {

    List<File> findAllByTask(Task task);

    @Query("""
            SELECT f
            FROM File f
            JOIN f.task t
            WHERE t.project.id = :projectId
        """)
    List<File> findAllByProjectId(@Param("projectId") UUID projectId);

    @Query("""
            SELECT f.task.id AS taskId, COUNT(f) AS count
            FROM File f
            WHERE f.task.id IN :taskIds AND f.deleted = false
            GROUP BY f.task.id
        """)
    List<FileCount> countByTaskIds(@Param("taskIds") List<UUID> taskIds);

    interface FileCount {

        UUID getTaskId();

        Long getCount();
    }
}
