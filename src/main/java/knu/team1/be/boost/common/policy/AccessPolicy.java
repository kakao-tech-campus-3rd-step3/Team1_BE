package knu.team1.be.boost.common.policy;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.projectMembership.entity.ProjectRole;
import knu.team1.be.boost.projectMembership.repository.ProjectMembershipRepository;
import knu.team1.be.boost.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccessPolicy {

    private final ProjectMembershipRepository projectMembershipRepository;
    private final TaskRepository taskRepository;

    public void ensureProjectMember(UUID projectId, UUID memberId) {
        if (!isProjectMember(projectId, memberId)) {
            throw new BusinessException(
                ErrorCode.PROJECT_MEMBER_ONLY,
                "projectId=" + projectId + ", memberId=" + memberId
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

        int count = projectMembershipRepository.countByProjectIdAndMemberIdIn(projectId,
            assigneeIds);
        if (count != assigneeIds.size()) {
            throw new BusinessException(
                ErrorCode.PROJECT_MEMBER_ONLY,
                "담당자 중 프로젝트에 속하지 않는 멤버 존재 projectId=" + projectId + ", assigneeIds=" + assigneeIds
            );
        }
    }

    public void ensureTaskAssignee(UUID taskId, UUID memberId) {
        if (!isTaskAssignee(taskId, memberId)) {
            throw new BusinessException(
                ErrorCode.TASK_ASSIGNEE_ONLY,
                "taskId=" + taskId + ", memberId=" + memberId
            );
        }
    }

    public void ensureProjectOwner(UUID projectId, UUID memberId) {
        if (!isProjectOwner(projectId, memberId)) {
            throw new BusinessException(
                ErrorCode.PROJECT_OWNER_ONLY,
                "projectId=" + projectId + ", memberId=" + memberId
            );
        }
    }

    public void ensureCommentAuthor(UUID commentAuthorId, UUID memberId) {
        if (!commentAuthorId.equals(memberId)) {
            throw new BusinessException(
                ErrorCode.COMMENT_AUTHOR_ONLY,
                "commentAuthorId=" + commentAuthorId + ", memberId=" + memberId
            );
        }
    }

    private boolean isProjectMember(UUID projectId, UUID memberId) {
        return projectMembershipRepository.existsByProjectIdAndMemberId(projectId, memberId);
    }

    private boolean isTaskAssignee(UUID taskId, UUID memberId) {
        return taskRepository.existsByIdAndAssigneesId(taskId, memberId);
    }

    private boolean isProjectOwner(UUID projectId, UUID memberId) {
        return projectMembershipRepository.existsByProjectIdAndMemberIdAndRole(
            projectId, memberId, ProjectRole.OWNER
        );
    }
}
