package knu.team1.be.boost.task.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.exception.MemberNotFoundException;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.exception.ProjectNotFoundException;
import knu.team1.be.boost.project.repository.ProjectRepository;
import knu.team1.be.boost.task.dto.TaskCreateRequestDto;
import knu.team1.be.boost.task.dto.TaskResponseDto;
import knu.team1.be.boost.task.entity.Task;
import knu.team1.be.boost.task.entity.TaskStatus;
import knu.team1.be.boost.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public TaskResponseDto createTask(UUID projectId, TaskCreateRequestDto request) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));

        // TODO: 인증 붙으면 현재 사용자 프로젝트 소속 여부 확인

        TaskStatus status = TaskStatus.from(request.status());
        List<String> tags = extractTags(request);
        Set<Member> assignees = findAssignees(request);

        Task task = Task.builder()
            .project(project)
            .title(request.title())
            .description(request.description())
            .status(status)
            .dueDate(request.dueDate())
            .urgent(request.urgent())
            .requiredReviewerCount(request.requiredReviewerCount())
            .tags(tags)
            .assignees(assignees)
            .build();

        Task saved = taskRepository.save(task);

        return TaskResponseDto.from(saved);
    }

    private List<String> extractTags(TaskCreateRequestDto request) {
        return Optional.ofNullable(request.tags())
            .map(ArrayList::new)
            .orElseGet(ArrayList::new);
    }

    private Set<Member> findAssignees(TaskCreateRequestDto request) {
        Set<Member> assignees = new HashSet<>();

        if (request.assignees() == null || request.assignees().isEmpty()) {
            return assignees;
        }

        List<UUID> assigneeIds = request.assignees();
        List<Member> foundAssignees = memberRepository.findAllById(assigneeIds);

        if (assigneeIds.size() != foundAssignees.size()) {
            Set<UUID> foundIds = new HashSet<>();
            for (Member member : foundAssignees) {
                foundIds.add(member.getId());
            }

            List<UUID> missingIds = new ArrayList<>();
            for (UUID id : assigneeIds) {
                if (!foundIds.contains(id)) {
                    missingIds.add(id);
                }
            }

            throw new MemberNotFoundException(missingIds);
        }

        assignees.addAll(foundAssignees);
        return assignees;
    }
}
