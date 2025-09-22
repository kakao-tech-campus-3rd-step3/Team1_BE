package knu.team1.be.boost.common.policy;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.projectMember.repository.ProjectMemberRepository;
import knu.team1.be.boost.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccessPolicy {

    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;

    public void ensureProjectMember(UUID projectId, UserPrincipalDto user) {
        if (!isProjectMember(projectId, user.id())) {
            throw new BusinessException(
                ErrorCode.PROJECT_MEMBER_ONLY,
                "projectId=" + projectId + ", userId=" + user.id()
            );
        }
    }

    public void ensureAssigneesAreProjectMembers(UUID projectId, Set<Member> assignees) {
        if (assignees == null || assignees.isEmpty()) {
            return;
        }

        Set<UUID> assigneeIds = assignees.stream()
            .map(Member::getId)
            .collect(Collectors.toSet());

        int count = projectMemberRepository.countByProjectIdAndMemberIdIn(projectId, assigneeIds);
        if (count != assigneeIds.size()) {
            throw new BusinessException(
                ErrorCode.PROJECT_MEMBER_ONLY,
                "담당자 중 프로젝트에 속하지 않는 멤버 존재 projectId=" + projectId + ", assigneeIds=" + assigneeIds
            );
        }
    }

    public void ensureTaskAssignee(UUID taskId, UserPrincipalDto user) {
        if (!isTaskAssignee(taskId, user.id())) {
            throw new BusinessException(
                ErrorCode.TASK_ASSIGNEE_ONLY,
                "taskId=" + taskId + ", userId=" + user.id()
            );
        }
    }

    private boolean isProjectMember(UUID projectId, UUID memberId) {
        return projectMemberRepository.existsByProjectIdAndMemberId(projectId, memberId);
    }

    private boolean isTaskAssignee(UUID taskId, UUID memberId) {
        return taskRepository.existsByIdAndAssigneesId(taskId, memberId);
    }
}
