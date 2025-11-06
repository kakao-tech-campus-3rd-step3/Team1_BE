package knu.team1.be.boost.notification.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("""
        SELECT n FROM Notification n
        WHERE n.member = :member
          AND (:cursorCreatedAt IS NULL
               OR (n.createdAt < :cursorCreatedAt)
               OR (n.createdAt = :cursorCreatedAt AND n.id < :cursorId)
              )
        ORDER BY n.createdAt DESC, n.id DESC
        """)
    List<Notification> findByMemberWithCursor(
        @Param("member") Member member,
        @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
        @Param("cursorId") UUID cursorId,
        Pageable pageable
    );

    long countByMember(Member member);

    long countByMemberAndIsReadFalse(Member member);
}
