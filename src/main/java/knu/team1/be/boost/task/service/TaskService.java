package knu.team1.be.boost.task.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.common.policy.AccessPolicy;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.repository.ProjectRepository;
import knu.team1.be.boost.projectMember.entity.ProjectMember;
import knu.team1.be.boost.projectMember.repository.ProjectMemberRepository;
import knu.team1.be.boost.task.dto.CursorInfo;
import knu.team1.be.boost.task.dto.TaskCreateRequestDto;
import knu.team1.be.boost.task.dto.TaskMemberSectionResponseDto;
import knu.team1.be.boost.task.dto.TaskResponseDto;
import knu.team1.be.boost.task.dto.TaskSortBy;
import knu.team1.be.boost.task.dto.TaskSortDirection;
import knu.team1.be.boost.task.dto.TaskStatusRequestDto;
import knu.team1.be.boost.task.dto.TaskStatusSectionDto;
import knu.team1.be.boost.task.dto.TaskUpdateRequestDto;
import knu.team1.be.boost.task.entity.Task;
import knu.team1.be.boost.task.entity.TaskStatus;
import knu.team1.be.boost.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    private final AccessPolicy accessPolicy;

    @Transactional
    public TaskResponseDto createTask(
        UUID projectId,
        TaskCreateRequestDto request,
        UserPrincipalDto user
    ) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND, "projectId: " + projectId
            ));

        accessPolicy.ensureProjectMember(projectId, user.id());

        List<String> tags = extractTags(request.tags());
        Set<Member> assignees = findAssignees(request.assignees());

        accessPolicy.ensureAssigneesAreProjectMembers(projectId, assignees);

        Task task = Task.builder()
            .project(project)
            .title(request.title())
            .description(request.description())
            .status(request.status())
            .dueDate(request.dueDate())
            .urgent(request.urgent())
            .requiredReviewerCount(request.requiredReviewerCount())
            .tags(tags)
            .assignees(assignees)
            .build();

        Task saved = taskRepository.save(task);

        return TaskResponseDto.from(saved);
    }

    @Transactional
    public TaskResponseDto updateTask(
        UUID projectId,
        UUID taskId,
        TaskUpdateRequestDto request,
        UserPrincipalDto user
    ) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND, "projectId: " + projectId
            ));

        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.TASK_NOT_FOUND, "taskId: " + taskId
            ));

        task.ensureTaskInProject(project.getId());

        accessPolicy.ensureProjectMember(projectId, user.id());
        accessPolicy.ensureTaskAssignee(taskId, user.id());

        List<String> tags = extractTags(request.tags());
        Set<Member> assignees = findAssignees(request.assignees());

        accessPolicy.ensureAssigneesAreProjectMembers(projectId, assignees);

        task.update(
            request.title(),
            request.description(),
            request.status(),
            request.dueDate(),
            request.urgent(),
            request.requiredReviewerCount(),
            tags,
            assignees
        );

        return TaskResponseDto.from(task);
    }

    @Transactional
    public void deleteTask(
        UUID projectId,
        UUID taskId,
        UserPrincipalDto user
    ) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND, "projectId: " + projectId
            ));

        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.TASK_NOT_FOUND, "taskId: " + taskId
            ));

        task.ensureTaskInProject(project.getId());

        accessPolicy.ensureProjectMember(projectId, user.id());
        accessPolicy.ensureTaskAssignee(taskId, user.id());

        taskRepository.delete(task);
    }

    @Transactional
    public TaskResponseDto changeTaskStatus(
        UUID projectId,
        UUID taskId,
        TaskStatusRequestDto request,
        UserPrincipalDto user
    ) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND, "projectId: " + projectId
            ));

        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.TASK_NOT_FOUND, "taskId: " + taskId
            ));

        task.ensureTaskInProject(project.getId());

        accessPolicy.ensureProjectMember(projectId, user.id());
        accessPolicy.ensureTaskAssignee(taskId, user.id());

        task.changeStatus(request.status());

        return TaskResponseDto.from(task);
    }

    @Transactional(readOnly = true)
    public TaskStatusSectionDto listByStatus(
        UUID projectId,
        TaskStatus status,
        TaskSortBy sortBy,
        TaskSortDirection direction,
        UUID cursorId,
        int limit,
        UserPrincipalDto user
    ) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND, "projectId: " + projectId
            ));

        accessPolicy.ensureProjectMember(projectId, user.id());

        CursorInfo cursorInfo = extractCursorInfo(cursorId);
        LocalDateTime cursorCreatedAtKey = cursorInfo.createdAt();
        LocalDate cursorDueDateKey = cursorInfo.dueDate();
        UUID cursorTaskId = cursorInfo.taskId();

        int safeLimit = Math.max(1, Math.min(limit, 50));
        Pageable pageable = PageRequest.of(0, safeLimit + 1);

        List<Task> tasks = findTasksByStatusWithCursor(
            project,
            status,
            sortBy,
            direction,
            cursorCreatedAtKey,
            cursorDueDateKey,
            cursorTaskId,
            pageable
        );

        return TaskStatusSectionDto.from(tasks, limit);
    }

    @Transactional(readOnly = true)
    public TaskStatusSectionDto listMyTasksByStatus(
        TaskStatus status,
        TaskSortBy sortBy,
        TaskSortDirection direction,
        UUID cursorId,
        int limit,
        UserPrincipalDto user
    ) {
        Member member = memberRepository.findById(user.id())
            .orElseThrow(() -> new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND, "memberId: " + user.id()
            ));

        List<Project> projects = projectMemberRepository.findAllByMember(member)
            .stream()
            .map(ProjectMember::getProject)
            .toList();

        CursorInfo cursorInfo = extractCursorInfo(cursorId);
        LocalDateTime cursorCreatedAtKey = cursorInfo.createdAt();
        LocalDate cursorDueDateKey = cursorInfo.dueDate();
        UUID cursorTaskId = cursorInfo.taskId();

        int safeLimit = Math.max(1, Math.min(limit, 50));
        Pageable pageable = PageRequest.of(0, safeLimit + 1);

        List<Task> tasks = findMyTasksByStatusWithCursor(
            projects,
            member,
            status,
            sortBy,
            direction,
            cursorCreatedAtKey,
            cursorDueDateKey,
            cursorTaskId,
            pageable
        );

        return TaskStatusSectionDto.from(tasks, limit);
    }

    @Transactional(readOnly = true)
    public TaskMemberSectionResponseDto listByMember(
        UUID projectId,
        UUID memberId,
        UUID cursorId,
        int limit,
        UserPrincipalDto user
    ) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND, "projectId: " + projectId
            ));

        accessPolicy.ensureProjectMember(projectId, user.id());

        ProjectMember projectMember =
            projectMemberRepository.findByProjectIdAndMemberId(projectId, memberId)
                .orElseThrow(() -> new BusinessException(
                    ErrorCode.MEMBER_NOT_FOUND,
                    "projectId: " + projectId + ", memberId: " + memberId
                ));

        Member member = projectMember.getMember();

        CursorInfo cursorInfo = extractCursorInfo(cursorId);

        Integer cursorStatusOrder = cursorInfo.taskStatus().getOrder();
        LocalDateTime cursorCreatedAt = cursorInfo.createdAt();
        UUID cursorTaskId = cursorInfo.taskId();

        int safeLimit = Math.max(1, Math.min(limit, 50));
        Pageable pageable = PageRequest.of(0, safeLimit + 1);

        List<Task> tasks = taskRepository.findTasksByAssigneeWithCursor(
            member,
            project,
            cursorStatusOrder,
            cursorCreatedAt,
            cursorTaskId,
            pageable
        );

        return TaskMemberSectionResponseDto.from(member, tasks, safeLimit);
    }


    private List<String> extractTags(List<String> tags) {
        return Optional.ofNullable(tags)
            .map(ArrayList::new)
            .orElseGet(ArrayList::new);
    }

    private Set<Member> findAssignees(List<UUID> assigneeIds) {
        if (assigneeIds == null || assigneeIds.isEmpty()) {
            return Set.of();
        }

        List<Member> foundAssignees = memberRepository.findAllById(assigneeIds);

        Set<UUID> foundIds = foundAssignees.stream()
            .map(Member::getId)
            .collect(Collectors.toSet());

        Set<UUID> missingIds = new HashSet<>(assigneeIds);
        missingIds.removeAll(foundIds);

        if (!missingIds.isEmpty()) {
            throw new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND, "MemberIds: " + missingIds
            );
        }

        return new HashSet<>(foundAssignees);
    }

    private CursorInfo extractCursorInfo(UUID cursorId) {
        if (cursorId == null) {
            return new CursorInfo(null, null, null, null);
        }

        Task cursorTask = taskRepository.findById(cursorId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.TASK_NOT_FOUND, "taskId: " + cursorId)
            );

        return new CursorInfo(
            cursorTask.getStatus(),
            cursorTask.getCreatedAt(),
            cursorTask.getDueDate(),
            cursorTask.getId()
        );
    }

    private List<Task> findTasksByStatusWithCursor(
        Project project,
        TaskStatus status,
        TaskSortBy sortBy,
        TaskSortDirection direction,
        LocalDateTime cursorCreatedAtKey,
        LocalDate cursorDueDateKey,
        UUID cursorId,
        Pageable pageable
    ) {
        switch (sortBy) {
            case CREATED_AT:
                if (direction == TaskSortDirection.ASC) {
                    return taskRepository.findByStatusOrderByCreatedAtAsc(project, status,
                        cursorCreatedAtKey, cursorId, pageable);
                } else {
                    return taskRepository.findByStatusOrderByCreatedAtDesc(project, status,
                        cursorCreatedAtKey, cursorId, pageable);
                }
            case DUE_DATE:
                if (direction == TaskSortDirection.ASC) {
                    return taskRepository.findByStatusOrderByDueDateAsc(project, status,
                        cursorDueDateKey, cursorId, pageable);
                } else {
                    return taskRepository.findByStatusOrderByDueDateDesc(project, status,
                        cursorDueDateKey, cursorId, pageable);
                }
            default:
                throw new IllegalArgumentException("올바르지 않은 sortBy 입니다. " + sortBy);
        }
    }

    private List<Task> findMyTasksByStatusWithCursor(
        List<Project> projects,
        Member member,
        TaskStatus status,
        TaskSortBy sortBy,
        TaskSortDirection direction,
        LocalDateTime cursorCreatedAtKey,
        LocalDate cursorDueDateKey,
        UUID cursorId,
        Pageable pageable
    ) {
        switch (sortBy) {
            case CREATED_AT:
                if (direction == TaskSortDirection.ASC) {
                    return taskRepository.findMyTasksOrderByCreatedAtAsc(projects, member, status,
                        cursorCreatedAtKey, cursorId, pageable);
                } else {
                    return taskRepository.findMyTasksOrderByCreatedAtDesc(projects, member, status,
                        cursorCreatedAtKey, cursorId, pageable);
                }
            case DUE_DATE:
                if (direction == TaskSortDirection.ASC) {
                    return taskRepository.findMyTasksOrderByDueDateAsc(projects, member, status,
                        cursorDueDateKey, cursorId, pageable);
                } else {
                    return taskRepository.findMyTasksOrderByDueDateDesc(projects, member, status,
                        cursorDueDateKey, cursorId, pageable);
                }
            default:
                throw new IllegalArgumentException("올바르지 않은 sortBy 입니다. " + sortBy);
        }
    }
}
