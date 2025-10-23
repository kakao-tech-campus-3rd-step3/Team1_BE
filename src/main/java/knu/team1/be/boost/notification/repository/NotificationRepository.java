package knu.team1.be.boost.notification.repository;

import java.util.UUID;
import knu.team1.be.boost.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

}
