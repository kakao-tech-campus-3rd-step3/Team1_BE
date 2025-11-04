package knu.team1.be.boost.task.event.dto;

import java.util.UUID;

public record TaskApproveEvent(
    UUID projectId,
    UUID taskId
) {

    public static TaskApproveEvent from(UUID projectId, UUID taskId) {
        return new TaskApproveEvent(projectId, taskId);
    }
}
