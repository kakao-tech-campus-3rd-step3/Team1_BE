package knu.team1.be.boost.notification.event;

import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.notification.event.dto.NotificationSavedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishNotificationSavedEvent(Member member, String title, String message) {
        eventPublisher.publishEvent(NotificationSavedEvent.from(member, title, message));
    }
}
