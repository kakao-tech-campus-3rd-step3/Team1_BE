package knu.team1.be.boost.comment.event.dto;

import java.util.UUID;

public record CommentCreatedEvent(
    UUID projectId,
    UUID taskId,
    UUID commenterId,
    String commentContent
) {

    public static CommentCreatedEvent from(
        UUID projectId,
        UUID taskId,
        UUID commenterId,
        String commentContent
    ) {
        return new CommentCreatedEvent(projectId, taskId, commenterId, commentContent);
    }
}
