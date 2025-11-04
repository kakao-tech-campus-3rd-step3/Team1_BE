package knu.team1.be.boost.notification.event;

import knu.team1.be.boost.notification.event.dto.NotificationSavedEvent;
import knu.team1.be.boost.notification.event.dto.NotificationType;
import knu.team1.be.boost.notification.service.NotificationService;
import knu.team1.be.boost.task.event.dto.TaskApproveEvent;
import knu.team1.be.boost.task.event.dto.TaskReReviewEvent;
import knu.team1.be.boost.webPush.service.WebPushClient;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationService notificationService;
    private final WebPushClient webPushClient;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskReviewEvent(TaskReReviewEvent event) {
        notificationService.notifyTaskReview(event.projectId(), event.taskId(),
            NotificationType.REVIEW);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskReReviewEvent(TaskReReviewEvent event) {
        notificationService.notifyTaskReview(event.projectId(), event.taskId(),
            NotificationType.RE_REVIEW);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskApproveEvent(TaskApproveEvent event) {
        notificationService.notifyTaskApprove(event.projectId(), event.taskId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationSavedEvent(NotificationSavedEvent event) {
        webPushClient.sendNotification(event.member(), event.title(), event.message());
    }
}
