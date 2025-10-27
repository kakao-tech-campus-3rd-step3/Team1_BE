package knu.team1.be.boost.task.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.task.dto.MemberTaskStatusCount;
import knu.team1.be.boost.task.dto.ProjectTaskStatusCount;
import knu.team1.be.boost.task.entity.Task;
import knu.team1.be.boost.task.entity.TaskStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    boolean existsByIdAndAssigneesId(UUID taskId, UUID memberId);

    @Query("""
            SELECT new knu.team1.be.boost.task.dto.ProjectTaskStatusCount(
                sum(case when t.status = 'TODO' then 1 else 0 end),
                sum(case when t.status = 'PROGRESS' then 1 else 0 end),
                sum(case when t.status = 'REVIEW' then 1 else 0 end),
                sum(case when t.status = 'DONE' then 1 else 0 end)
            )
            FROM Task t
            WHERE t.project.id = :projectId
        """)
    ProjectTaskStatusCount countByProjectGrouped(@Param("projectId") UUID projectId);

    @Query("""
            SELECT new knu.team1.be.boost.task.dto.MemberTaskStatusCount(
                a.id,
                sum(case when t.status = 'TODO' then 1 else 0 end),
                sum(case when t.status = 'PROGRESS' then 1 else 0 end),
                sum(case when t.status = 'REVIEW' then 1 else 0 end)
            )
            FROM Task t
            JOIN t.assignees a
            WHERE t.project.id = :projectId
            GROUP BY a.id
        """)
    List<MemberTaskStatusCount> countTasksByStatusForAllMembersGrouped(
        @Param("projectId") UUID projectId
    );

    @Query("""
            SELECT new knu.team1.be.boost.task.dto.ProjectTaskStatusCount(
                sum(case when t.status = 'TODO' then 1 else 0 end),
                sum(case when t.status = 'PROGRESS' then 1 else 0 end),
                sum(case when t.status = 'REVIEW' then 1 else 0 end),
                sum(case when t.status = 'DONE' then 1 else 0 end)
            )
            FROM Task t
            WHERE t.project.id = :projectId
              AND (LOWER(t.title) LIKE LOWER(:searchPattern) OR LOWER(t.description) LIKE LOWER(:searchPattern))
        """)
    ProjectTaskStatusCount countByProjectWithSearchGrouped(
        @Param("projectId") UUID projectId,
        @Param("searchPattern") String searchPattern
    );

    @Query("""
            SELECT new knu.team1.be.boost.task.dto.MemberTaskStatusCount(
                a.id,
                sum(case when t.status = 'TODO' then 1 else 0 end),
                sum(case when t.status = 'PROGRESS' then 1 else 0 end),
                sum(case when t.status = 'REVIEW' then 1 else 0 end)
            )
            FROM Task t
            JOIN t.assignees a
            WHERE t.project.id = :projectId
              AND (LOWER(t.title) LIKE LOWER(:searchPattern) OR LOWER(t.description) LIKE LOWER(:searchPattern))
            GROUP BY a.id
        """)
    List<MemberTaskStatusCount> countTasksByStatusForAllMembersWithSearchGrouped(
        @Param("projectId") UUID projectId,
        @Param("searchPattern") String searchPattern
    );

    @Query("""
            SELECT DISTINCT t FROM Task t
            LEFT JOIN t.tags tag
            WHERE t.project = :project
              AND t.status = :status
              AND (:tagId IS NULL OR tag.id = :tagId)
              AND (:cursorCreatedAtKey IS NULL
                   OR t.createdAt > :cursorCreatedAtKey
                   OR (t.createdAt = :cursorCreatedAtKey AND t.id > :cursorId))
            ORDER BY t.createdAt ASC, t.id ASC
        """)
    List<Task> findByStatusOrderByCreatedAtAsc(
        @Param("project") Project project,
        @Param("tagId") UUID tagId,
        @Param("status") TaskStatus status,
        @Param("cursorCreatedAtKey") LocalDateTime cursorCreatedAtKey,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );

    @Query("""
            SELECT DISTINCT t FROM Task t
            LEFT JOIN t.tags tag
            WHERE t.project = :project
              AND t.status = :status
              AND (:tagId IS NULL OR tag.id = :tagId)
              AND (:cursorCreatedAtKey IS NULL
                   OR t.createdAt < :cursorCreatedAtKey
                   OR (t.createdAt = :cursorCreatedAtKey AND t.id < :cursorId))
            ORDER BY t.createdAt DESC, t.id DESC
        """)
    List<Task> findByStatusOrderByCreatedAtDesc(
        @Param("project") Project project,
        @Param("tagId") UUID tagId,
        @Param("status") TaskStatus status,
        @Param("cursorCreatedAtKey") LocalDateTime cursorCreatedAtKey,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );

    @Query("""
            SELECT DISTINCT t FROM Task t
            LEFT JOIN t.tags tag
            WHERE t.project = :project
              AND t.status = :status
              AND (:tagId IS NULL OR tag.id = :tagId)
              AND (:cursorDueDateKey IS NULL
                   OR t.dueDate > :cursorDueDateKey
                   OR (t.dueDate = :cursorDueDateKey AND t.id > :cursorId))
            ORDER BY t.dueDate ASC, t.id ASC
        """)
    List<Task> findByStatusOrderByDueDateAsc(
        @Param("project") Project project,
        @Param("tagId") UUID tagId,
        @Param("status") TaskStatus status,
        @Param("cursorDueDateKey") LocalDate cursorDueDateKey,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );

    @Query("""
            SELECT DISTINCT t FROM Task t
            LEFT JOIN t.tags tag
            WHERE t.project = :project
              AND t.status = :status
              AND (:tagId IS NULL OR tag.id = :tagId)
              AND (:cursorDueDateKey IS NULL
                   OR t.dueDate < :cursorDueDateKey
                   OR (t.dueDate = :cursorDueDateKey AND t.id < :cursorId))
            ORDER BY t.dueDate DESC, t.id DESC
        """)
    List<Task> findByStatusOrderByDueDateDesc(
        @Param("project") Project project,
        @Param("tagId") UUID tagId,
        @Param("status") TaskStatus status,
        @Param("cursorDueDateKey") LocalDate cursorDueDateKey,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );

    @Query("""
            SELECT DISTINCT t FROM Task t
            LEFT JOIN t.tags tag
            WHERE t.project = :project
              AND t.status = :status
              AND (:tagId IS NULL OR tag.id = :tagId)
              AND (LOWER(t.title) LIKE LOWER(:searchPattern) OR LOWER(t.description) LIKE LOWER(:searchPattern))
              AND (:cursorCreatedAtKey IS NULL
                   OR t.createdAt > :cursorCreatedAtKey
                   OR (t.createdAt = :cursorCreatedAtKey AND t.id > :cursorId))
            ORDER BY t.createdAt ASC, t.id ASC
        """)
    List<Task> findByStatusWithSearchOrderByCreatedAtAsc(
        @Param("project") Project project,
        @Param("tagId") UUID tagId,
        @Param("status") TaskStatus status,
        @Param("searchPattern") String searchPattern,
        @Param("cursorCreatedAtKey") LocalDateTime cursorCreatedAtKey,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );

    @Query("""
            SELECT DISTINCT t FROM Task t
            LEFT JOIN t.tags tag
            WHERE t.project = :project
              AND t.status = :status
              AND (:tagId IS NULL OR tag.id = :tagId)
              AND (LOWER(t.title) LIKE LOWER(:searchPattern) OR LOWER(t.description) LIKE LOWER(:searchPattern))
              AND (:cursorCreatedAtKey IS NULL
                   OR t.createdAt < :cursorCreatedAtKey
                   OR (t.createdAt = :cursorCreatedAtKey AND t.id < :cursorId))
            ORDER BY t.createdAt DESC, t.id DESC
        """)
    List<Task> findByStatusWithSearchOrderByCreatedAtDesc(
        @Param("project") Project project,
        @Param("tagId") UUID tagId,
        @Param("status") TaskStatus status,
        @Param("searchPattern") String searchPattern,
        @Param("cursorCreatedAtKey") LocalDateTime cursorCreatedAtKey,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );

    @Query("""
            SELECT DISTINCT t FROM Task t
            LEFT JOIN t.tags tag
            WHERE t.project = :project
              AND t.status = :status
              AND (:tagId IS NULL OR tag.id = :tagId)
              AND (LOWER(t.title) LIKE LOWER(:searchPattern) OR LOWER(t.description) LIKE LOWER(:searchPattern))
              AND (:cursorDueDateKey IS NULL
                   OR t.dueDate > :cursorDueDateKey
                   OR (t.dueDate = :cursorDueDateKey AND t.id > :cursorId))
            ORDER BY t.dueDate ASC, t.id ASC
        """)
    List<Task> findByStatusWithSearchOrderByDueDateAsc(
        @Param("project") Project project,
        @Param("tagId") UUID tagId,
        @Param("status") TaskStatus status,
        @Param("searchPattern") String searchPattern,
        @Param("cursorDueDateKey") LocalDate cursorDueDateKey,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );

    @Query("""
            SELECT DISTINCT t FROM Task t
            LEFT JOIN t.tags tag
            WHERE t.project = :project
              AND t.status = :status
              AND (:tagId IS NULL OR tag.id = :tagId)
              AND (LOWER(t.title) LIKE LOWER(:searchPattern) OR LOWER(t.description) LIKE LOWER(:searchPattern))
              AND (:cursorDueDateKey IS NULL
                   OR t.dueDate < :cursorDueDateKey
                   OR (t.dueDate = :cursorDueDateKey AND t.id < :cursorId))
            ORDER BY t.dueDate DESC, t.id DESC
        """)
    List<Task> findByStatusWithSearchOrderByDueDateDesc(
        @Param("project") Project project,
        @Param("tagId") UUID tagId,
        @Param("status") TaskStatus status,
        @Param("searchPattern") String searchPattern,
        @Param("cursorDueDateKey") LocalDate cursorDueDateKey,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );

    @Query("""
            SELECT t FROM Task t
            WHERE t.project IN :projects
              AND :member MEMBER OF t.assignees
              AND t.status = :status
              AND (:cursorCreatedAtKey IS NULL
                   OR t.createdAt > :cursorCreatedAtKey
                   OR (t.createdAt = :cursorCreatedAtKey AND t.id > :cursorId))
            ORDER BY t.createdAt ASC, t.id ASC
        """)
    List<Task> findMyTasksOrderByCreatedAtAsc(
        @Param("projects") List<Project> projects,
        @Param("member") Member member,
        @Param("status") TaskStatus status,
        @Param("cursorCreatedAtKey") LocalDateTime cursorCreatedAtKey,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );

    @Query("""
            SELECT t FROM Task t
            WHERE t.project IN :projects
              AND :member MEMBER OF t.assignees
              AND t.status = :status
              AND (:cursorCreatedAtKey IS NULL
                   OR t.createdAt < :cursorCreatedAtKey
                   OR (t.createdAt = :cursorCreatedAtKey AND t.id < :cursorId))
            ORDER BY t.createdAt DESC, t.id DESC
        """)
    List<Task> findMyTasksOrderByCreatedAtDesc(
        @Param("projects") List<Project> projects,
        @Param("member") Member member,
        @Param("status") TaskStatus status,
        @Param("cursorCreatedAtKey") LocalDateTime cursorCreatedAtKey,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );

    @Query("""
            SELECT t FROM Task t
            WHERE t.project IN :projects
              AND :member MEMBER OF t.assignees
              AND t.status = :status
              AND (:cursorDueDateKey IS NULL
                   OR t.dueDate > :cursorDueDateKey
                   OR (t.dueDate = :cursorDueDateKey AND t.id > :cursorId))
            ORDER BY t.dueDate ASC, t.id ASC
        """)
    List<Task> findMyTasksOrderByDueDateAsc(
        @Param("projects") List<Project> projects,
        @Param("member") Member member,
        @Param("status") TaskStatus status,
        @Param("cursorDueDateKey") LocalDate cursorDueDateKey,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );

    @Query("""
            SELECT t FROM Task t
            WHERE t.project IN :projects
              AND :member MEMBER OF t.assignees
              AND t.status = :status
              AND (:cursorDueDateKey IS NULL
                   OR t.dueDate < :cursorDueDateKey
                   OR (t.dueDate = :cursorDueDateKey AND t.id < :cursorId))
            ORDER BY t.dueDate DESC, t.id DESC
        """)
    List<Task> findMyTasksOrderByDueDateDesc(
        @Param("projects") List<Project> projects,
        @Param("member") Member member,
        @Param("status") TaskStatus status,
        @Param("cursorDueDateKey") LocalDate cursorDueDateKey,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );

    @Query("""
            SELECT t FROM Task t
            WHERE t.project IN :projects
              AND :member MEMBER OF t.assignees
              AND t.status = :status
              AND (LOWER(t.title) LIKE LOWER(:searchPattern) OR LOWER(t.description) LIKE LOWER(:searchPattern))
              AND (:cursorCreatedAtKey IS NULL
                   OR t.createdAt > :cursorCreatedAtKey
                   OR (t.createdAt = :cursorCreatedAtKey AND t.id > :cursorId))
            ORDER BY t.createdAt ASC, t.id ASC
        """)
    List<Task> findMyTasksWithSearchOrderByCreatedAtAsc(
        @Param("projects") List<Project> projects,
        @Param("member") Member member,
        @Param("status") TaskStatus status,
        @Param("searchPattern") String searchPattern,
        @Param("cursorCreatedAtKey") LocalDateTime cursorCreatedAtKey,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );

    @Query("""
            SELECT t FROM Task t
            WHERE t.project IN :projects
              AND :member MEMBER OF t.assignees
              AND t.status = :status
              AND (LOWER(t.title) LIKE LOWER(:searchPattern) OR LOWER(t.description) LIKE LOWER(:searchPattern))
              AND (:cursorCreatedAtKey IS NULL
                   OR t.createdAt < :cursorCreatedAtKey
                   OR (t.createdAt = :cursorCreatedAtKey AND t.id < :cursorId))
            ORDER BY t.createdAt DESC, t.id DESC
        """)
    List<Task> findMyTasksWithSearchOrderByCreatedAtDesc(
        @Param("projects") List<Project> projects,
        @Param("member") Member member,
        @Param("status") TaskStatus status,
        @Param("searchPattern") String searchPattern,
        @Param("cursorCreatedAtKey") LocalDateTime cursorCreatedAtKey,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );

    @Query("""
            SELECT t FROM Task t
            WHERE t.project IN :projects
              AND :member MEMBER OF t.assignees
              AND t.status = :status
              AND (LOWER(t.title) LIKE LOWER(:searchPattern) OR LOWER(t.description) LIKE LOWER(:searchPattern))
              AND (:cursorDueDateKey IS NULL
                   OR t.dueDate > :cursorDueDateKey
                   OR (t.dueDate = :cursorDueDateKey AND t.id > :cursorId))
            ORDER BY t.dueDate ASC, t.id ASC
        """)
    List<Task> findMyTasksWithSearchOrderByDueDateAsc(
        @Param("projects") List<Project> projects,
        @Param("member") Member member,
        @Param("status") TaskStatus status,
        @Param("searchPattern") String searchPattern,
        @Param("cursorDueDateKey") LocalDate cursorDueDateKey,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );

    @Query("""
            SELECT t FROM Task t
            WHERE t.project IN :projects
              AND :member MEMBER OF t.assignees
              AND t.status = :status
              AND (LOWER(t.title) LIKE LOWER(:searchPattern) OR LOWER(t.description) LIKE LOWER(:searchPattern))
              AND (:cursorDueDateKey IS NULL
                   OR t.dueDate < :cursorDueDateKey
                   OR (t.dueDate = :cursorDueDateKey AND t.id < :cursorId))
            ORDER BY t.dueDate DESC, t.id DESC
        """)
    List<Task> findMyTasksWithSearchOrderByDueDateDesc(
        @Param("projects") List<Project> projects,
        @Param("member") Member member,
        @Param("status") TaskStatus status,
        @Param("searchPattern") String searchPattern,
        @Param("cursorDueDateKey") LocalDate cursorDueDateKey,
        @Param("cursorId") UUID cursorId,
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

    @Query("""
            SELECT t
            FROM Task t
            JOIN t.assignees a
            WHERE a = :assignee
              AND t.project = :project
              AND t.status <> knu.team1.be.boost.task.entity.TaskStatus.DONE
              AND (LOWER(t.title) LIKE LOWER(:searchPattern) OR LOWER(t.description) LIKE LOWER(:searchPattern))
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
    List<Task> findTasksByAssigneeWithSearchAndCursor(
        @Param("assignee") Member assignee,
        @Param("project") Project project,
        @Param("searchPattern") String searchPattern,
        @Param("cursorStatusOrder") Integer cursorStatusOrder,
        @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );

    @Query("""
            SELECT DISTINCT
                pm.member.id AS memberId,
                p.id AS projectId,
                p.name AS projectName,
                t.title AS taskTitle
            FROM Task t
            JOIN t.project p
            JOIN t.assignees a
            JOIN a.projectMemberships pm
                ON pm.project = t.project
            WHERE t.dueDate = :targetDate
              AND t.status <> knu.team1.be.boost.task.entity.TaskStatus.DONE
              AND pm.notificationEnabled = true
        """)
    List<DueTask> findDueTasksByMember(@Param("targetDate") LocalDate targetDate);

    interface DueTask {

        UUID getMemberId();

        UUID getProjectId();

        String getProjectName();

        String getTaskTitle();
    }
}
