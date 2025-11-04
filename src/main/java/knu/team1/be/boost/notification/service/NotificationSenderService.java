package knu.team1.be.boost.notification.service;

import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.notification.dto.NotificationSavedEvent;
import knu.team1.be.boost.notification.entity.Notification;
import knu.team1.be.boost.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationSenderService {

    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAndSendNotification(Member member, String title, String message) {
        Notification notification = Notification.create(member, title, message);
        notificationRepository.save(notification);

        if (member.isNotificationEnabled()) {
            eventPublisher.publishEvent(NotificationSavedEvent.from(member, title, message));
        }
    }
}
