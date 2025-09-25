package knu.team1.be.boost.task.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.task.entity.Task;
import knu.team1.be.boost.task.entity.TaskStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    boolean existsByIdAndAssigneesId(UUID taskId, UUID memberId);

    Optional<Task> findByIdAndProjectId(UUID taskId, UUID projectId);

    @Query("""
            SELECT t FROM Task t
            WHERE t.project = :project
              AND t.status = :status
              AND (
                  (:cursorCreatedAtKey IS NULL AND :cursorDueDateKey IS NULL)
                  OR (
                      (:sortBy = 'createdAt' AND :direction = 'ASC' AND (
                          t.createdAt > :cursorCreatedAtKey
                          OR (t.createdAt = :cursorCreatedAtKey AND t.id > :cursorId)
                      ))
                      OR (:sortBy = 'createdAt' AND :direction = 'DESC' AND (
                          t.createdAt < :cursorCreatedAtKey
                          OR (t.createdAt = :cursorCreatedAtKey AND t.id < :cursorId)
                      ))
                      OR (:sortBy = 'dueDate' AND :direction = 'ASC' AND (
                          t.dueDate > :cursorDueDateKey
                          OR (t.dueDate = :cursorDueDateKey AND t.id > :cursorId)
                      ))
                      OR (:sortBy = 'dueDate' AND :direction = 'DESC' AND (
                          t.dueDate < :cursorDueDateKey
                          OR (t.dueDate = :cursorDueDateKey AND t.id < :cursorId)
                      ))
                  )
              )
            ORDER BY
                CASE WHEN :sortBy = 'createdAt' AND :direction = 'ASC' THEN t.createdAt END ASC,
                CASE WHEN :sortBy = 'createdAt' AND :direction = 'DESC' THEN t.createdAt END DESC,
                CASE WHEN :sortBy = 'dueDate' AND :direction = 'ASC' THEN t.dueDate END ASC,
                CASE WHEN :sortBy = 'dueDate' AND :direction = 'DESC' THEN t.dueDate END DESC,
                t.id ASC
        """)
    List<Task> findTasksByStatusWithCursor(
        @Param("project") Project project,
        @Param("status") TaskStatus status,
        @Param("cursorCreatedAtKey") LocalDateTime cursorCreatedAtKey,
        @Param("cursorDueDateKey") LocalDate cursorDueDateKey,
        @Param("cursorId") UUID cursorId,
        @Param("sortBy") String sortBy,
        @Param("direction") String direction,
        Pageable pageable
    );

    @Query("""
            SELECT t
            FROM Task t
            JOIN t.assignees a
            WHERE a = :assignee
              AND t.project = :project
              AND t.status <> knu.team1.be.boost.task.entity.TaskStatus.DONE
              AND (:cursorStatusOrder IS NULL
                   OR (CASE t.status
                           WHEN knu.team1.be.boost.task.entity.TaskStatus.REVIEW THEN 1
                           WHEN knu.team1.be.boost.task.entity.TaskStatus.PROGRESS THEN 2
                           WHEN knu.team1.be.boost.task.entity.TaskStatus.TODO THEN 3
                       END, t.createdAt, t.id) > (:cursorStatusOrder, :cursorCreatedAt, :cursorId))
            ORDER BY
              CASE t.status
                  WHEN knu.team1.be.boost.task.entity.TaskStatus.REVIEW THEN 1
                  WHEN knu.team1.be.boost.task.entity.TaskStatus.PROGRESS THEN 2
                  WHEN knu.team1.be.boost.task.entity.TaskStatus.TODO THEN 3
              END ASC,
              t.createdAt ASC,
              t.id ASC
        """)
    List<Task> findTasksByAssigneeWithCursor(
        @Param("assignee") Member assignee,
        @Param("project") Project project,
        @Param("cursorStatusOrder") Integer cursorStatusOrder,
        @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );

}
