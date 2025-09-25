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

        if (!task.getProject().getId().equals(project.getId())) {
            throw new BusinessException(
                ErrorCode.TASK_NOT_IN_PROJECT, "projectId: " + projectId + ", taskId: " + taskId
            );
        }

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

        if (!task.getProject().getId().equals(project.getId())) {
            throw new BusinessException(
                ErrorCode.TASK_NOT_IN_PROJECT, "projectId: " + projectId + ", taskId: " + taskId
            );
        }

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

        if (!task.getProject().getId().equals(project.getId())) {
            throw new BusinessException(
                ErrorCode.TASK_NOT_IN_PROJECT, "projectId: " + projectId + ", taskId: " + taskId
            );
        }

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
        int limit
    ) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND, "projectId: " + projectId
            ));

        LocalDateTime cursorCreatedAtKey = null;
        LocalDate cursorDueDateKey = null;

        if (cursorId != null) {
            Task cursorTarget =
                taskRepository.findByIdAndProjectId(cursorId, projectId).orElse(null);
            if (cursorTarget != null) {
                if (sortBy == TaskSortBy.CREATED_AT) {
                    cursorCreatedAtKey = cursorTarget.getCreatedAt();
                } else if (sortBy == TaskSortBy.DUE_DATE) {
                    cursorDueDateKey = cursorTarget.getDueDate();
                }
            }
        }

        int safeLimit = Math.max(1, Math.min(limit, 50));
        Pageable pageable = PageRequest.of(0, safeLimit + 1);

        List<Task> tasks = taskRepository.findTasksByStatusWithCursor(
            project,
            status,
            cursorCreatedAtKey,
            cursorDueDateKey,
            cursorId,
            sortBy.name(),
            direction.name(),
            pageable
        );

        return TaskStatusSectionDto.from(tasks, limit);
    }

    @Transactional(readOnly = true)
    public TaskMemberSectionResponseDto listByMember(
        UUID projectId,
        UUID memberId,
        UUID cursorId,
        int limit
    ) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND, "projectId: " + projectId)
            );

        ProjectMember projectMember =
            projectMemberRepository.findByProjectIdAndMemberId(projectId, memberId)
                .orElseThrow(() -> new BusinessException(
                    ErrorCode.MEMBER_NOT_FOUND,
                    "projectId: " + projectId + ", memberId: " + memberId)
                );

        Member member = projectMember.getMember();

        int safeLimit = Math.max(1, Math.min(limit, 50));

        Integer cursorStatusOrder = null;
        LocalDateTime cursorCreatedAt = null;
        UUID cursorTaskId = null;

        if (cursorId != null) {
            Task cursorTarget = taskRepository.findById(cursorId)
                .orElseThrow(() -> new BusinessException(
                    ErrorCode.TASK_NOT_FOUND, "taskId: " + cursorId)
                );

            cursorStatusOrder = cursorTarget.getStatus().getOrder();
            cursorCreatedAt = cursorTarget.getCreatedAt();
            cursorTaskId = cursorTarget.getId();
        }

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
        Set<Member> assignees = new HashSet<>();

        if (assigneeIds == null || assigneeIds.isEmpty()) {
            return assignees;
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

        assignees.addAll(foundAssignees);
        return assignees;
    }
}
