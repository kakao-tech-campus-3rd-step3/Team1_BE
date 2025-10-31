package knu.team1.be.boost.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import knu.team1.be.boost.common.entity.SoftDeletableEntity;
import knu.team1.be.boost.member.entity.vo.OauthInfo;
import knu.team1.be.boost.projectMembership.entity.ProjectMembership;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@SuperBuilder
@EqualsAndHashCode(of = "id")
@Table(name = "members", uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_member_provider_provider_id",
        columnNames = {"provider", "provider_id"}
    )
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE members SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted = false")
public class Member extends SoftDeletableEntity {

    @Embedded
    private OauthInfo oauthInfo;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "avatar", nullable = false)
    private String avatar;

    @Column(name = "background_color", nullable = false, length = 7)
    private String backgroundColor;

    @Column(name = "notification_enabled", nullable = false)
    @Builder.Default
    private boolean notificationEnabled = false;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProjectMembership> projectMemberships = new ArrayList<>();

    public void updateName(String name) {
        this.name = name;
    }

    public void updateAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void updateBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void updateNotificationEnabled(boolean enabled) {
        this.notificationEnabled = enabled;
    }
}
