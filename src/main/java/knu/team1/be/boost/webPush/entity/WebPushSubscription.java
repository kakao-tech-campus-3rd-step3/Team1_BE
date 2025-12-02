package knu.team1.be.boost.webPush.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import knu.team1.be.boost.common.entity.SoftDeletableEntity;
import knu.team1.be.boost.member.entity.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Table(
    name = "web_push_subscriptions",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "device_info"})
    }
)
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE web_push_subscriptions SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted = false")
public class WebPushSubscription extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "device_info", nullable = false)
    private String deviceInfo;

    @Column(name = "web_push_url", nullable = false)
    private String webPushUrl;

    @Column(name = "public_key", nullable = false)
    private String publicKey;

    @Column(name = "auth_key", nullable = false)
    private String authKey;

    public void updateSubscription(
        String token,
        String webPushUrl,
        String publicKey,
        String authKey
    ) {
        this.token = token;
        this.webPushUrl = webPushUrl;
        this.publicKey = publicKey;
        this.authKey = authKey;
    }
}
