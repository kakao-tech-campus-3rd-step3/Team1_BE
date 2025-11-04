package knu.team1.be.boost.task.event;

import java.util.UUID;
import knu.team1.be.boost.task.event.dto.TaskApproveEvent;
import knu.team1.be.boost.task.event.dto.TaskReReviewEvent;
import knu.team1.be.boost.task.event.dto.TaskReviewEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishTaskReviewEvent(UUID projectId, UUID taskId) {
        eventPublisher.publishEvent(TaskReviewEvent.from(projectId, taskId));
    }

    public void publishTaskReReviewEvent(UUID projectId, UUID taskId) {
        eventPublisher.publishEvent(TaskReReviewEvent.from(projectId, taskId));
    }

    public void publishTaskApproveEvent(UUID projectId, UUID taskId) {
        eventPublisher.publishEvent(TaskApproveEvent.from(projectId, taskId));
    }
}
