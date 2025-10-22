package knu.team1.be.boost.task.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.comment.entity.Comment;
import knu.team1.be.boost.comment.repository.CommentRepository;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.common.policy.AccessPolicy;
import knu.team1.be.boost.file.entity.File;
import knu.team1.be.boost.file.repository.FileRepository;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.repository.ProjectRepository;
import knu.team1.be.boost.projectMembership.entity.ProjectMembership;
import knu.team1.be.boost.projectMembership.repository.ProjectMembershipRepository;
import knu.team1.be.boost.tag.entity.Tag;
import knu.team1.be.boost.tag.repository.TagRepository;
import knu.team1.be.boost.task.dto.CursorInfo;
import knu.team1.be.boost.task.dto.MemberTaskStatusCount;
import knu.team1.be.boost.task.dto.MemberTaskStatusCountResponseDto;
import knu.team1.be.boost.task.dto.ProjectTaskStatusCount;
import knu.team1.be.boost.task.dto.ProjectTaskStatusCountResponseDto;
import knu.team1.be.boost.task.dto.TaskApproveResponseDto;
import knu.team1.be.boost.task.dto.TaskCreateRequestDto;
import knu.team1.be.boost.task.dto.TaskDetailResponseDto;
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

    private final TagRepository tagRepository;
    private final TaskRepository taskRepository;
    private final FileRepository fileRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;

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

        accessPolicy.ensureProjectMember(project.getId(), user.id());

        List<Tag> tags = findTagsByIds(request.tags());
        tags.forEach(tag -> tag.ensureTagInProject(project.getId()));

        Set<Member> assignees = findAssignees(request.assignees());

        accessPolicy.ensureAssigneesAreProjectMembers(project.getId(), assignees);

        Task task = Task.create(
            project,
            request.title(),
            request.description(),
            request.status(),
            request.dueDate(),
            request.urgent(),
            request.requiredReviewerCount(),
            tags,
            assignees
        );

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

        accessPolicy.ensureProjectMember(project.getId(), user.id());
        accessPolicy.ensureTaskAssignee(task.getId(), user.id());

        List<Tag> tags = findTagsByIds(request.tags());
        tags.forEach(tag -> tag.ensureTagInProject(project.getId()));

        Set<Member> assignees = findAssignees(request.assignees());

        accessPolicy.ensureAssigneesAreProjectMembers(project.getId(), assignees);

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

        accessPolicy.ensureProjectMember(project.getId(), user.id());
        accessPolicy.ensureTaskAssignee(task.getId(), user.id());

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

        accessPolicy.ensureProjectMember(project.getId(), user.id());
        accessPolicy.ensureTaskAssignee(task.getId(), user.id());

        task.changeStatus(request.status());

        return TaskResponseDto.from(task);
    }

    @Transactional(readOnly = true)
    public TaskDetailResponseDto getTaskDetail(
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

        accessPolicy.ensureProjectMember(project.getId(), user.id());

        List<Comment> comments = commentRepository.findAllByTaskId(task.getId());
        List<File> files = fileRepository.findAllByTask(task);
        List<Member> projectMembers = projectMembershipRepository.findAllByProjectId(
                project.getId())
            .stream()
            .map(ProjectMembership::getMember)
            .toList();

        return TaskDetailResponseDto.from(task, comments, files, projectMembers);
    }

    @Transactional(readOnly = true)
    public ProjectTaskStatusCountResponseDto countTasksByStatusForProject(
        UUID projectId,
        UserPrincipalDto user
    ) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND, "projectId: " + projectId
            ));

        accessPolicy.ensureProjectMember(project.getId(), user.id());

        ProjectTaskStatusCount count = taskRepository.countByProjectGrouped(project.getId());

        return ProjectTaskStatusCountResponseDto.from(
            project.getId(),
            count.todo(),
            count.progress(),
            count.review(),
            count.done()
        );
    }

    @Transactional(readOnly = true)
    public List<MemberTaskStatusCountResponseDto> countTasksByStatusForAllMembers(
        UUID projectId,
        UserPrincipalDto user
    ) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND, "projectId: " + projectId
            ));

        accessPolicy.ensureProjectMember(project.getId(), user.id());

        List<MemberTaskStatusCount> counts = taskRepository
            .countTasksByStatusForAllMembersGrouped(project.getId());

        return counts.stream()
            .map(c -> MemberTaskStatusCountResponseDto.from(
                project.getId(),
                c.memberId(),
                c.todo(),
                c.progress(),
                c.review()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public TaskStatusSectionDto listByStatus(
        UUID projectId,
        UUID tagId,
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

        accessPolicy.ensureProjectMember(project.getId(), user.id());

        CursorInfo cursorInfo = extractCursorInfo(cursorId);
        LocalDateTime cursorCreatedAtKey = cursorInfo.createdAt();
        LocalDate cursorDueDateKey = cursorInfo.dueDate();
        UUID cursorTaskId = cursorInfo.taskId();

        int safeLimit = Math.max(1, Math.min(limit, 50));
        Pageable pageable = PageRequest.of(0, safeLimit + 1);

        List<Task> tasks = findTasksByStatusWithCursor(
            project,
            tagId,
            status,
            sortBy,
            direction,
            cursorCreatedAtKey,
            cursorDueDateKey,
            cursorTaskId,
            pageable
        );

        Map<UUID, Integer> fileCountMap = getFileCounts(tasks);
        Map<UUID, Integer> commentCountMap = getCommentCounts(tasks);

        return TaskStatusSectionDto.from(tasks, limit, fileCountMap, commentCountMap);
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

        List<Project> projects = projectMembershipRepository.findAllByMemberId(member.getId())
            .stream()
            .map(ProjectMembership::getProject)
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

        Map<UUID, Integer> fileCountMap = getFileCounts(tasks);
        Map<UUID, Integer> commentCountMap = getCommentCounts(tasks);

        return TaskStatusSectionDto.from(tasks, safeLimit, fileCountMap, commentCountMap);
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

        accessPolicy.ensureProjectMember(project.getId(), user.id());

        ProjectMembership projectMember =
            projectMembershipRepository.findByProjectIdAndMemberId(projectId, memberId)
                .orElseThrow(() -> new BusinessException(
                    ErrorCode.MEMBER_NOT_FOUND,
                    "projectId: " + projectId + ", memberId: " + memberId
                ));

        Member member = projectMember.getMember();

        CursorInfo cursorInfo = extractCursorInfo(cursorId);

        Integer cursorStatusOrder =
            cursorInfo.taskStatus() != null ? cursorInfo.taskStatus().getOrder() : null;
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

        Map<UUID, Integer> fileCountMap = getFileCounts(tasks);
        Map<UUID, Integer> commentCountMap = getCommentCounts(tasks);

        return TaskMemberSectionResponseDto.from(
            member,
            tasks,
            safeLimit,
            fileCountMap,
            commentCountMap
        );
    }

    @Transactional
    public TaskApproveResponseDto approveTask(
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

        List<Member> projectMembers = projectMembershipRepository.findAllByProjectId(
                project.getId())
            .stream()
            .map(ProjectMembership::getMember)
            .toList();

        Member member = memberRepository.findById(user.id())
            .orElseThrow(() -> new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND, "memberId: " + user.id()
            ));

        task.approve(member, projectMembers);

        return TaskApproveResponseDto.from(task, projectMembers);
    }

    private Map<UUID, Integer> getFileCounts(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return Collections.emptyMap();
        }

        List<UUID> taskIds = tasks.stream()
            .map(Task::getId)
            .toList();

        return fileRepository.countByTaskIds(taskIds);
    }

    private Map<UUID, Integer> getCommentCounts(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return Collections.emptyMap();
        }

        List<UUID> taskIds = tasks.stream()
            .map(Task::getId)
            .toList();

        return commentRepository.countByTaskIds(taskIds);
    }


    private List<Tag> findTagsByIds(List<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }

        List<Tag> foundTags = tagRepository.findAllById(tagIds);

        Set<UUID> foundIds = foundTags.stream()
            .map(Tag::getId)
            .collect(Collectors.toSet());

        Set<UUID> missingIds = new HashSet<>(tagIds);
        missingIds.removeAll(foundIds);

        if (!missingIds.isEmpty()) {
            throw new BusinessException(
                ErrorCode.TAG_NOT_FOUND, "TagIds: " + missingIds
            );
        }

        return foundTags;
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
        UUID tagId,
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
                    return taskRepository.findByStatusOrderByCreatedAtAsc(project, tagId, status,
                        cursorCreatedAtKey, cursorId, pageable);
                } else {
                    return taskRepository.findByStatusOrderByCreatedAtDesc(project, tagId, status,
                        cursorCreatedAtKey, cursorId, pageable);
                }
            case DUE_DATE:
                if (direction == TaskSortDirection.ASC) {
                    return taskRepository.findByStatusOrderByDueDateAsc(project, tagId, status,
                        cursorDueDateKey, cursorId, pageable);
                } else {
                    return taskRepository.findByStatusOrderByDueDateDesc(project, tagId, status,
                        cursorDueDateKey, cursorId, pageable);
                }
            default:
                throw new BusinessException(ErrorCode.INVALID_SORT_OPTION, "sortBy: " + sortBy);
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
                throw new BusinessException(ErrorCode.INVALID_SORT_OPTION, "sortBy: " + sortBy);
        }
    }
}
