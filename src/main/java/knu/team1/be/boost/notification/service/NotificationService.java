package knu.team1.be.boost.notification.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.common.policy.AccessPolicy;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.notification.dto.NotificationListResponseDto;
import knu.team1.be.boost.notification.dto.NotificationReadResponseDto;
import knu.team1.be.boost.notification.dto.NotificationSavedEvent;
import knu.team1.be.boost.notification.entity.Notification;
import knu.team1.be.boost.notification.repository.NotificationRepository;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.repository.ProjectRepository;
import knu.team1.be.boost.projectMembership.entity.ProjectMembership;
import knu.team1.be.boost.projectMembership.repository.ProjectMembershipRepository;
import knu.team1.be.boost.task.dto.TaskApproveEvent;
import knu.team1.be.boost.task.dto.TaskReviewEvent;
import knu.team1.be.boost.task.entity.Task;
import knu.team1.be.boost.task.repository.TaskRepository;
import knu.team1.be.boost.task.repository.TaskRepository.DueTask;
import knu.team1.be.boost.webPush.service.WebPushClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final NotificationRepository notificationRepository;
    private final ProjectMembershipRepository projectMembershipRepository;

    private final AccessPolicy accessPolicy;

    private final WebPushClient webPushClient;

    private final ApplicationEventPublisher eventPublisher;

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
    public void setProjectNotification(
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
    }

    @Transactional
    public void notifyTaskReview(Project project, Task task) {
        Set<Member> assignees = task.getAssignees();

        List<Member> members = project.getProjectMemberships().stream()
            .filter(ProjectMembership::isNotificationEnabled)
            .map(ProjectMembership::getMember)
            .filter(member -> !assignees.contains(member))
            .toList();

        for (Member member : members) {
            saveAndSendNotification(
                member,
                "작업 검토 요청",
                String.format("[%s] 작업이 검토 중 상태로 변경되었습니다.", task.getTitle())
            );
        }
    }

    @Transactional
    public void notifyTaskApprove(Project project, Task task) {
        List<Member> assignees = project.getProjectMemberships().stream()
            .filter(
                pm -> task.getAssignees().contains(pm.getMember()) && pm.isNotificationEnabled())
            .map(ProjectMembership::getMember)
            .toList();

        for (Member assignee : assignees) {
            saveAndSendNotification(
                assignee,
                "작업 승인 완료",
                String.format("[%s] 작업이 모든 승인자를 통해 승인되었습니다.", task.getTitle())
            );
        }
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    @Transactional
    public void notifyDueTomorrowTasks() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<DueTask> dueTasks = taskRepository.findDueTasksByMember(tomorrow);

        Map<UUID, Map<UUID, List<DueTask>>> groupedDueTask = dueTasks.stream()
            .collect(Collectors.groupingBy(
                DueTask::getMemberId,
                Collectors.groupingBy(DueTask::getProjectId)
            ));
        String formattedDate = tomorrow.format(DateTimeFormatter.ofPattern("MM월 dd일"));

        groupedDueTask.forEach((memberId, projectTasks) -> {
            Member member = findMember(memberId);
            String message = buildNotificationMessage(projectTasks);
            String title = formattedDate + " 마감 임박 작업";
            saveAndSendNotification(member, title, message);
        });
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskReviewEvent(TaskReviewEvent event) {
        notifyTaskReview(event.project(), event.task());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskApproveEvent(TaskApproveEvent event) {
        notifyTaskApprove(event.project(), event.task());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationSavedEvent(NotificationSavedEvent event) {
        webPushClient.sendNotification(event.member(), event.title(), event.message());
    }

    private void saveAndSendNotification(Member member, String title, String message) {
        Notification notification = Notification.create(member, title, message);

        notificationRepository.save(notification);

        eventPublisher.publishEvent(NotificationSavedEvent.from(member, title, message));
    }

    private String buildNotificationMessage(Map<UUID, List<DueTask>> projectTasks) {
        StringBuilder message = new StringBuilder();

        projectTasks.forEach((projectId, tasks) -> {
            String projectName = tasks.getFirst().getProjectName();
            String taskTitles = tasks.stream()
                .map(DueTask::getTaskTitle)
                .collect(Collectors.joining(", "));

            message.append("[")
                .append(projectName)
                .append("] ")
                .append(taskTitles)
                .append("\n");
        });

        return message.toString().trim();
    }

    private Member findMember(UUID memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND,
                "memberId: " + memberId
            ));
    }

    private boolean isCursorNotMine(Notification cursor, Member member) {
        return !cursor.getMember().equals(member);
    }

}
