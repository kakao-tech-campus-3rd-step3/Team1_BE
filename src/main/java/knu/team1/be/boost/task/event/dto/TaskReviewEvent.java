package knu.team1.be.boost.task.event.dto;

import java.util.UUID;

public record TaskReviewEvent(
    UUID projectId,
    UUID taskId
) {

    public static TaskReviewEvent from(UUID projectId, UUID taskId) {
        return new TaskReviewEvent(projectId, taskId);
    }
}
