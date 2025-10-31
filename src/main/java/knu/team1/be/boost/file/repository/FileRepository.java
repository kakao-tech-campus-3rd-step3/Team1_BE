package knu.team1.be.boost.file.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.file.entity.File;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.task.entity.Task;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileRepository extends JpaRepository<File, UUID> {

    List<File> findAllByTask(Task task);

    @Query("""
            SELECT f FROM File f
            JOIN f.task t
            WHERE t.project = :project
              AND f.status = knu.team1.be.boost.file.entity.FileStatus.COMPLETED
              AND (
                :cursorCreatedAt IS NULL
                OR (f.createdAt < :cursorCreatedAt)
                OR (f.createdAt = :cursorCreatedAt AND f.id < :cursorId)
              )
            ORDER BY f.createdAt DESC, f.id DESC
        """)
    List<File> findByProjectWithCursor(
        @Param("project") Project project,
        @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );

    @Query("""
            SELECT f.task.id AS taskId, COUNT(f) AS count
            FROM File f
            WHERE f.task.id IN :taskIds
              AND f.status = knu.team1.be.boost.file.entity.FileStatus.COMPLETED
            GROUP BY f.task.id
        """)
    List<FileCount> countByTaskIds(@Param("taskIds") List<UUID> taskIds);

    interface FileCount {

        UUID getTaskId();

        Long getCount();
    }
}
