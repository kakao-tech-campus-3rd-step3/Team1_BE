package knu.team1.be.boost.webPush.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.webPush.entity.WebPushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebPushRepository extends JpaRepository<WebPushSubscription, UUID> {

    Optional<WebPushSubscription> findByMemberIdAndDeviceInfo(UUID memberId, String deviceInfo);

    List<WebPushSubscription> findByMemberId(UUID memberId);
}
