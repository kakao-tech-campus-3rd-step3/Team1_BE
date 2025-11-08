package knu.team1.be.boost.comment.event;

import java.util.UUID;
import knu.team1.be.boost.comment.event.dto.CommentCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishCommentCreatedEvent(
        UUID projectId,
        UUID taskId,
        UUID commenterId,
        String commentContent
    ) {
        eventPublisher.publishEvent(
            CommentCreatedEvent.from(
                projectId,
                taskId,
                commenterId,
                commentContent
            )
        );
    }
}
