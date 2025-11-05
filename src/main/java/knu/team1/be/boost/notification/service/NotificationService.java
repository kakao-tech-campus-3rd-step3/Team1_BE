package knu.team1.be.boost.notification.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.common.policy.AccessPolicy;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.notification.dto.NotificationCountResponseDto;
import knu.team1.be.boost.notification.dto.NotificationListResponseDto;
import knu.team1.be.boost.notification.dto.NotificationReadResponseDto;
import knu.team1.be.boost.notification.dto.ProjectNotificationResponseDto;
import knu.team1.be.boost.notification.entity.Notification;
import knu.team1.be.boost.notification.event.dto.NotificationType;
import knu.team1.be.boost.notification.repository.NotificationRepository;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.repository.ProjectRepository;
import knu.team1.be.boost.projectMembership.entity.ProjectMembership;
import knu.team1.be.boost.projectMembership.repository.ProjectMembershipRepository;
import knu.team1.be.boost.task.entity.Task;
import knu.team1.be.boost.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final NotificationRepository notificationRepository;
    private final ProjectMembershipRepository projectMembershipRepository;

    private final AccessPolicy accessPolicy;

    private final NotificationSenderService notificationSenderService;


    @Transactional(readOnly = true)
    public NotificationListResponseDto getNotifications(
        UUID cursorId,
        int limit,
        UUID userId
    ) {
        Member member = memberRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND, "memberId: " + userId
            ));

        LocalDateTime cursorCreatedAt = null;
        if (cursorId != null) {
            Notification cursor = notificationRepository.findById(cursorId).orElse(null);

            if (cursor != null && isCursorNotMine(cursor, member)) {
                throw new BusinessException(ErrorCode.INVALID_CURSOR, "cursorId: " + cursorId);
            }

            if (cursor != null) {
                cursorCreatedAt = cursor.getCreatedAt();
            }
        }

        int safeLimit = Math.max(1, Math.min(limit, 50));
        Pageable pageable = PageRequest.of(0, safeLimit + 1);

        List<Notification> notifications = notificationRepository.findByMemberWithCursor(
            member,
            cursorCreatedAt,
            cursorId,
            pageable
        );

        return NotificationListResponseDto.from(notifications, safeLimit);
    }

    @Transactional
    public NotificationReadResponseDto markAsRead(
        UUID notificationId,
        UUID userId
    ) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.NOTIFICATION_NOT_FOUND, "notificationId: " + notificationId
            ));

        notification.ensureOwner(userId);
        notification.markAsRead();

        return NotificationReadResponseDto.from(notification);
    }

    @Transactional
    public ProjectNotificationResponseDto setProjectNotification(
        UUID projectId,
        boolean enabled,
        UUID userId
    ) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND,
                "projectId: " + projectId
            ));

        accessPolicy.ensureProjectMember(project.getId(), userId);

        ProjectMembership membership = projectMembershipRepository.findByProjectIdAndMemberId(
                project.getId(), userId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_MEMBER_NOT_FOUND,
                "projectId: " + project.getId() + ", memberId: " + userId
            ));

        membership.updateNotificationEnabled(enabled);

        return ProjectNotificationResponseDto.from(
            project.getId(),
            userId,
            membership.isNotificationEnabled()
        );
    }

    @Transactional(readOnly = true)
    public void notifyTaskReview(UUID projectId, UUID taskId, NotificationType type) {
        Project project = projectRepository.findByIdWithMemberships(projectId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND, "projectId: " + projectId
            ));
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.TASK_NOT_FOUND, "taskId: " + taskId
            ));

        List<Member> members = project.getProjectMemberships().stream()
            .filter(ProjectMembership::isNotificationEnabled)
            .map(ProjectMembership::getMember)
            .filter(member -> !task.getAssignees().contains(member))
            .toList();

        for (Member member : members) {
            try {
                notificationSenderService.saveAndSendNotification(
                    member,
                    type.title(),
                    type.message(task.getTitle())
                );
            } catch (Exception e) {
                log.error("Failed to send review notification to member: " + member.getId(), e);
            }
        }
    }

    @Transactional(readOnly = true)
    public void notifyTaskApprove(UUID projectId, UUID taskId) {
        Project project = projectRepository.findByIdWithMemberships(projectId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND, "projectId: " + projectId
            ));
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.TASK_NOT_FOUND, "taskId: " + taskId
            ));

        List<Member> assignees = project.getProjectMemberships().stream()
            .filter(
                pm -> pm.isNotificationEnabled() && task.getAssignees().contains(pm.getMember()))
            .map(ProjectMembership::getMember)
            .toList();

        for (Member assignee : assignees) {
            try {
                notificationSenderService.saveAndSendNotification(
                    assignee,
                    NotificationType.APPROVED.title(),
                    NotificationType.APPROVED.message(task.getTitle())
                );
            } catch (Exception e) {
                log.error("Failed to send approval notification to member: " + assignee.getId(), e);
            }
        }
    }

    @Transactional(readOnly = true)
    public NotificationCountResponseDto getNotificationCount(UUID userId) {
        Member member = memberRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND, "memberId: " + userId
            ));

        long totalCount = notificationRepository.countByMember(member);
        long unreadCount = notificationRepository.countByMemberAndIsReadFalse(member);

        return NotificationCountResponseDto.from(totalCount, unreadCount);
    }


    private boolean isCursorNotMine(Notification cursor, Member member) {
        return !cursor.getMember().equals(member);
    }

}
