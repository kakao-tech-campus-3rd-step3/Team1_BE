package knu.team1.be.boost.task.event.dto;

import java.util.UUID;

public record TaskReReviewEvent(
    UUID projectId,
    UUID taskId
) {

    public static TaskReReviewEvent from(UUID projectId, UUID taskId) {
        return new TaskReReviewEvent(projectId, taskId);
    }
}
