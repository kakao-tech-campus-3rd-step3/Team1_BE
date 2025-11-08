package knu.team1.be.boost.webPush.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.webPush.entity.WebPushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WebPushRepository extends JpaRepository<WebPushSubscription, UUID> {

    Optional<WebPushSubscription> findByMemberIdAndDeviceInfo(UUID memberId, String deviceInfo);

    List<WebPushSubscription> findByMemberId(UUID memberId);

    @Query(
        value = """
            SELECT * FROM web_push_subscriptions
            WHERE member_id = :memberId
              AND device_info = :deviceInfo
            LIMIT 1
            """,
        nativeQuery = true
    )
    Optional<WebPushSubscription> findByMemberIdAndDeviceInfoIncludingDeleted(
        @Param("memberId") UUID memberId,
        @Param("deviceInfo") String deviceInfo
    );

    @Modifying
    @Query("""
        UPDATE WebPushSubscription w
        SET w.deleted = true, w.deletedAt = CURRENT_TIMESTAMP
        WHERE w.member.id = :memberId AND w.deleted = false
        """)
    void softDeleteAllByMemberId(@Param("memberId") UUID memberId);
}
